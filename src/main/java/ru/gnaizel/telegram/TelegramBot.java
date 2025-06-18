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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.user.UserService;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final HashMap<Long, Integer> inProgress = new HashMap<>(); //  1 - user chat id | 2 - processId

    @Override
    public void onUpdateReceived(Update update) {
        creteMenuCommand();
        validationUpdate(update);
        foundUser(update);

        // Сначала проверяем процесс
        if (checkForProcessAndHandle(update)) {
            return; // Если процесс был обработан, выходим
        }

        // Только если не было in-progress действия, обрабатываем дальше
        handleUpdate(update);
    }

    private void creteMenuCommand() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/schedule_to_next_day", "Расписание на завтра"));
        botCommands.add(new BotCommand("/schedule_to_day", "Расписание на сегодня"));
        botCommands.add(new BotCommand("/schedule", "Расписание на неделю"));
        botCommands.add(new BotCommand("/start", "Команда для начального меню"));

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

        if (userService.checkingForANewUserByMassage(update)) {
            String welcomeMessage = new StringBuilder()
                    .append("Добро пожаловать ")
                    .append(userService.findUserByChatId(message.getChatId()).getUserName())
                    .append(" ! \n этот бот создан для оптимизации простых действий связаных с учёбой,")
                    .append("\n Пока он может прислать вам актуальное расписание. \n бот в процессе доработки \n By @Ganizel")
                    .toString();
            sendMessage(update, welcomeMessage);
        }
    }

    private void sendMessage(Update update, String massage) {
        long chatId = 0;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(massage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage()) {
            handleCommend(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private boolean checkForProcessAndHandle(Update update) {
        long chatId = 0;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else {
            return false;
        }

        if (inProgress.containsKey(chatId)) {
            switch (inProgress.get(chatId)) {
                case 1: // setCohort() (setGroup)
                    try {
                        if (update.getMessage().getText().isEmpty() || update.getMessage().getText() == null) {
                            sendMessage(update,"Поле группы не может быть пустым");
                            throw new MessageValidationError("message text is empty");
                        }

                        if (update.getMessage().getText().length() < 3 || update.getMessage().getText().length() > 10) {
                            sendMessage(update,"Это не похоже на название группы");
                            throw new MessageValidationError("Grout can't be it");
                        }

                        if (!update.getMessage().getText().contains("-")) {
                            sendMessage(update,"Это не похоже на название группы");
                            throw new MessageValidationError("Grout can't be it");
                        }
                        userService.setCohort(chatId, update.getMessage().getText().toUpperCase());
                        sendMessage(update, "Группа изменена на: "
                                + update.getMessage().getText().toUpperCase());
                        inProgress.remove(chatId);
                        return true; // Обработано, нужно выйти
                    } catch (Exception e) {
                        log.debug("Error setting cohort", e);
                        return true;
                    }
                default:
                    return false;
            }
        }
        return false;
    }

    private void handleCallback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();

        switch (callback) {
            case "setGroup":
                sendMessage(update, "Отправьте мне наименование вашей группы в формате: ИСП-999");
                inProgress.put(chatId, 1);
                break;
            default:
                sendMessage(update, "По какой-то причине кнопка не работает");
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
                sendMessage(update, scheduleService.buildScheduleToNextDay("ИСП-926"));
                break;
            case "schedule_to_day":
                sendMessage(update, scheduleService.buildScheduleToday("ИСП-926"));
                break;
            case "start":
                startCommand(update);
                break;
            default:
                sendMessage(update, "Эта команда не поддерживается");
        }
    }

    private void startCommand(Update update) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsIsLine = new ArrayList<>();
        List<InlineKeyboardButton> rowIsLine = new ArrayList<>();

        InlineKeyboardButton setGroupButton = new InlineKeyboardButton();
        setGroupButton.setText("✏️Группа");
        setGroupButton.setCallbackData("setGroup");
        InlineKeyboardButton setKorpusButton = new InlineKeyboardButton();
        setKorpusButton.setText("✏️Корпус");
        setKorpusButton.setCallbackData("setKorpus");

        rowIsLine.add(setGroupButton);
        rowIsLine.add(setKorpusButton);

        rowsIsLine.add(rowIsLine);

        inlineKeyboardMarkup.setKeyboard(rowsIsLine);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("\"Укажите данные профиля: \\n Группу, Корпус\"");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void validationUpdate(Update update) {
        if (update == null) {
            throw new TelegramUpdateValidationError("It is not massage");
        }
        if (update.hasCallbackQuery()) {
            return;
        }
        if (!update.hasMessage()
                && !(update.getMessage().hasText() || update.getMessage().hasDocument())) {
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
