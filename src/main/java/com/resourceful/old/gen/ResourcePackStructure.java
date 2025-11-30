package com.resourceful.old.gen;

import org.jetbrains.annotations.Nullable;
import com.resourceful.old.data.DirectoryData;
import com.resourceful.old.data.EntryData;
import com.resourceful.old.data.FileData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourcePackStructure {
    private ResourcePath namespace, minecraft;
    private ResourcePath nTextures, mcTextures, nModels, mcModels, nItems, mcItems, nBlockStates, mcBlockStates;

    private final List<PackModule> modules;

    public ResourcePackStructure() {
        this.modules = new ArrayList<>();
    }

    public ResourcePackStructure createStructure(Path outDir, String namespace) {
        this.namespace = new ResourcePath(
                new DirectoryData(outDir, namespace),
                null,
                null
        );
        this.minecraft = new ResourcePath(
                new DirectoryData(outDir, "minecraft"),
                null,
                null
        );

        ResourcePath[] textures = makePath("textures", this.namespace, this.minecraft),
                models = makePath("models", this.namespace, this.minecraft),
                items = makePath("items", this.namespace, this.minecraft),
                bs = makePath("blockstates", this.namespace, this.minecraft);

        this.nTextures = textures[0];
        this.mcTextures = textures[1];

        this.nModels = models[0];
        this.mcModels = models[1];

        this.nItems = items[0];
        this.mcItems = items[1];

        this.nBlockStates = bs[0];
        this.mcBlockStates = bs[1];

        return this;
    }

    private ResourcePath[] makePath(String name, ResourcePath namespace, ResourcePath minecraft) {
        DirectoryData nsDirData = ((DirectoryData) namespace.getSelf()), mcDirData = ((DirectoryData) minecraft.getSelf());
        ResourcePath nRP = new ResourcePath(new DirectoryData(nsDirData.getFullPath(), name), namespace, null);
        ResourcePath mcRP = new ResourcePath(new DirectoryData(mcDirData.getFullPath(), name), minecraft, null);
        return new ResourcePath[]{nRP, mcRP};
    }

    public ResourcePackStructure withModules(PackModule... modules) {
        this.modules.addAll(Arrays.stream(modules).toList());
        return this;
    }

    public List<FileData> generate() {
        ResourceTree ns = new ResourceTree(
                nTextures, nModels, nItems, nBlockStates
        );
        ResourceTree mc = new ResourceTree(
                mcTextures, mcModels, mcItems, mcBlockStates
        );
        GeneratedResourcePack grp = new GeneratedResourcePack(ns, mc);

        modules.forEach(m -> m.generate(grp));

        List<ResourcePath> out = getChildren(namespace);
        out.addAll(getChildren(minecraft));

        return out.stream().filter(rp -> rp.getSelf().asDirectoryData() == null).map(rp -> rp.getSelf().asFileData()).toList();
    }

    private static List<ResourcePath> getChildren(ResourcePath path) {
        List<ResourcePath> leaves = new ArrayList<>();
        collectChildren(path, leaves);
        return leaves;
    }

    private static void collectChildren(ResourcePath path, List<ResourcePath> out) {
        if (!path.hasChildren()) {
            out.add(path);
            return;
        }
        //noinspection DataFlowIssue
        for (ResourcePath child : path.getChildren()) {
            collectChildren(child, out);
        }
    }

    public record GeneratedResourcePack(ResourceTree namespace, ResourceTree minecraft) {}

    public record ResourceTree(ResourcePath textures, ResourcePath models, ResourcePath items, ResourcePath blockstates) {}

    public static class ResourcePath {
        private final EntryData self;
        private @Nullable ResourcePath parent;
        private @Nullable List<ResourcePath> children;

        public ResourcePath(EntryData self, @Nullable ResourcePath parent, @Nullable List<ResourcePath> children) {
            this.self = self;
            this.parent = parent;
            this.children = children;
        }

        public EntryData getSelf() {
            return self;
        }

        public @Nullable ResourcePath getParent() {
            return parent;
        }

        public void setParent(@Nullable ResourcePath parent) {
            this.parent = parent;
        }

        public boolean hasParent() {
            return getParent() != null;
        }

        public @Nullable List<ResourcePath> getChildren() {
            return children;
        }

        public void setChildren(@Nullable List<ResourcePath> children) {
            this.children = children;
        }

        public boolean hasChildren() {
            return getChildren() != null && !getChildren().isEmpty();
        }
    }
}
