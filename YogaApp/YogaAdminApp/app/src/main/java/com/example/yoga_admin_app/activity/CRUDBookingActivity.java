package com.example.yoga_admin_app.activity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.yoga_admin_app.database.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CRUDBookingActivity extends AppCompatActivity {
    private Spinner courseSpinner, statusSpinner,classInstanceSpinner;
    private EditText userEmailEditText, bookingDateEditText;
    private Button addBookingButton, updateBookingButton, deleteBookingButton, clearBookingButton;
    private ProgressBar progressBar;
    private ListView bookingListView;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;
    private ArrayAdapter<String> bookingAdapter;
    private ArrayList<String> bookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_booking);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();


        bookingDateEditText = findViewById(R.id.bookingDateEditText);
        courseSpinner = findViewById(R.id.courseSpinner);
        classInstanceSpinner = findViewById(R.id.classInstanceSpinner);
        statusSpinner = findViewById(R.id.statusSpinner);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        addBookingButton = findViewById(R.id.addBookingButton);
        updateBookingButton = findViewById(R.id.updateBookingButton);
        deleteBookingButton = findViewById(R.id.deleteBookingButton);
        clearBookingButton = findViewById(R.id.clearBookingButton);

        bookingDateEditText.setInputType(InputType.TYPE_NULL);
        bookingDateEditText.setOnClickListener(v -> showDatePickerDialog());

        progressBar = findViewById(R.id.progressBar);
        bookingListView = findViewById(R.id.bookingListView);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.booking_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        bookingList = new ArrayList<>();
        bookingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookingList);
        bookingListView.setAdapter(bookingAdapter);

        addBookingButton.setOnClickListener(v -> addBooking());
        updateBookingButton.setOnClickListener(v -> updateBooking());
        deleteBookingButton.setOnClickListener(v -> deleteBooking());
        clearBookingButton.setOnClickListener(v -> clearFields());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CRUDBookingActivity.this, AdminActivity.class);
            startActivity(intent);
            finish();
        });

        bookingListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBooking = bookingList.get(position);
            populateBookingDetails(selectedBooking);
        });

        loadBookings();
        loadCourses();
        loadClassInstances();

        // Attach the listener to the courseSpinner
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourseId = courseSpinner.getSelectedItem().toString();
                boolean hasClassInstances = loadClassInstancesByCourse(selectedCourseId);
                if (!hasClassInstances) {
                    Toast.makeText(CRUDBookingActivity.this, "No class instances available for the selected course. Please select another course.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

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
                    String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.getTime());
                    bookingDateEditText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void populateBookingDetails(String bookingDetails) {
        String[] details = bookingDetails.split(" - ");
        userEmailEditText.setText(details[0]);
        bookingDateEditText.setText(details[1]);
        statusSpinner.setSelection(((ArrayAdapter<String>) statusSpinner.getAdapter()).getPosition(details[2]));
    }

    private void clearFields() {
        userEmailEditText.setText("");
        bookingDateEditText.setText("");
        statusSpinner.setSelection(0);
    }

    private String getClassInstanceDate(String classInstanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLASS_INSTANCES, new String[]{DatabaseHelper.COLUMN_DATE},
                DatabaseHelper.COLUMN_CLASS_INSTANCES_ID + "=?", new String[]{classInstanceId}, null, null, null);

        String classInstanceDate = null;
        if (cursor.moveToFirst()) {
            classInstanceDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
        }
        cursor.close();
        return classInstanceDate;
    }

    private void loadBookings() {
        bookingList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String userEmail = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL));
                String bookingDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOKING_DATE));
                String bookingStatus = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOKING_STATUS));

                String bookingDetails = userEmail + " - " + bookingDate + " - " + bookingStatus;
                bookingList.add(bookingDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        bookingAdapter.notifyDataSetChanged();
    }

    private void loadCourses() {
        ArrayList<String> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String courseId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COURSE_ID));
                courseList.add(courseId);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);
    }

    private void loadClassInstances() {
        ArrayList<String> classInstanceList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLASS_INSTANCES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String classInstanceId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_INSTANCES_ID));
                classInstanceList.add(classInstanceId);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> classInstanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classInstanceList);
        classInstanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classInstanceSpinner.setAdapter(classInstanceAdapter);
    }

    private boolean loadClassInstancesByCourse(String courseId) {
        ArrayList<String> classInstanceList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLASS_INSTANCES, null, DatabaseHelper.COLUMN_COURSE_ID_FK + "=?", new String[]{courseId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String classInstanceId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_INSTANCES_ID));
                classInstanceList.add(classInstanceId);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> classInstanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classInstanceList);
        classInstanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classInstanceSpinner.setAdapter(classInstanceAdapter);

        return !classInstanceList.isEmpty();
    }

    private void addBooking() {
        String userEmail = userEmailEditText.getText().toString().trim();
        String bookingDate = bookingDateEditText.getText().toString().trim();
        String bookingStatus = statusSpinner.getSelectedItem().toString();
        String courseId = courseSpinner.getSelectedItem().toString();
        String classInstanceId = classInstanceSpinner.getSelectedItem().toString();

        if (userEmail.isEmpty() || bookingDate.isEmpty() || bookingStatus.isEmpty() || courseId.isEmpty() || classInstanceId.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate booking date
        String classInstanceDate = getClassInstanceDate(classInstanceId);
        if (!bookingDate.equals(classInstanceDate)) {
            Toast.makeText(this, "Please select a valid date according to the class instance date: " + classInstanceDate, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Save to SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, userEmail);
        values.put(DatabaseHelper.COLUMN_BOOKING_DATE, bookingDate);
        values.put(DatabaseHelper.COLUMN_BOOKING_STATUS, bookingStatus);
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId);
        values.put(DatabaseHelper.COLUMN_CLASS_INSTANCES_ID, classInstanceId);
        db.insert(DatabaseHelper.TABLE_BOOKINGS, null, values);

        // Save to Firebase
        Map<String, Object> booking = new HashMap<>();
        booking.put("userEmail", userEmail);
        booking.put("bookingDate", bookingDate);
        booking.put("bookingStatus", bookingStatus);
        booking.put("courseId", courseId);
        booking.put("classInstanceId", classInstanceId);

        this.db.collection("bookings").add(booking)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDBookingActivity.this, "Booking added successfully", Toast.LENGTH_SHORT).show();
                    loadBookings();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDBookingActivity.this, "Failed to add booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBooking() {
        String userEmail = userEmailEditText.getText().toString().trim();
        String bookingDate = bookingDateEditText.getText().toString().trim();
        String bookingStatus = statusSpinner.getSelectedItem().toString();
        String courseId = courseSpinner.getSelectedItem().toString();
        String classInstanceId = classInstanceSpinner.getSelectedItem().toString();

        if (userEmail.isEmpty() || bookingDate.isEmpty() || bookingStatus.isEmpty() || courseId.isEmpty() || classInstanceId.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate booking date
        String classInstanceDate = getClassInstanceDate(classInstanceId);
        if (!bookingDate.equals(classInstanceDate)) {
            Toast.makeText(this, "Please select a valid date according to the class instance date: " + classInstanceDate, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Update in SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, userEmail);
        values.put(DatabaseHelper.COLUMN_BOOKING_DATE, bookingDate);
        values.put(DatabaseHelper.COLUMN_BOOKING_STATUS, bookingStatus);
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId);
        values.put(DatabaseHelper.COLUMN_CLASS_INSTANCES_ID, classInstanceId);

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };

        db.update(DatabaseHelper.TABLE_BOOKINGS, values, selection, selectionArgs);

        // Update in Firebase
        Map<String, Object> booking = new HashMap<>();
        booking.put("userEmail", userEmail);
        booking.put("bookingDate", bookingDate);
        booking.put("bookingStatus", bookingStatus);
        booking.put("courseId", courseId);
        booking.put("classInstanceId", classInstanceId);

        this.db.collection("bookings").whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        this.db.collection("bookings").document(documentId).update(booking)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDBookingActivity.this, "Booking updated successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDBookingActivity.this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDBookingActivity.this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteBooking() {
        String userEmail = userEmailEditText.getText().toString().trim();

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter the user email of the booking to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Delete from SQLite
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };
        db.delete(DatabaseHelper.TABLE_BOOKINGS, selection, selectionArgs);

        // Delete from Firebase
        this.db.collection("bookings").whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        this.db.collection("bookings").document(documentId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDBookingActivity.this, "Booking deleted successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CRUDBookingActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CRUDBookingActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                });
    }
}