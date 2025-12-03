package com.resourceful.structure;

import com.resourceful.data.DataSource;
import com.resourceful.data.DirectoryData;
import com.resourceful.data.FileData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PackStructure {
    protected final DirectoryData namespace;
    protected DirectoryData atlases, equipment, font, items, lang,
            models, shaders, includeShaders, sounds, textures;
    protected DirectoryData modelsItem, modelsBlock;
    protected DirectoryData texturesItem, texturesBlock;

    protected FileData regionalComplianciesJson, soundsJson;

    protected final List<FileData> extras = new ArrayList<>();

    public PackStructure(DirectoryData assets, String namespace) {
        this.namespace = new DirectoryData(namespace, assets);
        this.atlases = new DirectoryData("atlases", this.namespace);
        this.equipment = new DirectoryData("equipment", this.namespace);
        this.font = new DirectoryData("font", this.namespace);
        this.items = new DirectoryData("items", this.namespace);
        this.lang = new DirectoryData("lang", this.namespace);
        this.models = new DirectoryData("models", this.namespace);
        this.shaders = new DirectoryData("shaders", this.namespace);
        this.includeShaders = new DirectoryData("include", this.shaders);
        this.sounds = new DirectoryData("sounds", this.namespace);
        this.textures = new DirectoryData("textures", this.namespace);

        this.modelsItem = new DirectoryData("item", this.models);
        this.modelsBlock = new DirectoryData("block", this.models);

        this.texturesItem = new DirectoryData("item", this.textures);
        this.texturesBlock = new DirectoryData("block", this.textures);

        this.regionalComplianciesJson = FileData.createText(
                "regional_compliancies",
                FileData.FileType.JSON,
                this.namespace
        );

        this.soundsJson = FileData.createText(
                "sounds",
                FileData.FileType.JSON,
                this.namespace
        );
    }


    public DirectoryData getAtlases() {
        return atlases;
    }

    public DirectoryData getEquipment() {
        return equipment;
    }

    public DirectoryData getFont() {
        return font;
    }

    public DirectoryData getItems() {
        return items;
    }

    public DirectoryData getLang() {
        return lang;
    }

    public DirectoryData getModels() {
        return models;
    }

    public DirectoryData getModelsItem() {
        return modelsItem;
    }

    public DirectoryData getModelsBlock() {
        return modelsBlock;
    }

    public DirectoryData getTexturesItem() {
        return texturesItem;
    }

    public DirectoryData getTexturesBlock() {
        return texturesBlock;
    }

    public DirectoryData getNamespace() {
        return namespace;
    }

    public DirectoryData getShaders() {
        return shaders;
    }

    public DirectoryData getIncludeShaders() {
        return includeShaders;
    }

    public DirectoryData getSounds() {
        return sounds;
    }

    public DirectoryData getTextures() {
        return textures;
    }

    public FileData getRegionalComplianciesJson() {
        return regionalComplianciesJson;
    }

    public FileData getSoundsJson() {
        return soundsJson;
    }

    public List<DirectoryData> getUsedDirectories() {
        HashSet<DirectoryData> req = new HashSet<>();
        this.extras.forEach(fd -> {
            DirectoryData parent = fd.parent();
            if (parent != null) {
                req.add(parent);
                req.addAll(parent.getParents());
            }
        });
        if(!((DataSource.TextSource)this.getSoundsJson().source()).getText().isEmpty()) req.add(this.getSoundsJson().parent());
        if(!((DataSource.TextSource)this.getRegionalComplianciesJson().source()).getText().isEmpty()) req.add(this.getRegionalComplianciesJson().parent());
        return List.copyOf(req);
    }

    public List<FileData> getExtras() {
        return this.extras;
    }

    public void write(FileData data) {
        this.extras.add(data);
    }

}


/*

RP STRUCTURE:

atlases: (Both)
    (MC's Atlases)
    armor_trims.json
    banner_patterns.json
    beds.json
    blocks.json
    chests.json
    decorated_pot.json
    gui.json
    map_decorations.json
    mob_effects.json
    paintings.json
    particles.json
    shield_patterns.json
    shulker_boxes.json
    signs.json

blockstates: (MC ONLY)

    block_name.json

equipment: (Both)

    (MC's jsons)
    armadillo_scute.json
    black_carpet.json
    blue_carpet.json
    brown_carpet.json
    chainmail.json
    cyan_carpet.json
    diamond.json
    elytra.json
    gold.json
    gray_carpet.json
    green_carpet.json
    iron.json
    leather.json
    light_blue_carpet.json
    light_gray_carpet.json
    lime_carpet.json
    magenta_carpet.json
    netherite.json
    orange_carpet.json
    pink_carpet.json
    purple_carpet.json
    red_carpet.json
    trader_llama.json
    turtle_scute.json
    white_carpet.json
    yellow_carpet.json

font: (Both)

    (MC's structure)
    include/[default.json, space.json, unifont.json]
    alt.json
    default.json
    illageralt.json
    unifont.zip
    unifont_jp.zip
    uniform.json

items: (Both)

    (MC's jsons)
    item_name.json

lang: (Both)

    (MC's jsons)
    lang_code.json

models: (Both, DEPRECATED BUT SUPPORTED)

    block: (Either)

        (MC's jsons)
        block_name.json

    item:

        (MC's jsons)
        item_name.json

particles: (MC ONLY)

    (MC's jsons)
    particle_name.json

post_effects: (MC ONLY)

    (MC's jsons)
    blur.json
    creeper.json
    entity_outline.json
    invert.json
    spider.json
    transparency.json

shaders: (Both)

    core:
        (MC's structure)
        shader_name.[fsh, vsh, json]

    include:
        (MC's structure)
        shader_name.[glsl, fsh, vsh] (Not sure on the last 2)

    post:
        (MC's structure)
        shader_name.[fsh, vsh, json]

sounds: (Both)

    (MC's structure)
    sound_type: (Goes down a few)
        sound_name.ogg

texts: (MC ONLY)

    (MC's structure)
    credits.json
    end.txt
    postcredits.txt
    splashes.txt

textures: (Both)

    (MC's structure)

    block:

        block_name.png
        block_name.png.mcmeta

    colormap:

        foliage.png
        grass.png

    effect:

        dither.png

    entity:

        entity_[name/feature].png

        entity_type:
            other_subdirs WITH
            other_textures.png

    environment:

        clouds.png
        end_sky.png
        moon_phases.png
        rain.png
        snow.png
        sun.png

    font:

        accented.png
        ascii.png
        ascii_sga.png
        asciillager.png
        nonlatin_european.png

    gui:

        textures.png

        texture: (Other things blah blah blah)
            other_textures.png

    item:

        item_name.png
        item_name.png.mcmeta

    map:

        map_background.png
        map_background_checkerboard.png

        decorations:

            icon.png

    misc:

        texture_name.png
        texture_name.png.mcmeta

    mob_effect:

        effect_name.png

    painting:

        painting_name.png

    particle:

        particle_name[_number].png
        particle_name.png.mcmeta

    trims:

        color_palettes:

            resource_name[_darker].png
            trim_palette.png

        entity:

            humanoid:

                trim_name.png

            humanoid_leggings:

                trim_name.png

        items:

            equipment_type_trim.png


gpu_warnlist.json (MC Only)
regional_compliancies.json (Both)
sounds.json (Both)

 */
