package sia.emotion_diary_bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class TelegramBotRegistrarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final MessageService messageService;

    public TelegramBotRegistrarService(MessageService messageService) {
        this.messageService = messageService;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(messageService);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
