package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Email;

public interface EmailRepo extends CrudRepository<Email, Long> {
    boolean existsByEmail(String email);
}
