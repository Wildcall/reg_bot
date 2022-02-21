package ru.wildmazubot.bot.command;

public enum UserCommand {
    USER_HELP       ("/user_help", 0),

    USER_NEW_CREATE ("/user_create", 0),
    USER_YES        ("/user_yes",0),
    USER_NO         ("/user_no",0),
    USER_EDIT       ("/user_edit", 0),

    USER_START      ("/user_start", 0),
    USER_MESSAGE    ("/user_message", 0),
    USER_PROCESS    ("/user_status", 0),

    USER_LIST       ("/user_list", 0),
    USER_LINK       ("/user_link", 0),
    USER_REFERRALS  ("/user_referrals", 0),
    USER_BONUSES    ("/user_bonuses", 0),
    USER_PAYMENT    ("/user_payment", 0);


    private final String command;

    private final int code;

    UserCommand(String command, int code) {
        this.command = command;
        this.code = code;
    }

    public String getCommand() {
        return command;
    }

    public int getCode() {
        return code;
    }
}
