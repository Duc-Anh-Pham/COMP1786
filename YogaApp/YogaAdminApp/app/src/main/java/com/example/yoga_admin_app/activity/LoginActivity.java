package com.example.yoga_admin_app.activity;

import static com.example.yoga_admin_app.database.DatabaseHelper.COLUMN_EMAIL;
import static com.example.yoga_admin_app.database.DatabaseHelper.COLUMN_PASSWORD;
import static com.example.yoga_admin_app.database.DatabaseHelper.TABLE_USERS;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton, forgotPasswordButton;
    private ProgressBar progressBar;
    private CheckBox showPasswordCheckBox;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());

        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String role = documentSnapshot.getString("role");
                                        saveUserSession(email, role);
                                        if ("Administrator".equals(role) || "Manager".equals(role)) {
                                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                        } else if ("Customer".equals(role)) {
                                            Toast.makeText(LoginActivity.this, "Access denied for Customer role", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Failed to retrieve user role", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserSession(String email, String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("role", role);
        editor.apply();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newPassword = "newPassword";
                        dbHelper.updatePassword(email, newPassword);
                        updatePasswordInFirestore(email, newPassword);
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePasswordInFirestore(String email, String newPassword) {
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String userId = task.getResult().getDocuments().get(0).getId();
                        db.collection("users").document(userId).update("password", newPassword)
                                .addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Password updated in Firestore", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Failed to update password in Firestore", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}