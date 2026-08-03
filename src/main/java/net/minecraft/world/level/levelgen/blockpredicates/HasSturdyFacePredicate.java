package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class HasSturdyFacePredicate implements BlockPredicate {
   private final Vec3i offset;
   private final Direction direction;
   public static final MapCodec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter($$0x -> $$0x.offset),
            Direction.CODEC.fieldOf("direction").forGetter($$0x -> $$0x.direction)
         )
         .apply($$0, HasSturdyFacePredicate::new)
   );

   public HasSturdyFacePredicate(Vec3i $$0, Direction $$1) {
      this.offset = $$0;
      this.direction = $$1;
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.offset(this.offset);
      return $$0.getBlockState($$2).isFaceSturdy($$0, $$2, this.direction);
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.HAS_STURDY_FACE;
   }
}
