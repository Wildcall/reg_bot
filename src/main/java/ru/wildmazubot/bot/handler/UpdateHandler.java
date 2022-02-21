package ru.wildmazubot.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.callback.CallbackHandler;
import ru.wildmazubot.bot.handler.message.MassageHandler;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.UserService;

@Slf4j
@Service
public class UpdateHandler {

    private final CallbackHandler callbackHandler;
    private final MassageHandler messageHandler;
    private final Cache cache;
    private final UserService userService;

    public UpdateHandler(CallbackHandler callbackHandler,
                         MassageHandler messageHandler,
                         Cache cache,
                         UserService userService) {
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
        this.cache = cache;
        this.userService = userService;
    }

    public ReceiveMessagePayload handle(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            long userId = update.getCallbackQuery().getFrom().getId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            String userData = update.getCallbackQuery().getData();

            if (!auth(userId, username, userData).equals(BotState.USER_IGNORED)) {
                return callbackHandler.handle(callbackQuery, auth(userId, username, userData));
            }
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();
            String userData = update.getMessage().getText();

            if (!auth(userId, username, userData).equals(BotState.USER_IGNORED)) {
                return messageHandler.handle(message, auth(userId, username, userData));
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
