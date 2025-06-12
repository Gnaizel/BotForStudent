package ru.gnaizel.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private Long chatId;
    private String userName;
    private LocalDateTime localDateTime;
}
