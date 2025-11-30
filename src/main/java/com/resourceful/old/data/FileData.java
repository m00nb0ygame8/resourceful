package com.resourceful.old.data;

import java.nio.file.Path;

public class FileData implements EntryData {
    private final DirectoryData parent;
    private final String name, extension;
    private String contents;

    public FileData(DirectoryData parent, String name, String extension) {
        this.parent = parent;
        this.name = name;
        this.extension = extension;
    }

    public DirectoryData parent() {
        return parent;
    }

    public String name() {
        return name;
    }

    public String extension() {
        return extension;
    }

    public String contents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Path getPath() {
        return parent.getFullPath().resolve("%s.%s".formatted(name, extension));
    }

    @Override
    public FileData asFileData() {
        return this;
    }

    @Override
    public DirectoryData asDirectoryData() {
        return null;
    }
}
