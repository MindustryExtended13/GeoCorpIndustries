package gmod.graphics;

import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gmod.parts.Part;
import gmod.parts.PartBuildPlan;
import gmod.parts.PartEntity;
import org.jetbrains.annotations.Contract;

public class MultiPartDraw extends PartDraw {
    public final Seq<PartDraw> parts;

    public MultiPartDraw() {
        this(new Seq<>());
    }

    public MultiPartDraw(PartDraw... parts) {
        this(Seq.with(parts));
    }

    @Contract(pure = true)
    public MultiPartDraw(Seq<PartDraw> parts) {
        this.parts = parts;
    }

    @Override
    public Seq<TextureRegion> icons() {
        Seq<TextureRegion> out = new Seq<>();
        parts.each(part -> out.addAll(part.icons()));
        return out;
    }

    @Override
    public void draw(PartEntity entity) {
        parts.each(partDraw -> partDraw.draw(entity));
    }

    @Override
    public void drawLight(PartEntity entity) {
        parts.each(partDraw -> partDraw.drawLight(entity));
    }

    @Override
    public void drawPlan(PartBuildPlan plan) {
        parts.each(partDraw -> partDraw.drawPlan(plan));
    }

    @Override
    public void load(Part part) {
        parts.each(partDraw -> partDraw.load(part));
    }
}