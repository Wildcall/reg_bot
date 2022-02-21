package ru.wildmazubot.bot.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.ReceiveMessagePayload;

@Slf4j
@Service
public class MassageHandler {

    private final UserMessageHandler userMessageHandler;
    private final OperatorMessageHandler operatorMessageHandler;


    public MassageHandler(UserMessageHandler userMessageHandler,
                          OperatorMessageHandler operatorMessageHandler) {
        this.userMessageHandler = userMessageHandler;
        this.operatorMessageHandler = operatorMessageHandler;
    }

    public ReceiveMessagePayload handle(Message message, BotState botState) {

        return switch (botState.getCode()) {
            case 0 -> userMessageHandler.handle(message, botState);
            case 1 -> operatorMessageHandler.handle(message, botState);
            default -> null;
        };
    }
}

