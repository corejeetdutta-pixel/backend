package com.recruitment.dto;

import com.recruitment.entity.Job;

public class JobWithSchemaResponse {
    private Job job;
    private String schema;

    public JobWithSchemaResponse(Job job, String schema) {
        this.job = job;
        this.schema = schema;
    }

    // Getters and Setters
    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
