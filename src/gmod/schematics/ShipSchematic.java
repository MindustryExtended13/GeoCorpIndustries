package gmod.schematics;

import arc.graphics.Color;
import arc.struct.Seq;
import gmod.parts.PartEntity;
import gmod.util.GeoGroups;
import gmod.world.block.units.SpaceShipConstructor.SpaceShipConstructorBuild;
import mindustry.io.JsonIO;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("FieldMayBeFinal")
public class ShipSchematic {
    private String name;
    private Part[] array;

    public static ShipSchematic loadSchematic(String string) {
        return JsonIO.read(ShipSchematic.class, string);
    }

    @Contract(pure = true)
    public ShipSchematic(@NotNull SpaceShipConstructorBuild build) {
        this.name = build.shipName;
        Seq<Part> parts = build.builder.entities.map((entity) -> {
            return new Part(entity.x, entity.y, entity.rotation, entity.part, entity.partColor);
        });
        array = new Part[parts.size];
        for(int i = 0; i < array.length; i++) {
            array[i] = parts.get(i);
        }
    }

    @Contract(pure = true)
    public ShipSchematic() {
    }

    public void uploadTo(@NotNull SpaceShipConstructorBuild build) {
        build.shipName = name;
        for(Part part : array) {
            PartEntity entity = part.getPart().makePart(part.x, part.y, part.rotation);
            entity.partColor = part.getColor();
            build.builder.entities.add(entity);
        }
    }

    public String asString() {
        return JsonIO.write(this);
    }

    public Part[] getArray() {
        return array;
    }

    public String getName() {
        return name;
    }

    public static class Part {
        private int x, y;
        private int rotation;
        private int partID;
        private float r, g, b;

        public Part(int x, int y, int rotation, gmod.parts.@NotNull Part part, @NotNull Color color) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.partID = part.id;
            this.r = color.r;
            this.g = color.g;
            this.b = color.b;
        }

        @Contract(pure = true)
        public Part() {
        }

        public Color getColor() {
            return new Color(r, g, b);
        }

        public gmod.parts.Part getPart() {
            return GeoGroups.PARTS.getByID(partID);
        }

        public int getRotation() {
            return rotation;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}