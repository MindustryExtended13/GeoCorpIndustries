package gmod.content;

import gmod.GeoCorp;
import gmod.parts.Part;
import gmod.world.block.parts.EnginePart;

public class GeoParts {
    public static Part bridge, engine, fusionReactor, smallArmor, smallArmorWedge;

    public static void load() {
        bridge = new Part("bridge", GeoCorp.instance) {{
            category(GeoCategories.flight);
            height = 2;
            addMask();
        }};
        engine = new EnginePart("basic-engine", GeoCorp.instance) {{
            category(GeoCategories.flight);
            size(2);
            speed(1);
            addMask();
        }};
        fusionReactor = new Part("fusion-reactor", GeoCorp.instance) {{
            category(GeoCategories.power);
            size(3);
            addMask();
        }};
        smallArmor = new Part("small-armor", GeoCorp.instance) {{
            category(GeoCategories.defense);
        }};
        smallArmorWedge = new Part("small-armor-wedge", GeoCorp.instance) {{
            category(GeoCategories.defense);
        }};
    }
}