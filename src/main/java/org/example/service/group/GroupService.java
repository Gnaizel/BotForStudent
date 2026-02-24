package org.example.service.group;

import org.example.enums.BotRoles;
import org.example.models.Group;

public interface GroupService {
    Group createGroup(long groupId, String groupName, BotRoles role, int usersCount);
    void migrateChatId(long oldChatId, long newChatId);
    Group updateGroupRole(long groupId, BotRoles role);
    boolean deleteGroupByChatId(long groupId);
}
