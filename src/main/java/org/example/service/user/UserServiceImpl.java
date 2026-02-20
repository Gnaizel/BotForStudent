package org.example.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.UserExistException;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserByTgUserId(long tgUserId) {
        Optional<User> user = userRepository.findByTgUserId(tgUserId);
        if (user.isEmpty()) {
            log.error("User with Telegram-id {} not found Class: {}", tgUserId, this.getClass().getSimpleName());
            throw new UserExistException("User with Telegram-id " + tgUserId + " does not exist");
        }

        return user.get();
    }

    @Override
    public void createUser(String username, Long tgUserId, String studentGroupName) {
        if (userRepository.findByTgUserId(tgUserId).isPresent()) {
            log.error("User with id {} already exists", tgUserId);
            throw new UserExistException(
                    String.format("User with id %d already exists", tgUserId));
        }

        User newUser = User.builder()
                .username(username)
                .tgUserId(tgUserId)
                .studentGroupName(studentGroupName)
                .registrationDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User with id {} tgUserId: {} username: {} created",
                savedUser.getId(),
                tgUserId,
                username);

    }

    @Override
    public User editGroupUser(long tgUserId ,String studentGroupName) {
        User user = getUserByTgUserId(tgUserId);
        user.setStudentGroupName(studentGroupName);
        User updateUser = userRepository.save(user);

        log.debug("User with id {} edit group name: {}",
                updateUser.getId(),
                updateUser.getStudentGroupName());
        return updateUser;
    }

    @Override
    public boolean existUserByTgUserId(Long tgUserId) {
        return userRepository.findByTgUserId(tgUserId).isPresent();
    }
}
