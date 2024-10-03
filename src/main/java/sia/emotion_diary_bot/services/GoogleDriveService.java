package sia.emotion_diary_bot.services;

import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleDriveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final String DRIVE_FOLDER_NAME = "emotion-diary";
    private static final String DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";


    public void createFolderIfNotExists(Drive googleDriveManager) throws IOException {
        if (isFolderNotExists(googleDriveManager)) {
            LOGGER.info("Folder not found and will be created.");
//            TODO add logic to create folder on drive
            String folderId = createDriveFolder(googleDriveManager);
            if (folderId != null) {
                LOGGER.info("Folder created with id: " + folderId);
            } else {
                LOGGER.info("Folder was not created: " + folderId);
            }
        }
    }

    private boolean isFolderNotExists(Drive googleDriveManager) throws IOException {
        String findFolderQuery = "mimeType = '" + DRIVE_FOLDER_MIME_TYPE + "' and name = '" + DRIVE_FOLDER_NAME + "'";
        String fields = "files(id,name)";

        return googleDriveManager.files().list()
                .setQ(findFolderQuery)
                .setSpaces("drive")
                .setFields(fields)
                .execute()
                .getFiles()
                .isEmpty();
    }


    private String createDriveFolder(Drive driveManager) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(DRIVE_FOLDER_NAME);
        fileMetadata.setMimeType(DRIVE_FOLDER_MIME_TYPE);
        fileMetadata.setParents(null);
        com.google.api.services.drive.model.File folder = driveManager.files().create(fileMetadata)
                .setFields("id")
                .execute();
        return folder.getId();
    }

    private void createDriveResource() {

    }
}
