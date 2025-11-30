package resourceful.write;

import org.jetbrains.annotations.Nullable;
import resourceful.data.DataSource;
import resourceful.data.DirectoryData;
import resourceful.data.FileData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public record BatchedFileWriter(int threads, Path outDir) {
    public BatchedFileWriter(int threads, Path outDir) {
        if (threads == -1) this.threads = Runtime.getRuntime().availableProcessors();
        else this.threads = threads;
        this.outDir = outDir;
    }

    public void write(List<DirectoryData> dirs, List<FileData> files) {
        this.write(dirs, files, true, null, null);
    }

    public void write(List<DirectoryData> dirs, List<FileData> files, boolean defaultShutdown, @Nullable WriterTimeout timeout, @Nullable Consumer<Boolean> result) {
        if(!defaultShutdown && timeout == null) throw new IllegalArgumentException("Cannot have a null timeout without the default shutdown!");
        ExecutorService executor = Executors.newFixedThreadPool(this.threads);
        List<FileWriteTask> writeTasks = new ArrayList<>();

        for(DirectoryData data : dirs) {
            try {
                Files.createDirectories(outDir.resolve(data.toPath()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create resourcepack directories!", e);
            }
        }

        files.forEach(data -> writeTasks.add(
                new FileWriteTask(
                        resolve(data),
                        data
                )
        ));

        for(FileWriteTask task : writeTasks) {
            executor.submit(task);
        }

        executor.shutdown();
        if(defaultShutdown) executor.close();
        else {
            try {
                if(result == null) result = (_) -> {};
                result.accept(executor.awaitTermination(timeout.timeout(), timeout.unit()));
            } catch (Exception e) {
                executor.shutdown();
            }
        }
    }

    private Path resolve(FileData data) {
        String path = data.parent().toPath() + "/" + data.fileName() + "." + data.type().name().toLowerCase();
        return this.outDir.resolve(path);
    }

    public record WriterTimeout(long timeout, TimeUnit unit) {}


    record FileWriteTask(Path filePath, FileData data) implements Runnable {
        @Override
        public void run() {
            try {
                if(!data.isValidFile()) throw new RuntimeException("Data for file \"%s\" is not valid: Invalid FileType for FileSourceType!".formatted(data.fileName() + "." + data.type().name().toLowerCase()));

                Files.deleteIfExists(filePath);
                Files.createFile(filePath);

                if (data.source() instanceof DataSource.TextSource text) {
                    try(BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                        writer.write(text.getText());
                    }
                } else if (data.source() instanceof DataSource.BinarySource bin) {
                    try(FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        fos.write(bin.getBytes());
                    }
                } else if (data.source() instanceof DataSource.FileSource(URI uri)) {
                    Files.copy(Path.of(uri), filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
