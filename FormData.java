package com.example.computerscience1;

public class FormData {

    String course,name,dob,branch,year,rollno;
    public FormData(String course, String name,String rollno, String dob, String branch, String year) {
        this.course = course;
        this.name = name;
        this.dob = dob;
        this.rollno = rollno;
        this.branch = branch;
        this.year = year;
    }

    public String getCourse() {
        return course;
    }

    public String getRollno() {
        return rollno;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getBranch() {
        return branch;
    }

    public String getYear() {
        return year;
    }
}
