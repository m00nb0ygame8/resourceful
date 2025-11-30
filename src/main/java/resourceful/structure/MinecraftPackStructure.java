package resourceful.structure;

import resourceful.Constants;
import resourceful.data.DataSource;
import resourceful.data.DirectoryData;
import resourceful.data.FileData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MinecraftPackStructure extends PackStructure {
    private final DirectoryData blockStates, particles, postEffects, texts;
    private final Map<VanillaFileId, FileData> cache;

    public MinecraftPackStructure(DirectoryData assets) {
        super(assets, "minecraft");

        this.blockStates = new DirectoryData(
                "blockstates",
                this.namespace
        );
        this.particles = new DirectoryData(
                "particles",
                this.namespace
        );
        this.postEffects = new DirectoryData(
                "post_effects",
                this.namespace
        );

        this.texts = new DirectoryData(
                "texts",
                this.namespace
        );

        this.cache = new HashMap<>();

        this.regionalComplianciesJson = fetchVanillaFile(this.namespace, "regional_compliancies", FileData.FileType.JSON);

        this.soundsJson = fetchVanillaFile(this.namespace, "sounds", FileData.FileType.JSON);
    }

    public DirectoryData getBlockStates() {
        return blockStates;
    }

    public DirectoryData getParticles() {
        return particles;
    }

    public DirectoryData getPostEffects() {
        return postEffects;
    }

    public DirectoryData getTexts() {
        return texts;
    }

    public void invalidateVanillaFile(DirectoryData dir, String file, FileData.FileType type) {
        this.cache.remove(new VanillaFileId(dir, file, type));
    }

    @Override
    public void write(FileData data) {
        VanillaFileId vFId = VanillaFileId.fromFileData(data);
        if(this.cache.containsKey(vFId)) super.write(data);
        else throw new IllegalArgumentException("Cannot write custom file to minecraft namespace! Must be fetched via MinecraftPackStructure.fetchVanillaFile().");
    }

    public FileData fetchVanillaFile(DirectoryData dir, String file, FileData.FileType type) {
        VanillaFileId vFId = new VanillaFileId(dir, file, type);
        if(this.cache.containsKey(vFId)) return this.cache.get(vFId);
        if(!dir.isChildOf(this.namespace)) throw new IllegalArgumentException("Cannot request file not in vanilla resource pack");
        String path = dir.toNamespaceRelativePath(this.namespace) + "/" + file + "." + type.name().toLowerCase();
        String imageUrl = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/refs/heads/%s/assets/minecraft/%s"
                .formatted(Constants.MC_VERSION, path);

        try(InputStream in = URI.create(imageUrl).toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            switch(FileData.getValidType(type)) {
                case TEXT -> {
                    String strData = new String(bytes, StandardCharsets.UTF_8);
                    FileData data = FileData.createText(
                            file,
                            type,
                            dir
                    );
                    ((DataSource.TextSource) data.source()).writer().write(strData);
                    this.cache.put(vFId, data);
                    return data;
                }
                case BINARY -> {
                    FileData data = FileData.createBinary(
                            file,
                            type,
                            dir
                    );
                    ((DataSource.BinarySource) data.source()).stream().write(bytes);
                    this.cache.put(vFId, data);
                    return data;
                }
                case FILE -> throw new UnsupportedOperationException("Cannot turn bytes into file reference!");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to open connection to fetch resource.", e);
        }
        return null;
    }

    record VanillaFileId(DirectoryData dir, String fileName, FileData.FileType type) {

        public static VanillaFileId fromFileData(FileData data) {
            DirectoryData dir = data.parent();
            String fileName = data.fileName();
            FileData.FileType type = data.type();
            return new VanillaFileId(dir, fileName, type);
        }
    }
}
