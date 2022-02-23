package ru.wildmazubot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPermissions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.wildmazubot.bot.handler.ReplyPayload;
import ru.wildmazubot.bot.handler.service.KeyboardService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

@Service
@Slf4j
public class TmpHandler {

    private final KeyboardService keyboardService;
    private final Set<Integer> messageSet = new LinkedHashSet<>();
    private long randomToken;
    private Integer lastMessageId;

    public TmpHandler(KeyboardService keyboardService) {
        this.keyboardService = keyboardService;
    }

    public ReplyPayload handle(Update update) {
        long userId;
        long chatId = 0;
        String username;
        String userData;
        Integer messageId;
        ReplyPayload reply = new ReplyPayload();

        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            chatId = callbackQuery.getMessage().getChatId();
            userId = callbackQuery.getFrom().getId();
            username = callbackQuery.getFrom().getUserName();
            userData = callbackQuery.getData();
            messageId = callbackQuery.getMessage().getMessageId();

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(lastMessageId);
            editMessageReplyMarkup.setChatId(String.valueOf(chatId));

            userData = userData.replaceFirst(String.valueOf(randomToken), "");

            switch (userData) {
                case "/one" -> {
                    editMessageReplyMarkup.setReplyMarkup(getBackKeyboard(randomToken));
                    lastMessageId = messageId;
                    log.info("Callback /yes / {} / {}", messageId, lastMessageId);

                    //return reply.setMessage(editMessageReplyMarkup);
                }
                case "/two" -> {
                    editMessageReplyMarkup.setReplyMarkup(getBackKeyboard(randomToken));
                    lastMessageId = messageId;
                    log.info("Callback /no / {} / {}", messageId, lastMessageId);

                    //return reply.setMessage(editMessageReplyMarkup);
                }
                case "/start" -> {
                    editMessageReplyMarkup.setReplyMarkup(getStartKeyboard(randomToken));
                    lastMessageId = messageId;
                    log.info("Callback /start / {} / {}", messageId, lastMessageId);

                    //return reply.setMessage(editMessageReplyMarkup);
                }
            }
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            chatId = message.getChatId();
            userId = message.getFrom().getId();
            username = message.getFrom().getUserName();
            userData = message.getText();
            messageId = message.getMessageId();

            if (userData.equals("/start")) {
                if (lastMessageId != null)
                    //reply.addPayload(getDeleteMessage(message.getChatId(), lastMessageId));

                lastMessageId = messageId + 1;
                randomToken = new Random().nextLong();

                log.info("Message /start / {} / {} / {}", messageId, lastMessageId, randomToken);
                SendMessage replyMessage = getSendMessage(chatId, "Menu");
                replyMessage.setReplyMarkup(getStartKeyboard(randomToken));
                return reply.setMessage(replyMessage);
            }
        }
            return null;
    }

    private DeleteMessage getDeleteMessage(long chatId,
                                           Integer messageId){
        return new DeleteMessage(String.valueOf(chatId), messageId);
    }

    private SendMessage getSendMessage(long chatId,
                                   String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        return message;
    }

    private EditMessageText getEditMessage(long chatId,
                                    String text,
                                           Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        return message;
    }

    private BotApiMethod<?> startMenu(long chatId, String text) {
        SendMessage message = getSendMessage(chatId, text);
        message.setReplyMarkup(getTwoKeyboard("one", "/one", "two", "/two"));
        return message;
    }

    private InlineKeyboardMarkup getBackKeyboard(long token) {
        return getOneKeyboard("Back", "/start" + token);
    }

    private InlineKeyboardMarkup getStartKeyboard(long token) {
        return getTwoKeyboard("One", "/one" + token, "Two", "/two" + token);
    }

    private InlineKeyboardMarkup getTwoKeyboard(String text_1, String command_1, String text_2, String command_2) {
        return  new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(text_1, command_1));
                        add(getButton(text_2, command_2));
                    }});
                }});
    }

    private InlineKeyboardMarkup getOneKeyboard(String text, String command) {
        return new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(text, command));
                    }});
                }});
    }

    private InlineKeyboardButton getButton(String text, String command) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(command);
        return btn;
    }
}
