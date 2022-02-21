package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;

import java.util.List;

public interface UserRepo extends CrudRepository<User, Long> {
    List<User> findAllByUserRole(UserRole role);
    List<User> findAllByStatusAndOperatorNull(UserStatus status);
    List<User> findAllByStatusAndOperator(UserStatus status, User user);
}
