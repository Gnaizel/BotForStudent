package org.example.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotInit implements ApplicationRunner {
    private final TelegramBotsLongPollingApplication botsApplication;
    private final TelegramBot telegramBot;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            botsApplication.registerBot(telegramBot.getBotToken(),  telegramBot);
            log.info("Bot {} started with token: {}",
                    telegramBot.getBotName(),
                    telegramBot.getBotToken());
        } catch (TelegramApiException e) {
            log.error("Bot {} could not be registered: {}", telegramBot.getBotName(), e.getMessage());
            throw new RuntimeException("Failed to register Telegram bot.");
        }
    }
}
