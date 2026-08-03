package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;

public class FixedPlacement extends PlacementModifier {
   public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockPos.CODEC.listOf().fieldOf("positions").forGetter($$0x -> $$0x.positions)).apply($$0, FixedPlacement::new)
   );
   private final List<BlockPos> positions;

   public static FixedPlacement of(BlockPos... $$0) {
      return new FixedPlacement(List.of($$0));
   }

   private FixedPlacement(List<BlockPos> $$0) {
      this.positions = $$0;
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      int $$3 = SectionPos.blockToSectionCoord($$2.getX());
      int $$4 = SectionPos.blockToSectionCoord($$2.getZ());
      boolean $$5 = false;

      for (BlockPos $$6 : this.positions) {
         if (isSameChunk($$3, $$4, $$6)) {
            $$5 = true;
            break;
         }
      }

      return !$$5 ? Stream.empty() : this.positions.stream().filter($$2x -> isSameChunk($$3, $$4, $$2x));
   }

   private static boolean isSameChunk(int $$0, int $$1, BlockPos $$2) {
      return $$0 == SectionPos.blockToSectionCoord($$2.getX()) && $$1 == SectionPos.blockToSectionCoord($$2.getZ());
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.FIXED_PLACEMENT;
   }
}
