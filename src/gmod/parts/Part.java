package gmod.parts;

import arc.func.Prov;
import arc.struct.Seq;
import gmod.GeoCorp;
import gmod.GeoCorpException;
import gmod.graphics.DrawRegionPart;
import gmod.graphics.MultiPartDraw;
import gmod.graphics.PartDraw;
import gmod.ui.PartsEditorElement;
import gmod.util.EnumMirror;
import gmod.util.GeoGroups;
import gmod.util.IEntity;
import mindustry.mod.Mods.LoadedMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    public void category(@NotNull PartsCategory category) {
        category.parts.add(this);
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

    public boolean canPlace(@NotNull PartBuildPlan plan, @NotNull PartsEditorElement element) {
        Seq<PartEntity> ent = element.build.builder.entities;
        int _tmp_w = plan.is2() ? plan.part.height : plan.part.width;
        int _tmp_h = plan.is2() ? plan.part.width : plan.part.height;
        for(PartEntity entity : ent) {
            int w = entity.is2() ? entity.part.height : entity.part.width;
            int h = entity.is2() ? entity.part.width : entity.part.height;
            if(((plan.x + _tmp_w) > entity.x && (plan.x + _tmp_w) <= (entity.x + w)) ||
                    (plan.x >= entity.x && plan.x < (entity.x + w))) {
                if (((plan.y + _tmp_h) > entity.y && (plan.y + _tmp_h) <= (entity.y + h)) ||
                        (plan.y >= entity.y && plan.y < (entity.y + h))) {
                    return false;
                }
            }
        }
        if(ent.isEmpty()) {
            return true;
        }
        for(PartEntity entity : ent) {
            int w = entity.is2() ? entity.part.height : entity.part.width;
            int h = entity.is2() ? entity.part.width : entity.part.height;
            if(((plan.x + _tmp_w) >= entity.x && (plan.x + _tmp_w) < (entity.x + w)) ||
                    (plan.x > entity.x && plan.x <= (entity.x + w))) {
                if (((plan.y + _tmp_h) >= entity.y && (plan.y + _tmp_h) < (entity.y + h)) ||
                        (plan.y > entity.y && plan.y <= (entity.y + h))) {
                    return true;
                }
            }
        }

        return false;
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