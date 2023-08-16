package gmod.graphics;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.parts.PartsConstructBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PartsGraphics {
    public static ObjectMap<Integer, Float> map = new ObjectMap<>();

    public static final int EDITOR_TRANSFORM_X = 0xAB;
    public static final int EDITOR_TRANSFORM_Y = 0xBA;
    public static final int EDITOR_WIDTH = 0xCC;
    public static final int EDITOR_HEIGHT = 0xDC;
    public static final int EDITOR_SCALE = 0x56;
    public static final int EDITOR_OFFSET_X = 0x14;
    public static final int EDITOR_OFFSET_Y = 0x18;

    public static float get(int id) {
        return map.get(id);
    }

    public static void set(int id, float value) {
        map.put(id, value);
    }

    @Contract("_, _ -> new")
    public static @NotNull Point2 uiToGrid(float x, float y) {
        float s = get(EDITOR_SCALE);
        float scl = Part.PART_TILESIZE * s;
        return new Point2(
                Mathf.floor((x - (get(EDITOR_OFFSET_X) - 2) * s) / scl),
                Mathf.floor((y - (get(EDITOR_OFFSET_Y) - 2) * s) / scl)
        );
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Vec2 gridToUI(int x, int y) {
        return new Vec2(x * Part.PART_TILESIZE, y * Part.PART_TILESIZE);
    }

    public static float transformX(float x) {
        return get(EDITOR_TRANSFORM_X) + (x + get(EDITOR_OFFSET_X)) * get(EDITOR_SCALE);
    }

    public static float transformY(float y) {
        return get(EDITOR_TRANSFORM_Y) + (y + get(EDITOR_OFFSET_Y)) * get(EDITOR_SCALE);
    }

    public static float transformWidth(float value) {
        return value * get(EDITOR_SCALE);
    }

    public static float transformHeight(float value) {
        return value * get(EDITOR_SCALE);
    }

    public static void texture(TextureRegion region, float x, float y) {
        texture(region, x, y, region.width, region.height);
    }

    public static void texture(TextureRegion region, float x, float y, float width, float height) {
        Draw.rect(region, transformX(x), transformY(y), transformWidth(width), transformHeight(height));
    }

    static {
        set(EDITOR_TRANSFORM_X, 0);
        set(EDITOR_TRANSFORM_Y, 0);
        set(EDITOR_WIDTH, 0);
        set(EDITOR_HEIGHT, 0);
        set(EDITOR_OFFSET_X, 0);
        set(EDITOR_OFFSET_Y, 0);
        set(EDITOR_SCALE, 1);
    }
}