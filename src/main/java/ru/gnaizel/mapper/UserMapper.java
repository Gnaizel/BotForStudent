package ru.gnaizel.mapper;

import ru.gnaizel.dto.user.UserCreateDto;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.User;

public class UserMapper {
    public static User userFromUserCreateDto(UserCreateDto userCreateDto) {
        return User.builder()
                .chatId(userCreateDto.getChatId())
                .userName(userCreateDto.getUserName())
                .cohort("no cohort")
                .korpus("no korpus")
                .registrationDate(userCreateDto.getLocalDateTime())
                .build();
    }

    public static UserDto userToDto(User user) {
        return  UserDto.builder()
                .chatId(user.getChatId())
                .userName(user.getUserName())
                .cohort(user.getCohort())
                .korpus(user.getKorpus())
                .build();
    }
}
