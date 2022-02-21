package ru.wildmazubot.bot.handler.service;

import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.service.ReplyMessageService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserSendMessageService {

    private final KeyboardService keyboardService;
    private final ReplyMessageService getReplyText;
    private final Cache cache;
    private static final BotState[] state = BotState.getUserState();

    public UserSendMessageService(KeyboardService keyboardService,
                                  ReplyMessageService getReplyText,
                                  Cache cache) {
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
        this.cache = cache;
    }

    public SendMessage getResponse(long chatId,
                                   String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        return message;
    }

    public SendMessage handleInputData(long chatId,
                                       long userId,
                                       BotState botState,
                                       String text) {
        int index = contains(botState, state);

        if (index != -1) {

            if (!validate(botState, text)) {
                return getResponse(
                        chatId,
                        getReplyText.getReplyText("reply.create." + botState.getTitle() + ".error"));
            }

            cache.addUserInputData(userId, BotState.getUserState()[index], text);

            if (index == state.length - 1) {
                cache.setUserBotState(userId, BotState.USER_NEW_CONFIRM);

                StringBuilder sb = new StringBuilder();
                cache.getUserInputData(userId).values().forEach(s -> sb.append(s).append("\n"));
                return getUserNewConfirmMenu(chatId, sb.toString());
            }

            cache.addUserInputData(userId, BotState.getUserState()[index], text);
            cache.setUserBotState(userId, BotState.getUserState()[index + 1]);
            return getResponse(chatId,
                    getReplyText.getReplyText("reply.create."
                            + state[index + 1].getTitle()
                            + ".title"));
        }

        return null;
    }

    public SendMessage getStartMenu(BotState botState, long chatId) {
        return switch (botState) {
            case USER_NEW               -> getUserNewMainMenu(chatId);
            case USER_PROCESS,
                    USER_WAIT_APPROVE   -> getUserProcessMainMenu(chatId);
            case USER_WAIT_KYC          -> getUserKycMainMenu(chatId);
            case USER_ACTIVE            -> getUserActiveMainReply(chatId);
            default -> null;
        };
    }

    public SendMessage getUserNewMainMenu(long chatId){
        return keyboardService.getReply(chatId,
                getReplyText.getReplyText("keyboard.user.new.title"),
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.new.create"),
                UserCommand.USER_NEW_CREATE.getCommand(),
                getReplyText.getReplyText("keyboard.user.new.help"),
                UserCommand.USER_HELP.getCommand());
    }

    public SendMessage getUserProcessMainMenu(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.user.process.title"),
                KeyboardService.UserKeyboardSize.ONE,
                getReplyText.getReplyText("keyboard.user.process.help"),
                UserCommand.USER_HELP.getCommand());
    }

    public SendMessage getUserActiveMainReply(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.user.active.title"),
                KeyboardService.UserKeyboardSize.FOUR,
                getReplyText.getReplyText("keyboard.user.active.link"),
                UserCommand.USER_LINK.getCommand(),
                getReplyText.getReplyText("keyboard.user.active.referrals"),
                UserCommand.USER_REFERRALS.getCommand(),
                getReplyText.getReplyText("keyboard.user.active.bonuses"),
                UserCommand.USER_BONUSES.getCommand(),
                getReplyText.getReplyText("keyboard.user.active.help"),
                UserCommand.USER_HELP.getCommand());
    }

    public SendMessage getUserNewConfirmMenu(long chatId, String ... text) {
        return keyboardService.getReply(chatId,
                getReplyText.getReplyText("keyboard.user.new.confirm.title", text.length == 1 ? text[0] : null),
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.new.confirm.yes"),
                UserCommand.USER_YES.getCommand(),
                getReplyText.getReplyText("keyboard.user.new.confirm.no"),
                UserCommand.USER_NO.getCommand());

    }

    public SendMessage getUserKycMainMenu(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.user.waitkyc.title"),
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.waitkyc.done"),
                UserCommand.USER_YES.getCommand(),
                getReplyText.getReplyText("keyboard.user.waitkyc.help"),
                UserCommand.USER_HELP.getCommand());
    }

    public SendMessage getBackMenu(long chatId, String text) {
        try{
            return keyboardService.getReply(chatId,
                    getReplyText.getReplyText(text),
                    KeyboardService.UserKeyboardSize.ONE,
                    getReplyText.getReplyText("keyboard.back"),
                    UserCommand.USER_START.getCommand());
        } catch (NoSuchMessageException e) {
            return keyboardService.getReply(chatId,
                    text,
                    KeyboardService.UserKeyboardSize.ONE,
                    getReplyText.getReplyText("keyboard.back"),
                    UserCommand.USER_START.getCommand());
        }
    }

    private static int contains(final BotState v, final BotState[] states) {
        int result = -1;
        for (int i = 0; i < states.length; i++) {
            if(states[i].equals(v)){
                result = i;
                break;
            }
        }
        return result;
    }

    private static boolean validate(BotState botState, String text) {
        Pattern pattern = Pattern.compile(botState.getPattern());
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
