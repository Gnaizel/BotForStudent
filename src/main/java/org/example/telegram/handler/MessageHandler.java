package org.example.telegram.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.telegram.handler.conversation.BotConversation;
import org.example.telegram.handler.enums.UserState;
import org.example.telegram.handler.message.BotMessageCmd;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageHandler {
    private final UserStateStorage stateStorage;
    private final Map<String, BotMessageCmd> commands;
    private final Map<UserState, BotConversation> conversations;

    public MessageHandler(List<BotMessageCmd> cmdList,
                          List<BotConversation> conversationsList,
                          UserStateStorage stateStorage) {
        this.commands = cmdList.stream()
                .collect(Collectors.toMap(BotMessageCmd::getMessageCmd, c -> c));

        this.conversations = conversationsList.stream()
                .collect(Collectors.toMap(BotConversation::getCommand, c -> c));

        this.stateStorage = stateStorage;
    }

    public void handleMessage(Message message) {
        User tgUser = message.getFrom();
        long tgUserId = tgUser.getId();

        UserState state = stateStorage.getState(tgUserId);
        if (state != UserState.NONE) {
            BotConversation conversation = conversations.get(state);
            if (conversation != null) {
                conversation.execute(message);
            }
            return;
        }

        BotMessageCmd cmd = commands.get(message.getText());
        if (cmd != null) {
            cmd.execute(message);
        }
    }
}
