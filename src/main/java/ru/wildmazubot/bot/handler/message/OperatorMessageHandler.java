package ru.wildmazubot.bot.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.KeyboardService;
import ru.wildmazubot.bot.handler.service.MessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.cache.UserDataCache;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OperatorMessageHandler {

    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final ReplyMessageService getReplyText;
    private final UserService userService;
    private final Cache cache;

    public OperatorMessageHandler(MessageService messageService,
                                  KeyboardService keyboardService,
                                  ReplyMessageService getReplyText,
                                  UserService userService, Cache cache) {
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
        this.userService = userService;
        this.cache = cache;
    }

    public ReplyPayload handle(BotState botState,
                               long chatId,
                               long userId,
                               Integer messageId,
                               UserDataCache dataCache,
                               String text) {
        ReplyPayload reply = new ReplyPayload();

        if (text != null && text.equals("/start")) {
            cache.setUserBotState(userId, messageId + 1, botState);
            return reply.setMessage(
                    messageService.getSendMessage(
                            chatId,
                            keyboardService.getStartKeyboard(
                                    botState,
                                    dataCache.getSessionToken()),
                            messageService.getTitle(botState)));
        }

        if (botState.equals(BotState.OPERATOR_START) && text != null) {
            long currentUser = parseLink(text);
            if (currentUser > 0) {
                User operator = userService.findById(userId);
                User user = userService.findById(currentUser);

                if (bindUser(operator, user)) {
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    cache.setUserBotState(userId, messageId + 1, BotState.OPERATOR_START);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    keyboardService.getBackKeyboard(
                                            OperatorCommand.OPERATOR_START.getCommand(),
                                            dataCache.getSessionToken()),
                                    getReplyText.getReplyText("reply.operator.bind", String.valueOf(user.getId()))));
                }

                if (addEmail(operator, user)) {
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    reply.addPayload(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.operator.email.info", dataForEmailRegistration(userService.findById(user.getId())))));
                    cache.setUserBotState(userId, messageId, BotState.OPERATOR_EMAIL);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.create.email.title")));
                }

                if (regCl(operator, user)) {
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    reply.addPayload(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.operator.cl.info", dataForCoinListRegistration(userService.findById(user.getId())))));
                    cache.setUserBotState(userId, messageId + 1, BotState.OPERATOR_CONFIRM_CL);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    keyboardService.getConfirmKeyboard(
                                            dataCache.getSessionToken(),
                                            getReplyText.getReplyText("keyboard.operator.confirm.yes"),
                                            OperatorCommand.OPERATOR_YES.getCommand(),
                                            getReplyText.getReplyText("keyboard.operator.confirm.no"),
                                            OperatorCommand.OPERATOR_NO.getCommand()),
                                    getReplyText.getReplyText("reply.operator.ready.to.cl")));
                }

                if (waitKYC(operator, user)){
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    cache.setUserBotState(userId, messageId + 1, BotState.OPERATOR_START);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    keyboardService.getBackKeyboard(
                                            OperatorCommand.OPERATOR_START.getCommand(),
                                            dataCache.getSessionToken()),
                                    getReplyText.getReplyText("reply.operator.waitkyc.user", dataWaitingKYCUser(userService.findById(user.getId())))));
                }

                if (approve(operator, user)) {
                    cache.addUserInputData(userId, BotState.OPERATOR_CURRENT_USER, String.valueOf(user.getId()));
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    cache.setUserBotState(userId, messageId + 1, BotState.OPERATOR_CONFIRM_APPROVE);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    keyboardService.getConfirmKeyboard(
                                            dataCache.getSessionToken(),
                                            getReplyText.getReplyText("keyboard.operator.confirm.yes"),
                                            OperatorCommand.OPERATOR_YES.getCommand(),
                                            getReplyText.getReplyText("keyboard.operator.confirm.no"),
                                            OperatorCommand.OPERATOR_NO.getCommand()),
                                    getReplyText.getReplyText("reply.operator.approve.info", dataForApprove(userService.findById(user.getId())))));
                }
            }
        }

        if (botState.equals(BotState.OPERATOR_EMAIL)) {
            cache.addUserInputData(userId, BotState.OPERATOR_EMAIL, text);
            cache.setUserBotState(userId, messageId, BotState.OPERATOR_PASSWORD);
            return reply.setMessage(
                    messageService.getSendMessage(
                            chatId,
                            null,
                            getReplyText.getReplyText("reply.create.password.title")));
        }

        if (botState.equals(BotState.OPERATOR_PASSWORD)) {
            cache.addUserInputData(userId, BotState.OPERATOR_PASSWORD, text);
            cache.setUserBotState(userId, messageId + 1, BotState.OPERATOR_CONFIRM_EMAIL);

            StringBuilder sb = new StringBuilder();
            cache.getUserInputData(userId).values().forEach(s -> sb.append(s).append("\n"));
            return reply.setMessage(
                    messageService.getSendMessage(
                            chatId,
                            keyboardService.getConfirmKeyboard(
                                    dataCache.getSessionToken(),
                                    getReplyText.getReplyText("keyboard.operator.confirm.yes"),
                                    OperatorCommand.OPERATOR_YES.getCommand(),
                                    getReplyText.getReplyText("keyboard.operator.confirm.no"),
                                    OperatorCommand.OPERATOR_NO.getCommand()),
                            getReplyText.getReplyText("keyboard.operator.confirm.title", sb.toString())));
        }

        return reply.setMessage(
                messageService.getSendMessage(
                        chatId,
                        null,
                        getReplyText.getReplyText("click.to.start")));
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

    private boolean waitKYC(User operator, User user) {
        if (operator != null && operator.getUserRole().equals(UserRole.OPERATOR)) {
            return operator.getProcessedUsers().contains(user) && user.getStatus().equals(UserStatus.WAIT_KYC);
        }
        return false;
    }

    private boolean approve(User operator, User user) {
        if (operator != null && operator.getUserRole().equals(UserRole.OPERATOR)) {
            return operator.getProcessedUsers().contains(user) && user.getStatus().equals(UserStatus.WAIT_APPROVE);
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

    private String dataForEmailRegistration(User user) {
        if (user != null){
            return user.getPassport().getLastName() + "\n" +
                    user.getPassport().getFirstName() + "\n" +
                    user.getPassport().getMiddleName() + "\n" +
                    user.getPassport().getBirthDay() + "\n";
        }
        return null;
    }

    private String dataForCoinListRegistration(User user) {
        if (user != null){
            return user.getPassport().getLastName() + "\n" +
                    user.getPassport().getFirstName() + "\n" +
                    user.getPassport().getMiddleName() + "\n" +
                    user.getPassport().getBirthDay() + "\n" +
                    user.getAddress().getCountry() + "\n" +
                    user.getAddress().getRegion() + "\n" +
                    user.getAddress().getCity() + "\n" +
                    user.getAddress().getStreet() + "\n" +
                    user.getAddress().getPostalCode() + "\n" +
                    user.getPhone().getNumber() + "\n" +
                    user.getEmail().getEmail() + "\n" +
                    user.getEmail().getPassword() + "\n";
        }
        return null;
    }

    private String dataWaitingKYCUser(User user) {
        if (user != null){
            return user.getEmail().getEmail() + "\n" +
                    user.getStatusTime();
        }
        return null;
    }

    private String dataForApprove(User user) {
        if (user != null){
            return user.getEmail().getEmail() + "\n";
        }
        return null;
    }
}
