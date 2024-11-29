package com.example.yoga_admin_app.model;

public class Course {
    private int courseId;
    private String dayOfWeek;
    private String timeOfCourse;
    private int capacity;
    private int duration;
    private double price;
    private String typeOfClass;
    private String description;

    public Course(int courseId, String dayOfWeek, String timeOfCourse, int capacity, int duration, double price, String typeOfClass, String description) {
        this.courseId = courseId;
        this.dayOfWeek = dayOfWeek;
        this.timeOfCourse = timeOfCourse;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.typeOfClass = typeOfClass;
        this.description = description;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeOfCourse() {
        return timeOfCourse;
    }

    public void setTimeOfCourse(String timeOfCourse) {
        this.timeOfCourse = timeOfCourse;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTypeOfClass() {
        return typeOfClass;
    }

    public void setTypeOfClass(String typeOfClass) {
        this.typeOfClass = typeOfClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}