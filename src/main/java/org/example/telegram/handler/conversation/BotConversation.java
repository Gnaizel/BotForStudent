package org.example.telegram.handler.conversation;

import org.example.telegram.handler.enums.UserState;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface BotConversation {
    void execute(Message message);
    UserState getCommand();
}
