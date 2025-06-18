package ru.gnaizel.service.schebule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gnaizel.client.ppk.PpkClient;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.formater.ScheduleFormatter;
import ru.gnaizel.model.ScheduleEntry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final PpkClient ppkClient;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String fetchAndExtractTeachersSchedule(String groupName, String korpusName) {
        String html = ppkClient.getHtmlScheduleForPpkSite();

        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> allEntries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName)
                .stream().filter(scheduleEntry -> scheduleEntry.getBuilding().equals(korpusName))
                .toList();


        return ScheduleFormatter.format(groupName, allEntries);
    }

    @Override
    public String buildScheduleToday(String groupName, String korpusName) {
        LocalDate todayDaty = LocalDate.now();

        String html = ppkClient.getHtmlScheduleForPpkSite();
        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(todayDaty);
                })
                .toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }

    @Override
    public String buildScheduleToNextDay(String groupName, String korpusName) {
        LocalDate nextDay = LocalDate.now().plusDays(1);

        String html = ppkClient.getHtmlScheduleForPpkSite();
        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(nextDay);
                })
                .toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }
}
