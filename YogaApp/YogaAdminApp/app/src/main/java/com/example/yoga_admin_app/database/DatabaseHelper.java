package com.example.yoga_admin_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.yoga_admin_app.model.Booking;
import com.example.yoga_admin_app.model.ClassInstances;
import com.example.yoga_admin_app.model.Course;
import com.example.yoga_admin_app.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "yoga_app.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";

    public static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSE_ID = "course_id";
    public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    public static final String COLUMN_TIME_OF_COURSE = "time_of_course";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TYPE_OF_CLASS = "type_of_class";
    public static final String COLUMN_DESCRIPTION = "description";

    public static final String TABLE_CLASS_INSTANCES = "class_instances";
    public static final String COLUMN_CLASS_INSTANCES_ID = "class_instances_id";
    public static final String COLUMN_COURSE_ID_FK = "course_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TEACHER = "teacher";
    public static final String COLUMN_COMMENTS = "comments";

    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COLUMN_BOOKING_ID = "booking_id";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_BOOKING_DATE = "booking_date";
    public static final String COLUMN_BOOKING_STATUS = "booking_status";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_ROLE + " TEXT);";

    private static final String TABLE_CREATE_COURSES =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DAY_OF_WEEK + " TEXT, " +
                    COLUMN_TIME_OF_COURSE + " TEXT, " +
                    COLUMN_CAPACITY + " INTEGER, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_TYPE_OF_CLASS + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT);";

    private static final String TABLE_CREATE_CLASS_INSTANCES =
            "CREATE TABLE " + TABLE_CLASS_INSTANCES + " (" +
                    COLUMN_CLASS_INSTANCES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COURSE_ID_FK + " INTEGER, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TEACHER + " TEXT, " +
                    COLUMN_COMMENTS + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_COURSE_ID_FK + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + "));";

    private static final String TABLE_CREATE_BOOKINGS =
            "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                    COLUMN_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COURSE_ID_FK + " INTEGER, " +
                    COLUMN_CLASS_INSTANCES_ID + " INTEGER, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_BOOKING_DATE + " TEXT, " +
                    COLUMN_BOOKING_STATUS + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_COURSE_ID_FK + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_CLASS_INSTANCES_ID + ") REFERENCES " + TABLE_CLASS_INSTANCES + "(" + COLUMN_CLASS_INSTANCES_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_COURSES);
        db.execSQL(TABLE_CREATE_CLASS_INSTANCES);
        db.execSQL(TABLE_CREATE_BOOKINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        onCreate(db);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                String role = cursor.getString(cursor.getColumnIndex(COLUMN_ROLE));

                User user = new User(id, email, password, User.Role.valueOf(role));
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    public void updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int courseId = cursor.getInt(cursor.getColumnIndex(COLUMN_COURSE_ID));
                String dayOfWeek = cursor.getString(cursor.getColumnIndex(COLUMN_DAY_OF_WEEK));
                String timeOfCourse = cursor.getString(cursor.getColumnIndex(COLUMN_TIME_OF_COURSE));
                int capacity = cursor.getInt(cursor.getColumnIndex(COLUMN_CAPACITY));
                int duration = cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION));
                double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                String typeOfClass = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_OF_CLASS));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));

                Course course = new Course(courseId, dayOfWeek, timeOfCourse, capacity, duration, price, typeOfClass, description);
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courses;
    }

    public List<ClassInstances> getAllClassInstances() {
        List<ClassInstances> classInstances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASS_INSTANCES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String classInstancesId = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_INSTANCES_ID));
                int courseId = cursor.getInt(cursor.getColumnIndex(COLUMN_COURSE_ID_FK));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                String teacher = cursor.getString(cursor.getColumnIndex(COLUMN_TEACHER));
                String comments = cursor.getString(cursor.getColumnIndex(COLUMN_COMMENTS));

                ClassInstances classInstance = new ClassInstances(classInstancesId, courseId, date, teacher, comments);
                classInstances.add(classInstance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classInstances;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKINGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int bookingId = cursor.getInt(cursor.getColumnIndex(COLUMN_BOOKING_ID));
                int courseId = cursor.getInt(cursor.getColumnIndex(COLUMN_COURSE_ID_FK));
                String classInstanceId = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_INSTANCES_ID));
                String userEmail = cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL));
                String bookingDate = cursor.getString(cursor.getColumnIndex(COLUMN_BOOKING_DATE));
                String bookingStatus = cursor.getString(cursor.getColumnIndex(COLUMN_BOOKING_STATUS));

                Booking booking = new Booking(bookingId, courseId, classInstanceId, userEmail, bookingDate, bookingStatus);
                bookings.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

}