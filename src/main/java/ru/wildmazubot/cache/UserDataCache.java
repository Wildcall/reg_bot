package ru.wildmazubot.cache;

import lombok.Data;
import ru.wildmazubot.bot.BotState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Data
public class UserDataCache {

    private BotState botState;
    private Integer messageId;
    private long sessionToken;
    private Map<BotState, String> inputData;
    private Long lastAction;

    public UserDataCache(Integer messageId, BotState botState) {
        this.messageId = messageId;
        this.botState = botState;
        this.sessionToken = new Random().nextLong();
        this.inputData = new LinkedHashMap<>();
        setLastAction();
    }

    public void setBotState(Integer messageId, BotState botState) {
        this.messageId = messageId > 0 ? messageId : this.messageId;
        this.botState = botState;
        setLastAction();
    }

    public void addData(BotState key, String value) {
        this.inputData.put(key, value);
    }

    public void setLastAction() {
        this.lastAction = System.currentTimeMillis();
    }
}
