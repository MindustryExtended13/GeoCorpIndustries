package gmod.parts;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import gmod.GeoCorp;
import gmod.entity.AbstractUnitEntity;
import gmod.util.EnumMirror;
import gmod.util.GeoGroups;
import me13.core.block.BlockAngles;
import org.jetbrains.annotations.NotNull;

public class PartEntity implements Position {
    public EnumMirror mirror = EnumMirror.NO_MIRROR;
    public Color partColor = Color.white;
    public AbstractUnitEntity entity;
    public int rotation = 0;
    public int x, y;
    public Part part;

    public boolean is2() {
        return rotation == 1 || rotation == 2;
    }

    public float drawRot() {
        return rotation * 90;
    }

    public int drawRotation() {
        return (rotation + 1) % 4;
    }

    public boolean isEditor() {
        return entity == null;
    }

    public void updateStats() {
    }

    public void update() {
    }

    public void draw() {
        part.drawer.draw(this);
    }

    public void init(AbstractUnitEntity entity) {
        this.entity = entity;
        GeoGroups.PARTS_ENTITY.register(this);
    }

    public void remove() {
        GeoGroups.PARTS_ENTITY.unregister(this);
    }

    public void drawLight() {
        part.drawer.drawLight(this);
    }

    public int classID() {
        return PartEntityRegister.id(getClass());
    }

    public void read(@NotNull Reads reads) {
        part = GeoGroups.PARTS.getByID(reads.i());
        EnumMirror[] mirrors = EnumMirror.values();
        mirror = mirrors[Mathf.clamp(reads.i(), 0, mirrors.length - 1)];
        rotation = reads.i();
        x = reads.i();
        y = reads.i();
    }

    public void write(@NotNull Writes writes) {
        writes.i(part.id);
        writes.i(GeoCorp.returnNonNull(mirror, EnumMirror.NO_MIRROR).ordinal());
        writes.i(rotation);
        writes.i(x);
        writes.i(y);
    }

    public float width() {
        return part.width * Part.PART_TILESIZE;
    }

    public float height() {
        return part.height * Part.PART_TILESIZE;
    }

    public Vec2 relativePosition() {
        return isEditor() ? new Vec2(getX() - Part.PART_TILESIZE / 2f,
                getY() - Part.PART_TILESIZE / 2f) : entity.relative(this);
    }

    @Override
    public float getX() {
        return x * Part.PART_TILESIZE + (is2() ? height() / 2 : width() / 2);
    }

    @Override
    public float getY() {
        return y * Part.PART_TILESIZE + (!is2() ? height() / 2 : width() / 2);
    }
}