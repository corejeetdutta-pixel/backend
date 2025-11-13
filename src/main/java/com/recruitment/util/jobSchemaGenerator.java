package com.recruitment.util;

import org.json.JSONObject;
import java.time.format.DateTimeFormatter;

import com.recruitment.entity.Job;

public class jobSchemaGenerator {

    public static String generateJobPostingSchema(Job job) {
        JSONObject schema = new JSONObject();
        schema.put("@context", "https://schema.org/");
        schema.put("@type", "JobPosting");
        schema.put("title", job.getTitle());
        schema.put("description", job.getDescription());

        // Use correct field names from Job entity
        schema.put("datePosted", job.getOpeningDate().format(DateTimeFormatter.ISO_DATE));
        schema.put("validThrough", job.getLastDate().format(DateTimeFormatter.ISO_DATE));

        JSONObject hiringOrg = new JSONObject();
        hiringOrg.put("@type", "Organization");
        hiringOrg.put("name", job.getCompany());
        // Removed companyWebsite as it doesn't exist in Job entity
        schema.put("hiringOrganization", hiringOrg);

        JSONObject jobLocation = new JSONObject();
        JSONObject address = new JSONObject();
        address.put("@type", "PostalAddress");
        address.put("addressLocality", job.getLocation());
        address.put("addressCountry", "IN");
        jobLocation.put("@type", "Place");
        jobLocation.put("address", address);
        schema.put("jobLocation", jobLocation);

        // Updated salary structure to handle min and max salary
        JSONObject baseSalary = new JSONObject();
        JSONObject salaryValue = new JSONObject();
        salaryValue.put("@type", "QuantitativeValue");

        // Handle salary range
        if (job.getMinSalary() != null && job.getMaxSalary() != null) {
            salaryValue.put("minValue", job.getMinSalary());
            salaryValue.put("maxValue", job.getMaxSalary());
        } else if (job.getMinSalary() != null) {
            salaryValue.put("value", job.getMinSalary());
        } else if (job.getMaxSalary() != null) {
            salaryValue.put("value", job.getMaxSalary());
        }

        salaryValue.put("unitText", "YEAR");
        baseSalary.put("@type", "MonetaryAmount");
        baseSalary.put("currency", "INR");
        baseSalary.put("value", salaryValue);
        schema.put("baseSalary", baseSalary);

        schema.put("employmentType", job.getEmploymentType());
        schema.put("directApply", true);

        // Added additional fields from Job entity that are relevant for schema
        if (job.getExperience() != null) {
            schema.put("experienceRequirements", job.getExperience());
        }

        if (job.getJobType() != null) {
            schema.put("jobLocationType", job.getJobType()); // Maps to remote/hybrid/onsite
        }

        if (job.getRequirements() != null) {
            schema.put("skills", job.getRequirements());
        }

        if (job.getResponsibilities() != null) {
            schema.put("responsibilities", job.getResponsibilities());
        }

        return schema.toString(2);
    }
}