package ru.gnaizel.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserCreateDto;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.Cohort;
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
    public boolean checkingForANewUserByMassage(Update update) {
        long chatId = 0;
        Message message = null;
        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = message.getChatId();
        } else if (update.hasCallbackQuery()) {
            return false;
        }

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
            createUser(chatId, userName);
            return true;
        }
        return false;
    }

    @Override
    public boolean setKorpus(long chatId, String korpus) {
        User user = userRepository.findById(chatId)
                .orElseThrow(() -> new TelegramUserByMassagValidationError("User not found"));
        user.setKorpus(korpus);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean setCohort(Long chatId, String cohort) {
        User user = userRepository.findByChatId(chatId)
                .orElseThrow(() -> new TelegramUserByMassagValidationError("User not found"));
        user.setCohort(cohort);
        userRepository.save(user);
        return true;
    }

    @Override
    public User createUser(long chatId, String userName) {
        if (userRepository.existsByChatId(chatId)) {
            throw new UserValidationError("User is exists");
        }

        if (userName.isBlank()) {
            throw new UserValidationError("user name can't be blank");
        }
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .chatId(chatId)
                .userName(userName)
                .localDateTime(LocalDateTime.now())
                .cohort("no cohort")
                .build();
        return userRepository.save(UserMapper.userFromUserCreateDto(userCreateDto));
    }

    @Override
    public UserDto findUserByChatId(long chatId) {
        User user = userRepository.findByChatId(chatId).orElseThrow(() -> new UserValidationError("User not found"));

        return UserMapper.userToDto(user);
    }
}
