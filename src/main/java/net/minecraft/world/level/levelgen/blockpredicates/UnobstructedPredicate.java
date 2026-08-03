package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.shapes.Shapes;

record UnobstructedPredicate(Vec3i offset) implements BlockPredicate {
   public static MapCodec<UnobstructedPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(UnobstructedPredicate::offset)).apply($$0, UnobstructedPredicate::new)
   );

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.UNOBSTRUCTED;
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return $$0.isUnobstructed(null, Shapes.block().move($$1));
   }
}
