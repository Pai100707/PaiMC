package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountPlacement extends RepeatingPlacement {
   public static final MapCodec<CountPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountPlacement::new, $$0 -> $$0.count);
   private final IntProvider count;

   private CountPlacement(IntProvider $$0) {
      this.count = $$0;
   }

   public static CountPlacement of(IntProvider $$0) {
      return new CountPlacement($$0);
   }

   public static CountPlacement of(int $$0) {
      return of(ConstantInt.of($$0));
   }

   @Override
   protected int count(RandomSource $$0, BlockPos $$1) {
      return this.count.sample($$0);
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT;
   }
}
