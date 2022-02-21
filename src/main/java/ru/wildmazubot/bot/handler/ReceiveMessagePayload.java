package ru.wildmazubot.bot.handler;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Data
public class ReceiveMessagePayload {

    private BotApiMethod<?> message;
    private List<SendMessage> payload;

    public ReceiveMessagePayload(BotApiMethod<?> message, List<SendMessage> payload) {
        this.message = message;
        this.payload = payload;
    }

    public ReceiveMessagePayload(BotApiMethod<?> message) {
        this.message = message;
        this.payload = null;
    }

    public void addPayload(SendMessage message) {
        this.payload.add(message);
    }
}
