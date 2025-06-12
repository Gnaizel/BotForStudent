package ru.gnaizel.service.user;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gnaizel.dto.user.UserDto;

public interface UserService {
    boolean checkingForANewUserByMassage(Message message);

    UserDto findUserByChatId(long id);
}
