package gmod.world.block.parts;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.util.Time;
import gmod.parts.Part;
import gmod.parts.PartEntity;
import gmod.parts.PartEntityRegister;
import me13.core.block.BlockAngles;
import mindustry.mod.Mods;

public class EnginePart extends Part {
    public float rotationBoost = 0;
    public float speedBoost = 0;
    public float engineSize = 2;
    public float engineOffset = Float.NaN;
    public Color engineColor = Color.blue;

    public EnginePart(String name, Mods.LoadedMod mod) {
        super(name, mod);
        prov = EnginePartEntity::new;
        PartEntityRegister.register(EnginePartEntity::new, EnginePartEntity.class);
    }

    public void speed(float amount) {
        rotationBoost = speedBoost = amount;
    }

    @Override
    public void init() {
        super.init();
        if(Float.isNaN(engineOffset)) {
            engineOffset = height * Part.PART_TILESIZE/1.5f;
        }
    }

    public class EnginePartEntity extends PartEntity {
        @Override
        public void updateStats() {
            if(rotation == 1 || rotation == 3) {
                entity.rotateSpeed += rotationBoost;
            } else if(rotation == 0) {
                entity.speed += speedBoost;
            } else {
                entity.speed -= speedBoost/2;
            }
        }

        @Override
        public void draw() {
            super.draw();
            if(!isEditor()) {
                Vec2 v = new Vec2(getX(), getY());
                float scl = Time.globalTime % 60 / 60;
                if(scl > 0.5f) scl = 1f - scl;
                Draw.color(engineColor);
                float s = engineSize + scl + scl;
                Point2 point2 = Geometry.d4(BlockAngles.reverse(drawRotation()));
                v.add(engineOffset * point2.x, engineOffset * point2.y);
                v = entity.relative(v);
                Fill.circle(v.x, v.y, s);
                Draw.color(Color.white);
                Fill.circle(v.x, v.y, s/2);
                Draw.reset();
            }
        }
    }
}