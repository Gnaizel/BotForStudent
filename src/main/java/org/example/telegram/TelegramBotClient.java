package org.example.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static java.lang.Math.toIntExact;

@Slf4j
@Component
public class TelegramBotClient {

    private final TelegramClient telegramClient;

    public TelegramBotClient(@Value("${telegram.bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    public void editKeyboard(long messageId, long chatId, InlineKeyboardMarkup keyboard) {
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .messageId((int) messageId)
                .chatId(chatId)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(editMarkup);
        } catch (TelegramApiException e) {
            log.warn("Keyboard edit failed messageId: {} chatId: {} reason: {}",
                    messageId, chatId, e.getMessage());
        }
    }

    public void editMessage(String text, long messageId, long chatId) {
        EditMessageText editMessageText = EditMessageText.builder()
                .text(text)
                .messageId(toIntExact(messageId))
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.warn("Message edit filed messageId: {} chatId: {}",
                    messageId,
                    chatId
            );
        }
    }

    public Message sendMessageWithReturn(String text, long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .parseMode("HTML")
                .build();
        try {
            return telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn("Message SendMessage failed: {}", e.getMessage());
            return null;
        }
    }

    public void sendMessage(String message, long chatId) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        sendMessage.enableHtml(true);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn("Message SendMessage failed: {}", e.getMessage());
        }
    }

    public void sendMessage(String message, long chatId, ReplyKeyboardMarkup keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(message)
                .replyMarkup(keyboard)
                .build();
        sendMessage.enableHtml(true);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn("Message SendMessage failed: {}", e.getMessage());
        }
    }

    public Integer getChatMemberCount(long chatId) {
        try {
            return telegramClient.execute(
                    GetChatMemberCount.builder()
                            .chatId(chatId)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.warn("GetChatMemberCount failed chatId: {} reason: {}", chatId, e.getMessage());
            return null;
        }
    }
}
