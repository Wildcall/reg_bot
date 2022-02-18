package ru.wildmazubot.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.wildmazubot.bot.handler.callback.CallbackHandler;
import ru.wildmazubot.bot.handler.message.MassageHandler;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class MainHandler {

    private final CallbackHandler callbackHandler;
    private final MassageHandler messageHandler;

    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public MainHandler(CallbackHandler callbackHandler,
                       MassageHandler messageHandler) {
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
    }

    public BotApiMethod<?> process(Update update) {

        if (update.hasCallbackQuery()) {
            if (debug)
                log.info("New callback from: {}, with text: {}",
                        update.getCallbackQuery().getFrom().getUserName(),
                        update.getCallbackQuery().getData());
            return callbackHandler.handle(update.getCallbackQuery());
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (debug)
                log.info("New message from: {}, chatId: {}, with text: {}",
                        message.getFrom().getUserName(),
                        message.getChatId(),
                        message.getText());
            return messageHandler.handle(message);
        }

        if (debug)
            log.info("MainHandler --> null");

        return null;
    }
}
