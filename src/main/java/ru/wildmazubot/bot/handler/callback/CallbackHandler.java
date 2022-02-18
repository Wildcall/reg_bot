package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.cache.Cache;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class CallbackHandler {

    private final Cache cache;
    private final UserCallbackHandler userCallbackHandler;
    private final OperatorCallbackHandler operatorCallbackHandler;

    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public CallbackHandler(Cache cache,
                           UserCallbackHandler userCallbackHandler,
                           OperatorCallbackHandler operatorCallbackHandler) {
        this.cache = cache;
        this.userCallbackHandler = userCallbackHandler;
        this.operatorCallbackHandler = operatorCallbackHandler;
    }

    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        String username = callbackQuery.getFrom().getUserName();
        BotState botState = cache.getUserBotState(username);

        if (botState.getCode() == -1) {
            if (debug)
                log.info("CallbackHandler --> UserMessageHandler {}",
                        botState.name());
            return null;
        }

        if (botState.getCode() == 0) {
            if (debug)
                log.info("CallbackHandler --> UserCallbackHandler {}",
                        botState.name());
            return userCallbackHandler.handle(callbackQuery, botState);
        }

        // TODO: 18.02.2022 implement
        if (botState.getCode() == 1) {
            if (debug)
                log.info("CallbackHandler --> OperatorCallbackHandler {}",
                        botState.name());
            return operatorCallbackHandler.handle(callbackQuery, botState);
        }

        if (debug)
            log.info("CallbackHandler --> null {}",
                    botState.name());

        return null;
    }
}
