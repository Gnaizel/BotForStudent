package org.example.telegram.handler.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface BotCallBack {
    void execute(CallbackQuery call);
    String getCommand();
}
