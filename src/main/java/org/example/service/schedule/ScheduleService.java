package org.example.service.schedule;

import org.example.scraper.models.ScheduleEntry;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<ScheduleEntry> getScheduleByDate(String groupName, LocalDate day);
    List<ScheduleEntry> getScheduleToWeek(String groupName);
}
