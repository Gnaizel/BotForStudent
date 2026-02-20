package org.example.telegram.handler.message;

import lombok.RequiredArgsConstructor;
import org.example.scraper.exception.ScraperHtmlValidationException;
import org.example.scraper.models.ScheduleEntry;
import org.example.service.schedule.ScheduleService;
import org.example.service.user.UserService;
import org.example.telegram.TelegramBotClient;
import org.example.telegram.handler.formater.ScheduleFormatter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ToDayScheduleMessage implements BotMessageCmd {
    private final UserService userService;
    private final ScheduleService scheduleService;
    private final TelegramBotClient client;

    @Override
    public void execute(Message message) {
        long tgUserId = message.getFrom().getId();

        String groupName = userService
                .getUserByTgUserId(tgUserId)
                .getStudentGroupName();

        String messageText;

        try {
            List<ScheduleEntry> schedule = scheduleService.getScheduleByDate(
                    groupName,
                    LocalDate.now());
            messageText = ScheduleFormatter.format(groupName, schedule);
        } catch (ScraperHtmlValidationException e) {
            messageText = "Вашего расписания по данной дате не опубликовано \n" +
                    "Вероятно его нет именно на сегодня \nПопробуйте \" \uD83D\uDCC5 На неделю \" ";
        }

        client.sendMessage(messageText, tgUserId);
    }

    @Override
    public String getMessageCmd() {
        return "\uD83D\uDDD3\uFE0F Сегодня";
    }
}
