package ru.gnaizel.service.schebule;

public interface ScheduleService {
    String  fetchAndExtractTeachersSchedule(String groupName, String korpusName);

    String buildScheduleToday(String groupName, String korpusName);

    String buildScheduleToNextDay(String groupName, String korpusName);
}
