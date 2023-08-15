package gmod.content;

import gmod.GeoCorp;
import gmod.parts.PartsCategory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GeoCategories {
    public static PartsCategory ammo, crew, defense, flight, laser,
            power, production, rocket, storage, structure, utility;

    public static void load() {
        ammo = register("ammo");
        crew = register("crew");
        defense = register("defense");
        flight = register("flight");
        laser = register("laser");
        power = register("power");
        production = register("production");
        rocket = register("rocket");
        storage = register("storage");
        structure = register("structure");
        utility = register("utility");
    }

    @Contract("_ -> new")
    private static @NotNull PartsCategory register(String name) {
        return new PartsCategory(name, GeoCorp.instance);
    }
}