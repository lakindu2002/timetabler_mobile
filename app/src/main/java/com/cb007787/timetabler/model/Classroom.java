package com.cb007787.timetabler.model;

public class Classroom {
    private int classroomId;
    private String classroomName;
    private int maxCapacity;
    private boolean acPresent;
    private boolean smartBoardPresent;

    public Classroom() {

    }

    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean isAcPresent() {
        return acPresent;
    }

    public void setAcPresent(boolean acPresent) {
        this.acPresent = acPresent;
    }

    public boolean isSmartBoardPresent() {
        return smartBoardPresent;
    }

    public void setSmartBoardPresent(boolean smartBoardPresent) {
        this.smartBoardPresent = smartBoardPresent;
    }
}
