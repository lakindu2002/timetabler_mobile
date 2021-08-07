package com.cb007787.timetabler.model;

import java.util.List;

public class BatchShow {
    private String batchCode;
    private String batchName;
    private List<User> studentList;
    private List<Module> moduleList;
    private List<LectureShow> lecturesForBatch;

    public BatchShow() {
    }

    public List<LectureShow> getLecturesForBatch() {
        return lecturesForBatch;
    }

    public void setLecturesForBatch(List<LectureShow> lecturesForBatch) {
        this.lecturesForBatch = lecturesForBatch;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public List<User> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<User> studentList) {
        this.studentList = studentList;
    }

    public List<Module> getModuleList() {
        return moduleList;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }
}
