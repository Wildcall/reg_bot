package ru.wildmazubot.bot;

// TODO: 18.02.2022 добавить внешинй файл с локализацией

public enum BotState {
    USER_IGNORED(-1, "", ""),

    USER_START(0, "", ""),
    USER_CREATE(0, "", ""),

    USER_C_PASSPORT_LAST_NAME       (0, "Фамилия", "[a-zA-Z]"),
    USER_C_PASSPORT_FIRST_NAME      (0, "Имя", "[a-zA-Z]"),
    USER_C_PASSPORT_MIDDLE_NAME     (0, "Отчество", "[a-zA-Z]"),
    USER_C_PASSPORT_BIRTHDAY        (0, "Дата рождения", "(?:0[1-9]|[12][0-9]|3[01])[-/.](?:0[1-9]|1[012])[-/.](?:19\\d{2}|20[01][0-9]|2022)"),
    USER_C_ADDRESS_COUNTRY          (0, "Страна", "[а-яА-Я]"),
    USER_C_ADDRESS_REGION           (0, "Регион", "[а-яА-Я]"),
    USER_C_ADDRESS_CITY             (0, "Город", "[а-яА-Я]"),
    USER_C_ADDRESS_STREET           (0, "Улица", "[а-яА-Я]"),
    USER_C_ADDRESS_POSTAL_CODE      (0, "Почтовый индекс", "[0-9]{6}"),
    USER_C_PHONE_NUMBER             (0, "Номер телефона", "^(7?|8?)[0-9]{10}$"),

    USER_CREATE_CONFIRM(0, "Все верно", ""),
    USER_CREATE_DECLINE(0, "Нужно исправить", ""),

    OPERATOR_START(1, "", ""),
    OPERATOR_TEST(1, "", "");

    private final int code;
    private final String title;
    private final String pattern;

    private static final BotState[] states = new BotState[]{
        BotState.USER_C_PASSPORT_LAST_NAME,
        BotState.USER_C_PASSPORT_FIRST_NAME,
        BotState.USER_C_PASSPORT_MIDDLE_NAME,
        BotState.USER_C_PASSPORT_BIRTHDAY,
        BotState.USER_C_ADDRESS_COUNTRY,
        BotState.USER_C_ADDRESS_REGION,
        BotState.USER_C_ADDRESS_CITY,
        BotState.USER_C_ADDRESS_STREET,
        BotState.USER_C_ADDRESS_POSTAL_CODE,
        BotState.USER_C_PHONE_NUMBER };

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

    public static BotState[] getStates(){
        return states;
    }
}

