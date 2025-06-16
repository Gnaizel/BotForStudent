package ru.gnaizel.service.schebule;

public interface ScheduleService {
    String  fetchAndExtractTeachersSchedule(String groupName);

    String buildScheduleToday(String groupName);

    String buildScheduleToNextDay(String groupName);
}
