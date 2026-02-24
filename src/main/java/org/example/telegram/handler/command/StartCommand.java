package org.example.telegram.handler.command;

import lombok.RequiredArgsConstructor;
import org.example.service.user.UserService;
import org.example.telegram.TelegramBotClient;
import org.example.telegram.handler.UserStateStorage;
import org.example.telegram.handler.enums.UserState;
import org.example.telegram.keyboard.MainKeyboard;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class StartCommand implements BotCommand {
    private final UserService userService;
    private final TelegramBotClient client;
    private final UserStateStorage stateStorage;

    @Override
    public String getCommand() { return "start"; }

    @Override
    public void execute(Message message) {
        User tgUser = message.getFrom();

        if (userService.existUserByTgUserId(tgUser.getId())) {
            if (message.getChat().getId() > 0) {
                client.sendMessage(
                        "Клавивтура обновленна",
                        tgUser.getId(),
                        MainKeyboard.getKeyboard());
            }
        } else {
            stateStorage.setState(tgUser.getId(), UserState.WAITING_FOR_GROUP);
            client.sendMessage("Привет! Напиши свою группу: ", tgUser.getId());
        }
    }
}
