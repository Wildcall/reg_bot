package ru.wildmazubot.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.callback.OperatorCallbackHandler;
import ru.wildmazubot.bot.handler.callback.UserCallbackHandler;
import ru.wildmazubot.bot.handler.message.OperatorMessageHandler;
import ru.wildmazubot.bot.handler.message.UserMessageHandler;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.UserService;

@Slf4j
@Service
public class UpdateHandler {

    private final Cache cache;
    private final UserService userService;
    private final UserMessageHandler userMessageHandler;
    private final OperatorMessageHandler operatorMessageHandler;
    private final UserCallbackHandler userCallbackHandler;
    private final OperatorCallbackHandler operatorCallbackHandler;

    public UpdateHandler(Cache cache,
                         UserService userService,
                         UserMessageHandler userMessageHandler,
                         OperatorMessageHandler operatorMessageHandler,
                         UserCallbackHandler userCallbackHandler,
                         OperatorCallbackHandler operatorCallbackHandler) {

        this.cache = cache;
        this.userService = userService;
        this.userMessageHandler = userMessageHandler;
        this.operatorMessageHandler = operatorMessageHandler;
        this.userCallbackHandler = userCallbackHandler;
        this.operatorCallbackHandler = operatorCallbackHandler;
    }

    public ReplyPayload handle(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            long userId = update.getCallbackQuery().getFrom().getId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            String userData = update.getCallbackQuery().getData();
            BotState botState = auth(userId, username, userData);

            if (!BotState.USER_IGNORED.equals(botState)) {
                return switch (botState.getCode()) {
                    case 0 -> userCallbackHandler.handle(callbackQuery, botState);
                    case 1 -> operatorCallbackHandler.handle(callbackQuery, botState);
                    default -> null;
                };
            }
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();
            String userData = update.getMessage().getText();
            BotState botState = auth(userId, username, userData);

            if (!BotState.USER_IGNORED.equals(botState)) {
                return switch (botState.getCode()) {
                    case 0 -> userMessageHandler.handle(message, botState);
                    case 1 -> operatorMessageHandler.handle(message, botState);
                    default -> null;
                };
            }
        }

        return null;
    }

    private BotState auth(long userId, String username, String text) {
        BotState botState = cache.getUserBotState(userId);

        if (botState != null) {
            return botState;
        }

        User user = userService.findById(userId);

        if (user != null) {
            if (user.getUserRole().equals(UserRole.OPERATOR)){
                cache.setUserBotState(userId, BotState.OPERATOR_START);
                return BotState.OPERATOR_START;
            }
            if (user.getUserRole().equals(UserRole.USER)){
                switch (user.getStatus()) {
                    case NEW -> {
                        cache.setUserBotState(userId, BotState.USER_NEW);
                        return BotState.USER_NEW;
                    }
                    case WAIT_EMAIL, WAIT_CL -> {
                        cache.setUserBotState(userId, BotState.USER_PROCESS);
                        return BotState.USER_PROCESS;
                    }
                    case WAIT_KYC -> {
                        cache.setUserBotState(userId, BotState.USER_WAIT_KYC);
                        return BotState.USER_WAIT_KYC;
                    }
                    case WAIT_APPROVE -> {
                        cache.setUserBotState(userId, BotState.USER_WAIT_APPROVE);
                        return BotState.USER_WAIT_APPROVE;
                    }
                    case ACTIVE -> {
                        cache.setUserBotState(userId, BotState.USER_ACTIVE);
                        return BotState.USER_ACTIVE;
                    }
                    default -> {
                        return BotState.USER_IGNORED;
                    }
                }
            }
        }

        long referrerId = getReferrerId(text);

        if (getReferrerId(text) > 0) {
            if (userId != referrerId) {
                User referrerUser = userService.findById(referrerId);
                if (referrerUser != null && referrerUser.getStatus().equals(UserStatus.ACTIVE)) {
                    userService.createNewUser(userId, username, referrerUser);
                    cache.setUserBotState(userId, BotState.USER_NEW);
                    return BotState.USER_NEW;
                }
            }
        }

        return BotState.USER_IGNORED;
    }

    private long getReferrerId(String text) {
        if (text != null) {
            if (text.startsWith("/start")){
                if (text.contains(" ")) {
                    String[] tmp = text.split(" ");
                    if (tmp.length == 2) {
                        try {
                            return Long.parseLong(tmp[1]);
                        } catch (NumberFormatException e) {
                            return -1;
                        }
                    }
                }
            }
        }
        return -1;
    }
}
