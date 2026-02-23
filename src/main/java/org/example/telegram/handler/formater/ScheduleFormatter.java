package org.example.telegram.handler.formater;

import org.example.scraper.models.ScheduleEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScheduleFormatter {

    public static String format(String groupName, List<ScheduleEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 <b>").append(groupName).append("</b>");

        Map<String, List<ScheduleEntry>> groupedByDay = entries.stream()
                .collect(Collectors.groupingBy(ScheduleEntry::getDay, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<ScheduleEntry>> dayEntry : groupedByDay.entrySet()) {
            List<ScheduleEntry> lessons = dayEntry.getValue().stream()
                    .filter(e -> e.getContent() != null && !e.getContent().isBlank() && !e.getContent().equals("—"))
                    .toList();

            if (lessons.isEmpty()) continue;

            sb.append("\n┌ 📆 <b>").append(formatDay(dayEntry.getKey())).append("</b>\n");

            for (ScheduleEntry entry : lessons) {
                String time = entry.getTime()
                        .replace(" - ", ":")
                        .replace(".",":").
                        substring(0, 5);
                String content = entry.getContent();

                sb.append("│ <b>").append(entry.getLessonNumber()).append("</b>")
                        .append(" - <code>").append(time).append("</code>\n");

                sb.append("│   ").append(formatSubject(content)).append("\n");

                String teacherLine = formatTeacherAndCabinet(content);
                if (!teacherLine.isEmpty()) {
                    sb.append("│   <i>").append(teacherLine).append("</i>\n");
                }
            }
        }
        return sb.toString();
    }

    private static String formatDay(String day) {
        Map<String, String> shortDays = Map.of(
                "Понедельник", "Пн",
                "Вторник", "Вт",
                "Среда", "Ср",
                "Четверг", "Чт",
                "Пятница", "Пт",
                "Суббота", "Сб",
                "Воскресенье", "Вс"
        );

        String[] parts = day.split(", ");
        if (parts.length < 2) return day;

        String shortName = shortDays.getOrDefault(parts[0], parts[0]);
        String shortDate = parts[1].substring(0, 5);
        return shortName + ", " + shortDate + " ─────────────";
    }

    private static String formatSubject(String content) {
        String cleaned = content;

        int kabStart = content.indexOf("(каб.");
        if (kabStart != -1) cleaned = content.substring(0, kabStart).trim();
        if (cleaned.endsWith(",")) cleaned = cleaned.substring(0, cleaned.length() - 1).trim();

        int commaIdx = cleaned.lastIndexOf(",");
        if (commaIdx != -1) return cleaned.substring(0, commaIdx).trim();

        return cleaned;
    }

    private static String formatTeacherAndCabinet(String content) {
        String cabinet = "";
        String cleaned = content;

        int kabStart = content.indexOf("(каб.");
        if (kabStart != -1) {
            int kabEnd = content.indexOf(")", kabStart);
            if (kabEnd != -1) {
                cabinet = content.substring(kabStart + 1, kabEnd).replace("каб.", "").trim();
                cleaned = content.substring(0, kabStart).trim();
            }
        }

        if (cleaned.endsWith(",")) cleaned = cleaned.substring(0, cleaned.length() - 1).trim();

        int commaIdx = cleaned.lastIndexOf(",");
        if (commaIdx == -1) return "";

        String teacher = cleaned.substring(commaIdx + 1).trim();
        return cabinet.isEmpty() ? teacher : teacher + " · " + cabinet;
    }
}