package gmod.parts;

public class PartBuildPlan {
    public Part part;
    public int x, y;
    public int rotation = 0;

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

    public boolean is2() {
        return rotation == 1 || rotation == 3;
    }

    public float drawx() {
        return x * Part.PART_TILESIZE + (is2() ? height() / 2 : width() / 2) - Part.PART_TILESIZE / 2f;
    }

    public float drawy() {
        return y * Part.PART_TILESIZE + (!is2() ? height() / 2 : width() / 2) - Part.PART_TILESIZE / 2f;
    }
}