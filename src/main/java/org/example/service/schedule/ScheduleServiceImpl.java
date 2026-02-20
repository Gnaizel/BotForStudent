package org.example.service.schedule;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.scraper.client.PpkClient;
import org.example.scraper.exception.ScheduleNotFound;
import org.example.scraper.exception.ScraperHtmlValidationException;
import org.example.scraper.models.ScheduleEntry;
import org.example.scraper.scraper.HtmlPPKScraper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final String ppkScheduleLink;
    private final PpkClient ppkClient;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private volatile String html;

    public ScheduleServiceImpl(@Value("${ppk.schedule-link}") String ppkScheduleLink) {
        this.ppkScheduleLink = ppkScheduleLink;
        this.ppkClient = new PpkClient(ppkScheduleLink);
    }

    @PostConstruct
    public void init() {
        log.info("Initial HTML fetch...");
        try {
            updateHtml();
            if (this.html == null) {
                throw new RuntimeException("Failed to fetch initial HTML. Application cannot start.");
            }
        } catch (Exception e) {
            log.error("CRITICAL: Could not load initial schedule: {}", e.getMessage());
            throw new IllegalStateException("Initial schedule load failed", e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledUpdate() {
        log.debug("Scheduled HTML update started...");
        try {
            updateHtml();
        } catch (Exception e) {
            log.error("Scheduled update failed: {}", e.getMessage());
        }
    }

    private void updateHtml() {
        String fetchedHtml = ppkClient.getHtmlPpk();
        if (fetchedHtml != null && !fetchedHtml.equals(this.html)) {
            this.html = fetchedHtml;
            log.info("Html updated successfully");
        } else if (fetchedHtml == null) {
            log.warn("Fetched HTML is null");
        } else {
            log.debug("No changes in HTML");
        }
    }

    @Override
    public List<ScheduleEntry> getScheduleByDate(String groupName, LocalDate day) throws ScraperHtmlValidationException {
        if (this.html == null) {
            throw new ScheduleNotFound("Schedule data is not available yet.");
        }

        String studentsBlock = HtmlPPKScraper.extractStudentsBlock(html);
        List<ScheduleEntry> entries = HtmlPPKScraper.parseSchedule(studentsBlock, groupName);

        return entries.stream()
                .filter(entry -> {
                    try {
                        String[] dateParse = entry.getDay().split(" ");
                        return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(day);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    @Override
    public List<ScheduleEntry> getScheduleToWeek(String groupName) {
        String studentsBlock = HtmlPPKScraper.extractStudentsBlock(html);
        List<ScheduleEntry> entries = HtmlPPKScraper.parseSchedule(studentsBlock, groupName);

        entries = entries.stream().toList();

        if (!entries.isEmpty()) {
            return entries;
        }

        throw new ScheduleNotFound("Not found any schedule for " + groupName);
    }
}
