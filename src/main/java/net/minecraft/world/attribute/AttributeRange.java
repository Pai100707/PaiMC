package net.minecraft.world.attribute;

import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public interface AttributeRange<Value> {
   net.minecraft.world.attribute.AttributeRange<Float> UNIT_FLOAT = ofFloat(0.0F, 1.0F);
   net.minecraft.world.attribute.AttributeRange<Float> NON_NEGATIVE_FLOAT = ofFloat(0.0F, Float.POSITIVE_INFINITY);

   static <Value> net.minecraft.world.attribute.AttributeRange<Value> any() {
      return new net.minecraft.world.attribute.AttributeRange<Value>() {
         @Override
         public DataResult<Value> validate(Value $$0) {
            return DataResult.success($$0);
         }

         @Override
         public Value sanitize(Value $$0) {
            return $$0;
         }
      };
   }

   static net.minecraft.world.attribute.AttributeRange<Float> ofFloat(final float $$0, final float $$1) {
      return new net.minecraft.world.attribute.AttributeRange<Float>() {
         public DataResult<Float> validate(Float $$0x) {
            return $$0 >= $$0 && $$0 <= $$1 ? DataResult.success($$0) : DataResult.error(() -> $$0 + " is not in range [" + $$0 + "; " + $$1 + "]");
         }

         public Float sanitize(Float $$0x) {
            return $$0 >= $$0 && $$0 <= $$1 ? $$0 : Mth.clamp($$0, $$0, $$1);
         }
      };
   }

   DataResult<Value> validate(Value var1);

   Value sanitize(Value var1);
}
