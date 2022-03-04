package ru.wildmazubot.model.entity;

public enum UserStatus {
    NEW,
    FILL_DATA,
    WAIT_EMAIL,
    WAIT_CL,
    READY_TO_KYC,
    WAIT_KYC,
    WAIT_APPROVE,
    ACTIVE,
    BANNED
}
