package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.ReceiveMessagePayload;
import ru.wildmazubot.cache.Cache;

@Slf4j
@Service
public class CallbackHandler {

    private final UserCallbackHandler userCallbackHandler;
    private final OperatorCallbackHandler operatorCallbackHandler;

    public CallbackHandler(UserCallbackHandler userCallbackHandler,
                           OperatorCallbackHandler operatorCallbackHandler) {
        this.userCallbackHandler = userCallbackHandler;
        this.operatorCallbackHandler = operatorCallbackHandler;
    }

    public ReceiveMessagePayload handle(CallbackQuery callbackQuery, BotState botState) {

        return switch (botState.getCode()) {
            case 0 -> userCallbackHandler.handle(callbackQuery, botState);
            case 1 -> operatorCallbackHandler.handle(callbackQuery, botState);
            default -> null;
        };
    }
}
