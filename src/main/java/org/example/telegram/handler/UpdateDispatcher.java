package org.example.telegram.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.group.GroupService;
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
    private final NewChatMemberHandler newChatMemberHandler;
    private final GroupService groupService;

    public void dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            callbackHandler.handleCallbackQuery(callbackQuery);
            return;
        }

        if (update.hasMessage()) {
            User tgUser = update.getMessage().getFrom();
            Message message = update.getMessage();

            if (message.getMigrateFromChatId() != null) {
                long oldChatId = message.getMigrateFromChatId();
                long newChatId = message.getChatId();
                log.info("Group migrated: oldChatId: {} -> newChatId: {}", oldChatId, newChatId);
                groupService.migrateChatId(oldChatId, newChatId);
                return;
            }

            if (tgUser.getIsBot()) return;

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
        } else if (update.hasMyChatMember()) {
            newChatMemberHandler.handleMyNewChatMember(update.getMyChatMember());
        }
    }
}
