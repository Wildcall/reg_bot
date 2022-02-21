package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.Email;
import ru.wildmazubot.model.repository.EmailRepo;

import java.util.Map;

@Service
public class EmailService {

    private final EmailRepo emailRepo;

    public EmailService(EmailRepo emailRepo) {
        this.emailRepo = emailRepo;
    }

    public Email save(Map<BotState, String> inputData) {
        Email email = new Email();
        email.setEmail(inputData.get(BotState.OPERATOR_EMAIL));
        email.setPassword(inputData.get(BotState.OPERATOR_PASSWORD));
        return emailRepo.save(email);
    }

    public boolean existByEmail(String email) {
        return emailRepo.existsByEmail(email);
    }
}
