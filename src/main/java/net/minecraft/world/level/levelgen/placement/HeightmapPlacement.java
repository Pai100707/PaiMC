package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapPlacement extends PlacementModifier {
   public static final MapCodec<HeightmapPlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter($$0x -> $$0x.heightmap)).apply($$0, HeightmapPlacement::new)
   );
   private final Heightmap.Types heightmap;

   private HeightmapPlacement(Heightmap.Types $$0) {
      this.heightmap = $$0;
   }

   public static HeightmapPlacement onHeightmap(Heightmap.Types $$0) {
      return new HeightmapPlacement($$0);
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      int $$3 = $$2.getX();
      int $$4 = $$2.getZ();
      int $$5 = $$0.getHeight(this.heightmap, $$3, $$4);
      return $$5 > $$0.getMinY() ? Stream.of(new BlockPos($$3, $$5, $$4)) : Stream.of();
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.HEIGHTMAP;
   }
}
