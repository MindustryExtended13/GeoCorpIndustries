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
    public void draw(PartEntity entity) {
        float rotation = entity.drawRot() + (entity.isEditor() ? 0 : entity.entity.rotation);
        Vec2 position = entity.relativePosition();
        Draw.color(GeoCorp.returnNonNull(color, entity.partColor));
        Draw.rect(region, position.x, position.y, rotation);
    }

    @Override
    public void drawPlan(PartBuildPlan plan) {
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.drawrot());
    }
}