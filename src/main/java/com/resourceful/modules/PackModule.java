package com.resourceful.modules;

import com.resourceful.structure.MinecraftPackStructure;
import com.resourceful.structure.PackStructure;

public interface PackModule {
    void generate(PackStructure namespaceStructure, MinecraftPackStructure minecraftStructure);
}
