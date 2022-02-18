package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Phone;

public interface PhoneRepo extends CrudRepository<Phone, Long> {
    boolean existsByNumber(String number);
}
