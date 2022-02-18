package ru.wildmazubot.cache;

import lombok.Data;
import ru.wildmazubot.bot.BotState;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class UserDataCache {

    private BotState botState;
    private Map<String, String> inputData;
    private Long lastAction;

    public UserDataCache(BotState botState) {
        this.botState = botState;
        this.inputData = new LinkedHashMap<>();
        setLastAction();
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
        setLastAction();
    }

    public void addData(String key, String value) {
        this.inputData.put(key, value);
    }

    public void setLastAction() {
        this.lastAction = System.currentTimeMillis();
    }
}
