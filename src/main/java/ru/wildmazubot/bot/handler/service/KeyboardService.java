package ru.wildmazubot.bot.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.service.ReplyMessageService;

import java.util.ArrayList;

@Slf4j
@Service
public class KeyboardService {

    private final ReplyMessageService messageService;

    public KeyboardService(ReplyMessageService messageService) {
        this.messageService = messageService;
    }

    public SendMessage getReply(long chatId,
                                String text,
                                UserKeyboardSize type,
                                String ... buttonsText) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(getKeyboard(type, buttonsText));
        return message;
    }

    private ReplyKeyboard getKeyboard(UserKeyboardSize type, String ... buttonsText) {
        return switch (type) {
            case ONE -> getOneKeyboard(buttonsText);
            case TWO -> getTwoKeyboard(buttonsText);
            case THREE -> getThreeKeyboard(buttonsText);
            case FOUR -> getFourKeyboard(buttonsText);
            case FIVE -> getFiveKeyboard(buttonsText);
            case SIX -> getSixKeyboard(buttonsText);
        };
    }

    private ReplyKeyboard getSixKeyboard(String[] buttonsText) {
        return buttonsText.length == 12
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                        add(getButton(buttonsText[2], buttonsText[3]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[4], buttonsText[5]));
                        add(getButton(buttonsText[6], buttonsText[7]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[8], buttonsText[9]));
                        add(getButton(buttonsText[10], buttonsText[11]));
                    }});
                }})
                : null;
    }

    private ReplyKeyboard getFiveKeyboard(String[] buttonsText) {
        return buttonsText.length == 10
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                        add(getButton(buttonsText[2], buttonsText[3]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[4], buttonsText[5]));
                        add(getButton(buttonsText[6], buttonsText[7]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[8], buttonsText[9]));
                    }});
                }})
                : null;
    }

    private ReplyKeyboard getFourKeyboard(String[] buttonsText) {
        return buttonsText.length == 8
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                        add(getButton(buttonsText[2], buttonsText[3]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[4], buttonsText[5]));
                        add(getButton(buttonsText[6], buttonsText[7]));
                    }});
                }})
                : null;
    }

    private ReplyKeyboard getThreeKeyboard(String[] buttonsText) {
        return buttonsText.length == 6
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                        add(getButton(buttonsText[2], buttonsText[3]));
                    }});
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[4], buttonsText[5]));
                    }});
                }})
                : null;
    }

    private ReplyKeyboard getTwoKeyboard(String[] buttonsText) {
        return buttonsText.length == 4
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                        add(getButton(buttonsText[2], buttonsText[3]));
                    }});
                }})
                : null;
    }

    private ReplyKeyboard getOneKeyboard(String[] buttonsText) {
        return buttonsText.length == 2
                ? new InlineKeyboardMarkup(
                new ArrayList<>(){{
                    add(new ArrayList<>(){{
                        add(getButton(buttonsText[0], buttonsText[1]));
                    }});
                }})
                : null;
    }

    private InlineKeyboardButton getButton(String text, String command) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(command);
        return btn;
    }

    public enum UserKeyboardSize {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX
    }
}
