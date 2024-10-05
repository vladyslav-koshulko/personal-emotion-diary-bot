package sia.emotion_diary_bot.services;

public abstract class Constants {
    public static final String DRIVE_FOLDER_NAME = "emotion-diary";
    public static final String DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    public static final String FIELDS = "files(id,name)";
    public static final String SPACES = "drive";
    public static final String SEARCH_FOLDERS_QUERY = "mimeType = '" + DRIVE_FOLDER_MIME_TYPE + "' and name = '" + DRIVE_FOLDER_NAME + "'";
}
