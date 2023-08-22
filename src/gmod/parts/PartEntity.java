package gmod.parts;

import arc.graphics.Color;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import gmod.entity.AbstractUnitEntity;
import gmod.util.GeoGroups;
import org.jetbrains.annotations.NotNull;

public class PartEntity implements Position {
    public Color partColor = Color.white;
    public AbstractUnitEntity entity;
    public int rotation = 0;
    public int x, y;
    public Part part;

    public boolean is2() {
        return rotation == 1 || rotation == 3;
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

    public void drawShadow() {
        part.drawer.drawShadow(this);
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
        rotation = reads.i();
        x = reads.i();
        y = reads.i();
    }

    public void write(@NotNull Writes writes) {
        writes.i(part.id);
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