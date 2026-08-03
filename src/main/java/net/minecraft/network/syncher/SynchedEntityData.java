package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SynchedEntityData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_ID_VALUE = 254;
   static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
   private final SyncedDataHolder entity;
   private final SynchedEntityData.DataItem<?>[] itemsById;
   private boolean isDirty;

   SynchedEntityData(SyncedDataHolder $$0, SynchedEntityData.DataItem<?>[] $$1) {
      this.entity = $$0;
      this.itemsById = $$1;
   }

   public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> $$0, EntityDataSerializer<T> $$1) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class<?> $$2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!$$2.equals($$0)) {
               LOGGER.debug("defineId called for: {} from {}", new Object[]{$$0, $$2, new RuntimeException()});
            }
         } catch (ClassNotFoundException var3) {
         }
      }

      int $$3 = ID_REGISTRY.define($$0);
      if ($$3 > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + $$3 + "! (Max is 254)");
      } else {
         return $$1.createAccessor($$3);
      }
   }

   private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> $$0) {
      return (SynchedEntityData.DataItem<T>)this.itemsById[$$0.id()];
   }

   public <T> T get(EntityDataAccessor<T> $$0) {
      return this.getItem($$0).getValue();
   }

   public <T> void set(EntityDataAccessor<T> $$0, T $$1) {
      this.set($$0, $$1, false);
   }

   public <T> void set(EntityDataAccessor<T> $$0, T $$1, boolean $$2) {
      SynchedEntityData.DataItem<T> $$3 = this.getItem($$0);
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

   @Nullable
   public List<SynchedEntityData.DataValue<?>> packDirty() {
      if (!this.isDirty) {
         return null;
      } else {
         this.isDirty = false;
         List<SynchedEntityData.DataValue<?>> $$0 = new ArrayList<>();

         for (SynchedEntityData.DataItem<?> $$1 : this.itemsById) {
            if ($$1.isDirty()) {
               $$1.setDirty(false);
               $$0.add($$1.value());
            }
         }

         return $$0;
      }
   }

   @Nullable
   public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
      List<SynchedEntityData.DataValue<?>> $$0 = null;

      for (SynchedEntityData.DataItem<?> $$1 : this.itemsById) {
         if (!$$1.isSetToDefault()) {
            if ($$0 == null) {
               $$0 = new ArrayList<>();
            }

            $$0.add($$1.value());
         }
      }

      return $$0;
   }

   public void assignValues(List<SynchedEntityData.DataValue<?>> $$0) {
      for (SynchedEntityData.DataValue<?> $$1 : $$0) {
         SynchedEntityData.DataItem<?> $$2 = this.itemsById[$$1.id];
         this.assignValue($$2, $$1);
         this.entity.onSyncedDataUpdated($$2.getAccessor());
      }

      this.entity.onSyncedDataUpdated($$0);
   }

   private <T> void assignValue(SynchedEntityData.DataItem<T> $$0, SynchedEntityData.DataValue<?> $$1) {
      if (!Objects.equals($$1.serializer(), $$0.accessor.serializer())) {
         throw new IllegalStateException(
            String.format(
               Locale.ROOT,
               "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
               $$0.accessor.id(),
               this.entity,
               $$0.value,
               $$0.value.getClass(),
               $$1.value,
               $$1.value.getClass()
            )
         );
      } else {
         $$0.setValue((T)$$1.value);
      }
   }

   public static class Builder {
      private final SyncedDataHolder entity;
      private final SynchedEntityData.DataItem<?>[] itemsById;

      public Builder(SyncedDataHolder $$0) {
         this.entity = $$0;
         this.itemsById = new SynchedEntityData.DataItem[SynchedEntityData.ID_REGISTRY.getCount($$0.getClass())];
      }

      public <T> SynchedEntityData.Builder define(EntityDataAccessor<T> $$0, T $$1) {
         int $$2 = $$0.id();
         if ($$2 > this.itemsById.length) {
            throw new IllegalArgumentException("Data value id is too big with " + $$2 + "! (Max is " + this.itemsById.length + ")");
         } else if (this.itemsById[$$2] != null) {
            throw new IllegalArgumentException("Duplicate id value for " + $$2 + "!");
         } else if (EntityDataSerializers.getSerializedId($$0.serializer()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + $$0.serializer() + " for " + $$2 + "!");
         } else {
            this.itemsById[$$0.id()] = new SynchedEntityData.DataItem<>($$0, $$1);
            return this;
         }
      }

      public SynchedEntityData build() {
         for (int $$0 = 0; $$0 < this.itemsById.length; $$0++) {
            if (this.itemsById[$$0] == null) {
               throw new IllegalStateException("Entity " + this.entity.getClass() + " has not defined synched data value " + $$0);
            }
         }

         return new SynchedEntityData(this.entity, this.itemsById);
      }
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

      public SynchedEntityData.DataValue<T> value() {
         return SynchedEntityData.DataValue.create(this.accessor, this.value);
      }
   }

   public record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {

      public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> $$0, T $$1) {
         EntityDataSerializer<T> $$2 = $$0.serializer();
         return new SynchedEntityData.DataValue<>($$0.id(), $$2, $$2.copy($$1));
      }

      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         int $$1 = EntityDataSerializers.getSerializedId(this.serializer);
         if ($$1 < 0) {
            throw new EncoderException("Unknown serializer type " + this.serializer);
         } else {
            $$0.writeByte(this.id);
            $$0.writeVarInt($$1);
            this.serializer.codec().encode($$0, this.value);
         }
      }

      public static SynchedEntityData.DataValue<?> read(net.minecraft.network.RegistryFriendlyByteBuf $$0, int $$1) {
         int $$2 = $$0.readVarInt();
         EntityDataSerializer<?> $$3 = EntityDataSerializers.getSerializer($$2);
         if ($$3 == null) {
            throw new DecoderException("Unknown serializer type " + $$2);
         } else {
            return read($$0, $$1, $$3);
         }
      }

      private static <T> SynchedEntityData.DataValue<T> read(net.minecraft.network.RegistryFriendlyByteBuf $$0, int $$1, EntityDataSerializer<T> $$2) {
         return new SynchedEntityData.DataValue<>($$1, $$2, $$2.codec().decode($$0));
      }
   }
}
