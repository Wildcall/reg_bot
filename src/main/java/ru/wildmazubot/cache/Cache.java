package ru.wildmazubot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.service.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@EnableScheduling
@PropertySource("classpath:data_cache.properties")
public class Cache {

    private final Map<String, UserDataCache> usersDataCache = new ConcurrentHashMap<>();
    private final UserService userService;

    @Value("${cache.timeout.in.sec:1800}")
    private Long cacheTimeout;
    @Value("${cache.debug:false}")
    private boolean debug;

    public Cache(UserService userService) {
        this.userService = userService;
    }

    public void addUserInputData(String username, String key, String value) {
        UserDataCache dataCache = usersDataCache.get(username);
        dataCache.addData(key, value);
        dataCache.setLastAction();
        usersDataCache.put(username, dataCache);
    }

    public Map<String, String> getUserInputData(String username) {
        return usersDataCache.get(username).getInputData();
    }

    public void wipeUserInputData(String username) {
        UserDataCache dataCache = usersDataCache.get(username);
        if (dataCache != null)
            usersDataCache.get(username).getInputData().clear();
    }

    public void setUserBotState(String username, BotState botState) {
        UserDataCache dataCache = usersDataCache.get(username);
        dataCache.setBotState(botState);
        usersDataCache.put(username, dataCache);
    }

    public BotState getUserBotState(String username) {
        UserDataCache dataCache = usersDataCache.get(username);

        if (dataCache == null) {
            User user = userService.findByUsername(username);
            if (user.getStatus().equals(UserStatus.BANNED))
                return BotState.USER_IGNORED;
            dataCache = user.getUserRole().equals(UserRole.OPERATOR)
                    ? new UserDataCache(BotState.OPERATOR_START)
                    : new UserDataCache(BotState.USER_START);
            usersDataCache.put(username, dataCache);
        }
        dataCache.setLastAction();

        return dataCache.getBotState();
    }

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}",
            initialDelayString = "${initialDelay.in.milliseconds}")
    private void wipeInactiveUserFromCache() {
        if (debug) log.info("Wipe cache");
        for (String username: usersDataCache.keySet()) {
            UserDataCache dataCache = usersDataCache.get(username);
            if ((System.currentTimeMillis() - dataCache.getLastAction()) / 1000 > cacheTimeout) {
                if (debug) debugWipeDataCacheInfo(username, dataCache);
                usersDataCache.remove(username, dataCache);
            }
        }
    }

    private void debugWipeDataCacheInfo(String username, UserDataCache dataCache) {
        log.info("Remove {} / {} / {} from cache!",
                String.format("%1$"+ 15 + "s", username),
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
