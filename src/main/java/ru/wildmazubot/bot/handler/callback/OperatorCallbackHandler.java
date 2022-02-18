package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class OperatorCallbackHandler {
    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public BotApiMethod<?> handle(CallbackQuery callbackQuery, BotState botState) {
        return null;
    }
}
