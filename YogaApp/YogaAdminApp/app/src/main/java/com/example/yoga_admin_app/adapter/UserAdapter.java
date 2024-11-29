package com.example.yoga_admin_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.activity.LoginActivity;
import com.example.yoga_admin_app.activity.CRUDUserActivity;
import com.example.yoga_admin_app.model.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
    public UserAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        TextView emailTextView = convertView.findViewById(R.id.emailTextView);
        TextView roleTextView = convertView.findViewById(R.id.roleTextView);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        Button logoutButton = convertView.findViewById(R.id.logoutButton);

        emailTextView.setText(user.getEmail());
        roleTextView.setText(user.getRole().toString());

        convertView.setOnClickListener(v -> {
            ((CRUDUserActivity) getContext()).populateUserDetails(user);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            getContext().startActivity(intent);
            ((CRUDUserActivity) getContext()).finish();
        });

        return convertView;
    }
}