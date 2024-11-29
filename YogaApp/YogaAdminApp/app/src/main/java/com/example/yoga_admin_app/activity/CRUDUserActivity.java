package com.example.yoga_admin_app.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.adapter.RoleAdapter;
import com.example.yoga_admin_app.adapter.UserAdapter;
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.example.yoga_admin_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRUDUserActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private TextView hiddenPasswordTextView;
    private Spinner roleSpinner;
    private Button addButton, updateButton, deleteButton, clearButton;
    private ListView userListView;
    private DatabaseHelper dbHelper;
    private List<User> userList;
    private UserAdapter userAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_user);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        hiddenPasswordTextView = findViewById(R.id.hiddenPasswordTextView);
        roleSpinner = findViewById(R.id.roleSpinner);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        userListView = findViewById(R.id.userListView);

        RoleAdapter adapter = new RoleAdapter(this,
                android.R.layout.simple_spinner_item,
                getResources().getTextArray(R.array.user_roles));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        addButton.setOnClickListener(v -> addUser());
        updateButton.setOnClickListener(v -> updateUser());
        deleteButton.setOnClickListener(v -> deleteUser());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CRUDUserActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> clearFields());

        loadUsers();
    }

    private void loadUsers() {
        userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
                String role = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE));

                User user = new User(id, email, password, User.Role.valueOf(role));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();

        userAdapter = new UserAdapter(this, userList);
        userListView.setAdapter(userAdapter);
    }

    private boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void clearFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        hiddenPasswordTextView.setText("");
        roleSpinner.setSelection(0);
        emailEditText.setEnabled(true);
        passwordEditText.setVisibility(View.VISIBLE);
        hiddenPasswordTextView.setVisibility(View.GONE);
    }

    private String getCurrentUserRole() {
        String uid = mAuth.getCurrentUser().getUid();
        Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COLUMN_ID + " = ?", new String[]{uid}, null, null, null);
        if (cursor.moveToFirst()) {
            int roleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);
            if (roleIndex >= 0) {
                String role = cursor.getString(roleIndex);
                cursor.close();
                return role;
            }
        }
        cursor.close();
        return null;
    }

    private String getUserRoleByEmail(String email) {
        Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst()) {
            int roleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);
            if (roleIndex >= 0) {
                String role = cursor.getString(roleIndex);
                cursor.close();
                return role;
            }
        }
        cursor.close();
        return null;
    }

    public void populateUserDetails(User user) {
        emailEditText.setText(user.getEmail());
        hiddenPasswordTextView.setText(user.getPassword());
        roleSpinner.setSelection(((ArrayAdapter<String>) roleSpinner.getAdapter()).getPosition(user.getRole().toString()));
        emailEditText.setEnabled(false);
        passwordEditText.setVisibility(View.GONE);
        hiddenPasswordTextView.setVisibility(View.GONE);
    }

    private void addUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("None".equals(role)) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailExists(email)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserRole = getCurrentUserRole();
        if ("Administrator".equals(currentUserRole)) {
            if ("Administrator".equals(role)) {
                Toast.makeText(this, "Cannot create user with Administrator role", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if ("Manager".equals(currentUserRole)) {
            if (!"Customer".equals(role)) {
                Toast.makeText(this, "Manager can only create users with Customer role", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("Manager".equals(role)) {
                Toast.makeText(this, "Cannot create user with Manager role", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        addUserToFirebase(email, password, role, uid);
                        addUserToSQLite(email, password, role);
                        Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
                        loadUsers();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Failed to add user to Firebase Authentication", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserRole = getCurrentUserRole();
        String targetUserRole = getUserRoleByEmail(email);

        if ("Administrator".equals(targetUserRole)) {
            Toast.makeText(this, "Cannot update user with Administrator role", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Manager".equals(currentUserRole) && "Customer".equals(targetUserRole) && "Manager".equals(role)) {
            Toast.makeText(this, "Manager cannot update Customer to Manager", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Manager".equals(currentUserRole) && "Manager".equals(targetUserRole) && "Customer".equals(role)) {
            Toast.makeText(this, "Manager cannot change another Manager to Customer", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Manager".equals(currentUserRole) && !"Customer".equals(targetUserRole)) {
            Toast.makeText(this, "Manager can only update users with Customer role", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_ROLE, role);

        if (!password.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        }

        int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});

        if (rowsAffected > 0) {
            updateUserInFirebase(email, password, role);
            Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }

        loadUsers();
    }

    private void addUserToSQLite(String email, String password, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_ROLE, role);
        db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    private void addUserToFirebase(String email, String password, String role, String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("role", role);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CRUDUserActivity.this, "User added to Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CRUDUserActivity.this, "Failed to add user to Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInFirebase(String email, String password, String role) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("role", role);

        db.collection("users").document(uid)
                .update(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CRUDUserActivity.this, "User updated in Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CRUDUserActivity.this, "Failed to update user in Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUser() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter the email", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserRole = getCurrentUserRole();
        String targetUserRole = getUserRoleByEmail(email);
        if ("Administrator".equals(targetUserRole)) {
            Toast.makeText(this, "Cannot delete user with Administrator role", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Manager".equals(currentUserRole) && !"Customer".equals(targetUserRole)) {
            Toast.makeText(this, "Manager can only delete users with Customer role", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_USERS, DatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});

        if (rowsDeleted > 0) {
            deleteUserFromFirebase(email);
            Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }

        loadUsers();
    }

    private void deleteUserFromFirebase(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String uid = task.getResult().getDocuments().get(0).getId();
                        db.collection("users").document(uid).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CRUDUserActivity.this, "User deleted from Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CRUDUserActivity.this, "Failed to delete user from Firestore", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(CRUDUserActivity.this, "User not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}