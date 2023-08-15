package gmod.graphics;

import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;

public class PartDraw {
    public Seq<TextureRegion> icons() {
        return new Seq<>();
    }

    public void draw(PartEntity entity) {
    }

    public void drawLight(PartEntity entity) {
    }

    public void drawPlan(PartBuildPlan plan) {
    }

    public void load(Part part) {
    }
}