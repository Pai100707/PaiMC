package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class InsideWorldBoundsPredicate implements BlockPredicate {
   public static final MapCodec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter($$0x -> $$0x.offset))
         .apply($$0, InsideWorldBoundsPredicate::new)
   );
   private final Vec3i offset;

   public InsideWorldBoundsPredicate(Vec3i $$0) {
      this.offset = $$0;
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return !$$0.isOutsideBuildHeight($$1.offset(this.offset));
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.INSIDE_WORLD_BOUNDS;
   }
}
