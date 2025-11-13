package com.recruitment.controller;

import com.recruitment.entity.Job;
import com.recruitment.repository.JobRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SitemapController {

    private final JobRepository jobRepository;

    public SitemapController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping(value = "/sitemap-jobs.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateJobSitemap() {
        try {
            List<Job> jobs = jobRepository.findAll();

            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);

            xmlWriter.writeStartDocument("UTF-8", "1.0");
            xmlWriter.writeStartElement("urlset");
            xmlWriter.writeDefaultNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");

            for (Job job : jobs) {
                xmlWriter.writeStartElement("url");

                // If slug doesn't exist, use ID as fallback for URL
                String slug = job.getJobId() != null ? job.getJobId() : job.getJobId().toString();
                xmlWriter.writeStartElement("loc");
                xmlWriter.writeCharacters("https://atract.in/jobs/" + slug);
                xmlWriter.writeEndElement(); // </loc>

                xmlWriter.writeEndElement(); // </url>
            }

            xmlWriter.writeEndElement(); // </urlset>
            xmlWriter.writeEndDocument();
            xmlWriter.close();

            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating job sitemap", e);
        }
    }
}