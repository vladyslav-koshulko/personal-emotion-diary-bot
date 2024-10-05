package sia.emotion_diary_bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource(value = "classpath:application-dev.yml", encoding = "UTF-8")
public class MessageService extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private GoogleDriveService googleDriveService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    @Value("${user.password}")
    private String userPassword;

    @Value("${user.questions}")
    private String userMessages;

    private Set<Long> users = new HashSet<>();

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.info("Update received.");
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChat().getId();
        if (users.contains(update.getMessage().getChatId())) {
            handleAndSaveMessageOnDrive(message, chatId);
            sendMessage(chatId, "Your answer was writen: " + message);
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {

                if (message.equals(userPassword)) {
                    LOGGER.info("Authentication success for {}.", chatId);
                    users.add(update.getMessage().getChatId());
                    sendMessage(chatId, "Authentication success.");
                    sendMessage(chatId, "Now you will get messages for answering on 3 simple question every day by scheduling.");
                    try {
                        sendMessage(chatId, googleDriveService.searchDriveFiles(null).get(0).toString());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                } else {
                    LOGGER.warn("Authentication failed for {}. Try again.", chatId);
                    sendMessage(chatId, "Authentication failed. Try again.");
                }
            }
        }
    }

    private void handleAndSaveMessageOnDrive(String message, long chatId) {
        LOGGER.info("Handling and saving message on drive.");
        try {
            String savedMessage = googleDriveService.saveMessageOnDrive(message);
            sendMessage(chatId, "Your answer was written on Google Drive: " + savedMessage);
        } catch (IOException e) {
            sendMessage(chatId, "An error occurred while saving the message. Try again.");
            sendDailyQuestions();
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void sendDailyQuestions() {
        LOGGER.info("Sending daily questions...");
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
        LOGGER.info("Sending message to: " + chatId);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
