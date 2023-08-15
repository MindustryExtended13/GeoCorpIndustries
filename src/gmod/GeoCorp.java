package gmod;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gmod.content.GeoBlocks;
import gmod.content.GeoCategories;
import gmod.content.GeoParts;
import gmod.entity.AbstractUnitEntity;
import gmod.parts.PartEntity;
import gmod.parts.PartsCategory;
import gmod.util.GeoGroups;
import gmod.world.block.GeoBlock;
import me13.core.logger.ILogger;
import me13.core.logger.LoggerFactory;
import me13.core.units.XeonUnits;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mods.LoadedMod;
import mindustry.mod.Mod;
import mindustry.type.UnitType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class GeoCorp extends Mod {
    public static final ILogger LOGGER = LoggerFactory.build(GeoCorp.class);
    public static final String MOD_NAME = "[accent]GeoCorp[] Industries";
    public static final String MOD_AUTHOR = "[black]The[red]EE[gray]145";
    public static final String MOD_ID = "gmod";
    public static LoadedMod instance;
    public static UnitType abstractc;

    @Override
    public void loadContent() {
        LOGGER.info("Starting load content");
        instance = requireNonNull(mods.getMod(GeoCorp.class));
        GeoCategories.load();
        GeoParts.load();
        GeoBlocks.load();
        LOGGER.info("Loading abstractc");
        XeonUnits.add(AbstractUnitEntity.class, AbstractUnitEntity::new);
        abstractc = new UnitType("abstractc") {{
            constructor = AbstractUnitEntity::new;
            flying = true;
        }};
    }

    @Override
    public void init() {
        LOGGER.info("Mod init");
        instance.meta.displayName = MOD_NAME;
        instance.meta.author = MOD_AUTHOR;

        on(EventType.ClientLoadEvent.class, () -> {
            GeoGroups.PARTS_CATEGORIES.init();
            GeoGroups.PARTS.init();
            GeoGroups.PARTS_CATEGORIES.load();
            GeoGroups.PARTS.load();
            GeoGroups.PARTS_CATEGORIES.freeze();
            GeoGroups.PARTS.freeze();
        });
    }

    public static AbstractUnitEntity construct(@NotNull Seq<PartEntity> entities, Team team, float x, float y) {
        AbstractUnitEntity abstractUnit = (AbstractUnitEntity) abstractc.spawn(team, x, y);
        entities.each(abstractUnit::addPart);
        return abstractUnit;
    }

    public static<T> void on(Class<T> cl, Runnable runnable) {
        Events.on(cl, ignored -> runnable.run());
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