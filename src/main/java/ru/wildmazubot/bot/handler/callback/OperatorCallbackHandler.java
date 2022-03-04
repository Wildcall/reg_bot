package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.KeyboardService;
import ru.wildmazubot.bot.handler.service.MessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.cache.UserDataCache;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.util.List;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class OperatorCallbackHandler {

    @Value("${user.bonus:1000}")
    private int bonus;
    @Value("${user.ref.bonus:100}")
    private int refBonus;

    private final Cache cache;
    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final UserService userService;
    private final ReplyMessageService getReplyText;

    public OperatorCallbackHandler(Cache cache,
                                   UserService userService,
                                   MessageService messageService,
                                   KeyboardService keyboardService, ReplyMessageService getReplyText) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
    }

    public ReplyPayload handle(BotState botState,
                               long chatId,
                               long userId,
                               Integer messageId,
                               UserDataCache dataCache,
                               String command) {

        ReplyPayload reply = new ReplyPayload();

        if (OperatorCommand.OPERATOR_START.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getStartKeyboard(
                                    botState,
                                    dataCache.getSessionToken()),
                            messageService.getTitle(botState)));
        }

        if (OperatorCommand.OPERATOR_FILL_DATA.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    OperatorCommand.OPERATOR_START.getCommand(),
                                    dataCache.getSessionToken()),
                            getReplyText.getReplyText("reply.operator.filldata.list", userList(userService.getUsersByStatus(null, UserStatus.FILL_DATA)))));
        }

        if (OperatorCommand.OPERATOR_WAIT_EMAIL.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    OperatorCommand.OPERATOR_START.getCommand(),
                                    dataCache.getSessionToken()),
                            getReplyText.getReplyText("reply.operator.waitemail.list", userList(userService.getUsersByStatus(userId, UserStatus.WAIT_EMAIL)))));
        }

        if (OperatorCommand.OPERATOR_WAIT_CL.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    OperatorCommand.OPERATOR_START.getCommand(),
                                    dataCache.getSessionToken()),
                            getReplyText.getReplyText("reply.operator.cl.list", userList(userService.getUsersByStatus(userId, UserStatus.WAIT_CL)))));
        }

        if (OperatorCommand.OPERATOR_WAIT_KYC.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    OperatorCommand.OPERATOR_START.getCommand(),
                                    dataCache.getSessionToken()),
                            getReplyText.getReplyText("reply.operator.waitkyc.list", userList(userService.getUsersByStatus(userId, UserStatus.WAIT_KYC)))));
        }

        if (OperatorCommand.OPERATOR_WAIT_APPROVE.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    OperatorCommand.OPERATOR_START.getCommand(),
                                    dataCache.getSessionToken()),
                            getReplyText.getReplyText("reply.operator.approve.list", userList(userService.getUsersByStatus(userId, UserStatus.WAIT_APPROVE)))));
        }

        if (OperatorCommand.OPERATOR_LINK.getCommand().equals(command)) {
            if (botState.equals(BotState.OPERATOR_START)){
                return reply.setMessage(
                        messageService.getEditMessageText(
                                chatId,
                                dataCache.getMessageId(),
                                keyboardService.getBackKeyboard(
                                        OperatorCommand.OPERATOR_START.getCommand(),
                                        dataCache.getSessionToken()),
                                getReplyText.getReplyText("keyboard.referral", String.valueOf(userId))));
            }
        }

        if (OperatorCommand.OPERATOR_YES.getCommand().equals(command)) {
            switch (botState) {
                case OPERATOR_CONFIRM_EMAIL -> {
                    cache.deleteFromCache(Long.parseLong(dataCache.getInputData().get(BotState.OPERATOR_CURRENT_USER)));
                    if (userService.saveEmail(dataCache.getInputData(), userId)){
                        cache.setUserBotState(userId, -1, BotState.OPERATOR_START);
                        return reply.setMessage(
                                messageService.getEditMessageText(
                                        chatId,
                                        dataCache.getMessageId(),
                                        keyboardService.getStartKeyboard(
                                                BotState.OPERATOR_START,
                                                dataCache.getSessionToken()),
                                        messageService.getTitle(BotState.OPERATOR_START)));
                    }

                    cache.setUserBotState(userId, -1, BotState.OPERATOR_EMAIL);
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    reply.addPayload(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.operator.duplicate.email")));
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.create.email.title")));
                }
                case OPERATOR_CONFIRM_CL -> {
                    long currentUserId = Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER));
                    userService.updateStatus(currentUserId, UserStatus.WAIT_KYC);
                    cache.deleteFromCache(currentUserId);
                    cache.setUserBotState(userId, messageId, BotState.OPERATOR_START);
                    reply.addPayload(
                            messageService.getSendMessage(
                                    currentUserId,
                                    null,
                                    getReplyText.getReplyText("notification.user.wait_kyc.ready")));
                    return reply.setMessage(
                            messageService.getEditMessageText(
                                    chatId,
                                    dataCache.getMessageId(),
                                    keyboardService.getStartKeyboard(
                                            BotState.OPERATOR_START,
                                            dataCache.getSessionToken()),
                                    messageService.getTitle(BotState.OPERATOR_START)));
                }
                case OPERATOR_CONFIRM_APPROVE -> {
                    long currentUserId = Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER));
                    cache.deleteFromCache(currentUserId);
                    userService.approveUser(currentUserId, bonus, refBonus);
                    cache.setUserBotState(userId, messageId, BotState.OPERATOR_START);
                    reply.addPayload(
                            messageService.getSendMessage(
                                    currentUserId,
                                    null,
                                    getReplyText.getReplyText("notification.user.approve.ready")));
                    return reply.setMessage(
                            messageService.getEditMessageText(
                                    chatId,
                                    dataCache.getMessageId(),
                                    keyboardService.getStartKeyboard(
                                            BotState.OPERATOR_START,
                                            dataCache.getSessionToken()),
                                    messageService.getTitle(BotState.OPERATOR_START)));
                }
            }
        }

        if (OperatorCommand.OPERATOR_NO.getCommand().equals(command)) {
            switch (botState) {
                case OPERATOR_CONFIRM_EMAIL -> {
                    cache.setUserBotState(userId, messageId, BotState.OPERATOR_EMAIL);
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.create.email.title")));
                }
                case OPERATOR_CONFIRM_CL, OPERATOR_CONFIRM_APPROVE -> {
                    cache.setUserBotState(userId, messageId, BotState.OPERATOR_START);
                    return reply.setMessage(
                            messageService.getEditMessageText(
                                    chatId,
                                    messageId,
                                    keyboardService.getStartKeyboard(
                                            BotState.OPERATOR_START,
                                            dataCache.getSessionToken()),
                                    messageService.getTitle(BotState.OPERATOR_START)));
                }
            }
        }

        return null;
    }

    private String userList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return getReplyText.getReplyText("reply.empty.list");
        }
        StringBuilder sb = new StringBuilder();
        users.forEach(u -> sb.append("/").append(u.getId()).append("\n"));
        return sb.toString();
    }
}
