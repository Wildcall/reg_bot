package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.bot.BotState;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;
import ru.wildmazubot.model.entity.core.User;
import ru.wildmazubot.model.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PassportService passportService;
    private final AddressService addressService;
    private final PhoneService phoneService;
    private final EmailService emailService;

    public UserService(UserRepo userRepo,
                       PassportService passportService,
                       AddressService addressService,
                       PhoneService phoneService,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.passportService = passportService;
        this.addressService = addressService;
        this.phoneService = phoneService;
        this.emailService = emailService;
    }

    public void createNewUser(long userId, String username, User referrerUser) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setUserRole(UserRole.USER);
        user.setStatus(UserStatus.NEW);
        user.setBonus(0);
        user.setReferrer(referrerUser);
        user.setRegistrationDate(LocalDateTime.now());
        user.setStatusTime(LocalDateTime.now());

        userRepo.save(user);
    }

    public User findById(Long id) {
        if (id == null)
            return null;
        return userRepo.findById(id).orElse(null);
    }

    public void updateStatus(long userId, UserStatus userStatus) {
        User user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(userStatus);
            user.setStatusTime(LocalDateTime.now());
            userRepo.save(user);
        }
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    public List<User> findAllOperators() {
        return userRepo.findAllByUserRole(UserRole.OPERATOR);
    }

    public List<User> getReferralList(long userId) {
        User user = findById(userId);
        if (user != null) {
            List<User> referrals = user.getReferralUsers();
            if (!referrals.isEmpty())
                return referrals;
        }
        return null;
    }

    public List<User> getUsersByStatus(Long userId, UserStatus userStatus) {
        return userRepo.findAllByStatusAndOperator(userStatus, findById(userId));
    }

    public boolean saveUserInputData(Map<BotState, String> inputData, long userId) {
        String phoneNumber = inputData.get(BotState.USER_C_NUMBER);
        if (phoneService.existByPhoneNumber(phoneNumber)) {
            updateStatus(userId, UserStatus.BANNED);
            return false;
        }
        User user = findById(userId);
        user.setStatus(UserStatus.FILL_DATA);
        user.setStatusTime(LocalDateTime.now());
        user.setPassport(passportService.save(inputData));
        user.setAddress(addressService.save(inputData));
        user.setPhone(phoneService.save(inputData));
        userRepo.save(user);

        return true;
    }

    public boolean saveEmail(Map<BotState, String> inputData, long operatorId) {
        String email = inputData.get(BotState.OPERATOR_EMAIL);

        if (emailService.existByEmail(email)) {
            return false;
        }

        User operator = findById(operatorId);
        if (operator != null){
            try {
                User currentUser = findById(Long.parseLong(inputData.get(BotState.OPERATOR_CURRENT_USER)));
                if (currentUser != null) {
                    if (currentUser.getEmail() == null) {
                        currentUser.setEmail(emailService.save(inputData));
                        currentUser.setOperator(operator);
                        currentUser.setStatus(UserStatus.WAIT_CL);
                        currentUser.setStatusTime(LocalDateTime.now());
                        operator.getProcessedUsers().add(userRepo.save(currentUser));
                        userRepo.save(operator);
                        return true;
                    }
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return false;
    }

    public long getOperatorId(long userId) {
        User user = findById(userId);
        if (user != null) {
            User operator = user.getOperator();
            if (operator != null){
                return operator.getId();
            }
        }
        return userId;
    }

    public void approveUser(long userId, int bonus, int refBonus) {
        User user = findById(userId);
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            user.setStatusTime(LocalDateTime.now());
            user.setBonus(user.getBonus() + bonus);
            User refUser = user.getReferrer();
            refUser.setBonus(refUser.getBonus() + refBonus);
            userRepo.save(user);
            userRepo.save(refUser);
        }
    }
}
