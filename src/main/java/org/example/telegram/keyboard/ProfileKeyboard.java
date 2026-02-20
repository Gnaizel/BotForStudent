package org.example.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

public class ProfileKeyboard {
    public static InlineKeyboardMarkup getKeyboard(Message botMessage, long tgUserId) {
        InlineKeyboardButton profileBtn = InlineKeyboardButton.builder()
                .text("Редактировать ✏\uFE0F")
                .callbackData("edit-profile_message_"
                        + botMessage.getMessageId() + "_"
                        + "chat_"
                        + botMessage.getChatId() + "_"
                        + "profile_"
                        + tgUserId)  // <- передаём явно
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(profileBtn)))
                .build();
    }
}
