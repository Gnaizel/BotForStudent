package ru.gnaizel.service.user;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.User;

public interface UserService {
    boolean checkingForANewUserByMassage(Message message);

    UserDto findUserByChatId(long id);

    User createUser(long chatId, String userName);
}
