package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.Phone;
import ru.wildmazubot.model.repository.PhoneRepo;

import java.util.Map;

@Service
public class PhoneService {

    private final PhoneRepo phoneRepo;

    public PhoneService(PhoneRepo phoneRepo) {
        this.phoneRepo = phoneRepo;
    }

    public Phone save(Map<BotState, String> userInputData) {
        Phone phone = new Phone();
        phone.setNumber(userInputData.get(BotState.USER_PHONE_NUMBER));

        return phoneRepo.save(phone);
    }

    public boolean existByPhoneNumber(String phoneNumber) {
        return phoneRepo.existsByNumber(phoneNumber);
    }
}
