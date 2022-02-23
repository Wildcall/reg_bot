package ru.wildmazubot.bot.handler;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReplyPayload {

    private BotApiMethod<?> message;
    private List<BotApiMethod<?>> payload;

    public ReplyPayload() {
        this.payload = new ArrayList<>();
    }

    public ReplyPayload setMessage(BotApiMethod<?> message) {
        this.message = message;
        return this;
    }

    public void addPayload(BotApiMethod<?> message) {
        this.payload.add(message);
    }

    public void addPayload(List<BotApiMethod<?>> messages) {
        this.payload.addAll(messages);
    }
}
