package ru.wildmazubot.bot.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.command.UserCommand;
import ru.wildmazubot.bot.handler.service.UserKeyboardService;
import ru.wildmazubot.cache.Cache;
import ru.wildmazubot.model.entity.core.Person;
import ru.wildmazubot.service.PersonService;

import java.util.List;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class UserCallbackHandler {

    private final UserKeyboardService userKeyboardService;
    private final PersonService personService;
    private final Cache cache;

    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public UserCallbackHandler(UserKeyboardService userKeyboardService,
                               PersonService personService,
                               Cache cache) {
        this.userKeyboardService = userKeyboardService;
        this.personService = personService;
        this.cache = cache;
    }

    public BotApiMethod<?> handle(CallbackQuery callbackQuery,
                                  BotState botState) {
        String command = callbackQuery.getData();
        String username = callbackQuery.getFrom().getUserName();
        String text;
        long chatId = callbackQuery.getMessage().getChatId();

        if (debug) log.info("Command - {} , botState - {}", command, botState);

        if (UserCommand.USER_LIST.getCommand().equals(command)){
            cache.setUserBotState(username, BotState.USER_START);
            List<Person> personList = personService.findAllByUsername(username);
            if(personList.isEmpty()) {
                text = "Пока что тут пусто...";
            } else {
                StringBuilder sb = new StringBuilder();
                personList.forEach(p -> sb.append(p.shortView()).append("\n"));
                text = sb.toString();
            }
            return userKeyboardService
                    .getReply(chatId,
                            text,
                            UserKeyboardService.UserKeyboardType.BACK);
        }

        if (UserCommand.USER_CREATE.getCommand().equals(command)){
            cache.setUserBotState(username, BotState.USER_CREATE);
            return userKeyboardService
                    .getReply(chatId,
                            "Ты хочешь зарегистрировать аккаунт для нас?",
                            UserKeyboardService.UserKeyboardType.YES_NO,
                            "Да",
                            "Нет");
        }

        if (UserCommand.USER_MESSAGE.getCommand().equals(command)){
            cache.setUserBotState(username, BotState.USER_START);
            text = "Тут будет опция написать оператору по любому вопросу!";
            return userKeyboardService
                    .getReply(chatId,
                            text,
                            UserKeyboardService.UserKeyboardType.BACK);
        }

        if (UserCommand.USER_HELP.getCommand().equals(command)){
            cache.setUserBotState(username, BotState.USER_START);
            text = "Тут будет файл или видео с инструкцией!";
            return userKeyboardService
                    .getReply(chatId,
                            text,
                            UserKeyboardService.UserKeyboardType.BACK);
        }

        if (UserCommand.USER_YES.getCommand().equals(command)){
            if (botState.equals(BotState.USER_CREATE)) {
                cache.setUserBotState(username, BotState.getStates()[0]);
                text = BotState.getStates()[0].getTitle();
                return getResponse(chatId, text);
            }
            if (botState.equals(BotState.USER_CREATE_CONFIRM)) {
                boolean ok = personService.save(cache.getUserInputData(username), username);
                cache.wipeUserInputData(username);
                if (ok) {
                    cache.setUserBotState(username, BotState.USER_START);
                    return userKeyboardService.getReply(
                            chatId,
                            "Выбери что-то из предложенного!",
                            UserKeyboardService.UserKeyboardType.MAIN);
                }
                cache.setUserBotState(username, BotState.USER_IGNORED);
                return getResponse(
                                    chatId,
                                    "Номер телефона уже используется, за такое у нас банят!");
            }
        }

        if (UserCommand.USER_NO.getCommand().equals(command)){
            if (botState.equals(BotState.USER_CREATE)) {
                cache.setUserBotState(username, BotState.USER_START);
                return userKeyboardService
                        .getReply(chatId,
                                "Выбери что-то из предложенного!",
                                UserKeyboardService.UserKeyboardType.MAIN);
            }
            if (botState.equals(BotState.USER_CREATE_CONFIRM)) {
                userKeyboardService.getReply(chatId,
                                "Тут будет меню c возможностью исправить информацию!",
                                UserKeyboardService.UserKeyboardType.MAIN);
            }
        }

        return userKeyboardService.getReply(chatId,
                        "Выбери что-то из предложенного!",
                        UserKeyboardService.UserKeyboardType.MAIN);
    }

    private SendMessage getResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(String.valueOf(chatId));
        return message;
    }
}
