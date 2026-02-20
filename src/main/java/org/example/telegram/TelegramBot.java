package org.example.telegram;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.telegram.handler.UpdateDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    @Getter
    private final String botToken;
    @Getter
    private final String botName;

    @Autowired
    private UpdateDispatcher updateDispatcher;

    public TelegramBot(@Value("${telegram.bot.token}") String botToken,
                       @Value("${telegram.bot.name}") String botName) {
        this.botToken = botToken;
        this.botName = botName;
    }

    @Override
    public void consume(Update update) {
        updateDispatcher.dispatch(update);
    }
}
