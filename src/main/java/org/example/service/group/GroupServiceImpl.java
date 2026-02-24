package org.example.service.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BotRoles;
import org.example.exception.GroupValidationException;
import org.example.models.Group;
import org.example.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository repository;

    @Override
    public Group createGroup(long chatId, String groupName, BotRoles role, int usersCount) {
        Optional<Group> optionalGroup = repository.findByChatId(chatId);
        if (optionalGroup.isPresent()) {
            log.error("Group with id {} already exists", chatId);
            return optionalGroup.get();
        }

        Group newGroup = repository.save(Group.builder()
                .chatId(chatId)
                .tgGroupName(groupName)
                .tgRole(role)
                .usersCount(usersCount)
                .build());
        
        log.info("Group created info: [ chatId: {} | title: {} | memCount: {} | botRole: {} ]",
                newGroup.getChatId(),
                newGroup.getTgGroupName(),
                newGroup.getUsersCount(),
                newGroup.getTgRole().name()
        );
        
        return newGroup;
    }

    @Override
    public Group updateGroupRole(long chatId, BotRoles role) {
        Optional<Group> groupOptional = repository.findByChatId(chatId);
        if (groupOptional.isEmpty()) {
            log.error("Group update failed chatId: {} not exists", chatId);
            throw new GroupValidationException("Group update failed chatId: " + chatId + " not exists");
        }
        Group group = groupOptional.get();
        group.setTgRole(role);
        group = repository.save(group);

        log.info("Bot change role: [chatId: {} title: {} memCount: {} botRole: {} ]",
                group.getChatId(),
                group.getTgGroupName(),
                group.getUsersCount(),
                group.getTgRole().name()
        );
        
        return group;
    }

    @Override
    public boolean deleteGroupByChatId(long chatId) {
        if (repository.findByChatId(chatId).isEmpty()) {
            log.error("Group remove failed chatId: {} not exists", chatId);
            throw new GroupValidationException("Group remove failed chatId: " + chatId + " not exists");
        }

        boolean isGroupDeleted = repository.deleteByChatId(chatId);
        log.info("Group deleted chatId: {}",  chatId);
        
        return isGroupDeleted;
    }

    @Override
    @Transactional
    public void migrateChatId(long oldChatId, long newChatId) {
        repository.findByChatId(oldChatId).ifPresentOrElse(
                group -> {
                    group.setChatId(newChatId);
                    repository.save(group);
                    log.info("Group chatId migrated: {} -> {}", oldChatId, newChatId);
                },
                () -> log.warn("Migration: group with chatId {} not found", oldChatId)
        );
    }
}
