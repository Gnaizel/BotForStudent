package org.example.telegram.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.service.user.UserService;
import org.example.telegram.TelegramBotClient;
import org.example.telegram.handler.UserStateStorage;
import org.example.telegram.handler.enums.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class ProfileGroupEditCallback implements BotCallBack {
    private final UserService userService;
    private final TelegramBotClient client;
    private final UserStateStorage stateStorage;

    @Override
    public String getCommand() {
        return "edit-profile";
    }

    @Override
    public void execute(CallbackQuery call) {
        String callbackData = call.getData();
        String[] parts = callbackData.split("_");

        int messageId = Integer.parseInt(parts[2]);
        long chatId = Long.parseLong(parts[4]);
        long tgUserId = Long.parseLong(parts[6]);

        client.editMessage("Введите новую группу: ", messageId, chatId);
        stateStorage.setState(tgUserId, UserState.WAITING_FOR_EDIT_GROUP);
    }
}
