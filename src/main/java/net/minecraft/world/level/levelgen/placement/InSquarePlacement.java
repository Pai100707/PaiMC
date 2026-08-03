package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class InSquarePlacement extends PlacementModifier {
   private static final InSquarePlacement INSTANCE = new InSquarePlacement();
   public static final MapCodec<InSquarePlacement> CODEC = MapCodec.unit(() -> INSTANCE);

   public static InSquarePlacement spread() {
      return INSTANCE;
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      int $$3 = $$1.nextInt(16) + $$2.getX();
      int $$4 = $$1.nextInt(16) + $$2.getZ();
      return Stream.of(new BlockPos($$3, $$2.getY(), $$4));
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.IN_SQUARE;
   }
}
