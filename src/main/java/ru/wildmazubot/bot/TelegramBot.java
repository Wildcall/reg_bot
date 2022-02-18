package ru.wildmazubot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.wildmazubot.bot.handler.MainHandler;

@Slf4j
@Component
@PropertySource("classpath:telegrambot.properties")
public class TelegramBot extends TelegramWebhookBot {

    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.url}")
    private String botPath;

    private final MainHandler mainHandler;

    public TelegramBot(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return mainHandler.process(update);
    }
}
