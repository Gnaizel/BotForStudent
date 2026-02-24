package org.example.telegram.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BotRoles;
import org.example.service.group.GroupService;
import org.example.telegram.TelegramBotClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewChatMemberHandler {
    private final GroupService service;
    private final TelegramBotClient client;

    public void handleMyNewChatMember(ChatMemberUpdated updated) {
        String oldStatus = updated.getOldChatMember().getStatus();
        String newStatus = updated.getNewChatMember().getStatus();
        long chatId = updated.getChat().getId();
        String groupTitle = updated.getChat().getTitle();

        BotRoles botRoles = switch (newStatus) {
            case "administrator" -> BotRoles.ADMIN;
            case "member" -> BotRoles.MEMBER;
            default -> null;
        };

        if ((oldStatus.equals("left") || oldStatus.equals("kicked")) &&
                (newStatus.equals("member") || newStatus.equals("administrator"))) {
            log.info("Bot invite to new Group: [title: {} chatId: {}]", groupTitle, chatId);

            int memberCount = client.getChatMemberCount(chatId);

            service.createGroup(
                chatId,
                groupTitle,
                botRoles,
                memberCount
            );

        } else if (oldStatus.equals("member") && newStatus.equals("administrator")) {
            log.info("Bot promoted to administrator in Group: [title: {} chatId: {}]", groupTitle, chatId);
            service.updateGroupRole(
                    chatId,
                    botRoles
            );
        } else if (oldStatus.equals("administrator") && newStatus.equals("member")) {
            log.info("Bot demoted from administrator in Group: [title: {} chatId: {}]", groupTitle, chatId);
            service.updateGroupRole(
                    chatId,
                    botRoles
            );

        } else if (newStatus.equals("left") || newStatus.equals("kicked")) {
            log.info("Bot removed from Group: [title: {} chatId: {}]", groupTitle, chatId);

            service.deleteGroupByChatId(chatId);
        }
    }
}
