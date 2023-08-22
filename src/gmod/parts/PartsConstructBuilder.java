package gmod.parts;

import arc.struct.Seq;
import gmod.util.EnumMirror;
import org.jetbrains.annotations.NotNull;

public class PartsConstructBuilder {
    public final Seq<PartEntity> entities = new Seq<>();
    public final int w, h;

    public PartsConstructBuilder(int w, int h){
        this.w = w;
        this.h = h;
    }

    public void set(Part part, int x, int y) {
        set(part, x, y, 0);
    }

    public void set(Part part, int x, int y, int rotation) {
        set(part, x, y, rotation, EnumMirror.NO_MIRROR);
    }

    public void set(@NotNull Part part, int x, int y, int rotation, EnumMirror mirror) {
        entities.add(part.makePart(x, y, rotation, mirror));
    }

    public void clear() {
        entities.clear();
    }
}