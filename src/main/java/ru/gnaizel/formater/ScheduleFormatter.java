package ru.gnaizel.formater;

import lombok.extern.slf4j.Slf4j;
import ru.gnaizel.model.ScheduleEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ScheduleFormatter {

    public static String format(String groupName, List<ScheduleEntry> entries) {
        StringBuilder sb = new StringBuilder("📅 Расписание для группы: ")
                .append(groupName)
                .append("\n\n");

        Map<String, List<ScheduleEntry>> groupedByDay = entries.stream()
                .collect(Collectors.groupingBy(ScheduleEntry::getDay, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<ScheduleEntry>> dayEntry : groupedByDay.entrySet()) {
            sb.append("📌 ").append(dayEntry.getKey()).append(":\n");

            for (ScheduleEntry entry : dayEntry.getValue()) {
                sb.append("  ")
                        .append(entry.getLessonNumber()).append(" (").append(entry.getTime()).append(")")
                        .append(" — ").append(entry.getContent()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}

