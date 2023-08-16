package gmod.parts;

import gmod.util.EnumMirror;

public class PartBuildPlan {
    public Part part;
    public int x, y;
    public int rotation = 0;
    public EnumMirror mirror = EnumMirror.NO_MIRROR;

    public PartBuildPlan(Part part, int x, int y) {
        this.part = part;
        this.x = x;
        this.y = y;
    }

    public float drawrot() {
        return rotation * 90;
    }

    public float width() {
        return part.width * Part.PART_TILESIZE;
    }

    public float height() {
        return part.height * Part.PART_TILESIZE;
    }

    public float drawx() {
        return x * Part.PART_TILESIZE + width() / 2 - Part.PART_TILESIZE / 2f;
    }

    public float drawy() {
        return y * Part.PART_TILESIZE + height() / 2 - Part.PART_TILESIZE / 2f;
    }
}