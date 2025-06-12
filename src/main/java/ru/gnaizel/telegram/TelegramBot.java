package ru.gnaizel.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.service.user.UserService;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final UserService userService;

    @Value("${telegram.bot.token}")
    private String TELEGRAM_BOT_TOKEN;

    @Value("${telegram.bot.username}")
    private String TELEGRAM_BOT_USERNAME;

    @Override
    public void onUpdateReceived(Update update) {
        validationUpdate(update);
        foundUser(update);
    }

    private void foundUser(Update update) {
        Message message = update.getMessage();

        if (userService.checkingForANewUserByMassage(message)) {
            String welcomeMessage = new StringBuilder()
                    .append("Greetings! We're delighted to have you here, ")
                    .append(userService.findUserByChatId(message.getChatId()).getUserName())
                    .append(". Ready to begin our journey together!")
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

    private void validationUpdate(Update update) {
        if (update == null) {
            throw new TelegramUpdateValidationError("It is not massage");
        }
        if (!update.hasMessage() && !(update.getMessage().hasText() || update.getMessage().hasDocument())) {
            throw new TelegramUpdateValidationError("Message is not available");
        }
    }

    @Override
    public String getBotToken() {
        return TELEGRAM_BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return TELEGRAM_BOT_USERNAME;
    }
}
