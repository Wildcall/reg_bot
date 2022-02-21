package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.handler.ReceiveMessagePayload;
import ru.wildmazubot.bot.handler.service.NotificationService;
import ru.wildmazubot.bot.handler.service.OperatorSendMessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

@Slf4j
@Service
public class OperatorCallbackHandler {

    private final Cache cache;
    private final UserService userService;
    private final OperatorSendMessageService messageService;
    private final ReplyMessageService getReplyText;
    private final NotificationService notificationService;

    private static final BotState[] state = BotState.getOperatorState();

    public OperatorCallbackHandler(Cache cache,
                                   UserService userService,
                                   OperatorSendMessageService messageService,
                                   ReplyMessageService getReplyText,
                                   NotificationService notificationService) {
        this.cache = cache;
        this.userService = userService;
        this.messageService = messageService;
        this.getReplyText = getReplyText;
        this.notificationService = notificationService;
    }

    public ReceiveMessagePayload handle(CallbackQuery callbackQuery, BotState botState) {

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        String command = callbackQuery.getData();

        if (OperatorCommand.OPERATOR_START.getCommand().equals(command)) {
            return messageService.handleInputData(chatId, userId, botState, null);
        }

        if (OperatorCommand.OPERATOR_FILL_DATA.getCommand().equals(command)) {
            return new ReceiveMessagePayload(
                    messageService.getBackMenu(
                            chatId, userService.getUsersByStatusString(null, UserStatus.FILL_DATA)));
        }

        if (OperatorCommand.OPERATOR_WAIT_EMAIL.getCommand().equals(command)) {
            return new ReceiveMessagePayload(
                    messageService.getBackMenu(
                            chatId, userService.getUsersByStatusString(userId, UserStatus.WAIT_EMAIL)));
        }

        if (OperatorCommand.OPERATOR_WAIT_CL.getCommand().equals(command)) {
            return new ReceiveMessagePayload(
                    messageService.getBackMenu(
                            chatId, userService.getUsersByStatusString(userId, UserStatus.WAIT_CL)));
        }

        if (OperatorCommand.OPERATOR_WAIT_KYC.getCommand().equals(command)) {
            return new ReceiveMessagePayload(
                    messageService.getBackMenu(
                            chatId, userService.getUsersByStatusString(userId, UserStatus.WAIT_KYC)));
        }

        if (OperatorCommand.OPERATOR_WAIT_APPROVE.getCommand().equals(command)) {
            return new ReceiveMessagePayload(
                    messageService.getBackMenu(
                            chatId, userService.getUsersByStatusString(userId, UserStatus.WAIT_APPROVE)));
        }

        if (OperatorCommand.OPERATOR_LINK.getCommand().equals(command)) {
            if (botState.equals(BotState.OPERATOR_START)){
                return new ReceiveMessagePayload(
                        messageService.getBackMenu(
                                chatId, getReplyText.getReplyText("reply.referral.link", String.valueOf(userId))));
            }
        }

        if (OperatorCommand.OPERATOR_YES.getCommand().equals(command)) {
            if (botState == BotState.OPERATOR_CONFIRM_EMAIL) {
                if (!userService.saveEmail(cache.getUserInputData(userId),userId)){
                    cache.setUserBotState(userId, BotState.OPERATOR_EMAIL);
                    return new ReceiveMessagePayload(
                            messageService.getResponse(
                                    chatId,
                                    getReplyText.getReplyText("reply.create.email.title")),
                            notificationService.getMessage(
                                    chatId,
                                    getReplyText.getReplyText("reply.operator.duplicate.email")));
                }
            }
            if (botState == BotState.OPERATOR_CONFIRM_CL) {
                long currentUserId = Long.parseLong(cache.getUserInputData(userId).get(BotState.OPERATOR_CURRENT_USER));
                userService.updateStatus(currentUserId, UserStatus.WAIT_KYC);
                cache.setUserBotState(userId, BotState.OPERATOR_START);
                return new ReceiveMessagePayload(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.operator.ready.to.kyc")),
                        notificationService.getMessage(
                                currentUserId,
                                getReplyText.getReplyText("notification.user.wait_kyc.ready")));
            }
        }

        if (OperatorCommand.OPERATOR_NO.getCommand().equals(command)) {
            if (botState == BotState.OPERATOR_CONFIRM_EMAIL) {
                cache.setUserBotState(userId, BotState.OPERATOR_EMAIL);
                return new ReceiveMessagePayload(
                        messageService.getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.email.title")));
            }
            if (botState == BotState.OPERATOR_CONFIRM_CL) {
                cache.setUserBotState(userId, BotState.OPERATOR_START);
            }
        }

        cache.setUserBotState(userId, BotState.OPERATOR_START);
        return messageService.handleInputData(chatId, userId, botState, null);
    }
}
