package com.svce.oejava;

public class courseSelection {
    private int id;
    private String title;
    private String code;
    private String instructor;
    private String department;
    private int capacity;
    private int enrolled;

    // new fields
    private String restricted;  // e.g. "IT,CS" or blank
    private boolean full;       // true if enrolled >= capacity
    private boolean disabled;   // true if restricted OR full

    public courseSelection(int id, String title, String code,
                           String instructor, String department,
                           int capacity, int enrolled, String restricted) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.instructor = instructor;
        this.department = department;
        this.capacity = capacity;
        this.enrolled = enrolled;
        this.restricted = restricted;
        this.full = false;
        this.disabled = false;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolled() { return enrolled; }
    public void setEnrolled(int enrolled) { this.enrolled = enrolled; }

    public String getRestricted() { return restricted; }
    public void setRestricted(String restricted) { this.restricted = restricted; }

    public boolean isFull() { return full; }
    public void setFull(boolean full) { this.full = full; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    // extra getters (frontend compatibility)
    public String getName() { return title; }
    public String getDescription() { return "Instructor: " + (instructor == null ? "" : instructor); }
}
