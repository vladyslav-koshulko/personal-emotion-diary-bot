package sia.emotion_diary_bot.config.credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sia.emotion_diary_bot.services.GoogleDriveService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Configuration
public class CredentialsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final JsonFactory jsonFactory;
    @Value("${credentials.path}")
    private String CREDENTIALS_FILE_PATH;
    @Value("${user.email}")
    private String USER_EMAIL;
    @Value("${server.port}")
    private int SERVER_PORT;
    @Value("${auth.receiver.host}")
    private String RECEIVER_HOST;

    public CredentialsConfiguration(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Bean
    public Credential credential(final NetHttpTransport netHttpTransport) throws IOException {
        LOGGER.debug("Credentials file: {}", CREDENTIALS_FILE_PATH);
        LOGGER.debug("Creating credentials...");
        try (InputStream credentialsInputStream = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH)) {
            if (credentialsInputStream == null) {
                LOGGER.error("Credentials file {} not found", CREDENTIALS_FILE_PATH);
                throw new FileNotFoundException(CREDENTIALS_FILE_PATH);
            }
            LOGGER.info("Credentials loaded.");
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(credentialsInputStream));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    netHttpTransport, jsonFactory, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(RECEIVER_HOST).setPort(SERVER_PORT).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER_EMAIL);
        }
    }
}
