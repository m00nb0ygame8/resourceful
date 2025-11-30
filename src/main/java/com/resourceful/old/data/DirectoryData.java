package com.resourceful.old.data;

import java.nio.file.Path;

public record DirectoryData(Path parentPath, String dirName) implements EntryData {
    public Path getFullPath() {
        return parentPath.resolve(dirName);
    }

    @Override
    public FileData asFileData() {
        return null;
    }

    @Override
    public DirectoryData asDirectoryData() {
        return this;
    }
}
