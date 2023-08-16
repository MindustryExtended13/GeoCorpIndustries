package gmod.content;

import gmod.world.block.units.SpaceShipConstructor;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

public class GeoBlocks {
    public static Block creativeConstructor;

    public static void load() {
        creativeConstructor = new SpaceShipConstructor("creative-constructor") {{
            requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
            size(300, 300);
            size = 3;
        }};
    }
}
