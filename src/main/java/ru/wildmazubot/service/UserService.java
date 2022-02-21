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
    private final ReplyMessageService getReplyText;
    private final PhoneService phoneService;
    private final EmailService emailService;

    public UserService(UserRepo userRepo,
                       PassportService passportService,
                       AddressService addressService,
                       ReplyMessageService getReplyText, PhoneService phoneService,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.passportService = passportService;
        this.addressService = addressService;
        this.getReplyText = getReplyText;
        this.phoneService = phoneService;
        this.emailService = emailService;
    }

    public User createNewUser(long userId, String username, User referrerUser) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setUserRole(UserRole.USER);
        user.setStatus(UserStatus.NEW);
        user.setReferrer(referrerUser);
        user.setRegistrationDate(LocalDateTime.now());
        user.setStatusTime(LocalDateTime.now());

        return userRepo.save(user);
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

    public String getUsersByStatusString(Long userId, UserStatus userStatus) {
        List<User> users = getUsersByStatus(userId, userStatus);
        if (users == null) {
            return getReplyText.getReplyText("reply.empty.list");
        }
        return listToString(users);
    }

    public String getReferralListString(long userId) {
        List<User> users = getReferralList(userId);
        if (users == null) {
            return getReplyText.getReplyText("reply.empty.list");
        }
        StringBuilder sb = new StringBuilder();
        users.forEach(u -> sb.append(u.getUsername()).append("\n"));
        return sb.toString();
    }

    private List<User> getReferralList(long userId) {
        User user = findById(userId);
        if (user != null) {
            List<User> referrals = user.getReferralUsers();
            if (!referrals.isEmpty())
                return referrals;
        }
        return null;
    }

    private List<User> getUsersByStatus(Long userId, UserStatus userStatus) {
        List<User> users = userRepo.findAllByStatusAndOperator(userStatus, findById(userId));
        if (!users.isEmpty())
            return users;
        return null;
    }

    public boolean saveUserInputData(Map<BotState, String> inputData, long userId) {
        String phoneNumber = inputData.get(BotState.USER_PHONE_NUMBER);
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
        String email = inputData.get(BotState.OPERATOR_PASSWORD);
        if (emailService.existByEmail(email)) {
            return false;
        }
        User operator = findById(operatorId);
        if (operator != null){
            long currenUserId;
            try {
                currenUserId = Long.parseLong(inputData.get(BotState.OPERATOR_CURRENT_USER));
                User currentUser = findById(currenUserId);
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

    private String listToString(List<User> users) {
        StringBuilder sb = new StringBuilder();
        users.forEach(u -> sb.append("/").append(u.getId()).append("\n"));
        return sb.toString();
    }

    public String userToEmailMsg(long userId) {
        User user = findById(userId);
        if (user != null){
            StringBuilder sb = new StringBuilder();
            sb.append(user.getPassport().getLastName()).append("\n");
            sb.append(user.getPassport().getFirstName()).append("\n");
            sb.append(user.getPassport().getMiddleName()).append("\n");
            sb.append(user.getPassport().getBirthDay()).append("\n");
            return sb.toString();
        }
        return null;
    }

    public String userToCoinlist(long userId) {
        User user = findById(userId);
        if (user != null){
            StringBuilder sb = new StringBuilder();
            sb.append(user.getPassport().getLastName()).append("\n");
            sb.append(user.getPassport().getFirstName()).append("\n");;
            sb.append(user.getPassport().getMiddleName()).append("\n");;
            sb.append(user.getPassport().getBirthDay()).append("\n");
            sb.append(user.getAddress().getCountry()).append("\n");
            sb.append(user.getAddress().getRegion()).append("\n");
            sb.append(user.getAddress().getCity()).append("\n");
            sb.append(user.getAddress().getStreet()).append("\n");
            sb.append(user.getAddress().getPostalCode()).append("\n");
            sb.append(user.getPhone().getNumber()).append("\n");
            sb.append(user.getEmail().getEmail()).append("\n");
            sb.append(user.getEmail().getPassword()).append("\n");

            return sb.toString();
        }
        return null;
    }
}
