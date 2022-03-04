package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
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
public class UserCallbackHandler {

    private final Cache cache;
    private final UserService userService;
    private final MessageService messageService;
    private final ReplyMessageService getReplyText;
    private final KeyboardService keyboardService;

    public UserCallbackHandler(Cache cache,
                               UserService userService,
                               MessageService messageService,
                               ReplyMessageService getReplyText, KeyboardService keyboardService) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.getReplyText = getReplyText;
        this.keyboardService = keyboardService;
    }

    public ReplyPayload handle(BotState botState,
                               long chatId,
                               long userId,
                               Integer messageId,
                               UserDataCache dataCache,
                               String command) {
        ReplyPayload reply = new ReplyPayload();

        // TODO: 24.02.2022 USER_START done
        if (UserCommand.USER_START.getCommand().equals(command)){
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getStartKeyboard(
                                    botState,
                                    dataCache.getSessionToken()),
                            messageService.getTitle(botState)));
        }

        // TODO: 24.02.2022 USER_NEW_CREATE done
        if (UserCommand.USER_NEW_CREATE.getCommand().equals(command)){
            if (BotState.USER_NEW.equals(botState)){
                cache.setUserBotState(userId, messageId, BotState.USER_C_LAST_NAME);
                reply.addPayload(
                        messageService.getDeleteMessage(
                                chatId,
                                messageId));
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.last.title")));
            }
        }

        // TODO: 01.03.2022 USER_HELP done
        if (UserCommand.USER_HELP.getCommand().equals(command)){
            String replyMessage;
            switch (botState) {
                case USER_NEW -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_NEW);
                    replyMessage = getReplyText.getReplyText("reply.new.help");
                }
                case USER_PROCESS -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_PROCESS);
                    replyMessage = getReplyText.getReplyText("reply.process.help");
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_WAIT_KYC);
                    replyMessage = getReplyText.getReplyText("reply.waitkyc.help");
                }
                case USER_WAIT_APPROVE -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_WAIT_APPROVE);
                    replyMessage = getReplyText.getReplyText("reply.waitapprove.help");
                }
                case USER_ACTIVE -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_ACTIVE);
                    replyMessage = getReplyText.getReplyText("reply.active.help");
                }
                default -> {
                    return null;
                }
            }
            return reply.setMessage(
                    messageService.getEditMessageText(
                            chatId,
                            dataCache.getMessageId(),
                            keyboardService.getBackKeyboard(
                                    UserCommand.USER_START.getCommand(),
                                    dataCache.getSessionToken()),
                            replyMessage));
        }

        if (UserCommand.USER_LINK.getCommand().equals(command)) {
            if (BotState.USER_ACTIVE.equals(botState)){
                return reply.setMessage(
                        messageService.getEditMessageText(
                                chatId,
                                dataCache.getMessageId(),
                                keyboardService.getBackKeyboard(
                                        UserCommand.USER_START.getCommand(),
                                        dataCache.getSessionToken()),
                                getReplyText.getReplyText("keyboard.referral", String.valueOf(userId))));
            }
        }

        if (UserCommand.USER_REFERRALS.getCommand().equals(command)){
            if (BotState.USER_ACTIVE.equals(botState)){
                List<User> referrals = userService.getReferralList(userId);
                return reply.setMessage(
                        messageService.getEditMessageText(
                                chatId,
                                dataCache.getMessageId(),
                                keyboardService.getBackKeyboard(
                                        UserCommand.USER_START.getCommand(),
                                        dataCache.getSessionToken()),
                                getReplyText.getReplyText("reply.user.active.referral.title", referralList(referrals))));
            }
        }

        if (UserCommand.USER_BONUSES.getCommand().equals(command)){
            if (botState.equals(BotState.USER_ACTIVE)){
                User user = userService.findById(userId);
                return reply.setMessage(
                        messageService.getEditMessageText(
                                chatId,
                                dataCache.getMessageId(),
                                keyboardService.getBackKeyboard(
                                        UserCommand.USER_START.getCommand(),
                                        dataCache.getSessionToken()),
                                getReplyText.getReplyText("keyboard.user.active.bonus.title", user.getBonus())));

            }
        }

        if (UserCommand.USER_YES.getCommand().equals(command)){
            switch (botState) {
                // TODO: 24.02.2022 USER_NEW_CONFIRM done
                case USER_NEW_CONFIRM -> {
                    if (userService.saveUserInputData(cache.getUserInputData(userId), userId)) {
                        cache.deleteFromCache(userId);
                        reply.addPayload(
                                messageService.getDeleteMessage(
                                        chatId,
                                        dataCache.getMessageId()));
                        reply.addPayload(
                                messageService.getSendMessage(
                                        chatId,
                                        null,
                                        getReplyText.getReplyText("reply.process.help")));
                        reply.addPayload(
                                messageService.getEmailNotification(
                                        userId));
                        return reply.setMessage(
                                messageService.getSendMessage(
                                        chatId,
                                        null,
                                        getReplyText.getReplyText("click.to.start")));
                    }

                    cache.deleteFromCache(userId);
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.user.new.banned")));
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_WAIT_APPROVE);
                    userService.updateStatus(userId, UserStatus.WAIT_APPROVE);
                    reply.addPayload(
                            messageService.getSendMessage(
                                userService.getOperatorId(userId),
                                null,
                                getReplyText.getReplyText("notification.operator.wait_approve", String.valueOf(userId))));
                    return reply.setMessage(
                            messageService.getEditMessageText(
                                    chatId,
                                    dataCache.getMessageId(),
                                    keyboardService.getStartKeyboard(
                                            BotState.USER_WAIT_APPROVE,
                                            dataCache.getSessionToken()),
                                    messageService.getTitle(BotState.USER_WAIT_APPROVE)));
                }
            }
        }

        if (UserCommand.USER_NO.getCommand().equals(command)){
            switch (botState) {
                // TODO: 24.02.2022 USER_NEW_CONFIRM done
                case USER_NEW_CONFIRM -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_C_LAST_NAME);
                    reply.addPayload(
                            messageService.getDeleteMessage(
                                    chatId,
                                    dataCache.getMessageId()));
                    return reply.setMessage(
                            messageService.getSendMessage(
                                    chatId,
                                    null,
                                    getReplyText.getReplyText("reply.create.last.title")));
                }
                case USER_ACTIVE_PAYMENT -> {
                    cache.setUserBotState(userId, messageId, BotState.USER_ACTIVE);
                    return reply.setMessage(
                            messageService.getEditMessageText(
                                    chatId,
                                    dataCache.getMessageId(),
                                    keyboardService.getStartKeyboard(
                                            BotState.USER_ACTIVE,
                                            dataCache.getSessionToken()),
                                    messageService.getTitle(BotState.USER_ACTIVE)));
                }
            }
        }

        return null;
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
