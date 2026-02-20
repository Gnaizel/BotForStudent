package org.example.scraper.models;

import lombok.Data;

@Data
public class ScheduleEntry {
    private String building;
    private String day;
    private String lessonNumber;
    private String time;
    private String content;
}
