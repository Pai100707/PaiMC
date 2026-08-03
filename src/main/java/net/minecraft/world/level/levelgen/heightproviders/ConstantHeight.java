package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
   public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
   public static final MapCodec<ConstantHeight> CODEC = VerticalAnchor.CODEC.fieldOf("value").xmap(ConstantHeight::new, ConstantHeight::getValue);
   private final VerticalAnchor value;

   public static ConstantHeight of(VerticalAnchor $$0) {
      return new ConstantHeight($$0);
   }

   private ConstantHeight(VerticalAnchor $$0) {
      this.value = $$0;
   }

   public VerticalAnchor getValue() {
      return this.value;
   }

   @Override
   public int sample(RandomSource $$0, WorldGenerationContext $$1) {
      return this.value.resolveY($$1);
   }

   @Override
   public HeightProviderType<?> getType() {
      return HeightProviderType.CONSTANT;
   }

   @Override
   public String toString() {
      return this.value.toString();
   }
}
