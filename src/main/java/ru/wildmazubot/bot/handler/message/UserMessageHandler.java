package ru.wildmazubot.bot.handler.message;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.KeyboardService;
import ru.wildmazubot.bot.handler.service.MessageService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.cache.UserDataCache;
import ru.wildmazubot.service.ReplyMessageService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserMessageHandler {

    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final ReplyMessageService getReplyText;
    private final Cache cache;

    public UserMessageHandler(MessageService messageService,
                              KeyboardService keyboardService,
                              ReplyMessageService getReplyText,
                              Cache cache) {
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
        this.cache = cache;
    }

    public ReplyPayload handle(BotState botState,
                               long chatId,
                               long userId,
                               Integer messageId,
                               UserDataCache dataCache,
                               String text) {
        ReplyPayload reply = new ReplyPayload();

        if (text.equals("/start")) {
            cache.setUserBotState(userId, messageId + 1, botState);
            return reply.setMessage(
                    messageService.getSendMessage(
                            chatId,
                            keyboardService.getStartKeyboard(
                                    botState,
                                    dataCache.getSessionToken()),
                            messageService.getTitle(botState)));
        }

        if (botState.name().startsWith("USER_C_"))
            return handleCreateInput(chatId, userId, messageId, botState, text);

        return reply.setMessage(
                messageService.getSendMessage(
                        chatId,
                        null,
                        getReplyText.getReplyText("click.to.start")));
    }

    private ReplyPayload handleCreateInput(long chatId,
                                           long userId,
                                           Integer messageId,
                                           BotState botState,
                                           String text){
        UserDataCache dataCache = cache.getUserDataCache(userId);
        ReplyPayload reply = new ReplyPayload();

        if (!validate(botState, text)) {
            return reply.setMessage(
                    messageService.getSendMessage(
                            chatId,
                            null,
                            getReplyText.getReplyText("reply.create." + botState.getTitle() + ".error")));

        }
        switch (botState) {
            case USER_C_LAST_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_FIRST_NAME);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.first.title")));
            }
            case USER_C_FIRST_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_MIDDLE_NAME);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.middle.title")));
            }
            case USER_C_MIDDLE_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_BIRTHDAY);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.birthday.title")));
            }
            case USER_C_BIRTHDAY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_COUNTRY);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.country.title")));
            }
            case USER_C_COUNTRY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_REGION);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.region.title")));
            }
            case USER_C_REGION -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_CITY);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.city.title")));
            }
            case USER_C_CITY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_STREET);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.street.title")));
            }
            case USER_C_STREET -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_POSTAL_CODE);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.postcode.title")));
            }
            case USER_C_POSTAL_CODE -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, -1, BotState.USER_C_NUMBER);
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                null,
                                getReplyText.getReplyText("reply.create.number.title")));
            }
            case USER_C_NUMBER -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, messageId + 1, BotState.USER_NEW_CONFIRM);
                StringBuilder sb = new StringBuilder();
                cache.getUserInputData(userId).values().forEach(s -> sb.append(s).append("\n"));
                return reply.setMessage(
                        messageService.getSendMessage(
                                chatId,
                                keyboardService.getConfirmKeyboard(
                                        dataCache.getSessionToken(),
                                        getReplyText.getReplyText("keyboard.user.new.confirm.yes"),
                                        UserCommand.USER_YES.getCommand(),
                                        getReplyText.getReplyText("keyboard.user.new.confirm.no"),
                                        UserCommand.USER_NO.getCommand()),
                                getReplyText.getReplyText("keyboard.user.new.confirm.title", sb.toString())));
            }
        }

        return null;
    }

    private boolean validate(BotState botState, String text) {
        Pattern pattern = Pattern.compile(botState.getPattern());
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
