package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.model.repository.UserRepo;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User findByUsername(String username) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setUserRole(UserRole.USER);
            user.setStatus(UserStatus.ACTIVE);
            userRepo.save(user);
        }
        return user;
    }

    public void updateStatus(String username, UserStatus userStatus) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user != null) {
            user.setStatus(userStatus);
            userRepo.save(user);
        }
    }
}
