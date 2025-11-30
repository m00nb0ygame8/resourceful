package com.resourceful.old.write;

import org.jetbrains.annotations.Nullable;
import com.resourceful.old.data.FileData;
import com.resourceful.old.gen.PackModule;
import com.resourceful.old.gen.ResourcePackStructure;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class ResourcePackGenerator {
    private final File outFolder;
    private ResourcePackStructure structure;
    private WriterSettings settings;

    public ResourcePackGenerator(String namespace, File outFolder) {
        this.outFolder = outFolder;
        this.structure = new ResourcePackStructure()
                .createStructure(outFolder.toPath(), namespace);
    }

    public void configureStructure(PackModule... modules) {
        this.structure = this.structure.withModules(modules);
    }

    public void configureWriter(boolean defaultShutdown, @Nullable BatchedFileWriter.WriterTimeout timeout, @Nullable Consumer<Boolean> result) {
        this.settings = new WriterSettings(defaultShutdown, timeout, result);
    }

    public void generate(int threads) {
        List<FileData> data = this.structure.generate();
        BatchedFileWriter writer = new BatchedFileWriter(threads, this.outFolder.toPath());

        writer.write(
                data,
                this.settings.defaultShutdown(),
                this.settings.timeout(),
                this.settings.result()
        );
    }

    record WriterSettings(boolean defaultShutdown, BatchedFileWriter.WriterTimeout timeout, Consumer<Boolean> result) { }
}
