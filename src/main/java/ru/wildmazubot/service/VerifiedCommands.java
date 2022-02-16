package ru.wildmazubot.service;

public enum VerifiedCommands {
    ADD("/verified_add", "Добавить", ""),
    ALL_PERSONS("/verified_info", "Просмотреть всех", ""),
    START("/verified_start", "В начало", ""),
    CREATE("/verified_create", "Да", "Нет"),
    APPROVE("/verified_approve","Все верно",""),
    EDIT("/verified_edit","Нужно исправить","");

    private final String command;
    private final String title;
    private final String decline;

    VerifiedCommands(String command, String title, String decline) {
        this.command = command;
        this.title = title;
        this.decline = decline;
    }

    public String getCommand() {
        return command;
    }

    public String getTitle() {
        return title;
    }

    public String getDecline() {
        return decline;
    }
}
