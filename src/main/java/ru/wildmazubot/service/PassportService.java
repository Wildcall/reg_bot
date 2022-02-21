package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.Passport;
import ru.wildmazubot.model.repository.PassportRepo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PassportService {

    private final PassportRepo passportRepo;

    public PassportService(PassportRepo passportRepo) {
        this.passportRepo = passportRepo;
    }

    public Passport save(Map<BotState, String> userInputData) {
        Passport passport = new Passport();
        passport.setLastName(userInputData.get(BotState.USER_PASSPORT_LAST_NAME));
        passport.setFirstName(userInputData.get(BotState.USER_PASSPORT_FIRST_NAME));
        passport.setMiddleName(userInputData.get(BotState.USER_PASSPORT_MIDDLE_NAME));
        String pattern = "dd.MM.yyyy";
        LocalDate localDate = LocalDate.parse(userInputData.get(BotState.USER_PASSPORT_BIRTHDAY), DateTimeFormatter.ofPattern(pattern));
        passport.setBirthDay(localDate);

        return passportRepo.save(passport);
    }
}
