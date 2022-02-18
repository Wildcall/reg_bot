package ru.wildmazubot.bot.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.wildmazubot.bot.command.UserCommand;

import java.util.ArrayList;

@Slf4j
@Service
public class UserKeyboardService {

    public SendMessage getReply(long chatId,
                                String text,
                                UserKeyboardType type,
                                String ... buttonsText) {
        return createMessage(chatId, text, getKeyboard(type, buttonsText));
    }

    private ReplyKeyboard getKeyboard(UserKeyboardType type, String ... buttonsText) {
        return switch (type) {
            case YES_NO -> getYesNoKeyboard(buttonsText);
            case BACK -> getBackKeyboard(buttonsText);
            default -> getMainKeyboard(buttonsText);
        };
    }

    private ReplyKeyboard getYesNoKeyboard(String ... buttonsText) {
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(buttonsText[0]);
        button1.setCallbackData(UserCommand.USER_YES.getCommand());

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText(buttonsText[1]);
        button2.setCallbackData(UserCommand.USER_NO.getCommand());

        return new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(button1);
                        add(button2);
                    }});
                }});
    }

    private ReplyKeyboard getBackKeyboard(String ... buttonsText) {
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("В начало");
        button1.setCallbackData(UserCommand.USER_START.getCommand());
        return new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(button1);
                    }});
                }});
    }

    private InlineKeyboardMarkup getMainKeyboard(String ... buttonsText) {

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Добавить");
        button1.setCallbackData(UserCommand.USER_CREATE.getCommand());
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Список");
        button2.setCallbackData(UserCommand.USER_LIST.getCommand());
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Помощь");
        button3.setCallbackData(UserCommand.USER_HELP.getCommand());
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Написать");
        button4.setCallbackData(UserCommand.USER_MESSAGE.getCommand());
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("В начало");
        button5.setCallbackData(UserCommand.USER_START.getCommand());

        return new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(button1);
                        add(button2);
                    }});
                    add(new ArrayList<>(){{
                        add(button3);
                        add(button4);
                    }});
                    add(new ArrayList<>(){{
                        add(button5);
                    }});
                }});
    }

    private SendMessage createMessage(long chatId,
                                      String text,
                                      ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboard);
        return message;
    }

    public enum UserKeyboardType {
        MAIN,
        YES_NO,
        BACK
    }
}
