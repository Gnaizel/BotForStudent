package ru.gnaizel.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.user.UserService;

import java.time.LocalDate;
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

    private final HashMap<Long, String> inProgress = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        try {
            createMenuCommand();
            validationUpdate(update);
            foundUser(update);

            if (checkForProcessAndHandle(update)) {
                return;
            }

            handleUpdate(update);
        } catch (Exception e) {
            log.error("Error processing update", e);
            sendMessage(update, "Произошла ошибка при обработке команды");
        }
    }

    private ReplyKeyboardMarkup createKeyboard(Update update) {
        SendMessage sendMessage = new SendMessage();
        long chatId;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        sendMessage.setChatId(chatId);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow rowInLine1 = new KeyboardRow();
        rowInLine1.add("Расписание");
        rowInLine1.add("Расписание преподавателей");

        keyboardRows.add(rowInLine1);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;

    }

    private void createMenuCommand() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/schedule_to_next_day", "Расписание на завтра"));
        botCommands.add(new BotCommand("/schedule_for_teacher", "Расписания преподавателей"));
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
                    .append(" ! \nэтот бот создан для оптимизации простых действий связаных с учёбой")
                    .append("\n")
                    .append("\nПока он может только прислать вам актуальное расписание. ")
                    .append("\n\n")
                    .toString();
            sendMessage(update, welcomeMessage);

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
            sendMessage.setText("Укажите данные: Группа, Корпус");
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Message sendMessage(Update update, String massage) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(massage);
        sendMessage.setReplyMarkup(createKeyboard(update));
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage()) {
            handleCommand(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        } else {
            log.warn("Received update without message or callback");
        }
    }

    private boolean checkForProcessAndHandle(Update update) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else {
            return false;
        }

        // Если находится в процессе проверяет и тут обработка далее
        if (inProgress.containsKey(chatId)) {
            switch (inProgress.get(chatId)) {
                case "schedule_to_next_day":
                    String sename = update.getMessage().getText().toLowerCase().trim();
                    sename = sename.substring(0, 1).toUpperCase() + sename.substring(1);

                    sendMessage(update ,scheduleService.fetchAndExtractTeachersSchedule(sename));
                    inProgress.remove(chatId);
                    return true;
                case "setGroup": // setCohort() (setGroup)
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
                        userService.setCohort(chatId, update.getMessage().getText()
                                .toUpperCase().replaceAll(" ",""));
                        sendMessage(update, "Группа изменена на: "
                                + update.getMessage().getText().toUpperCase());
                        inProgress.remove(chatId);
                        return true;
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
        UserDto user = userService.findUserByChatId(update.getCallbackQuery().getMessage().getChatId());

        switch (callback) {
            case "setGroup":
                sendMessage(update, "Отправьте мне наименование вашей группы \nВ формате: ГГГ-999");
                inProgress.put(chatId, "setGroup");
                break;
            case "setKorpus":
                try {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Выберете 1 из корпусов ниже\nКорпуса ППК СГТУ");
                    sendMessage.setChatId(chatId);

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    List<InlineKeyboardButton> row3 = new ArrayList<>();

                    InlineKeyboardButton oneKorpusButton = new InlineKeyboardButton();
                    oneKorpusButton.setText("Горького, 9");
                    oneKorpusButton.setCallbackData("oneKorpusButton");

                    InlineKeyboardButton tooKorpusButton = new InlineKeyboardButton();
                    tooKorpusButton.setText("Ильинская площадь, 4");
                    tooKorpusButton.setCallbackData("tooKorpusButton");

                    InlineKeyboardButton threeKorpusButton = new InlineKeyboardButton();
                    threeKorpusButton.setText("Крымская, 19");
                    threeKorpusButton.setCallbackData("threeKorpusButton");

                    InlineKeyboardButton fourKorpusButton = new InlineKeyboardButton();
                    fourKorpusButton.setText("Международная, 24");
                    fourKorpusButton.setCallbackData("fourKorpusButton");

                    InlineKeyboardButton fiveKorpusButton = new InlineKeyboardButton();
                    fiveKorpusButton.setText("Сакко и Ванцетти, 15");
                    fiveKorpusButton.setCallbackData("fiveKorpusButton");

                    InlineKeyboardButton sixKorpusButton = new InlineKeyboardButton();
                    sixKorpusButton.setText("Сакко и Ванцетти (физкультурники)");
                    sixKorpusButton.setCallbackData("sixKorpusButton");

                    row1.add(oneKorpusButton);
                    row1.add(tooKorpusButton);
                    row2.add(threeKorpusButton);
                    row2.add(fourKorpusButton);
                    row3.add(fiveKorpusButton);
                    row3.add(sixKorpusButton);

                    rowsInLine.add(row1);
                    rowsInLine.add(row2);
                    rowsInLine.add(row3);

                    inlineKeyboardMarkup.setKeyboard(rowsInLine);

                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                    execute(sendMessage);
                    inProgress.remove(chatId);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }

                inProgress.put(chatId, "setKorpus");
                break;
            case "oneKorpusButton":
                userService.setKorpus(chatId, "Горького, 9");
                sendMessage(update, "Корпус установлен: Горького, 9");
                log.info("User {} set korpus to: Горького, 9", chatId);
                break;
            case "tooKorpusButton":
                userService.setKorpus(chatId, "Ильинская площадь, 4");
                sendMessage(update, "Корпус установлен: Ильинская площадь, 4");
                log.info("User {} set korpus to: Ильинская площадь, 4", chatId);
                break;
            case "threeKorpusButton":
                userService.setKorpus(chatId, "Крымская, 19");
                sendMessage(update, "Корпус установлен: Крымская, 19");
                log.info("User {} set korpus to: Крымская, 19", chatId);
                break;
            case "fourKorpusButton":
                userService.setKorpus(chatId, "Международная, 24");
                sendMessage(update, "Корпус установлен: Международная, 24");
                log.info("User {} set korpus to: Международная, 24", chatId);
                break;
            case "fiveKorpusButton":
                userService.setKorpus(chatId, "Сакко и Ванцетти, 15");
                sendMessage(update, "Корпус установлен: Сакко и Ванцетти, 15");
                log.info("User {} set korpus to: Сакко и Ванцетти, 15", chatId);
                break;
            case "sixKorpusButton":
                userService.setKorpus(chatId, "Сакко и Ванцетти (физкультурники)");
                sendMessage(update, "Корпус установлен: Сакко и Ванцетти (физкультурники)");
                log.info("User {} set korpus to: Сакко и Ванцетти (физкультурники)", chatId);
                break;
            default:
                sendMessage(update, "По какой-то причине кнопка не работает");
        }
    }


    private void getSchedule(Update update) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        UserDto user = userService.findUserByChatId(chatId);

        if (user.getCohort().equals( "no cohort")) {
            sendMessage(update ,"Заполните начальные данные (группа корпус)");
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(scheduleService.buildScheduleToday(user.getCohort(), user.getKorpus()));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending schedule message", e);
            throw new RuntimeException(e);
        }
    }

    private void editTextMessage(long chatId, int messageId, String editText) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(editText);
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new TelegramUpdateValidationError(e.getMessage());
        }
    }

    private void getScheduleForTeacher(Update update) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        inProgress.put(chatId, "schedule_to_next_day");
        sendMessage(update, "Пришли фамилию преподавателя:");
    }

    private void handleCommand(Update update) {
        String command;

        if (update.getMessage().getText().isBlank()) {
            throw new MessageValidationError("Message can't be blank");
        } else command = update.getMessage().getText();

        if (command.startsWith("/")) {
            command = update.getMessage().getText().toLowerCase().replace("/", "");
        }

        if (command.contains("@")) {
            String[] commandSplit = command.split("@");
            if (!commandSplit[1].equalsIgnoreCase(getBotUsername())) {
                return;
            }
            command = commandSplit[0];
        }
        log.info(command);

        UserDto user = userService.findUserByChatId(update.getMessage().getChatId());
        try {
            switch (command) {
                case "schedule", "Расписание":
                    getSchedule(update);
//                    sendMessage(update, scheduleService.fetchAndExtractTeachersSchedule(user.getCohort(), user.getKorpus()));
                    break;
//
                case "schedule_for_teacher", "Расписание преподавателей":
                    getScheduleForTeacher(update);
                    break;
                case "schedule_to_next_day":
                    sendMessage(update, scheduleService.buildScheduleToNextDay(user.getCohort(), user.getKorpus()));
                    break;
                case "schedule_to_day":
                    sendMessage(update, scheduleService.buildScheduleToday(user.getCohort(), user.getKorpus()));
                    break;
                case "start":
                    startCommand(update);
                    break;
                default:
                    sendMessage(update, "Эта команда не поддерживается");
            }
        } catch (ScheduleValidationError e) {
            sendMessage(update, "Актуального расписания не найдено");
//            throw new ScheduleValidationError(e.getMessage());
        }
    }

    private void startCommand(Update update) {

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
