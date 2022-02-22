package ru.wildmazubot.bot.handler.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.ReplyMessageService;
import ru.wildmazubot.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserSendMessageService {

    private final KeyboardService keyboardService;
    private final ReplyMessageService getReplyText;
    private final UserService userService;
    private final Cache cache;
    private static final BotState[] state = BotState.getUserState();

    public UserSendMessageService(KeyboardService keyboardService,
                                  ReplyMessageService getReplyText,
                                  UserService userService,
                                  Cache cache) {
        this.keyboardService = keyboardService;
        this.getReplyText = getReplyText;
        this.userService = userService;
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

    public ReplyPayload handleInputData(long chatId,
                                        long userId,
                                        BotState botState,
                                        String text) {
        ReplyPayload reply = new ReplyPayload();
//        if (botState.equals(BotState.USER_ACTIVE_PAYMENT)) {
//            ReplyPayload replyPayload = new ReplyPayload(getStartMenu(botState, chatId));
//            replyPayload.addPayload(
//                    new SendMessage(
//                            String.valueOf(userService.getOperatorId(userId)),
//                            text));
//            replyPayload.addPayload(
//                    new SendMessage(
//                            String.valueOf(chatId),
//                            getReplyText.getReplyText("reply.user.active.payment.send")));
//            return replyPayload;
//        }

        if (botState.name().startsWith("USER_C_")) {
            return handleCreateInput(chatId, userId, botState, text);
        }

        return reply.setMessage(getStartMenu(botState, chatId));
    }

    private ReplyPayload handleCreateInput(long chatId,
                                          long userId,
                                          BotState botState,
                                          String text){
        ReplyPayload reply = new ReplyPayload();

        if (!validate(botState, text)) {
            return reply.setMessage(getResponse(
                    chatId,
                    getReplyText.getReplyText("reply.create." + botState.getTitle() + ".error")));

        }
        switch (botState) {
            case USER_C_LAST_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_FIRST_NAME);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.first.title")));
            }
            case USER_C_FIRST_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_MIDDLE_NAME);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.middle.title")));
            }
            case USER_C_MIDDLE_NAME -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_BIRTHDAY);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.birthday.title")));
            }
            case USER_C_BIRTHDAY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_COUNTRY);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.country.title")));
            }
            case USER_C_COUNTRY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_REGION);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.region.title")));
            }
            case USER_C_REGION -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_CITY);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.city.title")));
            }
            case USER_C_CITY -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_STREET);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.street.title")));
            }
            case USER_C_STREET -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_POSTAL_CODE);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.postcode.title")));
            }
            case USER_C_POSTAL_CODE -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_C_NUMBER);
                reply.setMessage(
                        getResponse(
                                chatId,
                                getReplyText.getReplyText("reply.create.number.title")));
            }
            case USER_C_NUMBER -> {
                cache.addUserInputData(userId, botState, text);
                cache.setUserBotState(userId, BotState.USER_NEW_CONFIRM);
                StringBuilder sb = new StringBuilder();
                cache.getUserInputData(userId).values().forEach(s -> sb.append(s).append("\n"));
                return reply.setMessage(getUserNewConfirmMenu(chatId, sb.toString()));
            }
        }

        return reply.setMessage(getStartMenu(botState, chatId));
    }

    public SendMessage getStartMenu(BotState botState, long chatId) {
        return switch (botState) {
            case USER_NEW                           -> getUserNewMainMenu(chatId);
            case USER_PROCESS,
                    USER_WAIT_APPROVE               -> getUserProcessMainMenu(chatId);
            case USER_WAIT_KYC                      -> getUserKycMainMenu(chatId);
            case USER_ACTIVE, USER_ACTIVE_PAYMENT   -> getUserActiveMainReply(chatId);
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

    private SendMessage getUserNewConfirmMenu(long chatId, String ... text) {
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

    public SendMessage getBonusMenu(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.user.active.bonus.title"),
                KeyboardService.UserKeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.active.bonus.yes"),
                UserCommand.USER_YES.getCommand(),
                getReplyText.getReplyText("keyboard.user.active.bonus.no"),
                UserCommand.USER_NO.getCommand());
    }

    public SendMessage getBackMenu(long chatId) {
        return keyboardService.getReply(
                chatId,
                getReplyText.getReplyText("keyboard.back.title"),
                KeyboardService.UserKeyboardSize.ONE,
                getReplyText.getReplyText("keyboard.back"),
                UserCommand.USER_START.getCommand());

    }

    public List<SendMessage> getEmailNotification(long userId) {
        String text = getReplyText.getReplyText("notification.operator.user.WAIT_EMAIL", String.valueOf(userId));
        List<User> operators = userService.findAllOperators();
        List<SendMessage> notification = new ArrayList<>();
        operators.forEach(o -> {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(o.getId()));
            message.setText(text);
            notification.add(message);
        });
        return notification;
    }

    private static boolean validate(BotState botState, String text) {
        Pattern pattern = Pattern.compile(botState.getPattern());
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
