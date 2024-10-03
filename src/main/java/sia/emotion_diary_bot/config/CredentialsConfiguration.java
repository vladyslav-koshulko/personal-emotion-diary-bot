package sia.emotion_diary_bot.config;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sia.emotion_diary_bot.services.GoogleDriveService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Configuration
public class CredentialsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Value("${credentials.path}")
    private String CREDENTIALS_FILE_PATH;


    @Autowired
    private JsonFactory jsonFactory;

    @Bean
    public Credential credential(final NetHttpTransport netHttpTransport) throws IOException {
        LOGGER.info("Credentials file: " + CREDENTIALS_FILE_PATH);
        try (InputStream credentialsInputStream = Files.newInputStream(Paths.get(CREDENTIALS_FILE_PATH))) {
            if (credentialsInputStream == null) {
                LOGGER.warn("Credential input stream is null: " + credentialsInputStream);
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(credentialsInputStream));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    netHttpTransport, jsonFactory, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("vlad");
        }
    }
}
