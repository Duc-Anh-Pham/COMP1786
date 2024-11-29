package com.example.yoga_admin_app.activity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin_app.R;
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CRUDClassInstanceActivity extends AppCompatActivity {
    private EditText dateEditText, teacherEditText, commentsEditText;
    private Button addButton, updateButton, deleteButton, clearButton;
    private ListView classInstanceListView;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> classInstanceAdapter;
    private ArrayList<String> classInstanceList;
    private String selectedClassInstanceId;
    private int courseId;
    private String dayOfWeek;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_class_instance);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();
        dateEditText = findViewById(R.id.dateEditText);
        teacherEditText = findViewById(R.id.teacherEditText);
        commentsEditText = findViewById(R.id.commentsEditText);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        clearButton = findViewById(R.id.clearButton);
        classInstanceListView = findViewById(R.id.classInstanceListView);

        classInstanceList = new ArrayList<>();
        classInstanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classInstanceList);
        classInstanceListView.setAdapter(classInstanceAdapter);

        // Get course details from intent
        Intent intent = getIntent();
        dayOfWeek = intent.getStringExtra("dayOfWeek");
        courseId = intent.getIntExtra("courseId", -1);

        // Preselect the date
        preselectDate(dayOfWeek);

        addButton.setOnClickListener(v -> addClassInstance());
        updateButton.setOnClickListener(v -> updateClassInstance());
        deleteButton.setOnClickListener(v -> deleteClassInstance());
        clearButton.setOnClickListener(v -> clearFields());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(CRUDClassInstanceActivity.this, CRUDCourseActivity.class);
            startActivity(backIntent);
            finish();
        });

        classInstanceListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedClassInstance = classInstanceList.get(position);
            populateClassInstanceDetails(selectedClassInstance);
        });

        dateEditText.setInputType(InputType.TYPE_NULL); // Make EditText non-editable
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        loadClassInstances();
    }

    private void populateClassInstanceDetails(String classInstanceDetails) {
        String[] details = classInstanceDetails.split(" - ");
        selectedClassInstanceId = details[0].trim(); // Set the selectedClassInstanceId
        dateEditText.setText(details[1].trim());
        teacherEditText.setText(details[2].trim());
        commentsEditText.setText(details[3].trim());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year1, monthOfYear, dayOfMonth);
                    String selectedDayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedCalendar.getTime());

                    if (selectedDayOfWeek.equalsIgnoreCase(dayOfWeek)) {
                        String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.getTime());
                        dateEditText.setText(selectedDate);
                    } else {
                        Toast.makeText(this, "Please select a " + dayOfWeek, Toast.LENGTH_SHORT).show();
                    }
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void preselectDate(String dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int targetDayOfWeek = getDayOfWeekInt(dayOfWeek);

        while (currentDayOfWeek != targetDayOfWeek) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        }

        String preselectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        dateEditText.setText(preselectedDate);
    }

    private int getDayOfWeekInt(String dayOfWeek) {
        switch (dayOfWeek.toLowerCase()) {
            case "sunday":
                return Calendar.SUNDAY;
            case "monday":
                return Calendar.MONDAY;
            case "tuesday":
                return Calendar.TUESDAY;
            case "wednesday":
                return Calendar.WEDNESDAY;
            case "thursday":
                return Calendar.THURSDAY;
            case "friday":
                return Calendar.FRIDAY;
            case "saturday":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    private void addClassInstance() {
        String date = dateEditText.getText().toString().trim();
        String teacher = teacherEditText.getText().toString().trim();
        String comments = commentsEditText.getText().toString().trim();

        if (date.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Date and Teacher fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SQLite
        SQLiteDatabase dbWritable = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_TEACHER, teacher);
        values.put(DatabaseHelper.COLUMN_COMMENTS, comments);
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId); // Attach courseId
        long newRowId = dbWritable.insert(DatabaseHelper.TABLE_CLASS_INSTANCES, null, values);

        if (newRowId != -1) {
            // Save to Firestore
            Map<String, Object> classInstance = new HashMap<>();
            classInstance.put("date", date);
            classInstance.put("teacher", teacher);
            classInstance.put("comments", comments);
            classInstance.put("courseId", courseId); // Attach courseId

            db.collection("classInstances").add(classInstance)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(CRUDClassInstanceActivity.this, "Class instance added successfully", Toast.LENGTH_SHORT).show();
                        loadClassInstances();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CRUDClassInstanceActivity.this, "Failed to add class instance", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Failed to add class instance to SQLite", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateClassInstance() {
        String date = dateEditText.getText().toString().trim();
        String teacher = teacherEditText.getText().toString().trim();
        String comments = commentsEditText.getText().toString().trim();

        if (date.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Date and Teacher fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedClassInstanceId == null) {
            Toast.makeText(this, "Please select a class instance to update", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update in SQLite
        SQLiteDatabase dbWritable = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_TEACHER, teacher);
        values.put(DatabaseHelper.COLUMN_COMMENTS, comments);
        int rowsUpdated = dbWritable.update(DatabaseHelper.TABLE_CLASS_INSTANCES, values, DatabaseHelper.COLUMN_CLASS_INSTANCES_ID + "=?", new String[]{selectedClassInstanceId});

        if (rowsUpdated > 0) {
            // Retrieve Firestore document ID
            db.collection("classInstances")
                    .whereEqualTo("classInstanceId", selectedClassInstanceId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            // Update in Firestore
                            Map<String, Object> classInstance = new HashMap<>();
                            classInstance.put("date", date);
                            classInstance.put("teacher", teacher);
                            classInstance.put("comments", comments);

                            db.collection("classInstances").document(documentId).update(classInstance)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CRUDClassInstanceActivity.this, "Class instance updated successfully", Toast.LENGTH_SHORT).show();
                                        loadClassInstances();
                                        clearFields();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(CRUDClassInstanceActivity.this, "Failed to update class instance in Firestore", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(CRUDClassInstanceActivity.this, "Class instance not found in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(CRUDClassInstanceActivity.this, "Failed to retrieve class instance from Firestore", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Failed to update class instance in SQLite", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteClassInstance() {
        if (selectedClassInstanceId == null) {
            Toast.makeText(this, "Please select a class instance to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete from SQLite
        SQLiteDatabase dbWritable = dbHelper.getWritableDatabase();
        int rowsDeleted = dbWritable.delete(DatabaseHelper.TABLE_CLASS_INSTANCES, DatabaseHelper.COLUMN_CLASS_INSTANCES_ID + "=?", new String[]{selectedClassInstanceId});

        if (rowsDeleted > 0) {
            // Delete from Firestore
            db.collection("classInstances")
                    .whereEqualTo("classInstancesId", selectedClassInstanceId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            db.collection("classInstances").document(documentId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CRUDClassInstanceActivity.this, "Class instance deleted successfully", Toast.LENGTH_SHORT).show();
                                        loadClassInstances();
                                        clearFields();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(CRUDClassInstanceActivity.this, "Failed to delete class instance from Firestore", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(CRUDClassInstanceActivity.this, "Class instance not found in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(CRUDClassInstanceActivity.this, "Failed to retrieve class instance from Firestore", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Failed to delete class instance from SQLite", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        dateEditText.setText("");
        teacherEditText.setText("");
        commentsEditText.setText("");
        selectedClassInstanceId = null;
    }

    private void loadClassInstances() {
        classInstanceList.clear();
        SQLiteDatabase dbReadable = dbHelper.getReadableDatabase();
        Cursor cursor = dbReadable.query(DatabaseHelper.TABLE_CLASS_INSTANCES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String classInstanceId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_INSTANCES_ID));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                String teacher = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TEACHER));
                String comments = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENTS));

                String classInstanceDetails = classInstanceId + " - " + date + " - " + teacher + " - " + comments;
                classInstanceList.add(classInstanceDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        classInstanceAdapter.notifyDataSetChanged();
    }
}