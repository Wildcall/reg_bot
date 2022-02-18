package ru.wildmazubot.bot.command;

public enum UserCommand {
    USER_START      ("/user_start", 0),
    USER_LIST       ("/user_list", 0),
    USER_YES        ("/user_yes",0),
    USER_NO         ("/user_no",0),
    USER_CREATE     ("/user_create", 0),
    USER_APPROVE    ("/user_approve", 0),
    USER_EDIT       ("/user_edit", 0),
    USER_MESSAGE    ("/user_message", 0),
    USER_HELP       ("/user_help", 0);

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
