package gmod;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import me13.core.logger.ILogger;
import me13.core.logger.LoggerFactory;
import mindustry.mod.Mods.LoadedMod;
import mindustry.mod.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class GeoCorp extends Mod {
    public static final ILogger LOGGER = LoggerFactory.build(GeoCorp.class);
    public static final String MOD_NAME = "[accent]GeoCorp[] Industries";
    public static final String MOD_AUTHOR = "[black]The[red]EE[gray]145";
    public static final String MOD_ID = "gmod";
    public static LoadedMod instance;

    public void initInstance() {
        instance = mods.getMod(GeoCorp.class);
    }

    @Override
    public void loadContent() {
        initInstance();
    }

    @Override
    public void init() {
        if(instance == null) {
            LOGGER.err("Unknown reason why but [accent]instance[] is null, re-init...");
            initInstance();
        }

        requireNonNull(instance);
        instance.meta.displayName = MOD_NAME;
        instance.meta.author = MOD_AUTHOR;
    }

    public static String bundle(String name) {
        return Core.bundle.get(MOD_ID + "." + name);
    }

    public static TextureRegion asset(String region) {
        return Core.atlas.find(MOD_ID + "-" + region);
    }

    @Contract(value = "null, _ -> param2; !null, _ -> param1", pure = true)
    public static<T> @NotNull T returnNonNull(T value, @NotNull T def) {
        return value == null ? requireNonNull(def) : value;
    }

    @Contract(value = "null, null -> true; null, !null -> false", pure = true)
    public static boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    @Contract(value = "null -> false; !null -> true", pure = true)
    public static boolean nonNull(Object object) {
        return object != null;
    }

    @Contract("null -> fail; _ -> param1")
    public static<T> @NotNull T requireNonNull(T object) {
        if(object == null) {
            throw new GeoCorpException("object is null");
        }
        return object;
    }
}