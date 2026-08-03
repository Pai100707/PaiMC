package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttribute<Value> {
   private final net.minecraft.world.attribute.AttributeType<Value> type;
   private final Value defaultValue;
   private final net.minecraft.world.attribute.AttributeRange<Value> valueRange;
   private final boolean isSyncable;
   private final boolean isPositional;
   private final boolean isSpatiallyInterpolated;

   EnvironmentAttribute(
      net.minecraft.world.attribute.AttributeType<Value> $$0,
      Value $$1,
      net.minecraft.world.attribute.AttributeRange<Value> $$2,
      boolean $$3,
      boolean $$4,
      boolean $$5
   ) {
      this.type = $$0;
      this.defaultValue = $$1;
      this.valueRange = $$2;
      this.isSyncable = $$3;
      this.isPositional = $$4;
      this.isSpatiallyInterpolated = $$5;
   }

   public static <Value> net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> builder(net.minecraft.world.attribute.AttributeType<Value> $$0) {
      return new net.minecraft.world.attribute.EnvironmentAttribute.Builder<>($$0);
   }

   public net.minecraft.world.attribute.AttributeType<Value> type() {
      return this.type;
   }

   public Value defaultValue() {
      return this.defaultValue;
   }

   public Codec<Value> valueCodec() {
      return this.type.valueCodec().validate(this.valueRange::validate);
   }

   public Value sanitizeValue(Value $$0) {
      return this.valueRange.sanitize($$0);
   }

   public boolean isSyncable() {
      return this.isSyncable;
   }

   public boolean isPositional() {
      return this.isPositional;
   }

   public boolean isSpatiallyInterpolated() {
      return this.isSpatiallyInterpolated;
   }

   @Override
   public String toString() {
      return Util.getRegisteredName(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE, this);
   }

   public static class Builder<Value> {
      private final net.minecraft.world.attribute.AttributeType<Value> type;
      @Nullable
      private Value defaultValue;
      private net.minecraft.world.attribute.AttributeRange<Value> valueRange = net.minecraft.world.attribute.AttributeRange.any();
      private boolean isSyncable = false;
      private boolean isPositional = true;
      private boolean isSpatiallyInterpolated = false;

      public Builder(net.minecraft.world.attribute.AttributeType<Value> $$0) {
         this.type = $$0;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> defaultValue(Value $$0) {
         this.defaultValue = $$0;
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> valueRange(net.minecraft.world.attribute.AttributeRange<Value> $$0) {
         this.valueRange = $$0;
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> syncable() {
         this.isSyncable = true;
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> notPositional() {
         this.isPositional = false;
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> spatiallyInterpolated() {
         this.isSpatiallyInterpolated = true;
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttribute<Value> build() {
         return new net.minecraft.world.attribute.EnvironmentAttribute<>(
            this.type,
            Objects.requireNonNull(this.defaultValue, "Missing default value"),
            this.valueRange,
            this.isSyncable,
            this.isPositional,
            this.isSpatiallyInterpolated
         );
      }
   }
}
