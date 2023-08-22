package gmod.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import gmod.GeoCorp;
import gmod.entity.AbstractUnitEntity;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.blocks.environment.Floor;
import org.jetbrains.annotations.NotNull;

import static gmod.graphics.PartsGraphics.*;
import static mindustry.Vars.world;

public class DrawRegionPart extends PartDraw {
    public Color color = Color.white;
    public TextureRegion region;
    public String suffix;

    public DrawRegionPart() {
        //draw default
        this("");
        color = null;
    }

    public DrawRegionPart(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void drawShadow(PartEntity entity) {
        AbstractUnitEntity unit = entity.entity;
        float e = Mathf.clamp(unit.elevation, unit.type.shadowElevation, 1f) *
                unit.type.shadowElevationScl * (1f - unit.drownTime);
        float x = unit.x + UnitType.shadowTX * e, y = unit.y + UnitType.shadowTY * e;
        Floor floor = world.floorWorld(x, y);

        float dest = floor.canShadow ? 1f : 0f;
        //yes, this updates state in draw()... which isn't a problem, because I don't want it to be obvious anyway
        unit.shadowAlpha = unit.shadowAlpha < 0 ? dest : Mathf.approachDelta(unit.shadowAlpha, dest, 0.11f);
        Draw.color(Pal.shadow, Pal.shadow.a * unit.shadowAlpha);

        Vec2 v = entity.relativePosition();
        Draw.rect(region, v.x + UnitType.shadowTX * e, v.y + UnitType.shadowTY * e,
                entity.width(), entity.height(), rotation(entity));
        Draw.color();
    }

    @Override
    public void load(Part part) {
        region = Core.atlas.find(part.publicName() + suffix);
    }

    @Override
    public Seq<TextureRegion> icons() {
        return Seq.with(region);
    }

    @Override
    public void draw(@NotNull PartEntity entity) {
        Vec2 position = entity.relativePosition();
        Draw.color(GeoCorp.returnNonNull(color, entity.partColor));
        texture(region, position.x, position.y, entity.width(), entity.height(), rotation(entity));
        Draw.reset();
    }

    @Override
    public void drawPlan(@NotNull PartBuildPlan plan) {
        texture(region, plan.drawx(), plan.drawy(), plan.width(), plan.height(), plan.drawrot());
    }
}