package ru.gnaizel.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final UserService userService;
    private final ScheduleService scheduleService;

    @Value("${telegram.bot.token}")
    private String TELEGRAM_BOT_TOKEN;

    @Value("${telegram.bot.username}")
    private String TELEGRAM_BOT_USERNAME;

    @Override
    public void onUpdateReceived(Update update) {
        creteMenuCommand();
        validationUpdate(update);
        foundUser(update);
        handleCommend(update);
    }

    private void creteMenuCommand() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/schedule_to_next_day", "Расписание на завтра"));
        botCommands.add(new BotCommand("/schedule", "Расписание на неделю"));

        SetMyCommands myCommands = new SetMyCommands();
        myCommands.setCommands(botCommands);

        try {
            execute(myCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void foundUser(Update update) {
        Message message = update.getMessage();

        if (userService.checkingForANewUserByMassage(message)) {
            String welcomeMessage = new StringBuilder()
                    .append("Добро пожаловать ")
                    .append(userService.findUserByChatId(message.getChatId()).getUserName())
                    .append(" ! \n этот бот создан в целях по больше нихуя не делать,")
                    .append("\n он может прислать вам актуальное расписание. \n By @Ganizel")
                    .toString();
            sendMessage(update, welcomeMessage);
        }
    }

    private void sendMessage(Update update, String massage) {
        long chatId = update.getMessage().getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(massage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCommend(Update update) {
        String command;

        if (update.getMessage().getText().isBlank()) {
            throw new MessageValidationError("Message can't be blank");
        } else command = update.getMessage().getText();

        if (command.startsWith("/")) {
            command = update.getMessage().getText().toLowerCase().replace("/", "");
        } else {
            sendMessage(update, "Это не команда, все команды начинаются с /");
            throw new MessageValidationError("It is not command");
        }

        switch (command) {
            case "schedule":
                log.debug("Команда на запрос расписания в работе");
                sendMessage(update, scheduleService.fetchAndExtractTeachersSchedule("ИСП-926")); // НУЖНО ДАБАВИТЬ ВЫБОР ГРУППЫ
                break;
            case "schedule_to_next_day":
                break;
            default:
                sendMessage(update, "Эта команда не поддерживается");
        }
    }

    private void validationUpdate(Update update) {
        if (update == null) {
            throw new TelegramUpdateValidationError("It is not massage");
        }
        if (!update.hasMessage() && !(update.getMessage().hasText() || update.getMessage().hasDocument())) {
            throw new TelegramUpdateValidationError("Message is not available");
        }
    }

    @Override
    public String  getBotToken() {
        return TELEGRAM_BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return TELEGRAM_BOT_USERNAME;
    }
}
