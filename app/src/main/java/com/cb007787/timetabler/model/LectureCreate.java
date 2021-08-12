package com.cb007787.timetabler.model;

import java.util.Date;

public class LectureCreate {
    private int lectureId;
    private Date lectureDate;
    private String startTime;
    private String endTime;
    private int moduleId;
    private String[] batchList;
    private int classroomID;

    public LectureCreate() {
    }

    public int getLectureId() {
        return lectureId;
    }

    public void setLectureId(int lectureId) {
        this.lectureId = lectureId;
    }

    public Date getLectureDate() {
        return lectureDate;
    }

    public void setLectureDate(Date lectureDate) {
        this.lectureDate = lectureDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String[] getBatchList() {
        return batchList;
    }

    public void setBatchList(String[] batchList) {
        this.batchList = batchList;
    }

    public int getClassroomID() {
        return classroomID;
    }

    public void setClassroomID(int classroomID) {
        this.classroomID = classroomID;
    }
}
