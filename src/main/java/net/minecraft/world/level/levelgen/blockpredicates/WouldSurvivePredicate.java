package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public class WouldSurvivePredicate implements BlockPredicate {
   public static final MapCodec<WouldSurvivePredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter($$0x -> $$0x.offset),
            BlockState.CODEC.fieldOf("state").forGetter($$0x -> $$0x.state)
         )
         .apply($$0, WouldSurvivePredicate::new)
   );
   private final Vec3i offset;
   private final BlockState state;

   protected WouldSurvivePredicate(Vec3i $$0, BlockState $$1) {
      this.offset = $$0;
      this.state = $$1;
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return this.state.canSurvive($$0, $$1.offset(this.offset));
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.WOULD_SURVIVE;
   }
}
