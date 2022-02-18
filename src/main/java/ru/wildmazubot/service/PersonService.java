package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.PersonStatus;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.Person;
import ru.wildmazubot.model.repository.PersonRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PersonService {

    private final PersonRepo personRepo;
    private final UserService userService;
    private final PassportService passportService;
    private final AddressService addressService;
    private final PhoneService phoneService;
    private final EmailService emailService;

    public PersonService(PersonRepo personRepo,
                         UserService userService,
                         PassportService passportService,
                         AddressService addressService,
                         PhoneService phoneService,
                         EmailService emailService) {
        this.personRepo = personRepo;
        this.userService = userService;
        this.passportService = passportService;
        this.addressService = addressService;
        this.phoneService = phoneService;
        this.emailService = emailService;
    }

    public List<Person> findAllByUsername(String username) {
        return personRepo.findAllByUser(userService.findByUsername(username));
    }

    public boolean save(Map<String, String> userInputData, String username) {
        String phoneNumber = userInputData.get(BotState.USER_C_PHONE_NUMBER.name());
        if (phoneService.existByPhoneNumber(phoneNumber)) {
            userService.updateStatus(username, UserStatus.BANNED);
            return false;
        }
        Person person = new Person();
        person.setStatus(PersonStatus.NEW);
        person.setUser(userService.findByUsername(username));
        person.setStatusTime(LocalDateTime.now());
        person.setRegistrationDate(LocalDateTime.now());
        person.setPassport(passportService.save(userInputData));
        person.setAddress(addressService.save(userInputData));
        person.setPhone(phoneService.save(userInputData));
        personRepo.save(person);

        return true;
    }

}
