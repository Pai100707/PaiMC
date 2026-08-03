package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class HeightRangePlacement extends PlacementModifier {
   public static final MapCodec<HeightRangePlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(HeightProvider.CODEC.fieldOf("height").forGetter($$0x -> $$0x.height)).apply($$0, HeightRangePlacement::new)
   );
   private final HeightProvider height;

   private HeightRangePlacement(HeightProvider $$0) {
      this.height = $$0;
   }

   public static HeightRangePlacement of(HeightProvider $$0) {
      return new HeightRangePlacement($$0);
   }

   public static HeightRangePlacement uniform(VerticalAnchor $$0, VerticalAnchor $$1) {
      return of(UniformHeight.of($$0, $$1));
   }

   public static HeightRangePlacement triangle(VerticalAnchor $$0, VerticalAnchor $$1) {
      return of(TrapezoidHeight.of($$0, $$1));
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      return Stream.of($$2.atY(this.height.sample($$1, $$0)));
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.HEIGHT_RANGE;
   }
}
