package gmod.world.block.units;

import arc.scene.ui.layout.Table;
import gmod.GeoCorp;
import gmod.parts.PartsConstructBuilder;
import gmod.ui.PartsEditorDialog;
import gmod.world.block.GeoBlock;
import mindustry.gen.Icon;

public class SpaceShipConstructor extends GeoBlock {
    public boolean creative = false;
    public int width, height;

    public void size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public SpaceShipConstructor(String name) {
        super(name);
        configurable = true;
    }

    public class SpaceShipConstructorBuild extends GeoBuild {
        public PartsConstructBuilder builder = new PartsConstructBuilder(width, height);

        public boolean isCreative() {
            return creative;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button("Open editor", Icon.editor, () -> {
                new PartsEditorDialog(this).show();
            }).size(250, 50).padBottom(3).row();

            table.button("Create", Icon.units, () -> {
                builder.entities.each(builder::toUnit);
                GeoCorp.construct(builder.entities, team, x, y);
                builder.clear();
            }).size(250, 50);
        }
    }
}