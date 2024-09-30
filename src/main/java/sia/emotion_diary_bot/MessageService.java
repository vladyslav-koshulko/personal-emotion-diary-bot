package sia.emotion_diary_bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageService extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.user.password}")
    private String userPassword;

    private Map<Long, String> users = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChat().getId();
        if (users.containsKey(update.getMessage().getChatId())) {
                users.putIfAbsent(chatId, message);
                sendMessage(chatId, "Questions have been written. Notifying will be on 10:00 PM");
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {

                if (message.equals(userPassword)) {
                    users.put(update.getMessage().getChatId(), null);
                    sendMessage(chatId, "Authentication success.");
                    sendMessage(chatId, "Now you will getting messages for answering on 3 simple question every day by scheduling.");
                    sendMessage(chatId, "Write your 3 questions.");
                } else {
                    sendMessage(chatId, "Authentication failed. Try again.");
                }
            }
        }
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void sendDailyQuestions() {
        users.forEach(this::sendMessage);
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
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
