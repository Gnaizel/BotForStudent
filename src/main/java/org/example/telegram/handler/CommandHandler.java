package org.example.telegram.handler;

import org.example.telegram.handler.command.BotCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommandHandler {
    private final Map<String, BotCommand> commands;

    public CommandHandler(List<BotCommand> commandList) {
        this.commands = commandList.stream()
                .collect(Collectors.toMap(BotCommand::getCommand, c -> c));
    }

    public void handleCommand(Message message) {
        String key = message.getText()
                .replace("/", "")
                .split(" ")[0]
                .toLowerCase();

        BotCommand command = commands.get(key);
        if (command != null) {
            command.execute(message);
        }
    }
}