package com.example.yoga_admin_app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RoleAdapter extends ArrayAdapter<CharSequence> {
    public RoleAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        // Disable the "Administrator" role
        return !getItem(position).equals("Administrator");
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (getItem(position).equals("Administrator")) {
            // Set the disabled item text color to gray
            textView.setTextColor(Color.GRAY);
        } else {
            // Set the enabled item text color to default
            textView.setTextColor(Color.BLACK);
        }
        return view;
    }
}