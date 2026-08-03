package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;

public record ConstantValue(float value) implements NumberProvider {
   public static final MapCodec<ConstantValue> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.FLOAT.fieldOf("value").forGetter(ConstantValue::value)).apply($$0, ConstantValue::new)
   );
   public static final Codec<ConstantValue> INLINE_CODEC = Codec.FLOAT.xmap(ConstantValue::new, ConstantValue::value);

   @Override
   public LootNumberProviderType getType() {
      return NumberProviders.CONSTANT;
   }

   @Override
   public float getFloat(LootContext $$0) {
      return this.value;
   }

   public static ConstantValue exactly(float $$0) {
      return new ConstantValue($$0);
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return $$0 != null && this.getClass() == $$0.getClass() ? Float.compare(((ConstantValue)$$0).value, this.value) == 0 : false;
      }
   }

   @Override
   public int hashCode() {
      return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
   }
}
