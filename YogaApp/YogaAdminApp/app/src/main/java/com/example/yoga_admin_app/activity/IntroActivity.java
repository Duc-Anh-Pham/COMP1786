package com.example.yoga_admin_app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin_app.R;

public class IntroActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        progressBar = findViewById(R.id.progressBar);
        startBtn = findViewById(R.id.startBtn);

        startBtn.setOnClickListener(v -> {
            progressBar.setVisibility(android.view.View.VISIBLE);
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            progressBar.setVisibility(android.view.View.GONE);
        });
    }
}