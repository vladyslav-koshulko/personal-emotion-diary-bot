package sia.emotion_diary_bot.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DriveConfiguration {


    @Value("${google.drive.app.name}")
    private String APPLICATION_NAME;


    @Autowired
    private JsonFactory jsonFactory;

    @Autowired
    private Credential credential;


    @Autowired
    private NetHttpTransport netHttpTransport;


    @Bean
    public Drive drive() {
        return new Drive.Builder(
                netHttpTransport,
                jsonFactory,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


}
