package resourceful.data;

import java.util.ArrayList;
import java.util.Collections;
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
        if(parent != null) this.parent.addChild(this);
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

    public List<DirectoryData> getParents() {
        List<DirectoryData> parents = new ArrayList<>();
        DirectoryData cur = this.parent;
        while(cur != null) {
            parents.add(cur);
            cur = cur.getParent();
        }
        return parents;
    }

    public String toNamespaceRelativePath(DirectoryData namespace) {
        DirectoryData cur = this;
        List<String> parts = new ArrayList<>();
        while (cur != null && cur != namespace.getParent()) {
            parts.add(cur.getName());
            cur = cur.getParent();
        }
        Collections.reverse(parts);
        return String.join("/", parts);
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
