package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.model.repository.AddressRepo;

@Service
public class EmailService {

    private final AddressRepo userRepo;

    public EmailService(AddressRepo userRepo) {
        this.userRepo = userRepo;
    }
}
