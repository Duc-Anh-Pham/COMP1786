// Booking.java
package com.example.yoga_admin_app.model;

import java.util.Date;

public class Booking {
    private int bookingId;
    private int courseId;
    private String classInstanceId;
    private String userEmail;
    private String bookingDate;
    private String bookingStatus;

    public Booking(int bookingId, int courseId, String classInstanceId, String userEmail, String bookingDate, String bookingStatus) {
        this.bookingId = bookingId;
        this.courseId = courseId;
        this.classInstanceId = classInstanceId;
        this.userEmail = userEmail;
        this.bookingDate = bookingDate;
        this.bookingStatus = bookingStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getClassInstanceId() {
        return classInstanceId;
    }

    public void setClassInstanceId(String classInstanceId) {
        this.classInstanceId = classInstanceId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}