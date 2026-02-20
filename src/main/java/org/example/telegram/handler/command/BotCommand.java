package org.example.telegram.handler.command;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface BotCommand {
    void execute(Message message);
    String getCommand();
}
