package ru.wildmazubot.bot;

// TODO: 18.02.2022 добавить внешинй файл с локализацией

public enum BotState {
    USER_IGNORED                    (0, "", ""),
    USER_NEW                        (0, "", ""),
    USER_PROCESS                    (0, "", ""),
    USER_ACTIVE                     (0, "", ""),

    USER_WAIT_KYC                   (0, "", ""),
    USER_WAIT_APPROVE               (0, "", ""),

    USER_PASSPORT_LAST_NAME         (0, "last", "[a-zA-Z]"),
    USER_PASSPORT_FIRST_NAME        (0, "first", "[a-zA-Z]"),
    USER_PASSPORT_MIDDLE_NAME       (0, "middle", "[a-zA-Z]"),
    USER_PASSPORT_BIRTHDAY          (0, "birthday", "(?:0[1-9]|[12][0-9]|3[01])[-/.](?:0[1-9]|1[012])[-/.](?:19\\d{2}|20[01][0-9]|2022)"),
    USER_ADDRESS_COUNTRY            (0, "country", "[а-яА-Я]"),
    USER_ADDRESS_REGION             (0, "region", "[а-яА-Я]"),
    USER_ADDRESS_CITY               (0, "city", "[а-яА-Я]"),
    USER_ADDRESS_STREET             (0, "street", "[а-яА-Я]"),
    USER_ADDRESS_POSTAL_CODE        (0, "postcode", "^[0-9]{6}$"),
    USER_PHONE_NUMBER               (0, "number", "^(7?|8?)[0-9]{10}$"),

    USER_NEW_CONFIRM                (0, "", ""),

    OPERATOR_START                  (1, "", ""),
    OPERATOR_EMAIL                  (1, "email", ""),
    OPERATOR_PASSWORD               (1, "password", ""),
    OPERATOR_TEST                   (1, "", ""),
    OPERATOR_CONFIRM_EMAIL          (1, "", ""),
    OPERATOR_CONFIRM_CL             (1, "", ""),
    OPERATOR_CURRENT_USER           (1, "", "");

    private final int code;
    private final String title;
    private final String pattern;

    private static final BotState[] userState = new BotState[]{
        BotState.USER_PASSPORT_LAST_NAME,
        BotState.USER_PASSPORT_FIRST_NAME,
        BotState.USER_PASSPORT_MIDDLE_NAME,
        BotState.USER_PASSPORT_BIRTHDAY,
        BotState.USER_ADDRESS_COUNTRY,
        BotState.USER_ADDRESS_REGION,
        BotState.USER_ADDRESS_CITY,
        BotState.USER_ADDRESS_STREET,
        BotState.USER_ADDRESS_POSTAL_CODE,
        BotState.USER_PHONE_NUMBER};

    private static final BotState[] operatorState = new BotState[]{
            BotState.OPERATOR_EMAIL,
            BotState.OPERATOR_PASSWORD};

    BotState(int code, String title, String pattern) {
        this.code = code;
        this.title = title;
        this.pattern = pattern;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getPattern() {
        return pattern;
    }

    public static BotState[] getUserState(){
        return userState;
    }

    public static BotState[] getOperatorState(){
        return operatorState;
    }
}

