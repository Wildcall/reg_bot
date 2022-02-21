package ru.wildmazubot.bot.command;

public enum OperatorCommand {
    OPERATOR_START          ("/operator_start"),
    OPERATOR_FILL_DATA      ("/operator_fill_data"),
    OPERATOR_WAIT_EMAIL     ("/operator_email"),
    OPERATOR_WAIT_CL        ("/operator_cl"),
    OPERATOR_WAIT_KYC       ("/operator_kyc"),
    OPERATOR_WAIT_APPROVE   ("/operator_approve"),
    OPERATOR_LINK           ("/operator_link"),
    OPERATOR_YES            ("/operator_yes"),
    OPERATOR_NO             ("/operator_no");

    private final String command;

    OperatorCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
