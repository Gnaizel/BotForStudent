package org.example.telegram.handler;

import org.example.telegram.handler.callback.BotCallBack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CallbackQueryHandler {
    private final Map<String, BotCallBack> callBacks;

    public CallbackQueryHandler(List<BotCallBack> commandList) {
        this.callBacks = commandList.stream()
                .collect(Collectors.toMap(BotCallBack::getCommand, c -> c));
    }

    public void handleCallbackQuery(CallbackQuery call) {
        String dataCommand = call.getData().split("_")[0];
        BotCallBack command = callBacks.get(dataCommand);
        if (command != null) {
            command.execute(call);
        }
    }
}
