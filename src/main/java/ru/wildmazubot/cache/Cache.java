package ru.wildmazubot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@EnableScheduling
@PropertySource("classpath:data_cache.properties")
public class Cache {

    private final Map<Long, UserDataCache> usersDataCache = new ConcurrentHashMap<>();

    @Value("${cache.timeout.in.sec:1800}")
    private Long cacheTimeout;
    @Value("${cache.debug:false}")
    private boolean debug;

    public void addUserInputData(long userId, BotState key, String value) {
        UserDataCache dataCache = usersDataCache.get(userId);
        dataCache.addData(key, value);
        dataCache.setLastAction();
        usersDataCache.put(userId, dataCache);
    }

    public Map<BotState, String> getUserInputData(long userId) {
        return usersDataCache.get(userId).getInputData();
    }

    public void setUserBotState(long userId, BotState botState) {
        UserDataCache dataCache = usersDataCache.get(userId);
        if (dataCache == null)
            dataCache = new UserDataCache(botState);
        dataCache.setBotState(botState);
        usersDataCache.put(userId, dataCache);
    }

    public BotState getUserBotState(long userId) {
        UserDataCache dataCache = usersDataCache.get(userId);

        if (dataCache != null) {
            dataCache.setLastAction();
            return dataCache.getBotState();
        }

        return null;
    }

    public void deleteFromCache(long userId) {
        usersDataCache.remove(userId);
    }

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}",
            initialDelayString = "${initialDelay.in.milliseconds}")
    private void wipeInactiveUserFromCache() {
        if (debug) log.info("Wipe cache");
        for (long userId: usersDataCache.keySet()) {
            UserDataCache dataCache = usersDataCache.get(userId);
            if ((System.currentTimeMillis() - dataCache.getLastAction()) / 1000 > cacheTimeout) {
                if (debug) debugWipeDataCacheInfo(userId, dataCache);
                usersDataCache.remove(userId, dataCache);
            }
        }
    }

    private void debugWipeDataCacheInfo(long userId, UserDataCache dataCache) {
        log.info("Remove {} / {} / {} from cache!",
                String.format("%1$"+ 15 + "s", userId),
                String.format("%1$"+ 10 + "s", dataCache.getBotState()),
                dataCache.getInputData().values());
    }

    @Scheduled(fixedDelayString = "${cache.debug.delay}")
    private void printCache() {
        if (debug)
            usersDataCache.forEach((k, v) -> log.info("{} / {} / {} / {} sec.",
                            String.format("%1$"+ 15 + "s", k),
                            String.format("%1$"+ 10 + "s", v.getBotState()),
                            v.getInputData().values(),
                            (System.currentTimeMillis() - v.getLastAction()) / 1000));

    }

}
