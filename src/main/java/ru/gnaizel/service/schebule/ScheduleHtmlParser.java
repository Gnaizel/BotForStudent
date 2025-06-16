package ru.gnaizel.service.schebule;

import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.model.ScheduleEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleHtmlParser {

    public static String extractStudentsBlock(String html) {
        Pattern pattern = Pattern.compile("let students\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            throw new ScheduleValidationError("'Students' block not found");
        }
        return matcher.group(1);
    }

    public static List<ScheduleEntry> parseSchedule(String studentsBlock, String groupName) {
        List<ScheduleEntry> result = new ArrayList<>();

        Pattern buildingPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{(.*?)\\}(,?)", Pattern.DOTALL);
        Matcher buildingMatcher = buildingPattern.matcher(studentsBlock);

        while (buildingMatcher.find()) {
            String building = buildingMatcher.group(1);
            String groupsBlock = buildingMatcher.group(2);

            Pattern groupPattern = Pattern.compile("\"" + Pattern.quote(groupName) + "\"\\s*:\\s*\\[(.*?)\\]",
                    Pattern.DOTALL);
            Matcher groupMatcher = groupPattern.matcher(groupsBlock);

            if (groupMatcher.find()) {
                String scheduleArrayRaw = groupMatcher.group(1);

                result.addAll(parseScheduleTable(scheduleArrayRaw, building));
            }
        }

        if (result.isEmpty()) {
            throw new ScheduleValidationError("Group '" + groupName + "' not found");
        }

        return result;
    }

    private static List<ScheduleEntry> parseScheduleTable(String raw, String building) {
        List<ScheduleEntry> entries = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        List<String> times = new ArrayList<>();

        Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
        Matcher rowMatcher = rowPattern.matcher(raw);

        int rowIndex = 0;
        while (rowMatcher.find()) {
            String row = rowMatcher.group(1);
            String[] cells = cleanRowCells(row.split("</td>"));

            if (rowIndex == 0) {
                headers.addAll(Arrays.asList(cells).subList(2, cells.length));
            } else if (rowIndex >= 2) {
                String lessonNumber = cells[0];
                String time = new StringBuilder(cells[1]).insert(5, " - ").toString();
                for (int i = 2; i < cells.length; i++) {
                    String day = headers.get(i - 2);
                    String content = cells[i].isEmpty() ? "—" : cells[i];

                    ScheduleEntry entry = new ScheduleEntry();
                    entry.setBuilding(building);
                    entry.setDay(day);
                    entry.setLessonNumber(lessonNumber);
                    entry.setTime(time);
                    entry.setContent(content);
                    entries.add(entry);
                }
            }
            rowIndex++;
        }

        return entries;
    }

    private static String[] cleanRowCells(String[] rawCells) {
        String[] cleaned = new String[rawCells.length];
        for (int i = 0; i < rawCells.length; i++) {
            cleaned[i] = rawCells[i].replaceAll("<.*?>", "").replace("&nbsp;", " ").trim();
        }
        return cleaned;
    }
}

