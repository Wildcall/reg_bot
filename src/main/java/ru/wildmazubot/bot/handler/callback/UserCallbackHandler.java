package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.UserSendMessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.util.List;

@Slf4j
@Service
public class UserCallbackHandler {

    private final Cache cache;
    private final UserService userService;
    private final UserSendMessageService messageService;
    private final ReplyMessageService getReplyText;

    public UserCallbackHandler(Cache cache,
                               UserService userService,
                               UserSendMessageService messageService,
                               ReplyMessageService getReplyText) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.getReplyText = getReplyText;
    }

    public ReplyPayload handle(CallbackQuery callbackQuery,
                               BotState botState) {
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        String command = callbackQuery.getData();
        ReplyPayload reply = new ReplyPayload();

        if (UserCommand.USER_START.getCommand().equals(command)){
            return reply.setMessage(
                    messageService.getStartMenu(
                            botState,
                            chatId));
        }

        if (UserCommand.USER_NEW_CREATE.getCommand().equals(command)){
            if (BotState.USER_NEW.equals(botState)){
                cache.setUserBotState(userId, BotState.USER_C_LAST_NAME);
                return reply.setMessage(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.last.title")));
            }
        }

        if (UserCommand.USER_HELP.getCommand().equals(command)){
            switch (botState) {
                case USER_NEW -> {
                    cache.setUserBotState(userId, BotState.USER_NEW);
                    reply.addPayload(messageService.getResponse(
                            chatId,
                            getReplyText.getReplyText("reply.new.help")));
                    return reply.setMessage(
                            messageService.getBackMenu(chatId));
                }
                case USER_PROCESS -> {
                    cache.setUserBotState(userId, BotState.USER_PROCESS);
                    reply.addPayload(messageService.getResponse(
                            chatId,
                            getReplyText.getReplyText("reply.process.help")));
                    return reply.setMessage(
                            messageService.getBackMenu(chatId));
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_KYC);
                    reply.addPayload(messageService.getResponse(
                            chatId,
                            getReplyText.getReplyText("reply.waitkyc.help")));
                    return reply.setMessage(
                            messageService.getBackMenu(chatId));
                }
                case USER_WAIT_APPROVE -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_APPROVE);
                    reply.addPayload(messageService.getResponse(
                            chatId,
                            getReplyText.getReplyText("reply.waitapprove.help")));
                    return reply.setMessage(
                            messageService.getBackMenu(chatId));
                }
                case USER_ACTIVE -> {
                    cache.setUserBotState(userId, BotState.USER_ACTIVE);
                    reply.addPayload(messageService.getResponse(
                            chatId,
                            getReplyText.getReplyText("reply.active.help")));
                    return reply.setMessage(
                            messageService.getBackMenu(chatId));
                }
            }
        }

        if (UserCommand.USER_LINK.getCommand().equals(command)) {
            if (BotState.USER_ACTIVE.equals(botState)){
                reply.addPayload(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("keyboard.referral", String.valueOf(userId))));
                return reply.setMessage(
                        messageService.getBackMenu(chatId));
            }
        }

        if (UserCommand.USER_REFERRALS.getCommand().equals(command)){
            if (BotState.USER_ACTIVE.equals(botState)){
                reply.addPayload(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.user.active.referral.title", referralList(userService.getReferralList(userId)))));
                return reply.setMessage(
                        messageService.getBackMenu(chatId));
            }
        }

        if (UserCommand.USER_BONUSES.getCommand().equals(command)){
            if (botState.equals(BotState.USER_ACTIVE)){
                User user = userService.findById(userId);
                cache.setUserBotState(userId, BotState.USER_ACTIVE_PAYMENT);
                reply.addPayload(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("keyboard.user.active.bonus.count", user.getBonus())));
                return reply.setMessage(
                        messageService.getBonusMenu(chatId));
            }
        }

        if (UserCommand.USER_YES.getCommand().equals(command)){
            switch (botState) {
                case USER_NEW_CONFIRM -> {
                    if (userService.saveUserInputData(cache.getUserInputData(userId), userId)) {
                        cache.deleteFromCache(userId);
                        cache.setUserBotState(userId, BotState.USER_PROCESS);
                        reply.addPayload(
                                messageService.getEmailNotification(userId));
                        return reply.setMessage(
                                messageService.getStartMenu(BotState.USER_PROCESS, chatId));
                    }

                    cache.deleteFromCache(userId);
                    return reply.setMessage(
                            messageService.getResponse(
                                    chatId, getReplyText.getReplyText("reply.user.new.banned")));
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_APPROVE);
                    userService.updateStatus(userId, UserStatus.WAIT_APPROVE);
                    reply.addPayload(messageService.getResponse(
                            userService.getOperatorId(userId),
                            getReplyText.getReplyText("notification.operator.wait_approve", String.valueOf(userId))));
                    return reply.setMessage(
                            messageService.getUserProcessMainMenu(chatId));
                }
                case USER_ACTIVE_PAYMENT -> {

                }
            }
        }

        if (UserCommand.USER_NO.getCommand().equals(command)){
            switch (botState) {
                case USER_NEW_CONFIRM -> {
                    cache.setUserBotState(userId, BotState.USER_C_LAST_NAME);
                    return reply.setMessage(
                            messageService.getResponse(
                                    chatId, getReplyText.getReplyText("reply.create.last.title")));
                }
                case USER_ACTIVE_PAYMENT -> {
                    cache.setUserBotState(userId, BotState.USER_ACTIVE);
                    return reply.setMessage(
                            messageService.getStartMenu(BotState.USER_ACTIVE, chatId));
                }
            }
        }

        return reply.setMessage(
                messageService.getStartMenu(botState, chatId));
    }

    private String referralList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return getReplyText.getReplyText("reply.empty.list");
        }
        StringBuilder sb = new StringBuilder();
        users.forEach(u -> sb.append(u.getUsername()).append("\n"));
        return sb.toString();
    }
}
