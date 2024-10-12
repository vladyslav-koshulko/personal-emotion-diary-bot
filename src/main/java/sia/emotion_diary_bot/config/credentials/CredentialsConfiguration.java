package sia.emotion_diary_bot.config.credentials;

import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sia.emotion_diary_bot.services.GoogleDriveService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Configuration
public class CredentialsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    @Value("${auth.service.account.key}")
    private String ACCOUNT_SECRET_JSON;

    @Bean
    public GoogleCredentials credential() {
        LOGGER.info("Creating credentials...");
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(ACCOUNT_SECRET_JSON.getBytes(StandardCharsets.UTF_8))).createScoped(SCOPES);
            LOGGER.info("Credentials loaded successfully");
            return credentials;
        } catch (IOException e) {
            LOGGER.error("Could not create credentials.", e);
            throw new RuntimeException(e);
        }
    }
}
