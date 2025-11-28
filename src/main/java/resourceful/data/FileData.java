package resourceful.data;

import java.io.File;
import java.util.List;

public record FileData(String fileName, FileType type, DirectoryData parent, DataSource source) implements EntryData {

    public FileSourceType getSourceType() {
        return source instanceof DataSource.TextSource ? FileSourceType.TEXT : (source instanceof DataSource.BinarySource ? FileSourceType.BINARY : FileSourceType.FILE);
    }

    public boolean isValidFile() {
        switch (getSourceType()) {
            case TEXT -> {
                List<FileType> pass = List.of(FileType.JSON, FileType.FSH, FileType.VSH, FileType.GLSL, FileType.TXT, FileType.MCMETA);
                return pass.contains(this.type);
            }
            case BINARY -> {
                List<FileType> pass = List.of(FileType.PNG, FileType.OGG, FileType.ZIP);
                return pass.contains(this.type);
            }
            case FILE -> {
                return true;
            }
            case null, default -> {
                return false;
            }
        }
    }

    public static FileData createText(String fileName, FileType type, DirectoryData parent) {
        return new FileData(fileName, type, parent, new DataSource.TextSource());
    }

    public static FileData createBinary(String fileName, FileType type, DirectoryData parent) {
        return new FileData(fileName, type, parent, new DataSource.BinarySource());
    }

    public static FileData referenceFile(String fileName, FileType type, DirectoryData parent, File ref) {
        return new FileData(fileName, type, parent, new DataSource.FileSource(ref));
    }

    public enum FileSourceType {
        TEXT,
        BINARY,
        FILE
    }

    public enum FileType {
        JSON,
        PNG,
        FSH,
        VSH,
        GLSL,
        OGG,
        ZIP,
        TXT,
        MCMETA
    }
}
