package ru.wildmazubot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.wildmazubot.entity.core.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// TODO: 16.02.2022 Добавить верификацию введенных данных 
/// TODO: 16.02.2022 Добавить базу данных 

@Slf4j
@Component
public class TelegramBot extends TelegramWebhookBot {

    private final String botUsername = System.getenv("BOT_USERNAME");
    private final String botToken = System.getenv("BOT_TOKEN");;
    private final String botPath = System.getenv("BOT_PATH");;

    private final List<Person> persons = new ArrayList<>();
    private final Map<Long, Person> regMap = new HashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//
//            String text = update.getMessage().getText();
//            String chatId = String.valueOf(update.getMessage().getChatId());
//            User user = update.getMessage().getFrom();
//
//            return getCommandResponse(text, user, chatId);
//        } else if (update.hasCallbackQuery()) {
//            String text = update.getCallbackQuery().getData();
//            String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
//            User user = update.getCallbackQuery().getFrom();
//
//            SendMessage message = getCommandResponse(text, user, chatId);
//            message.enableHtml(true);
//            message.setParseMode(ParseMode.HTML);
//            return message;
//        }
        return null;
    }

//    private SendMessage getCommandResponse(String text, User user, String chatId) {
//
//        if (text.equals(VerifiedCommands.ADD.getCommand())) {
//            return handleAddCommand(chatId);
//        }
//
//        if (text.equals(VerifiedCommands.ALL_PERSONS.getCommand())) {
//            return handleInfoCommand(chatId);
//        }
//
//        if (text.equals(VerifiedCommands.CREATE.getCommand())) {
//            Person person = new Person();
//            regMap.put(user.getId(), person);
//            return handleCreateCommand(0, chatId);
//        }
//
//        if (text.equals(VerifiedCommands.APPROVE.getCommand())) {
//            Person person = regMap.get(user.getId());
//            if (person != null) {
//                person.setUserId(user.getId());
//                log.info("DB save - " + person);
//                persons.add(person);
//                regMap.remove(user.getId());
//            }
//            return handleStartCommand(user, chatId);
//        }
//
//        if (text.equals(VerifiedCommands.EDIT.getCommand())) {
//            regMap.remove(user.getId());
//            Person person = new Person();
//            regMap.put(user.getId(), person);
//            return handleCreateCommand(0, chatId);
//        }
//
//        if (regMap.containsKey(user.getId())) {
//            int command = regMap.get(user.getId()).addProperty(text);
//            if (command == 5) {
//                return handleCheckCommand(user, chatId);
//            }
//            return handleCreateCommand(command, chatId);
//        }
//
//        return handleStartCommand(user, chatId);
//    }
//
//    private SendMessage handleCheckCommand(User user, String chatId) {
//        SendMessage message = new SendMessage();
//        message.setText(regMap.get(user.getId()).toString());
//        message.setChatId(chatId);
//        message.setReplyMarkup(getCheckKeyboard());
//
//        return message;
//    }
//
//    private SendMessage handleCreateCommand(int command, String chatId) {
//        SendMessage message = new SendMessage();
//        message.setText(Person.getFieldName().get(command));
//        message.setReplyMarkup(null);
//        message.setChatId(chatId);
//
//        return message;
//    }
//
//    private SendMessage handleStartCommand(User user, String chatId) {
//        SendMessage message = new SendMessage();
//        message.setText(user.getUserName() + " Ну что, начнем?");
//        message.setReplyMarkup(getVerifiedUserKeyboard());
//        message.setChatId(chatId);
//
//        return message;
//    }
//
//    private SendMessage handleInfoCommand(String chatId) {
//        SendMessage message = new SendMessage();
//        StringBuilder sb = new StringBuilder();
//        sb.append("Информация по всем аккаунтам которые ты зарегистрировал для нас:\n");
//        persons.forEach(person -> sb.append(person.toShortString()).append("\n"));
//
//        message.setText(sb.toString());
//        message.setReplyMarkup(getVerifiedUserKeyboard());
//        message.setChatId(chatId);
//
//        return message;
//    }
//
//    private SendMessage handleAddCommand(String chatId) {
//        SendMessage message = new SendMessage();
//        message.setText("Ты хочешь зарегистрировать новый аккаунт для нас?");
//        message.setReplyMarkup(getRegistrationKeyboard());
//        message.setChatId(chatId);
//
//        return message;
//    }
//
//    private ReplyKeyboard getRegistrationKeyboard() {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//
//        InlineKeyboardButton inlineKeyboardButton_1 = new InlineKeyboardButton();
//        inlineKeyboardButton_1.setText(VerifiedCommands.CREATE.getTitle());
//        inlineKeyboardButton_1.setCallbackData(VerifiedCommands.CREATE.getCommand());
//
//        InlineKeyboardButton inlineKeyboardButton_2 = new InlineKeyboardButton();
//        inlineKeyboardButton_2.setText(VerifiedCommands.CREATE.getDecline());
//        inlineKeyboardButton_2.setCallbackData(VerifiedCommands.START.getCommand());
//
//        inlineKeyboardMarkup.setKeyboard(
//                new ArrayList<>(){{
//                    add(new ArrayList<>(){{
//                        add(inlineKeyboardButton_1);
//                        add(inlineKeyboardButton_2);
//                    }});
//                }});
//
//        return inlineKeyboardMarkup;
//    }
//
//    private ReplyKeyboard getVerifiedUserKeyboard() {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//
//        InlineKeyboardButton inlineKeyboardButton_1_1 = new InlineKeyboardButton();
//        inlineKeyboardButton_1_1.setText(VerifiedCommands.ADD.getTitle());
//        inlineKeyboardButton_1_1.setCallbackData(VerifiedCommands.ADD.getCommand());
//
//        InlineKeyboardButton inlineKeyboardButton_1_2 = new InlineKeyboardButton();
//        inlineKeyboardButton_1_2.setText(VerifiedCommands.ALL_PERSONS.getTitle());
//        inlineKeyboardButton_1_2.setCallbackData(VerifiedCommands.ALL_PERSONS.getCommand());
//
//        InlineKeyboardButton inlineKeyboardButton_2_1 = new InlineKeyboardButton();
//        inlineKeyboardButton_2_1.setText(VerifiedCommands.START.getTitle());
//        inlineKeyboardButton_2_1.setCallbackData(VerifiedCommands.START.getCommand());
//
//        inlineKeyboardMarkup.setKeyboard(
//                new ArrayList<>(){{
//                    add(new ArrayList<>(){{
//                        add(inlineKeyboardButton_1_1);
//                        add(inlineKeyboardButton_1_2);
//                    }});
//                    add(new ArrayList<>(){{
//                        add(inlineKeyboardButton_2_1);
//                    }});
//                }});
//
//        return inlineKeyboardMarkup;
//    }
//
//    private ReplyKeyboard getCheckKeyboard() {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//
//        InlineKeyboardButton inlineKeyboardButton_1 = new InlineKeyboardButton();
//        inlineKeyboardButton_1.setText(VerifiedCommands.APPROVE.getTitle());
//        inlineKeyboardButton_1.setCallbackData(VerifiedCommands.APPROVE.getCommand());
//
//        InlineKeyboardButton inlineKeyboardButton_2 = new InlineKeyboardButton();
//        inlineKeyboardButton_2.setText(VerifiedCommands.EDIT.getTitle());
//        inlineKeyboardButton_2.setCallbackData(VerifiedCommands.EDIT.getCommand());
//
//        inlineKeyboardMarkup.setKeyboard(
//                new ArrayList<>(){{
//                    add(new ArrayList<>(){{
//                        add(inlineKeyboardButton_1);
//                        add(inlineKeyboardButton_2);
//                    }});
//                }});
//
//        return inlineKeyboardMarkup;
//    }
}
