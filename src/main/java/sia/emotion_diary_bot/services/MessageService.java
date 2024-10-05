package sia.emotion_diary_bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@PropertySources(value = {
        @PropertySource(value = "classpath:application.yml", encoding = "UTF-8"),
        @PropertySource(value = "classpath:application-dev.yml", encoding = "UTF-8"),
})
public class MessageService extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    private final Set<Long> users = new HashSet<>();
    private final GoogleDriveService googleDriveService;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.name}")
    private String botName;
    @Value("${user.password}")
    private String userPassword;
    @Value("${user.questions}")
    private String userMessages;
    @Value("${bot.chat-id}")
    private long chatId;

    public MessageService(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChat().getId();
        if (users.contains(update.getMessage().getChatId())) {
            handleAndSaveMessageOnDrive(message, chatId);
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                if (secureChat(chatId)) {
                    if (message.equals(userPassword)) {
                        LOGGER.debug("Authentication success.");
                        users.add(update.getMessage().getChatId());
                        sendMessage(chatId, "Authentication success.");
                        sendMessage(chatId, "Now you will get messages for answering on 3 simple question every day by scheduling.");
                    } else {
                        LOGGER.warn("Authentication failed. Try again.");
                        sendMessage(chatId, "Authentication failed. Try again.");
                    }
                }
            }
        }
    }

    private void handleAndSaveMessageOnDrive(String message, long chatId) {
        LOGGER.info("Handling and saving message on drive.");
        try {
            String savedMessage = googleDriveService.saveMessageOnDrive(message);
            if (savedMessage.contains(message)) {
                sendMessage(chatId, "Your answer was successfully written on Drive.");
            } else {
                sendMessage(chatId, "Your answer was not written on drive: " + message);
            }
        } catch (IOException e) {
            sendMessage(chatId, "An error occurred while saving the message. Try again.");
            sendDailyQuestions();
            LOGGER.error(e.getMessage());
        }
    }

    private boolean secureChat(long chatId) {
        LOGGER.debug("Trying to secure chat with id: " + chatId);
        if (this.chatId == chatId) {
            sendMessage(this.chatId, "Chat already secured.");
            return true;
        } else {
            Runnable securityNotifier = () -> {
                for (int i = 0; i < 5; i++) {
                    sendMessage(this.chatId, "*** Unsecured access ***");
                    sendMessage(this.chatId, "Chat is not secured!!!");
                    sendMessage(this.chatId, "Somebody trying to use this bot.");
                    try {
                        if (i >= 3) {
                            Thread.sleep(10000);

                        } else {
                            Thread.sleep(2500);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            };
            securityNotifier.run();
            return false;

        }
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void sendDailyQuestions() {
        LOGGER.debug("Sending daily questions...");
        users.forEach(chatId -> sendMessage(chatId, userMessages));
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendMessage(Long chatId, String message) {
        LOGGER.debug("Sending message to: {}", chatId);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
