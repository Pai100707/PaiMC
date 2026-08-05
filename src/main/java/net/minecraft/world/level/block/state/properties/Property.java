package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
   private final Class<T> clazz;
   private final String name;
   
   private Integer hashCode;
   private final Codec<T> codec = Codec.STRING
      .comapFlatMap(
         $$0x -> this.getValue($$0x)
            .<DataResult>map(DataResult::success)
            .orElseGet(() -> DataResult.error(() -> "Unable to read property: " + this + " with value: " + $$0x)),
         this::getName
      );
   private final Codec<Property.Value<T>> valueCodec = this.codec.xmap(this::value, Property.Value::value);

   protected Property(String $$0, Class<T> $$1) {
      this.clazz = $$1;
      this.name = $$0;
   }

   public Property.Value<T> value(T $$0) {
      return new Property.Value<>(this, $$0);
   }

   public Property.Value<T> value(StateHolder<?, ?> $$0) {
      return new Property.Value<>(this, $$0.getValue(this));
   }

   public Stream<Property.Value<T>> getAllValues() {
      return this.getPossibleValues().stream().map(this::value);
   }

   public Codec<T> codec() {
      return this.codec;
   }

   public Codec<Property.Value<T>> valueCodec() {
      return this.valueCodec;
   }

   public String getName() {
      return this.name;
   }

   public Class<T> getValueClass() {
      return this.clazz;
   }

   public abstract List<T> getPossibleValues();

   public abstract String getName(T var1);

   public abstract Optional<T> getValue(String var1);

   public abstract int getInternalIndex(T var1);

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof Property<?> $$1) ? false : this.clazz.equals($$1.clazz) && this.name.equals($$1.name);
      }
   }

   @Override
   public final int hashCode() {
      if (this.hashCode == null) {
         this.hashCode = this.generateHashCode();
      }

      return this.hashCode;
   }

   public int generateHashCode() {
      return 31 * this.clazz.hashCode() + this.name.hashCode();
   }

   public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> $$0, S $$1, U $$2) {
      DataResult<T> $$3 = this.codec.parse($$0, $$2);
      return $$3.map($$1x -> $$1.setValue(this, $$1x)).setPartial($$1);
   }

   public record Value<T extends Comparable<T>>(Property<T> property, T value) {
      public Value(Property<T> property, T value) {
         if (!property.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Value " + value + " does not belong to property " + property);
         } else {
            this.property = property;
            this.value = value;
         }
      }

      @Override
      public String toString() {
         return this.property.getName() + "=" + this.property.getName(this.value);
      }
   }
}
