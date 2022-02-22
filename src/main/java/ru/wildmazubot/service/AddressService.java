package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.core.Address;
import ru.wildmazubot.model.repository.AddressRepo;

import java.util.Map;

@Service
public class AddressService {

    private final AddressRepo addressRepo;

    public AddressService(AddressRepo addressRepo) {
        this.addressRepo = addressRepo;
    }

    public Address save(Map<BotState, String> userInputData) {
        Address address = new Address();
        address.setCountry(userInputData.get(BotState.USER_C_COUNTRY));
        address.setRegion(userInputData.get(BotState.USER_C_REGION));
        address.setCity(userInputData.get(BotState.USER_C_CITY));
        address.setStreet(userInputData.get(BotState.USER_C_STREET));
        address.setPostalCode(userInputData.get(BotState.USER_C_POSTAL_CODE));

        return addressRepo.save(address);
    }
}
