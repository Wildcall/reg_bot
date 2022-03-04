package ru.wildmazubot.bot.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.OperatorCommand;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.service.ReplyMessageService;

import java.util.ArrayList;

@Slf4j
@Service
public class KeyboardService {

    private final ReplyMessageService getReplyText;

    public KeyboardService(ReplyMessageService getReplyText) {
        this.getReplyText = getReplyText;
    }

    public ReplyKeyboard getKeyboard(KeyboardSize type, String ... buttonsText) {
        return switch (type) {
            case ONE -> getOneKeyboard(buttonsText);
            case TWO -> getTwoKeyboard(buttonsText);
            case THREE -> getThreeKeyboard(buttonsText);
            case FOUR -> getFourKeyboard(buttonsText);
            case FIVE -> getFiveKeyboard(buttonsText);
            case SIX -> getSixKeyboard(buttonsText);
        };
    }

    public ReplyKeyboard getStartKeyboard(BotState botState, long token) {
        return switch (botState) {
            case USER_NEW                           -> getUserNewMainKeyboard(token);
            case USER_PROCESS,
                    USER_WAIT_APPROVE               -> getUserProcessMainKeyboard(token);
            case USER_WAIT_KYC                      -> getUserKycMainKeyboard(token);
            case USER_ACTIVE, USER_ACTIVE_PAYMENT   -> getUserActiveMainKeyboard(token);
            case OPERATOR_START                     -> getOperatorMainMenu(token);
            default -> null;
        };
    }

    private ReplyKeyboard getOperatorMainMenu(long token) {
        return getKeyboard(
                KeyboardService.KeyboardSize.SIX,
                getReplyText.getReplyText("keyboard.operator.main.ALL"),
                OperatorCommand.OPERATOR_FILL_DATA.getCommand() + token,
                getReplyText.getReplyText("keyboard.operator.main.WAIT_EMAIL"),
                OperatorCommand.OPERATOR_WAIT_EMAIL.getCommand() + token,
                getReplyText.getReplyText("keyboard.operator.main.WAIT_CL"),
                OperatorCommand.OPERATOR_WAIT_CL.getCommand() + token,
                getReplyText.getReplyText("keyboard.operator.main.WAIT_KYC"),
                OperatorCommand.OPERATOR_WAIT_KYC.getCommand() + token,
                getReplyText.getReplyText("keyboard.operator.main.WAIT_APPROVE"),
                OperatorCommand.OPERATOR_WAIT_APPROVE.getCommand() + token,
                getReplyText.getReplyText("keyboard.operator.main.LINK"),
                OperatorCommand.OPERATOR_LINK.getCommand() + token);
    }

    public ReplyKeyboard getBackKeyboard(String command, long token) {
        return getKeyboard(
                KeyboardSize.ONE,
                getReplyText.getReplyText("keyboard.back"),
                command + token);
    }

    public ReplyKeyboard getConfirmKeyboard(long token, String ... buttonsText) {
        return getKeyboard(
                KeyboardSize.TWO,
                buttonsText);
    }

    private ReplyKeyboard getUserNewMainKeyboard(long token) {
        return getKeyboard(
                KeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.new.create"),
                UserCommand.USER_NEW_CREATE.getCommand() + token,
                getReplyText.getReplyText("keyboard.user.new.help"),
                UserCommand.USER_HELP.getCommand() + token);
    }

    private ReplyKeyboard getUserProcessMainKeyboard(long token) {
        return getKeyboard(
                KeyboardSize.ONE,
                getReplyText.getReplyText("keyboard.user.process.help"),
                UserCommand.USER_HELP.getCommand() + token);
    }

    private ReplyKeyboard getUserKycMainKeyboard(long token) {
        return getKeyboard(
                KeyboardSize.TWO,
                getReplyText.getReplyText("keyboard.user.waitkyc.done"),
                UserCommand.USER_YES.getCommand() + token,
                getReplyText.getReplyText("keyboard.user.waitkyc.help"),
                UserCommand.USER_HELP.getCommand() + token);
    }

    private ReplyKeyboard getUserActiveMainKeyboard(long token) {
        return getKeyboard(
                KeyboardSize.FOUR,
                getReplyText.getReplyText("keyboard.user.active.link"),
                UserCommand.USER_LINK.getCommand() + token,
                getReplyText.getReplyText("keyboard.user.active.referrals"),
                UserCommand.USER_REFERRALS.getCommand() + token,
                getReplyText.getReplyText("keyboard.user.active.bonuses"),
                UserCommand.USER_BONUSES.getCommand() + token,
                getReplyText.getReplyText("keyboard.user.active.help"),
                UserCommand.USER_HELP.getCommand() + token);
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

    public enum KeyboardSize {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX
    }
}
