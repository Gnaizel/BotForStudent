package ru.gnaizel.dto.user;

import lombok.Builder;
import lombok.Data;
import ru.gnaizel.enums.Cohort;

@Data
@Builder
public class UserDto {
    private Long chatId;
    private String userName;
    private Cohort cohort;
}
