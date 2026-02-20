package org.example.telegram.handler.conversation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.models.User;
import org.example.service.user.UserService;
import org.example.telegram.TelegramBotClient;
import org.example.telegram.handler.UserStateStorage;
import org.example.telegram.handler.enums.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileGroupEditConv implements BotConversation {
    private final UserService userService;
    private final TelegramBotClient sender;
    private final UserStateStorage stateStorage;

    @Override
    public UserState getCommand() {
        return UserState.WAITING_FOR_EDIT_GROUP;
    }

    @Override
    public void execute(Message message) {
        log.debug("12341234");
        long tgUserId = message.getFrom().getId();
        String text = message.getText().trim().toUpperCase();

        if (!text.matches("[А-Я]{3}-\\d{3}")) {
            sender.sendMessage("Это не похоже на название группы, попробуй ещё раз", message.getChatId());
            return;
        }

        User user = userService.editGroupUser(tgUserId, text);
        stateStorage.clearState(tgUserId);
        sender.sendMessage("Группа изменена на: " + user.getStudentGroupName(), message.getChatId());
    }
}
