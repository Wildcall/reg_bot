package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Person;
import ru.wildmazubot.model.entity.core.User;

import java.util.List;

public interface PersonRepo extends CrudRepository<Person, Long> {

    List<Person> findAllByUser(User user);
}
