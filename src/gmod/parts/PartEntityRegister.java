package gmod.parts;

import arc.func.Prov;
import arc.struct.ObjectMap;
import gmod.GeoCorp;

public class PartEntityRegister {
    private static final ObjectMap<Class<? extends PartEntity>, Integer> idMap = new ObjectMap<>();
    private static final ObjectMap<Integer, Prov<PartEntity>> map = new ObjectMap<>();
    public static int id = 0;

    public static void register(Prov<PartEntity> partEntityProv, Class<? extends PartEntity> cl) {
        map.put(id, GeoCorp.returnNonNull(partEntityProv, PartEntity::new));
        idMap.put(GeoCorp.requireNonNull(cl), id);
        id++;
    }

    public static int id(Class<? extends PartEntity> cl) {
        return GeoCorp.returnNonNull(idMap.get(cl), -1);
    }

    public static PartEntity get(int id) {
        return GeoCorp.returnNonNull(map.get(id), PartEntity::new).get();
    }
}