package org.example.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MainKeyboard {
    public static ReplyKeyboardMarkup getKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("\uD83D\uDDD3\uFE0F Сегодня");
        row1.add("\uD83D\uDCC5 На неделю");
        keyboardRows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("\uD83D\uDC64 Профиль");
        keyboardRows.add(row2);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }
}
