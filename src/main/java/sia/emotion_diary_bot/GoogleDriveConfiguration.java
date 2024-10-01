package sia.emotion_diary_bot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleDriveConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    @Value("drive.app.name")
    private String APPLICATION_NAME;


    @Value("credentials.path")
    private String CREDENTIALS_FILE_PATH;


    @Autowired
    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    @Autowired
    private NetHttpTransport netHttpTransport;

    @Autowired
    private Credential credential;


    @Bean
    public Drive drive() {
        return new Drive.Builder(
                netHttpTransport,
                jsonFactory,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public NetHttpTransport netHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

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
