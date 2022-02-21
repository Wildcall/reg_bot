package ru.wildmazubot.service;

import org.springframework.stereotype.Service;

@Service
public class ReplyMessageService {

    private final LocaleMessageService localeMessageService;

    public ReplyMessageService(LocaleMessageService localeMessageService) {
        this.localeMessageService = localeMessageService;
    }

    public String getReplyText(String replyText) {
        return localeMessageService.getMessage(replyText);
    }

    public String getReplyText(String replyText, Object... args) {
        return localeMessageService.getMessage(replyText, args);
    }
}
