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
import java.util.Arrays;

public class MinecraftPackStructure extends PackStructure {
    private final DirectoryData blockStates, particles, postEffects, texts;

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

        this.regionalComplianciesJson = FileData.referenceFile(
                "regional_compliancies",
                FileData.FileType.JSON,
                this.namespace,
                new File("TBD") //Replace with actual file from version
        );

        this.soundsJson = FileData.referenceFile(
                "sounds",
                FileData.FileType.JSON,
                this.namespace,
                new File("TBD") //Replace with actual file from version
        );
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

    public FileData fetchVanillaFile(DirectoryData dir, String file, FileData.FileType type) {
        if(!dir.isChildOf(this.namespace)) throw new IllegalArgumentException("Cannot request file not in vanilla resource pack");
        String path = dir.toPath() + "/" + file + "." + type.name().toLowerCase();
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
                    return data;
                }
                case BINARY -> {
                    FileData data = FileData.createBinary(
                            file,
                            type,
                            dir
                    );
                    ((DataSource.BinarySource) data.source()).stream().write(bytes);
                    return data;
                }
                case FILE -> throw new UnsupportedOperationException("Cannot turn bytes into file reference!");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to open connection to fetch resource.", e);
        }
        return null;
    }
}
