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
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final String APPLICATION_NAME = "emotion-diary-bot";
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\HP Omen 16\\IdeaProjects\\personal-emotion-diary-bot\\src\\main\\resources\\credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static Credential getCredentials(final NetHttpTransport netHttpTransport) throws IOException {
        LOGGER.info("Credentials file: ", CREDENTIALS_FILE_PATH);
        try (InputStream credentialsInputStream = Files.newInputStream(Paths.get(CREDENTIALS_FILE_PATH))) {
            if (credentialsInputStream == null) {
                LOGGER.warn("Credential input stream is null: " + credentialsInputStream);
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(credentialsInputStream));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    netHttpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("vlad");
        }
    }

    public List<String> showFile() throws IOException, GeneralSecurityException {
        NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = new Drive.Builder(netHttpTransport, JSON_FACTORY, getCredentials(netHttpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();

        FileList driveFiles = drive.files().list().execute();
        List<com.google.api.services.drive.model.File> files = driveFiles.getFiles();
        if (files == null) {
            String notFoundFiles = "No files found";
            System.out.println(notFoundFiles);
            return Collections.singletonList(notFoundFiles);
        } else {
            System.out.println("Files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
            return files.stream().map(com.google.api.services.drive.model.File::getName).toList();
        }
    }
}
