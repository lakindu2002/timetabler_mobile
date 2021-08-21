package com.cb007787.timetabler.model;

public class BatchCreate {
    private String batchCode;
    private String batchName;
    private String[] moduleId;

    public BatchCreate() {
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

    public String[] getModuleId() {
        return moduleId;
    }

    public void setModuleId(String[] moduleId) {
        this.moduleId = moduleId;
    }
}
