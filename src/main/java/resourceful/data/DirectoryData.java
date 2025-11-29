package resourceful.data;

import java.util.ArrayList;
import java.util.List;

public class DirectoryData implements EntryData {

    private final String name;
    private final DirectoryData parent;
    private final List<EntryData> children;

    public DirectoryData(String name) {
        this(name, null);
    }

    public DirectoryData(String name, DirectoryData parent) {
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public DirectoryData getParent() {
        return this.parent;
    }


    public void addChild(EntryData entry) {
        this.children.add(entry);
    }

    public List<DirectoryData> getSubDirs() {
        return this.children.stream().filter(DirectoryData.class::isInstance).map(DirectoryData.class::cast).toList();
    }

    public List<FileData> getFiles() {
        return this.children.stream().filter(FileData.class::isInstance).map(FileData.class::cast).toList();
    }

    public String toPath() {
        if(parent == null || parent.getName() == null || parent.getName().isEmpty()) {
            return name;
        }
        return parent.toPath() + "/" + name;
    }

    public boolean isChildOf(DirectoryData parent) {
        DirectoryData current = this.parent;
        while (current != null) {
            if (current.equals(parent)) return true;
            current = current.getParent();
        }
        return false;
    }
}
