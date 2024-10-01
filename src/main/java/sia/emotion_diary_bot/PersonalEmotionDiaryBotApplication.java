package sia.emotion_diary_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalEmotionDiaryBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalEmotionDiaryBotApplication.class, args);
    }

}
