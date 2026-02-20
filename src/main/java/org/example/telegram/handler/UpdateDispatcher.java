package org.example.telegram.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;


@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final MessageHandler messageHandler;
    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackHandler;
    private final DocumentHandler documentHandler;

    public void dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            callbackHandler.handleCallbackQuery(callbackQuery);
            return;
        }

        User tgUser = update.getMessage().getFrom();
        if (tgUser.getIsBot()) {
            return;
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                log.debug("User: [username: {} | tgUserId: {}] Send message to [text: {} | messageId: {}]",
                        (tgUser.getUserName() != null) ? tgUser.getUserName() : tgUser.getFirstName(),
                        tgUser.getId(),
                        message.getText(),
                        message.getMessageId()
                );

                if (message.getText().startsWith("/")) {
                    commandHandler.handleCommand(message);
                } else {
                    messageHandler.handleMessage(message);
                }
            } else if (message.hasDocument()) {
                documentHandler.handleDocument(message);
            }
            return;
        }
        // TODO: Реализовать другие эвенты по типо добавления бота в группу и так далие

    }
}
