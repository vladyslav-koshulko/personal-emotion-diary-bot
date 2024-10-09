package sia.emotion_diary_bot.config.credentials;

import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import sia.emotion_diary_bot.services.GoogleDriveService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Configuration
public class CredentialsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    @Value("${auth.service.account.key}")
    private Resource ACCOUNT_KEY;

    @Bean
    public GoogleCredentials credential() {
        LOGGER.debug("Creating credentials...");
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(ACCOUNT_KEY.getInputStream()).createScoped(SCOPES);
            LOGGER.debug("Credentials loaded successfully");
            return credentials;
        } catch (IOException e) {
            LOGGER.error("Could not create credentials.", e);
            LOGGER.warn("Credentials: {}", ACCOUNT_KEY.toString());
            throw new RuntimeException(e);
        }
    }
}
