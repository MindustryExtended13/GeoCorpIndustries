package gmod.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;
import org.jetbrains.annotations.NotNull;

import static gmod.graphics.PartsGraphics.*;

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