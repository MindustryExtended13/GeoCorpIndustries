package gmod.entity;

import arc.math.Angles;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import gmod.GeoCorp;
import gmod.parts.PartEntity;
import gmod.parts.PartEntityRegister;
import me13.core.units.XeonUnitEntity;
import org.jetbrains.annotations.NotNull;

public class AbstractUnitEntity extends XeonUnitEntity {
    public Seq<PartEntity> entities = new Seq<>();

    public void addPart(PartEntity entity) {
        GeoCorp.requireNonNull(entity);
        entity.init(this);
        entities.add(entity);
    }

    public Vec2 relative(@NotNull Position p) {
        float x = p.getX(), y = p.getY();
        return new Vec2(
                this.x + Angles.trnsx(rotation - 90, x, y),
                this.y + Angles.trnsy(rotation - 90, x, y)
        );
    }

    @Override
    public void update() {
        super.update();
        entities.each(PartEntity::update);
    }

    @Override
    public void draw() {
        super.draw();
        entities.each(PartEntity::draw);
        entities.each(PartEntity::drawLight);
    }

    @Override
    public void remove() {
        super.remove();
        entities.each(PartEntity::remove);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.i(entities.size);
        entities.each(entity -> {
            write.i(entity.classID());
        });
        entities.each(entity -> {
            entity.write(write);
        });
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        int s = read.i();
        for(int i = 0; i < s; i++) {
            addPart(PartEntityRegister.get(read.i()));
        }
        entities.each(entity -> {
            entity.read(read);
        });
    }
}