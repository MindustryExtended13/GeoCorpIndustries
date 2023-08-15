package gmod.type;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.gen.Unit;
import mindustry.type.AmmoType;
import org.jetbrains.annotations.Contract;

public class MultiAmmoType implements AmmoType {
    public final Seq<AmmoType> types;

    public MultiAmmoType() {
        this(new Seq<>());
    }

    @Contract(pure = true)
    public MultiAmmoType(Seq<AmmoType> types) {
        this.types = types;
    }

    public MultiAmmoType(AmmoType... types) {
        this(Seq.with(types));
    }

    @Override
    public String icon() {
        return "";
    }

    @Override
    public Color color() {
        return Color.white;
    }

    @Override
    public Color barColor() {
        return Color.white;
    }

    @Override
    public void resupply(Unit unit) {
        types.each(type -> type.resupply(unit));
    }
}