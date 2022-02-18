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

    public Address save(Map<String, String> userInputData) {
        Address address = new Address();
        address.setCountry(userInputData.get(BotState.USER_C_ADDRESS_COUNTRY.name()));
        address.setRegion(userInputData.get(BotState.USER_C_ADDRESS_REGION.name()));
        address.setCity(userInputData.get(BotState.USER_C_ADDRESS_CITY.name()));
        address.setStreet(userInputData.get(BotState.USER_C_ADDRESS_STREET.name()));
        address.setPostalCode(userInputData.get(BotState.USER_C_ADDRESS_POSTAL_CODE.name()));

        return addressRepo.save(address);
    }
}
