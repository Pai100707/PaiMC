/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.apache.commons.lang3.ObjectUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ID_VALUE = 254;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SyncedDataHolder entity;
    private final DataItem<?>[] itemsById;
    private boolean isDirty;

    SynchedEntityData(SyncedDataHolder $$0, DataItem<?>[] $$1) {
        this.entity = $$0;
        this.itemsById = $$1;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> $$0, EntityDataSerializer<T> $$1) {
        int $$3;
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> $$2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!$$2.equals($$0)) {
                    LOGGER.debug("defineId called for: {} from {}", new Object[]{$$0, $$2, new RuntimeException()});
                }
            }
            catch (ClassNotFoundException $$2) {
                // empty catch block
            }
        }
        if (($$3 = ID_REGISTRY.define($$0)) > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + $$3 + "! (Max is 254)");
        }
        return $$1.createAccessor($$3);
    }

    private <T> DataItem<T> getItem(EntityDataAccessor<T> $$0) {
        return this.itemsById[$$0.id()];
    }

    public <T> T get(EntityDataAccessor<T> $$0) {
        return this.getItem($$0).getValue();
    }

    public <T> void set(EntityDataAccessor<T> $$0, T $$1) {
        this.set($$0, $$1, false);
    }

    public <T> void set(EntityDataAccessor<T> $$0, T $$1, boolean $$2) {
        DataItem<T> $$3 = this.getItem($$0);
        if ($$2 || ObjectUtils.notEqual($$1, $$3.getValue())) {
            $$3.setValue($$1);
            this.entity.onSyncedDataUpdated($$0);
            $$3.setDirty(true);
            this.isDirty = true;
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public @Nullable List<DataValue<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        }
        this.isDirty = false;
        ArrayList $$0 = new ArrayList();
        for (DataItem<?> $$1 : this.itemsById) {
            if (!$$1.isDirty()) continue;
            $$1.setDirty(false);
            $$0.add($$1.value());
        }
        return $$0;
    }

    public @Nullable List<DataValue<?>> getNonDefaultValues() {
        ArrayList $$0 = null;
        for (DataItem<?> $$1 : this.itemsById) {
            if ($$1.isSetToDefault()) continue;
            if ($$0 == null) {
                $$0 = new ArrayList();
            }
            $$0.add($$1.value());
        }
        return $$0;
    }

    public void assignValues(List<DataValue<?>> $$0) {
        for (DataValue<?> $$1 : $$0) {
            DataItem<?> $$2 = this.itemsById[$$1.id];
            this.assignValue($$2, $$1);
            this.entity.onSyncedDataUpdated($$2.getAccessor());
        }
        this.entity.onSyncedDataUpdated($$0);
    }

    private <T> void assignValue(DataItem<T> $$0, DataValue<?> $$1) {
        if (!Objects.equals($$1.serializer(), $$0.accessor.serializer())) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", $$0.accessor.id(), this.entity, $$0.value, $$0.value.getClass(), $$1.value, $$1.value.getClass()));
        }
        $$0.setValue($$1.value);
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> $$0, T $$1) {
            this.accessor = $$0;
            this.initialValue = $$1;
            this.value = $$1;
        }

        public EntityDataAccessor<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T $$0) {
            this.value = $$0;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean $$0) {
            this.dirty = $$0;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public DataValue<T> value() {
            return DataValue.create(this.accessor, this.value);
        }
    }

    public static final class DataValue<T>
    extends Record {
        final int id;
        private final EntityDataSerializer<T> serializer;
        final T value;

        public DataValue(int $$0, EntityDataSerializer<T> $$1, T $$2) {
            this.id = $$0;
            this.serializer = $$1;
            this.value = $$2;
        }

        public static <T> DataValue<T> create(EntityDataAccessor<T> $$0, T $$1) {
            EntityDataSerializer<T> $$2 = $$0.serializer();
            return new DataValue<T>($$0.id(), $$2, $$2.copy($$1));
        }

        public void write(RegistryFriendlyByteBuf $$0) {
            int $$1 = EntityDataSerializers.getSerializedId(this.serializer);
            if ($$1 < 0) {
                throw new EncoderException("Unknown serializer type " + String.valueOf(this.serializer));
            }
            $$0.writeByte(this.id);
            $$0.writeVarInt($$1);
            this.serializer.codec().encode($$0, this.value);
        }

        public static DataValue<?> read(RegistryFriendlyByteBuf $$0, int $$1) {
            int $$2 = $$0.readVarInt();
            EntityDataSerializer<?> $$3 = EntityDataSerializers.getSerializer($$2);
            if ($$3 == null) {
                throw new DecoderException("Unknown serializer type " + $$2);
            }
            return DataValue.read($$0, $$1, $$3);
        }

        private static <T> DataValue<T> read(RegistryFriendlyByteBuf $$0, int $$1, EntityDataSerializer<T> $$2) {
            return new DataValue<T>($$1, $$2, $$2.codec().decode($$0));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this, $$0);
        }

        public int id() {
            return this.id;
        }

        public EntityDataSerializer<T> serializer() {
            return this.serializer;
        }

        public T value() {
            return this.value;
        }
    }

    public static class Builder {
        private final SyncedDataHolder entity;
        private final @Nullable DataItem<?>[] itemsById;

        public Builder(SyncedDataHolder $$0) {
            this.entity = $$0;
            this.itemsById = new DataItem[ID_REGISTRY.getCount($$0.getClass())];
        }

        public <T> Builder define(EntityDataAccessor<T> $$0, T $$1) {
            int $$2 = $$0.id();
            if ($$2 > this.itemsById.length) {
                throw new IllegalArgumentException("Data value id is too big with " + $$2 + "! (Max is " + this.itemsById.length + ")");
            }
            if (this.itemsById[$$2] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + $$2 + "!");
            }
            if (EntityDataSerializers.getSerializedId($$0.serializer()) < 0) {
                throw new IllegalArgumentException("Unregistered serializer " + String.valueOf($$0.serializer()) + " for " + $$2 + "!");
            }
            this.itemsById[$$0.id()] = new DataItem<T>($$0, $$1);
            return this;
        }

        public SynchedEntityData build() {
            for (int $$0 = 0; $$0 < this.itemsById.length; ++$$0) {
                if (this.itemsById[$$0] != null) continue;
                throw new IllegalStateException("Entity " + String.valueOf(this.entity.getClass()) + " has not defined synched data value " + $$0);
            }
            return new SynchedEntityData(this.entity, this.itemsById);
        }
    }
}

