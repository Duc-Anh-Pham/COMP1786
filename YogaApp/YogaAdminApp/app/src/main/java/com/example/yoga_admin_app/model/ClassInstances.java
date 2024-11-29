package com.example.yoga_admin_app.model;

import java.util.Date;

public class ClassInstances {
    private String classInstancesId;
    private int courseId;
    private String date;
    private String teacher;
    private String comments;


    public ClassInstances(String classInstancesId, int courseId, String date, String teacher, String comments) {
        this.classInstancesId = classInstancesId;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    public String getClassInstancesId() {
        return classInstancesId;
    }

    public void setClassInstancesId(String classInstancesId) {
        this.classInstancesId = classInstancesId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}