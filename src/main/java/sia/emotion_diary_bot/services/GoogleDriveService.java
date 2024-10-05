package sia.emotion_diary_bot.services;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static sia.emotion_diary_bot.services.Constants.*;

@Service
public class GoogleDriveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static volatile String FILENAME;

    private static String FOLDER_ID;
    private static String SEARCH_FILES_QUERY;

    private final Drive drive;

    public GoogleDriveService(Drive drive) {
        this.drive = drive;
    }

    private synchronized static String getFileName() {
        LOGGER.debug("Getting file name, current is: {}", FILENAME);
        String newFilename = LocalDate.now().toString().replace("-", "_") + ".txt";
        if (FILENAME == null || FILENAME.isEmpty() || !FILENAME.equals(newFilename)) {
            FILENAME = newFilename;
            LOGGER.info("New filename is: {}", FILENAME);
        }
        return FILENAME;
    }

    public String saveMessageOnDrive(String message) throws IOException {
        LOGGER.info("Starting saving message on drive.");
        FOLDER_ID = getOrCreateFolder().getId();
        SEARCH_FILES_QUERY = "name = '" + getFileName() + "' and '" + FOLDER_ID + "' in parents";
        File file = getOrCreateFile();
        File written = writeMessageOnDrive(file.getId(), message);
        LOGGER.info("Finished saving message on drive.");
        if (written != null) {
            return getFileContent(file.getId());
        } else {
            NoSuchFileException noSuchFileException = new NoSuchFileException(file.toPrettyString());
            LOGGER.error("Something wet wrong when trying to write message on Drive.",noSuchFileException);
            throw noSuchFileException;
        }
    }

    private File writeMessageOnDrive(String fileId, String message) throws IOException {
        LOGGER.debug("Writing message to {}", fileId);
        File file = drive.files().get(fileId).execute();
        if (file == null || !file.getId().equals(fileId)) {
            LOGGER.error("File {} not found", fileId);
            throw new NoSuchFileException(fileId);
        }
        String currentContent = getFileContent(fileId);
        String updatedContent = currentContent + "\n\n" + System.nanoTime() + "\n" + message;
        ByteArrayContent fileContent = new ByteArrayContent("text/plain", updatedContent.getBytes(StandardCharsets.UTF_8));
        File executed = drive.files().update(fileId, null, fileContent).setFields("id, name").execute();
        LOGGER.debug("File updated: {}\nMessage: {}", fileId, updatedContent);
        return executed;
    }

    private String getFileContent(String fileId) throws IOException {
        LOGGER.debug("Getting file content by file id: {}", fileId);
        try (BufferedReader driveBufferedReader = new BufferedReader(
                new InputStreamReader(drive.files().get(fileId).executeMediaAsInputStream(), StandardCharsets.UTF_8))) {
            return driveBufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    private File getOrCreateFolder() throws IOException {
        LOGGER.debug("Creating a new or getting folder.");
        List<File> driveFiles = searchDriveFiles(SEARCH_FOLDERS_QUERY);
        if (driveFiles.isEmpty()) {
            LOGGER.info("Folder not found and will be created.");
            File folder = createDriveResource(null, DRIVE_FOLDER_NAME, DRIVE_FOLDER_MIME_TYPE);
            if (folder != null) {
                LOGGER.debug("Folder created with id: {}", folder);
            } else {
                LOGGER.error("Folder was not created: {}", DRIVE_FOLDER_NAME);
            }
            return folder;
        } else {
            return driveFiles.get(0);
        }
    }

    private File getOrCreateFile() throws IOException {
        LOGGER.debug("Creating new or getting a file.");
        List<File> driveFiles = searchDriveFiles(SEARCH_FILES_QUERY);
        if (driveFiles.isEmpty()) {
            LOGGER.info("File not found and will be created.");
            File file = createDriveResource(Collections.singletonList(FOLDER_ID), getFileName(), null);
            if (file != null) {
                LOGGER.debug("File created with id: {}", file);
            } else {
                LOGGER.error("File was not created with name: {}", getFileName());
            }
            return file;
        } else {
            return driveFiles.get(0);
        }
    }

    public List<File> searchDriveFiles(String query) throws IOException {
        LOGGER.debug("Searching for drive files with query: {}", query);
        Drive.Files.List list = drive.files().list();
        if (query != null) {
            list.setQ(query);
        }
        return list.setSpaces(SPACES)
                .setFields(FIELDS)
                .execute()
                .getFiles();

    }

    private File createDriveResource(List<String> parents, String name, String mimeType) throws IOException {
        LOGGER.debug("Creating drive resource: {}", name);
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(name);
        if (mimeType != null && !mimeType.isEmpty()) {
            fileMetadata.setMimeType(mimeType);
        }
        fileMetadata.setParents(parents); //Parent folder can be null, means that root directory
        return drive.files().create(fileMetadata)
                .setFields("id")
                .execute();
    }
}
