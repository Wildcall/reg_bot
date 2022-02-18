package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Passport;

import java.util.List;

public interface PassportRepo extends CrudRepository<Passport, Long> {
    List<Passport> findAllByIdIn(List<Long> idList);
}
