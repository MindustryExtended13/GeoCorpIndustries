package gmod.parts;

import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gmod.util.GeoGroups;
import gmod.util.IEntity;
import mindustry.mod.Mods.LoadedMod;
import org.jetbrains.annotations.NotNull;

import static arc.Core.*;

public class PartsCategory implements IEntity {
    public final Seq<Part> parts = new Seq<>();
    public final String modName;
    public final String name;
    public TextureRegion icon;

    public PartsCategory(String name, @NotNull LoadedMod mod) {
        this.name = name;
        this.modName = mod.name;
        GeoGroups.PARTS_CATEGORIES.register(this);
    }

    public String localizedName() {
        return bundle.get("category." + publicName() + ".name");
    }

    public String publicName() {
        return modName + "-" + name;
    }

    @Override
    public void load() {
        icon = atlas.find(modName + "-category-" + name);
    }
}