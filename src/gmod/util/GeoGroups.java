package gmod.util;

import arc.func.Cons;
import arc.struct.Seq;
import gmod.GeoCorpException;
import gmod.parts.Part;
import gmod.parts.PartEntity;
import gmod.parts.PartsCategory;

public class GeoGroups {
    public static final GeoCorpException exception = new GeoCorpException("Group is frozen");

    public static final GeoEntityGroup<PartsCategory> PARTS_CATEGORIES = new GeoEntityGroup<>();
    public static final GeoEntityGroup<Part> PARTS = new GeoEntityGroup<>();
    public static final GeoGroup<PartEntity> PARTS_ENTITY = new GeoGroup<>();

    public static class GeoGroup<E> {
        private final Seq<E> entities = new Seq<>();

        public void register(E entity) {
            if(entity != null) entities.add(entity);
        }

        public void unregister(E entity) {
            if(entity != null) entities.remove(entity, true);
        }

        public void each(Cons<E> cons) {
            entities.each(cons);
        }
    }

    public static class GeoEntityGroup<E extends IEntity> {
        private final Seq<E> entities = new Seq<>();
        private boolean frozen = false;

        public int index() {
            return entities.size;
        }

        public boolean isFrozen() {
            return frozen;
        }

        public void freeze() {
            frozen = true;
        }

        public void load() {
            if(frozen) throw exception;
            entities.each(IEntity::load);
        }

        public void init() {
            if(frozen) throw exception;
            entities.each(IEntity::init);
        }

        public void register(E entity) {
            if(frozen) throw exception;
            entities.add(entity);
        }

        public E getByID(int id) {
            return entities.get(id);
        }

        public void each(Cons<E> cons) {
            entities.each(cons);
        }
    }
}