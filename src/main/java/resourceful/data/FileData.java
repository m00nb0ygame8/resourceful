package resourceful.data;

import java.io.File;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class FileData implements EntryData {
    private final String fileName;
    private final FileType type;
    private final DirectoryData parent;
    private final DataSource source;

    public FileData(String fileName, FileType type, DirectoryData parent, DataSource source) {
        this.fileName = fileName;
        this.type = type;
        this.parent = parent;
        this.source = source;
        if(parent != null) parent.addChild(this);
    }

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

    public static FileSourceType getValidType(FileType type) {
        switch(type) {
            case JSON, FSH, VSH, GLSL, TXT, MCMETA -> {
                return FileSourceType.TEXT;
            }
            case PNG, OGG, ZIP -> {
                return FileSourceType.BINARY;
            }
        }
        throw new IllegalArgumentException("Unsupported file type: " + type);
    }

    public String fileName() {
        return this.fileName;
    }

    public FileType type() {
        return this.type;
    }

    public DirectoryData parent() {
        return this.parent;
    }

    public DataSource source() {
        return this.source;
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
