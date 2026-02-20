package org.example.telegram.handler.message;

import lombok.RequiredArgsConstructor;
import org.example.models.User;
import org.example.service.user.UserService;
import org.example.telegram.TelegramBotClient;
import org.example.telegram.handler.formater.ProfileFormatter;
import org.example.telegram.keyboard.ProfileKeyboard;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class ProfileMessage implements BotMessageCmd {
    private final UserService userService;
    private final TelegramBotClient client;

    @Override
    public void execute(Message message) {
        long chatId = message.getFrom().getId();

        User user = userService.getUserByTgUserId(chatId);
        Message botMessage = client.sendMessageWithReturn(ProfileFormatter.format(user), chatId);

        if (botMessage == null) return;

        client.editKeyboard(
                botMessage.getMessageId(),
                chatId,
                ProfileKeyboard.getKeyboard(botMessage, chatId)  // передаём tgUserId
        );
    }

    @Override
    public String getMessageCmd() {
        return "\uD83D\uDC64 Профиль";
    }
}
