package ru.wildmazubot.bot.handler.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.handler.ReceiveMessagePayload;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.time.LocalDateTime;

@Service
public class OperatorSendMessageService {

    private final KeyboardService keyboardService;
    private final ReplyMessageService getReplyText;
    private final NotificationService notificationService;
    private final UserService userService;
    private final Cache cache;

    public OperatorSendMessageService(KeyboardService keyboardService,
                                      ReplyMessageService getReplyText,
                                      NotificationService notificationService,
                                      UserService userService,
                                      Cache cache) {
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
        this.notificationService = notificationService;
        this.userService = userService;
        this.cache = cache;
    }

    public SendMessage getResponse(long chatId,
                                   String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        return message;
    }

    public ReceiveMessagePayload handleInputData(long chatId,
                                                 long userId,
                                                 BotState botState,
                                                 String text) {
        if (botState.equals(BotState.OPERATOR_START)) {
            if (text == null){
                return new ReceiveMessagePayload(
                        getOperatorMainMenu(chatId));
            }
            long currentUser = parseLink(text);
            if (currentUser > 0) {
                User operator = userService.findById(userId);
                User user = userService.findById(currentUser);
                if (bindUser(operator, user)){
                    return new ReceiveMessagePayload(
                            getBackMenu(chatId, getReplyText.getReplyText("reply.operator.bind", String.valueOf(user.getId()))));
                }
                if (addEmail(operator, user)){
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    cache.setUserBotState(userId, BotState.OPERATOR_EMAIL);
                    return new ReceiveMessagePayload(
                            getResponse(
                                    chatId,
                                    getReplyText.getReplyText("reply.create.email.title")),
                            notificationService.getMessage(chatId, userService.userToEmailMsg(user.getId())));
                }
                if (regCl(operator, user)){
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    cache.setUserBotState(userId, BotState.OPERATOR_CONFIRM_CL);
                    return new ReceiveMessagePayload(
                            getOperatorConfirmMenu(
                                    chatId, getReplyText.getReplyText("reply.operator.ready.to.cl")),
                            notificationService.getMessage(chatId, userService.userToCoinlist(user.getId())));
                }
            }
        }

        if (botState.equals(BotState.OPERATOR_EMAIL)) {
            cache.addUserInputData(userId, BotState.OPERATOR_EMAIL, text);
            cache.setUserBotState(userId, BotState.OPERATOR_PASSWORD);
            return new ReceiveMessagePayload(
                    getResponse(
                            chatId, getReplyText.getReplyText("reply.create.password.title")));
        }

        if (botState.equals(BotState.OPERATOR_PASSWORD)) {
            cache.addUserInputData(userId, BotState.OPERATOR_PASSWORD, text);
            cache.setUserBotState(userId, BotState.OPERATOR_CONFIRM_EMAIL);

            StringBuilder sb = new StringBuilder();
            cache.getUserInputData(userId).values().forEach(s -> sb.append(s).append("\n"));
            return new ReceiveMessagePayload(getOperatorConfirmMenu(chatId, sb.toString()));
        }

        return new ReceiveMessagePayload(getOperatorMainMenu(chatId));
    }

    public SendMessage getBackMenu(long chatId, String text) {
        return keyboardService.getReply(chatId,
                text,
                KeyboardService.UserKeyboardSize.ONE,
                getReplyText.getReplyText("keyboard.back"),
                OperatorCommand.OPERATOR_START.getCommand());
    }

    private SendMessage getOperatorMainMenu(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.user.active.title"),
                KeyboardService.UserKeyboardSize.SIX,
                getReplyText.getReplyText("keyboard.operator.main.ALL"),
                OperatorCommand.OPERATOR_FILL_DATA.getCommand(),
                getReplyText.getReplyText("keyboard.operator.main.WAIT_EMAIL"),
                OperatorCommand.OPERATOR_WAIT_EMAIL.getCommand(),
                getReplyText.getReplyText("keyboard.operator.main.WAIT_CL"),
                OperatorCommand.OPERATOR_WAIT_CL.getCommand(),
                getReplyText.getReplyText("keyboard.operator.main.WAIT_KYC"),
                OperatorCommand.OPERATOR_WAIT_KYC.getCommand(),
                getReplyText.getReplyText("keyboard.operator.main.WAIT_APPROVE"),
                OperatorCommand.OPERATOR_WAIT_APPROVE.getCommand(),
                getReplyText.getReplyText("keyboard.operator.main.LINK"),
                OperatorCommand.OPERATOR_LINK.getCommand());
    }

    private SendMessage getOperatorConfirmMenu(long chatId,
                                               String ... text) {
        return keyboardService.getReply(chatId,
                getReplyText.getReplyText("keyboard.operator.confirm.title", text[0]),
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.operator.confirm.yes"),
                OperatorCommand.OPERATOR_YES.getCommand(),
                getReplyText.getReplyText("keyboard.operator.confirm.no"),
                OperatorCommand.OPERATOR_NO.getCommand());
    }

    private boolean bindUser(User operator, User user) {
        if (operator != null
                && operator.getUserRole().equals(UserRole.OPERATOR)
                && user != null
                && user.getUserRole().equals(UserRole.USER)
                && user.getStatus().equals(UserStatus.FILL_DATA)) {
            user.setOperator(operator);
            user.setStatus(UserStatus.WAIT_EMAIL);
            user.setStatusTime(LocalDateTime.now());
            operator.getProcessedUsers().add(userService.save(user));
            userService.save(operator);
            return true;
        }
        return false;
    }

    private boolean addEmail(User operator, User user) {
        if (operator != null && operator.getUserRole().equals(UserRole.OPERATOR)) {
            return operator.getProcessedUsers().contains(user) && user.getEmail() == null;
        }
        return false;
    }

    private boolean regCl(User operator, User user) {
        if (operator != null && operator.getUserRole().equals(UserRole.OPERATOR)) {
            return operator.getProcessedUsers().contains(user) && user.getStatus().equals(UserStatus.WAIT_CL);
        }
        return false;
    }

    private long parseLink(String text) {
        if (text.startsWith("/")) {
            try {
                text = text.replaceFirst("/", "");
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
