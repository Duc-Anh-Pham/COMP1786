package com.example.yoga_admin_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.model.ClassInstances;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<ClassInstances> {
    private Context context;
    private List<ClassInstances> classInstancesList;

    public SearchAdapter(Context context, List<ClassInstances> classInstancesList) {
        super(context, R.layout.item_class, classInstancesList);
        this.context = context;
        this.classInstancesList = classInstancesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_class, parent, false);
        }

        ClassInstances classInstance = classInstancesList.get(position);

        TextView classNameTextView = convertView.findViewById(R.id.classNameTextView);
        TextView teacherNameTextView = convertView.findViewById(R.id.teacherNameTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);

        classNameTextView.setText(classInstance.getClassInstancesId());
        teacherNameTextView.setText(classInstance.getTeacher());
        dateTextView.setText(classInstance.getDate());

        return convertView;
    }
}