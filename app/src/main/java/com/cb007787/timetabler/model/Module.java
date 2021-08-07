package com.cb007787.timetabler.model;

import java.util.List;

public class Module {
    private int moduleId;
    private String moduleName;
    private String creditCount;
    private String contactHours;
    private String independentHours;
    private User theLecturer;
    private List<BatchShow> theBatchList;

    public Module() {
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getCreditCount() {
        return creditCount;
    }

    public void setCreditCount(String creditCount) {
        this.creditCount = creditCount;
    }

    public String getContactHours() {
        return contactHours;
    }

    public void setContactHours(String contactHours) {
        this.contactHours = contactHours;
    }

    public String getIndependentHours() {
        return independentHours;
    }

    public void setIndependentHours(String independentHours) {
        this.independentHours = independentHours;
    }

    public User getTheLecturer() {
        return theLecturer;
    }

    public void setTheLecturer(User theLecturer) {
        this.theLecturer = theLecturer;
    }

    public List<BatchShow> getTheBatchList() {
        return theBatchList;
    }

    public void setTheBatchList(List<BatchShow> theBatchList) {
        this.theBatchList = theBatchList;
    }
}
