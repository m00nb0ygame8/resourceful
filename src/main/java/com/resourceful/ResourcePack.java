package com.resourceful;

import com.resourceful.data.DataSource;
import com.resourceful.data.DirectoryData;
import com.resourceful.data.FileData;
import com.resourceful.modules.PackModule;
import com.resourceful.structure.MinecraftPackStructure;
import com.resourceful.structure.PackStructure;
import com.resourceful.write.BatchedFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePack {
    private final String namespace;
    private final String packName;
    private final Path outDir;
    private final List<PackModule> modules;
    private final BatchedFileWriter writer;
    private PackStructure ps;
    private MinecraftPackStructure mps;
    private FileData packMeta;
    private DirectoryData root, assets;

    //Writer Configs

    private boolean defaultShutdown = true;
    private BatchedFileWriter.WriterTimeout timeout;
    private Consumer<Boolean> result;

    private boolean zipOnOut = false;

    private int compression = Deflater.DEFAULT_COMPRESSION;


    public ResourcePack(String namespace, String packName, int writeThreads) {
        this(namespace, packName, Paths.get("").toAbsolutePath(), writeThreads);
    }

    public ResourcePack(String namespace, String packName, Path outDir, int writeThreads) {
        this.namespace = namespace;
        this.packName = packName;
        this.outDir = outDir;
        this.modules = new ArrayList<>();
        this.writer = new BatchedFileWriter(writeThreads, outDir);
    }

    public ResourcePack withModules(List<PackModule> modules) {
        this.modules.addAll(modules);
        return this;
    }

    public ResourcePack configureWriter(boolean defaultShutdown, BatchedFileWriter.WriterTimeout timeout, Consumer<Boolean> result) {
        this.defaultShutdown = defaultShutdown;
        this.timeout = timeout;
        this.result = result;
        return this;
    }

    public ResourcePack withZipOnOut(boolean zipOnOut) {
        this.zipOnOut = zipOnOut;
        return this;
    }

    public ResourcePack withCompression(int compression) {
        this.compression = compression;
        return this;
    }

    public ResourcePack generate() {
        this.root = new DirectoryData(this.packName, null);
        this.assets = new DirectoryData("assets", this.root);
        this.packMeta = FileData.createText("pack", FileData.FileType.MCMETA, this.root);

        try {
            ((DataSource.TextSource) this.packMeta.source())
                    .writer()
                    .write("{\"pack\": {\"pack_format\": %d,\"description\": \"%s\"}}".formatted(
                            ResourcefulConstants.PACK_FORMAT,
                            this.packName
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write pack.mcmeta information", e);
        }

        this.ps = new PackStructure(this.assets, this.namespace);
        this.mps = new MinecraftPackStructure(this.assets);

        this.modules.forEach(pm -> pm.generate(this.ps, this.mps));

        return this;
    }

    public void output() {
        List<FileData> files = new ArrayList<>(ps.getExtras());
        files.add(ps.getSoundsJson());
        files.add(ps.getRegionalComplianciesJson());
        files.add(this.packMeta);

        files.addAll(mps.getExtras());

        List<DirectoryData> dirs = new ArrayList<>(ps.getUsedDirectories());
        dirs.add(this.root);
        dirs.add(this.assets);
        dirs.addAll(mps.getUsedDirectories());

        if(defaultShutdown) writer.write(dirs, files);
        else writer.write(dirs, files, false, timeout, result);

        if(zipOnOut) zip(compression);
    }

    public void zip(int compression) {
        Path rootPath = outDir.resolve(packName);
        Path zipPath = outDir.resolve(packName + ".zip");
        try {
            Files.deleteIfExists(zipPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete old zip!", e);
        }
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.setLevel(compression);
            Files.walk(rootPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String entryName = rootPath.relativize(path).toString().replace("\\", "/");
                            ZipEntry entry = new ZipEntry(entryName);
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to zip file: " + path, e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("Failed to zip resource pack", e);
        }

    }

}
