package ru.wildmazubot.bot.handler.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final UserService userService;
    private final ReplyMessageService getReplyText;
    private final KeyboardService keyboardService;

    public NotificationService(UserService userService,
                               ReplyMessageService getReplyText, KeyboardService keyboardService) {
        this.userService = userService;
        this.getReplyText = getReplyText;
        this.keyboardService = keyboardService;
    }

    public List<SendMessage> getEmailNotification(long userId) {
        String text = getReplyText.getReplyText("notification.operator.user.WAIT_EMAIL", String.valueOf(userId));
        List<User> operators = userService.findAllOperators();
        List<SendMessage> notification = new ArrayList<>();
        operators.forEach(o -> {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(o.getId()));
            message.setText(text);
            notification.add(message);
        });
        return notification;
    }

    public List<SendMessage> getMessage(long chatId, String text) {
        return new ArrayList<>(){{
            add(new SendMessage(String.valueOf(chatId), text));
        }};
    }

    public List<SendMessage> getUserKycMainMenu(long chatId, String text) {
        return new ArrayList<>(){{
            keyboardService.getReply(
                chatId,
                text,
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.waitkyc.done"),
                UserCommand.USER_YES.getCommand(),
                getReplyText.getReplyText("keyboard.user.waitkyc.help"),
                UserCommand.USER_HELP.getCommand());
        }};
    }
}
