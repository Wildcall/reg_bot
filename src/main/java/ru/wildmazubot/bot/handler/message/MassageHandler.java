package ru.wildmazubot.bot.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.cache.Cache;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class MassageHandler {

    private final Cache cache;
    private final UserMessageHandler userMessageHandler;
    private final OperatorMessageHandler operatorMessageHandler;

    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public MassageHandler(Cache cache,
                          UserMessageHandler userMessageHandler,
                          OperatorMessageHandler operatorMessageHandler) {
        this.cache = cache;
        this.userMessageHandler = userMessageHandler;
        this.operatorMessageHandler = operatorMessageHandler;
    }

    public SendMessage handle(Message message) {
        String username = message.getFrom().getUserName();
        BotState botState = cache.getUserBotState(username);

        if (botState.getCode() == -1) {
            if (debug)
                log.info("MassageHandler --> UserMessageHandler {}",
                        botState.name());
            return null;
        }

        if (botState.getCode() == 0) {
            if (debug)
                log.info("MassageHandler --> UserMessageHandler {}",
                        botState.name());
            return userMessageHandler.handle(message, botState);
        }

        // TODO: 18.02.2022 implement 
        if (botState.getCode() == 1) {
            if (debug)
                log.info("MassageHandler --> OperatorMessageHandler {}",
                        botState.name());
            return operatorMessageHandler.handle(message, botState);
        }

        if (debug)
            log.info("MassageHandler --> null {}",
                botState.name());

        return null;
    }
}
