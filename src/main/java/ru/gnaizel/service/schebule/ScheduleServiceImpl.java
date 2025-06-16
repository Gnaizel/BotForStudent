package ru.gnaizel.service.schebule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gnaizel.client.ppk.PpkClient;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.formater.ScheduleFormatter;
import ru.gnaizel.model.ScheduleEntry;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final PpkClient ppkClient;

    @Override
    public String fetchAndExtractTeachersSchedule(String groupName) {
        String html = ppkClient.getHtmlScheduleForPpkSite();

        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> allEntries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        List<ScheduleEntry> filtered = applyFilters(allEntries);

        return ScheduleFormatter.format(groupName, filtered);
    }

    private List<ScheduleEntry> applyFilters(List<ScheduleEntry> entries) {
        // По умолчанию ничего не фильтруем, но можно гибко менять:
        return entries.stream()
                //.filter(entry -> entry.getDay().equals("Понедельник"))
                //.filter(entry -> entry.getLessonNumber().equals("2"))
                //.filter(entry -> entry.getBuilding().equals("1"))
                .collect(Collectors.toList());
    }

    @Override
    public String buildScheduleToday(String groupName) {
        LocalDate todayDaty = LocalDate.now();

        return null;
    }

    @Override
    public String buildScheduleToNextDay(String groupName) {
        LocalDate nextDaty = LocalDate.now().plusDays(1);

        return null;
    }
}
