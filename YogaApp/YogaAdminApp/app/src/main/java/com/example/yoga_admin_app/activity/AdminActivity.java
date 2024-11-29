package com.example.yoga_admin_app.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.adapter.SearchAdapter;
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.example.yoga_admin_app.model.ClassInstances;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;
    private Button addCourseButton, addUserButton, manageBookingButton;
    private ProgressBar progressBar;
    private ImageView imageView;
    private EditText searchEditText;
    private ListView searchResultsListView;
    private SearchAdapter searchAdapter;
    private List<ClassInstances> classInstancesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserInfoPopup(v);
            }
        });

        progressBar = findViewById(R.id.progressBar);
        addCourseButton = findViewById(R.id.addCourseButton);
        addCourseButton.setOnClickListener(v -> {
            progressBar.setVisibility(android.view.View.VISIBLE);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(AdminActivity.this, CRUDCourseActivity.class);
                startActivity(intent);
                progressBar.setVisibility(android.view.View.GONE);
            }, 2000); // Simulate loading time
        });

        addCourseButton = findViewById(R.id.addCourseButton);
        addCourseButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, CRUDCourseActivity.class);
            startActivity(intent);
        });

        addUserButton = findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, CRUDUserActivity.class);
            startActivity(intent);
        });

        manageBookingButton = findViewById(R.id.manageBookingButton);
        manageBookingButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, CRUDBookingActivity.class);
            startActivity(intent);
        });

        searchEditText = findViewById(R.id.editText);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        dbHelper = new DatabaseHelper(this);

        classInstancesList = new ArrayList<>();
        searchAdapter = new SearchAdapter(this, classInstancesList);
        searchResultsListView.setAdapter(searchAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchClass(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

    }

    private void showUserInfoPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_user_info, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        TextView userNameTextView = popupView.findViewById(R.id.userNameTextView);
        TextView userEmailTextView = popupView.findViewById(R.id.userEmailTextView);
        Button logoutButton = popupView.findViewById(R.id.logoutButton);

        // Retrieve user session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "User Email");
        String role = sharedPreferences.getString("role", "User Role");

        userEmailTextView.setText(email);
        userNameTextView.setText(role);

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            clearUserSession();
            Toast.makeText(AdminActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });

        popupWindow.showAsDropDown(anchorView);
    }

    private void clearUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void saveUserToSQLite(String uid, String email, String password, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_ROLE, role);
        db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    private void saveUserToFirebase(String uid, String email, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // User added successfully
                    Toast.makeText(AdminActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add user
                    Toast.makeText(AdminActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchClass(String keyword) {
        classInstancesList.clear();
        if (!keyword.isEmpty()) {
            List<ClassInstances> allClassInstances = dbHelper.getAllClassInstances();
            for (ClassInstances classInstance : allClassInstances) {
                if (classInstance.getTeacher().toLowerCase().contains(keyword.toLowerCase()) ||
                        classInstance.getDate().toLowerCase().contains(keyword.toLowerCase()) ||
                        classInstance.getComments().toLowerCase().contains(keyword.toLowerCase())) {
                    classInstancesList.add(classInstance);
                }
            }
        }
        searchAdapter.notifyDataSetChanged();
    }

    private void logoutUser() {
        clearUserSession();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(AdminActivity.this, LoginActivity.class));
        finish();
    }

}