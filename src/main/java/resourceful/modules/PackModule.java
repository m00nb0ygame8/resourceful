package resourceful.modules;

import resourceful.structure.MinecraftPackStructure;
import resourceful.structure.PackStructure;

public interface PackModule {
    void generate(PackStructure namespaceStructure, MinecraftPackStructure minecraftStructure);
}
