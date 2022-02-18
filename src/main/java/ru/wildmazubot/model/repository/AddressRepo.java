package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Address;

public interface AddressRepo extends CrudRepository<Address, Long> {
}
