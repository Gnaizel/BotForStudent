package ru.gnaizel.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gnaizel.dto.user.UserCreateDto;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.exception.TelegramUserByMassagValidationError;
import ru.gnaizel.exception.UserValidationError;
import ru.gnaizel.mapper.UserMapper;
import ru.gnaizel.model.User;
import ru.gnaizel.repository.user.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public boolean checkingForANewUserByMassage(Message message) {
        long chatId = message.getChatId();

        if (message.getFrom() == null) {
            throw new TelegramUserByMassagValidationError("User not found");
        }

        String userName = message.getFrom().getUserName();
        if (userName == null || userName.isBlank()) {
            userName = message.getFrom().getFirstName();
            if (userName.isBlank()) {
                userName = message.getFrom().getLastName();
            }
        }

        if (!userRepository.existsByChatId(chatId)) {
            UserCreateDto userCreateDto = UserCreateDto.builder()
                    .chatId(chatId)
                    .userName(userName)
                    .localDateTime(LocalDateTime.now())
                    .build();

            userRepository.save(UserMapper.userFromUserCreateDto(userCreateDto));
            return true;
        }
        return false;
    }

    @Override
    public UserDto findUserByChatId(long chatId) {
        User user = userRepository.findByChatId(chatId).orElseThrow(() -> new UserValidationError("User not found"));

        return UserMapper.userToDto(user);
    }
}
