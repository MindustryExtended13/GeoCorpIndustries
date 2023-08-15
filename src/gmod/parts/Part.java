package gmod.parts;

import arc.func.Prov;
import gmod.GeoCorp;
import gmod.GeoCorpException;
import gmod.graphics.DrawRegionPart;
import gmod.graphics.MultiPartDraw;
import gmod.graphics.PartDraw;
import gmod.util.EnumMirror;
import gmod.util.GeoGroups;
import gmod.util.IEntity;
import mindustry.mod.Mods.LoadedMod;
import org.jetbrains.annotations.Contract;

import static arc.Core.*;
import static gmod.util.EnumMirror.*;

public class Part implements IEntity {
    public static final int PART_MAX_SIZE = 32;
    public static final int PART_TILESIZE = 4;
    public final LoadedMod mod;
    public final String name;
    public final int id;

    public PartDraw drawer = new DrawRegionPart();
    public EnumMirror mirror = MIRROR_XY;
    public Prov<PartEntity> prov = null;
    public int height = 1;
    public int width = 1;

    @Contract(pure = true)
    public Part(String name, LoadedMod mod) {
        this.name = GeoCorp.requireNonNull(name);
        this.mod = GeoCorp.requireNonNull(mod);
        id = GeoGroups.PARTS.index();
        GeoGroups.PARTS.register(this);
    }

    public void size(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public void size(int size) {
        size(size, size);
    }

    public PartEntity makePart(int x, int y) {
        return makePart(x, y, 0);
    }

    public PartEntity makePart(int x, int y, int rotation) {
        return makePart(x, y, rotation, NO_MIRROR);
    }

    public PartEntity makePart(int x, int y, int rotation, EnumMirror mirror) {
        PartEntity entity = prov.get();
        entity.part = this;
        entity.x = x;
        entity.y = y;
        entity.rotation = rotation;
        entity.mirror = mirror;
        return entity;
    }

    public void addRegion(PartDraw draw) {
        if(drawer instanceof MultiPartDraw) {
            ((MultiPartDraw) drawer).parts.add(draw);
        } else {
            drawer = new MultiPartDraw(drawer, draw);
        }
    }

    public void addRegion(String suffix) {
        addRegion(new DrawRegionPart(suffix));
    }

    public void addMask() {
        addRegion("-mask");
    }

    public void drawPlan(PartBuildPlan plan) {
        drawer.drawPlan(plan);
    }

    public String publicName() {
        return mod.name + "-" + name;
    }

    public String localizedName() {
        return bundle.get("parts." + publicName() + ".name");
    }

    public String description() {
        return bundle.get("parts." + publicName() + ".description");
    }

    @Override
    public void load() {
        drawer.load(this);
    }

    @Override
    public void init() {
        mirror = GeoCorp.returnNonNull(mirror, NO_MIRROR);
        drawer = GeoCorp.returnNonNull(drawer, new PartDraw());
        prov = GeoCorp.returnNonNull(prov, PartEntity::new);
        if(width <= 0 || height <= 0 || width >= PART_MAX_SIZE || height >= PART_MAX_SIZE) {
            throw new GeoCorpException("Illegal size: " + width + "x" + height);
        }
    }
}