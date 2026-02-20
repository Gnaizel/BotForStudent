package org.example.telegram.handler.message;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface BotMessageCmd {
    void execute(Message message);
    String getMessageCmd();
}
