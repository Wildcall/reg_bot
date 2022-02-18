package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.Passport;
import ru.wildmazubot.model.repository.PassportRepo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PassportService {

    private final PassportRepo passportRepo;

    public PassportService(PassportRepo passportRepo) {
        this.passportRepo = passportRepo;
    }

    public Passport save(Map<String, String> userInputData) {
        Passport passport = new Passport();
        passport.setLastName(userInputData.get(BotState.USER_C_PASSPORT_LAST_NAME.name()));
        passport.setFirstName(userInputData.get(BotState.USER_C_PASSPORT_FIRST_NAME.name()));
        passport.setMiddleName(userInputData.get(BotState.USER_C_PASSPORT_MIDDLE_NAME.name()));
        String pattern = "dd.MM.yyyy";
        LocalDate localDate = LocalDate.parse(userInputData.get(BotState.USER_C_PASSPORT_BIRTHDAY.name()), DateTimeFormatter.ofPattern(pattern));
        passport.setBirthDay(localDate);

        return passportRepo.save(passport);
    }
}
