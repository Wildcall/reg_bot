package ru.wildmazubot.bot.handler.message;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;

@Service
public class OperatorMessageHandler {
    public SendMessage handle(Message message, BotState botState) {
        return null;
    }
}
