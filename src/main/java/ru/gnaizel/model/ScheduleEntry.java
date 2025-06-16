package ru.gnaizel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleEntry {
    private String building;
    private String day;
    private String lessonNumber;
    private String time;
    private String content;
}