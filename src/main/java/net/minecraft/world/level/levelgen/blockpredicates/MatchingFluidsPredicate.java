package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
   private final HolderSet<Fluid> fluids;
   public static final MapCodec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> stateTestingCodec($$0)
         .and(RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter($$0x -> $$0x.fluids))
         .apply($$0, MatchingFluidsPredicate::new)
   );

   public MatchingFluidsPredicate(Vec3i $$0, HolderSet<Fluid> $$1) {
      super($$0);
      this.fluids = $$1;
   }

   @Override
   protected boolean test(BlockState $$0) {
      return $$0.getFluidState().is(this.fluids);
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_FLUIDS;
   }
}
