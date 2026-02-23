package org.example.telegram.handler.formater;

import org.example.models.User;

import java.time.format.DateTimeFormatter;

public class ProfileFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm ");

    public static String format(User user) {

        return "👤 <b>Профиль</b>\n" +
                "───────────────────\n\n" +
                "🏷 <b>Имя:</b> " +
                user.getUsername() +
                "\n" +
                "🎓 <b>Группа:</b> <code>" +
                user.getStudentGroupName() +
                "</code>\n\n" +
                "📅 <b>Дата регистрации:</b> " +
                user.getRegistrationDate()
                        .format(DATE_FORMATTER) +
                "\n";
    }
}