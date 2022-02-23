package ru.wildmazubot.bot.handler.message;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.UserSendMessageService;

@Service
public class UserMessageHandler {

    private final UserSendMessageService messageService;

    public UserMessageHandler(UserSendMessageService userSendMessageService) {
        this.messageService = userSendMessageService;
    }

    public ReplyPayload handle(Message message, BotState botState) {
        Integer messageId = message.getMessageId();
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        String text = message.getText();

        return messageService.handleInputData(chatId, userId, messageId, botState, text);
    }
}
