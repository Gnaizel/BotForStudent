package ru.gnaizel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gnaizel.enums.Cohort;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    @NotNull
    private Long chatId;
    @NotBlank
    private String userName;
    @NotNull
    private LocalDateTime localDateTime;
    private String  cohort;
}
