package com.cb007787.timetabler.model;

import java.util.Date;
import java.util.List;

public class LectureShow {
    private int lectureId;
    private Date lectureDate;
    private String startTime;
    private String endTime;
    private Classroom theClassroom;
    private Module theModule;
    private List<BatchShow> batchesLectureConducedTo;

    public LectureShow() {
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

    public Classroom getTheClassroom() {
        return theClassroom;
    }

    public void setTheClassroom(Classroom theClassroom) {
        this.theClassroom = theClassroom;
    }

    public Module getTheModule() {
        return theModule;
    }

    public void setTheModule(Module theModule) {
        this.theModule = theModule;
    }

    public List<BatchShow> getBatchesLectureConducedTo() {
        return batchesLectureConducedTo;
    }

    public void setBatchesLectureConducedTo(List<BatchShow> batchesLectureConducedTo) {
        this.batchesLectureConducedTo = batchesLectureConducedTo;
    }
}
