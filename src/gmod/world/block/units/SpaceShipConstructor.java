package gmod.world.block.units;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import gmod.GeoCorp;
import gmod.content.GeoParts;
import gmod.world.block.GeoBlock;
import mindustry.gen.Icon;

public class SpaceShipConstructor extends GeoBlock {
    public SpaceShipConstructor(String name) {
        super(name);
        configurable = true;
    }

    public class SpaceShipConstructorBuild extends GeoBuild {
        @Override
        public void buildConfiguration(Table table) {
            table.button("Create", Icon.units, () -> {
                GeoCorp.construct(Seq.with(
                        GeoParts.fusionReactor.makePart(-1, 0),
                        GeoParts.bridge.makePart(-2, 0),
                        GeoParts.engine.makePart(-2, -2),
                        GeoParts.engine.makePart(0, -2)
                ), team, x, y);
            }).size(250, 50);
        }
    }
}