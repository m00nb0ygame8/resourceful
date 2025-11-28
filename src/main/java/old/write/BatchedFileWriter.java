package old.write;

import org.jetbrains.annotations.Nullable;
import old.data.FileData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public void write(List<FileData> files) {
        this.write(files, true, null, null);
    }

    public void write(List<FileData> files, boolean defaultShutdown, @Nullable WriterTimeout timeout, @Nullable Consumer<Boolean> result) {
        if(!defaultShutdown && timeout == null) throw new IllegalArgumentException("Cannot have a null timeout without the default shutdown!");
        ExecutorService executor = Executors.newFixedThreadPool(this.threads);
        List<FileWriteTask> writeTasks = new ArrayList<>();

        files.forEach(data -> writeTasks.add(
                new FileWriteTask(
                        data.getPath(),
                        data.contents()
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

    public record WriterTimeout(long timeout, TimeUnit unit) {}


    record FileWriteTask(Path filePath, String content) implements Runnable {
        @Override
        public void run() {
            File parentDir = filePath().getParent().toFile();
            if(!parentDir.exists()) {
                if(!parentDir.mkdirs()) throw new RuntimeException("Failed to make required directories.");
            }
            try {
                File f = new File(filePath.toString());
                if(!f.exists()) {
                    if(!f.createNewFile()) throw new RuntimeException("File already exists!");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create new file.", e);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write(content);
                writer.flush();
            } catch (Exception e) {
                throw new RuntimeException("Failed to write contents to file in path \"%s\"".formatted(filePath.toString()), e);
            }
        }
    }
}
