package sia.emotion_diary_bot.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DriveConfiguration {

    private final JsonFactory jsonFactory;
    private final Credential credential;
    private final NetHttpTransport netHttpTransport;

    @Value("${google.drive.app.name}")
    private String APPLICATION_NAME;

    public DriveConfiguration(JsonFactory jsonFactory, Credential credential, NetHttpTransport netHttpTransport) {
        this.jsonFactory = jsonFactory;
        this.credential = credential;
        this.netHttpTransport = netHttpTransport;
    }


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
