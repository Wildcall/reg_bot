package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.bot.handler.ReceiveMessagePayload;
import ru.wildmazubot.bot.handler.service.NotificationService;
import ru.wildmazubot.bot.handler.service.UserSendMessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

@Slf4j
@Service
public class UserCallbackHandler {

    private final Cache cache;
    private final UserService userService;
    private final UserSendMessageService messageService;
    private final ReplyMessageService getReplyText;
    private final NotificationService notificationService;

    private static final BotState[] state = BotState.getUserState();

    public UserCallbackHandler(Cache cache, UserService userService,
                               UserSendMessageService messageService,
                               ReplyMessageService getReplyText,
                               NotificationService notificationService) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.getReplyText = getReplyText;
        this.notificationService = notificationService;
    }

    public ReceiveMessagePayload handle(CallbackQuery callbackQuery,
                                  BotState botState) {
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        String command = callbackQuery.getData();

        if (UserCommand.USER_START.getCommand().equals(command)){
            return new ReceiveMessagePayload(messageService.getStartMenu(botState, chatId));
        }

        if (UserCommand.USER_NEW_CREATE.getCommand().equals(command)){
            if (BotState.USER_NEW.equals(botState)){
                cache.setUserBotState(userId, state[0]);
                return new ReceiveMessagePayload(
                        messageService.getResponse(
                                chatId, getReplyText.getReplyText("reply.create." + state[0].getTitle() + ".title")));
            }
        }

        if (UserCommand.USER_HELP.getCommand().equals(command)){
            switch (botState) {
                case USER_NEW -> {
                    cache.setUserBotState(userId, BotState.USER_NEW);
                    return new ReceiveMessagePayload(
                            messageService.getBackMenu(
                                    chatId, "reply.new.help"));
                }
                case USER_PROCESS -> {
                    cache.setUserBotState(userId, BotState.USER_PROCESS);
                    return new ReceiveMessagePayload(
                            messageService.getBackMenu(
                                    chatId, "reply.process.help"));
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_KYC);
                    return new ReceiveMessagePayload(
                            messageService.getBackMenu(
                                    chatId, "reply.waitkyc.help"));
                }
                case USER_WAIT_APPROVE -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_APPROVE);
                    return new ReceiveMessagePayload(
                            messageService.getBackMenu(
                                    chatId, "reply.waitapprove.help"));
                }
                case USER_ACTIVE -> {
                    cache.setUserBotState(userId, BotState.USER_ACTIVE);
                    return new ReceiveMessagePayload(
                            messageService.getBackMenu(
                                    chatId, "reply.active.help"));
                }
            }
        }

        if (UserCommand.USER_LINK.getCommand().equals(command)) {
            if (botState.equals(BotState.USER_ACTIVE)){
                return new ReceiveMessagePayload(
                        messageService.getBackMenu(chatId, getReplyText.getReplyText("reply.referral.link", String.valueOf(userId))));
            }
        }

        if (UserCommand.USER_REFERRALS.getCommand().equals(command)){
            if (botState.equals(BotState.USER_ACTIVE)){

                return new ReceiveMessagePayload(
                        messageService.getBackMenu(
                                chatId, userService.getReferralListString(userId)));
            }
        }

        if (UserCommand.USER_BONUSES.getCommand().equals(command)){
            if (botState.equals(BotState.USER_ACTIVE)){
                User user = userService.findById(userId);
                return new ReceiveMessagePayload(
                        messageService.getUserKycMainMenu(chatId, user.getBonus()));
            }
        }

        if (UserCommand.USER_PAYMENT.getCommand().equals(command)){
            if (botState.equals(BotState.USER_ACTIVE)){
                cache.setUserBotState(userId, BotState.USER_ACTIVE_PAYMENT);
                return new ReceiveMessagePayload(
                        messageService.getResponse(chatId, getReplyText.getReplyText("reply.user.active.payment.info")));
            }
        }

        if (UserCommand.USER_YES.getCommand().equals(command)){
            switch (botState) {
                case USER_NEW_CONFIRM -> {
                    if (userService.saveUserInputData(cache.getUserInputData(userId), userId)) {
                        cache.deleteFromCache(userId);
                        cache.setUserBotState(userId, BotState.USER_PROCESS);
                        return new ReceiveMessagePayload(
                                messageService.getStartMenu(BotState.USER_PROCESS, chatId),
                                notificationService.getEmailNotification(userId));
                    }

                    cache.deleteFromCache(userId);
                    return new ReceiveMessagePayload(
                            messageService.getResponse(
                                    chatId, getReplyText.getReplyText("reply.user.new.banned")));
                }
                case USER_WAIT_KYC -> {
                    cache.setUserBotState(userId, BotState.USER_WAIT_APPROVE);
                    userService.updateStatus(userId, UserStatus.WAIT_APPROVE);
                    return new ReceiveMessagePayload(
                            messageService.getUserProcessMainMenu(chatId),
                            notificationService.getMessage(
                                    userService.getOperatorId(userId),
                                    getReplyText.getReplyText("notification.operator.wait_approve", String.valueOf(userId))));
                }
            }
        }

        if (UserCommand.USER_NO.getCommand().equals(command)){
            if (botState.equals(BotState.USER_NEW_CONFIRM)) {
                cache.setUserBotState(userId, state[0]);
                return new ReceiveMessagePayload(
                        messageService.getResponse(
                                chatId, getReplyText.getReplyText("reply.create." + state[0].getTitle() + ".title")));
            }
        }

        return null;
    }
}
