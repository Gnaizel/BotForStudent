package org.example.service.user;

import org.example.models.User;

public interface UserService {
    User getUserByTgUserId(long tgUserId);
    User editGroupUser(long tgUserId,String studentGroupName);
    void createUser(String username, Long tgUserId, String studentGroupName);
    boolean existUserByTgUserId(Long tgUserId);
}
