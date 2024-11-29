// CourseAdapter.java
package com.example.yoga_admin_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.activity.CRUDClassInstanceActivity;
import com.example.yoga_admin_app.activity.CRUDCourseActivity;
import com.example.yoga_admin_app.database.DatabaseHelper;

import java.util.ArrayList;

public class CourseAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> courses;

    public CourseAdapter(Context context, ArrayList<String> courses) {
        super(context, 0, courses);
        this.context = context;
        this.courses = courses;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        }

        String courseDetails = courses.get(position);
        TextView courseDetailsTextView = convertView.findViewById(R.id.courseDetailsTextView);
        Button viewClassInstancesButton = convertView.findViewById(R.id.viewClassInstancesButton);

        courseDetailsTextView.setText(courseDetails);

        viewClassInstancesButton.setOnClickListener(v -> {
            String[] details = courseDetails.split(" - ");
            String dayOfWeek = details[0];
            String time = details[1];
            String capacity = details[2];
            String duration = details[3];
            String price = details[4];
            String classType = details[5];
            String description = details[6];

            int courseId = getCourseId(dayOfWeek, time, capacity, duration, price, classType, description);

            Intent intent = new Intent(context, CRUDClassInstanceActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("dayOfWeek", dayOfWeek);
            context.startActivity(intent);
        });

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CRUDCourseActivity.class);
            intent.putExtra("courseDetails", courseDetails);
            context.startActivity(intent);
        });

        return convertView;
    }

    private int getCourseId(String dayOfWeek, String time, String capacity, String duration, String price, String classType, String description) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES, new String[]{DatabaseHelper.COLUMN_COURSE_ID},
                DatabaseHelper.COLUMN_DAY_OF_WEEK + "=? AND " + DatabaseHelper.COLUMN_TIME_OF_COURSE + "=? AND " +
                        DatabaseHelper.COLUMN_CAPACITY + "=? AND " + DatabaseHelper.COLUMN_DURATION + "=? AND " +
                        DatabaseHelper.COLUMN_PRICE + "=? AND " + DatabaseHelper.COLUMN_TYPE_OF_CLASS + "=? AND " +
                        DatabaseHelper.COLUMN_DESCRIPTION + "=?",
                new String[]{dayOfWeek, time, capacity, duration, price, classType, description}, null, null, null);

        int courseId = -1;
        if (cursor.moveToFirst()) {
            courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));
        }
        cursor.close();
        return courseId;
    }
}