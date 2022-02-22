package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.OperatorSendMessageService;
import ru.wildmazubot.cache.Cache;
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
    private final UserService userService;
    private final OperatorSendMessageService messageService;
    private final ReplyMessageService getReplyText;

    public OperatorCallbackHandler(Cache cache,
                                   UserService userService,
                                   OperatorSendMessageService messageService,
                                   ReplyMessageService getReplyText) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.getReplyText = getReplyText;
    }

    public ReplyPayload handle(CallbackQuery callbackQuery, BotState botState) {

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        String command = callbackQuery.getData();
        ReplyPayload reply = new ReplyPayload();

        if (OperatorCommand.OPERATOR_START.getCommand().equals(command)) {
            return reply.setMessage(messageService.getOperatorMainMenu(chatId));
        }

        if (OperatorCommand.OPERATOR_FILL_DATA.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getBackMenu(
                            chatId,
                            userList(userService.getUsersByStatus(null, UserStatus.FILL_DATA))));
        }

        if (OperatorCommand.OPERATOR_WAIT_EMAIL.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getBackMenu(
                            chatId,
                            userList(userService.getUsersByStatus(userId, UserStatus.WAIT_EMAIL))));
        }

        if (OperatorCommand.OPERATOR_WAIT_CL.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getBackMenu(
                            chatId,
                            userList(userService.getUsersByStatus(userId, UserStatus.WAIT_CL))));
        }

        if (OperatorCommand.OPERATOR_WAIT_KYC.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getBackMenu(
                            chatId,
                            userList(userService.getUsersByStatus(userId, UserStatus.WAIT_KYC))));
        }

        if (OperatorCommand.OPERATOR_WAIT_APPROVE.getCommand().equals(command)) {
            return reply.setMessage(
                    messageService.getBackMenu(
                            chatId,
                            userList(userService.getUsersByStatus(userId, UserStatus.WAIT_APPROVE))));
        }

        if (OperatorCommand.OPERATOR_LINK.getCommand().equals(command)) {
            if (botState.equals(BotState.OPERATOR_START)){
                return reply.setMessage(
                        messageService.getBackMenu(
                                chatId, getReplyText.getReplyText("reply.referral.link", String.valueOf(userId))));
            }
        }

        if (OperatorCommand.OPERATOR_YES.getCommand().equals(command)) {
            if (botState == BotState.OPERATOR_CONFIRM_EMAIL) {
                cache.deleteFromCache(Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER)));
                if (!userService.saveEmail(cache.getUserInputData(userId),userId)){
                    cache.setUserBotState(userId, BotState.OPERATOR_EMAIL);
                    reply.addPayload(
                            messageService.getResponse(
                                    chatId,
                                    getReplyText.getReplyText("reply.operator.duplicate.email")));
                    return reply.setMessage(
                            messageService.getResponse(
                                    chatId,
                                    getReplyText.getReplyText("reply.create.email.title")));
                }
            }
            if (botState == BotState.OPERATOR_CONFIRM_CL) {
                long currentUserId = Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER));
                userService.updateStatus(currentUserId, UserStatus.WAIT_KYC);
                cache.deleteFromCache(currentUserId);
                cache.setUserBotState(userId, BotState.OPERATOR_START);
                reply.addPayload(messageService.getResponse(
                        currentUserId,
                        getReplyText.getReplyText("notification.user.wait_kyc.ready")));
                return reply.setMessage(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.operator.ready.to.kyc")));
            }
            if (botState == BotState.OPERATOR_CONFIRM_APPROVE) {
                long currentUserId = Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER));
                cache.deleteFromCache(currentUserId);
                userService.approveUser(currentUserId, bonus, refBonus);
            }
        }

        if (OperatorCommand.OPERATOR_NO.getCommand().equals(command)) {
            if (botState == BotState.OPERATOR_CONFIRM_EMAIL) {
                cache.setUserBotState(userId, BotState.OPERATOR_EMAIL);
                return reply.setMessage(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.email.title")));
            }
        }

        cache.setUserBotState(userId, BotState.OPERATOR_START);
        return messageService.handleInputData(chatId, userId, botState, null);
    }

    private String userList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return getReplyText.getReplyText("reply.empty.list");
        }
        StringBuilder sb = new StringBuilder();
        users.forEach(u -> sb.append(u.getUsername()).append("\n"));
        return sb.toString();
    }
}
