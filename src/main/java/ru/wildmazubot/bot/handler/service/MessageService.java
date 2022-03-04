package ru.wildmazubot.bot.handler.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private final ReplyMessageService getReplyText;
    private final UserService userService;

    public MessageService(ReplyMessageService getReplyText, UserService userService) {
        this.getReplyText = getReplyText;
        this.userService = userService;
    }

    public EditMessageText getEditMessageText(long chatId,
                                              Integer messageId,
                                              ReplyKeyboard replyKeyboard,
                                              String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.enableHtml(true);
        editMessageText.setParseMode(ParseMode.HTML);
        editMessageText.setReplyMarkup((InlineKeyboardMarkup)replyKeyboard);
        editMessageText.setText(text);

        return editMessageText;
    }

    public DeleteMessage getDeleteMessage(long chatId,
                                          Integer messageId) {
        return new DeleteMessage(String.valueOf(chatId), messageId);
    }

    public SendMessage getSendMessage(long chatId,
                                      ReplyKeyboard replyKeyboard,
                                      String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        if (replyKeyboard != null)
            message.setReplyMarkup(replyKeyboard);
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);

        return message;
    }

    public String getTitle(BotState botState) {
        return switch (botState) {
            case USER_NEW                           -> getReplyText.getReplyText("keyboard.user.new.title");
            case USER_PROCESS                       -> getReplyText.getReplyText("keyboard.user.process.title");
            case USER_WAIT_KYC                      -> getReplyText.getReplyText("keyboard.user.waitkyc.title");
            case USER_WAIT_APPROVE                  -> getReplyText.getReplyText("keyboard.user.waitapprove.title");
            case USER_ACTIVE, USER_ACTIVE_PAYMENT   -> getReplyText.getReplyText("keyboard.user.active.title");
            case OPERATOR_START                     -> getReplyText.getReplyText("keyboard.main");
            default -> "";
        };
    }

    public List<BotApiMethod<?>> getEmailNotification(long userId) {
        String text = getReplyText.getReplyText("notification.operator.user.WAIT_EMAIL", String.valueOf(userId));
        List<User> operators = userService.findAllOperators();
        List<BotApiMethod<?>> notification = new ArrayList<>();
        operators.forEach(o -> {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(o.getId()));
            message.setText(text);
            notification.add(message);
        });
        return notification;
    }
}
