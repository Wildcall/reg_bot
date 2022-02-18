package ru.wildmazubot.bot.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.bot.handler.service.UserKeyboardService;
import ru.wildmazubot.cache.Cache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@PropertySource("classpath:telegrambot.properties")
public class UserMessageHandler {

    private final UserKeyboardService userKeyboardService;
    private final Cache cache;

    @Value("${telegram.bot.debug:false}")
    private boolean debug;

    public UserMessageHandler(UserKeyboardService userKeyboardService,
                              Cache cache) {
        this.userKeyboardService = userKeyboardService;
        this.cache = cache;
    }

    public SendMessage handle(Message message, BotState botState) {

        long chatId = message.getChatId();
        String username = message.getFrom().getUserName();
        String text = message.getText();
        String response;

        if (botState.equals(BotState.USER_START)) {
            return userKeyboardService.getReply(
                    chatId,
                    "Выбери что-то из предложенного!",
                    UserKeyboardService.UserKeyboardType.MAIN);
        }

        int index = contains(BotState.getStates(), botState);

        if (index != -1) {
            Pattern pattern = Pattern.compile(BotState.getStates()[index].getPattern());
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find()) {
                cache.setUserBotState(username, BotState.getStates()[index]);
                response = "Упс, ты напечатал что-то с ошибкой...";
                return getResponse(chatId, response);
            } else {
                cache.addUserInputData(
                                username,
                                BotState.getStates()[index].name(),
                                text);
            }

            if (index == BotState.getStates().length - 1) {
                cache.setUserBotState(username, BotState.USER_CREATE_CONFIRM);
                StringBuilder stringBuilder = new StringBuilder("Проверь пожалуйста данные!\n");
                cache.getUserInputData(username).values().forEach(s -> stringBuilder.append(s).append("\n"));
                return userKeyboardService.getReply(chatId,
                        stringBuilder.toString(),
                        UserKeyboardService.UserKeyboardType.YES_NO,
                        "Все верно",
                        "Нужно исправить");
            }

            cache.setUserBotState(username, BotState.getStates()[index + 1]);
            response = BotState.getStates()[index + 1].getTitle();
            return getResponse(chatId, response);
        }

        return null;
    }

    private static int contains(final BotState[] states, final BotState v) {

        int result = -1;

        for (int i = 0; i < states.length; i++) {
            if(states[i].equals(v)){
                result = i;
                break;
            }
        }

        return result;
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
