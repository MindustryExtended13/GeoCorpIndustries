package gmod.util;

import org.jetbrains.annotations.Contract;

public enum EnumMirror {
    NO_MIRROR,
    MIRROR_X,
    MIRROR_Y,
    MIRROR_XY;

    @Contract(pure = true)
    public static boolean mirrorX(EnumMirror mirror) {
        return mirror == MIRROR_X || mirror == MIRROR_XY;
    }

    @Contract(pure = true)
    public static boolean mirrorY(EnumMirror mirror) {
        return mirror == MIRROR_Y || mirror == MIRROR_XY;
    }
}