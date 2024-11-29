package com.example.yoga_admin_app.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.adapter.CourseAdapter;
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CRUDCourseActivity extends AppCompatActivity {
    private Spinner timeSpinner, durationSpinner, dayOfWeekSpinner, classTypeSpinner;
    private EditText capacityEditText, priceEditText, descriptionEditText;
    private Button addCourseButton, updateCourseButton, deleteCourseButton, clearCourseButton;
    private ProgressBar progressBar;
    private TextView endTimeTextView;
    private ListView courseListView;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;
    private ArrayList<String> courseList;
    private CourseAdapter courseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_course);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        timeSpinner = findViewById(R.id.timeSpinner);
        durationSpinner = findViewById(R.id.durationSpinner);
        capacityEditText = findViewById(R.id.capacityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        classTypeSpinner = findViewById(R.id.classTypeSpinner);
        addCourseButton = findViewById(R.id.addCourseButton);
        updateCourseButton = findViewById(R.id.updateCourseButton);
        deleteCourseButton = findViewById(R.id.deleteCourseButton);
        clearCourseButton = findViewById(R.id.clearCourseButton);
        progressBar = findViewById(R.id.progressBar);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        courseListView = findViewById(R.id.courseListView);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this,
                R.array.duration_options, android.R.layout.simple_spinner_item);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);

        ArrayAdapter<CharSequence> classTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        classTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classTypeSpinner.setAdapter(classTypeAdapter);

        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(this, courseList);
        courseListView.setAdapter(courseAdapter);

        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = courseList.get(position);
            populateCourseDetails(selectedCourse);

            Button viewButton = view.findViewById(R.id.viewClassInstancesButton);
            viewButton.setOnClickListener(v -> {
                String[] details = selectedCourse.split(" - ");
                int courseId = getCourseId(details[0], details[1], details[2], details[3], details[4], details[5], details[6]);

                // Save course_id into class_instances
                saveCourseIdToClassInstances(courseId);

                // Switch to CRUDClassInstanceActivity
                Intent intent = new Intent(CRUDCourseActivity.this, CRUDClassInstanceActivity.class);
                intent.putExtra("courseId", courseId);
                startActivity(intent);
            });
        });


        Intent intent1 = getIntent();
        if (intent1.hasExtra("courseDetails")) {
            String courseDetails = intent1.getStringExtra("courseDetails");
            populateCourseDetails(courseDetails);
        }

        addCourseButton.setOnClickListener(v -> addCourse());
        updateCourseButton.setOnClickListener(v -> updateCourse());
        deleteCourseButton.setOnClickListener(v -> deleteCourse());
        clearCourseButton.setOnClickListener(v -> clearFields());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(CRUDCourseActivity.this, AdminActivity.class);
            startActivity(intent2);
            finish();
        });

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateEndTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateEndTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        loadCourses();
    }

    private void populateCourseDetails(String courseDetails) {
        String[] details = courseDetails.split(" - ");
        String dayOfWeek = details[0];
        String time = details[1];
        String capacity = details[2];
        String duration = details[3];
        String price = details[4];
        String classType = details[5];
        String description = details[6];

        dayOfWeekSpinner.setSelection(((ArrayAdapter) dayOfWeekSpinner.getAdapter()).getPosition(dayOfWeek));
        timeSpinner.setSelection(((ArrayAdapter) timeSpinner.getAdapter()).getPosition(time));
        capacityEditText.setText(capacity);
        durationSpinner.setSelection(((ArrayAdapter) durationSpinner.getAdapter()).getPosition(duration));
        priceEditText.setText(price);
        classTypeSpinner.setSelection(((ArrayAdapter) classTypeSpinner.getAdapter()).getPosition(classType));
        descriptionEditText.setText(description);
    }

    private void saveCourseIdToClassInstances(int courseId) {
        SQLiteDatabase dbWritable = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId);
        dbWritable.insert(DatabaseHelper.TABLE_CLASS_INSTANCES, null, values);
    }

    private int getCourseId(String dayOfWeek, String time, String capacity, String duration, String price, String classType, String description) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES, new String[]{DatabaseHelper.COLUMN_COURSE_ID},
                DatabaseHelper.COLUMN_DAY_OF_WEEK + "=? AND " + DatabaseHelper.COLUMN_TIME_OF_COURSE + "=? AND " +
                        DatabaseHelper.COLUMN_CAPACITY + "=? AND " + DatabaseHelper.COLUMN_DURATION + "=? AND " +
                        DatabaseHelper.COLUMN_PRICE + "=? AND " + DatabaseHelper.COLUMN_TYPE_OF_CLASS + "=? AND " +
                        DatabaseHelper.COLUMN_DESCRIPTION + "=?",
                new String[]{dayOfWeek, time, capacity, duration, price, classType, description}, null, null, null);

        int courseId = -1;
        if (cursor.moveToFirst()) {
            courseId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_COURSE_ID));
        }
        cursor.close();
        return courseId;
    }


    private void calculateEndTime() {
        String time = timeSpinner.getSelectedItem().toString();
        String duration = durationSpinner.getSelectedItem().toString();

        if (!time.isEmpty() && !duration.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date startTime = sdf.parse(time);
                int durationMinutes = Integer.parseInt(duration);

                if (startTime != null) {
                    long endTimeMillis = startTime.getTime() + durationMinutes * 60 * 1000;
                    Date endTime = new Date(endTimeMillis);
                    String endTimeStr = sdf.format(endTime);
                    endTimeTextView.setText("End Time: " + endTimeStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        dayOfWeekSpinner.setSelection(0);
        timeSpinner.setSelection(0);
        capacityEditText.setText("");
        durationSpinner.setSelection(0);
        priceEditText.setText("");
        classTypeSpinner.setSelection(0);
        descriptionEditText.setText("");
        endTimeTextView.setText("End Time: ");
    }

    private void loadCourses() {
        courseList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String dayOfWeek = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DAY_OF_WEEK));
                String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME_OF_COURSE));
                String capacity = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CAPACITY));
                String duration = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION));
                String price = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE));
                String classType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE_OF_CLASS));
                String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION));

                String courseDetails = dayOfWeek + " - " + time + " - " + capacity + " - " + duration + " - " + price + " - " + classType + " - " + description;
                courseList.add(courseDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        courseAdapter.notifyDataSetChanged();
    }

    private void showConfirmationDialog(String dayOfWeek, String time, String capacity, String duration, String price, String classType, String description, boolean isUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isUpdate ? "Confirm Update Course Details" : "Confirm Add Course Details");
        builder.setMessage("Day of Week: " + dayOfWeek + "\n" +
                "Time: " + time + "\n" +
                "Capacity: " + capacity + "\n" +
                "Duration: " + duration + "\n" +
                "Price: " + price + "\n" +
                "Class Type: " + classType + "\n" +
                "Description: " + description);
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (isUpdate) {
                updateCourse(dayOfWeek, time, capacity, duration, price, classType, description);
            } else {
                addCourse(dayOfWeek, time, capacity, duration, price, classType, description);
            }
        });
        builder.setNegativeButton("Edit", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addCourse() {
        String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();
        String time = timeSpinner.getSelectedItem().toString();
        String capacity = capacityEditText.getText().toString().trim();
        String duration = durationSpinner.getSelectedItem().toString();
        String price = priceEditText.getText().toString().trim();
        String classType = classTypeSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();

        if (time.isEmpty() || capacity.isEmpty() || duration.isEmpty() || price.isEmpty() || classType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showConfirmationDialog(dayOfWeek, time, capacity, duration, price, classType, description, false);
    }

    private void updateCourse() {
        String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();
        String time = timeSpinner.getSelectedItem().toString();
        String capacity = capacityEditText.getText().toString().trim();
        String duration = durationSpinner.getSelectedItem().toString();
        String price = priceEditText.getText().toString().trim();
        String classType = classTypeSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();

        if (time.isEmpty() || capacity.isEmpty() || duration.isEmpty() || price.isEmpty() || classType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showConfirmationDialog(dayOfWeek, time, capacity, duration, price, classType, description, true);
    }

    private void addCourse(String dayOfWeek, String time, String capacity, String duration, String price, String classType, String description) {
        progressBar.setVisibility(View.VISIBLE);

        // Save to SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(DatabaseHelper.COLUMN_TIME_OF_COURSE, time);
        values.put(DatabaseHelper.COLUMN_CAPACITY, capacity);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_PRICE, price);
        values.put(DatabaseHelper.COLUMN_TYPE_OF_CLASS, classType);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        db.insert(DatabaseHelper.TABLE_COURSES, null, values);

        // Save to Firebase
        Map<String, Object> course = new HashMap<>();
        course.put("dayOfWeek", dayOfWeek);
        course.put("time", time);
        course.put("capacity", capacity);
        course.put("duration", duration);
        course.put("price", price);
        course.put("classType", classType);
        course.put("description", description);

        this.db.collection("courses").add(course)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDCourseActivity.this, "Course added successfully", Toast.LENGTH_SHORT).show();
                    loadCourses();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDCourseActivity.this, "Failed to add course", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCourse(String dayOfWeek, String time, String capacity, String duration, String price, String classType, String description) {
        if (time.isEmpty() || capacity.isEmpty() || duration.isEmpty() || price.isEmpty() || classType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Update in SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(DatabaseHelper.COLUMN_TIME_OF_COURSE, time);
        values.put(DatabaseHelper.COLUMN_CAPACITY, capacity);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_PRICE, price);
        values.put(DatabaseHelper.COLUMN_TYPE_OF_CLASS, classType);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);

        String selection = DatabaseHelper.COLUMN_TIME_OF_COURSE + " = ?";
        String[] selectionArgs = { time };

        db.update(DatabaseHelper.TABLE_COURSES, values, selection, selectionArgs);

        // Update in Firebase
        Map<String, Object> course = new HashMap<>();
        course.put("dayOfWeek", dayOfWeek);
        course.put("time", time);
        course.put("capacity", capacity);
        course.put("duration", duration);
        course.put("price", price);
        course.put("classType", classType);
        course.put("description", description);

        this.db.collection("courses").whereEqualTo("time", time).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        this.db.collection("courses").document(documentId).update(course)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDCourseActivity.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                                    loadCourses();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDCourseActivity.this, "Failed to update course", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDCourseActivity.this, "Failed to update course", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteCourse() {
        String time = timeSpinner.getSelectedItem().toString();

        if (time.isEmpty()) {
            Toast.makeText(this, "Please enter the time of the course to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Delete from SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_TIME_OF_COURSE + " = ?";
        String[] selectionArgs = { time };
        db.delete(DatabaseHelper.TABLE_COURSES, selection, selectionArgs);

        // Delete from Firebase
        this.db.collection("courses").whereEqualTo("time", time).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            this.db.collection("courses").document(document.getId()).delete();
                        }
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CRUDCourseActivity.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                        loadCourses();
                        clearFields();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CRUDCourseActivity.this, "Course not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDCourseActivity.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
                });
    }

}