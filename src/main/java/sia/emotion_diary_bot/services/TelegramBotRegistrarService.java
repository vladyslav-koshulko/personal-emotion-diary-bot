package sia.emotion_diary_bot.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBotRegistrarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final MessageService messageService;
    private final TelegramBotsApi telegramBotsApi;

    public TelegramBotRegistrarService(MessageService messageService, TelegramBotsApi telegramBotsApi) {
        this.messageService = messageService;
        this.telegramBotsApi = telegramBotsApi;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            telegramBotsApi.registerBot(messageService);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @PreDestroy
    @PostConstruct
    public void clearWebhook() {
        try {
            LOGGER.debug("Removing webhook...");
            DeleteWebhook deleteWebhook = new DeleteWebhook();
            messageService.execute(deleteWebhook);
            LOGGER.debug("Webhook removed.");
        } catch (TelegramApiException e) {
            LOGGER.debug("Webhook could not be removed.");
            throw new RuntimeException(e);
        }
    }


}
