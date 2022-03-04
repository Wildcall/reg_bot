package ru.wildmazubot.bot.handler;

import liquibase.pro.packaged.S;
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
import ru.wildmazubot.cache.UserDataCache;
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
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            String userData = callbackQuery.getData();
            String username = update.getCallbackQuery().getFrom().getUserName();

            BotState botState = auth(userId, messageId, username, userData);

            UserDataCache dataCache = cache.getUserDataCache(userId);
            String command = userData.replaceFirst(String.valueOf(dataCache.getSessionToken()), "");

            if (!BotState.USER_IGNORED.equals(botState)) {
                return switch (botState.getCode()) {
                    case 0 -> userCallbackHandler.handle(botState, chatId, userId, messageId, dataCache, command);
                    case 1 -> operatorCallbackHandler.handle(botState, chatId, userId, messageId, dataCache, command);
                    default -> null;
                };
            }
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            long chatId = message.getChatId();
            long userId = update.getMessage().getFrom().getId();
            Integer messageId = message.getMessageId();
            String text = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            BotState botState = auth(userId, messageId, username, text);

            UserDataCache dataCache = cache.getUserDataCache(userId);

            if (!BotState.USER_IGNORED.equals(botState)) {
                return switch (botState.getCode()) {
                    case 0 -> userMessageHandler.handle(botState, chatId, userId, messageId, dataCache, text);
                    case 1 -> operatorMessageHandler.handle(botState, chatId, userId, messageId, dataCache, text);
                    default -> null;
                };
            }
        }

        return null;
    }

    private BotState auth(long userId, Integer messageId, String username, String text) {

        if (text.startsWith("/start")) {
            cache.deleteFromCache(userId);
        } else {
            BotState botState = cache.getUserBotState(userId);

            if (botState != null) {
                return botState;
            }
        }

        User user = userService.findById(userId);

        if (user != null) {
            if (user.getUserRole().equals(UserRole.OPERATOR)){
                cache.setUserBotState(userId, messageId, BotState.OPERATOR_START);
                return BotState.OPERATOR_START;
            }
            if (user.getUserRole().equals(UserRole.USER)){
                switch (user.getStatus()) {
                    case NEW -> {
                        cache.setUserBotState(userId, messageId, BotState.USER_NEW);
                        return BotState.USER_NEW;
                    }
                    case WAIT_EMAIL, WAIT_CL, FILL_DATA -> {
                        cache.setUserBotState(userId, messageId, BotState.USER_PROCESS);
                        return BotState.USER_PROCESS;
                    }
                    case WAIT_KYC -> {
                        cache.setUserBotState(userId, messageId, BotState.USER_WAIT_KYC);
                        return BotState.USER_WAIT_KYC;
                    }
                    case WAIT_APPROVE -> {
                        cache.setUserBotState(userId, messageId, BotState.USER_WAIT_APPROVE);
                        return BotState.USER_WAIT_APPROVE;
                    }
                    case ACTIVE -> {
                        cache.setUserBotState(userId, messageId, BotState.USER_ACTIVE);
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
                    cache.setUserBotState(userId, messageId, BotState.USER_NEW);
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
