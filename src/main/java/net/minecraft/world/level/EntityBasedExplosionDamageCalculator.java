package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityBasedExplosionDamageCalculator extends net.minecraft.world.level.ExplosionDamageCalculator {
   private final Entity source;

   public EntityBasedExplosionDamageCalculator(Entity $$0) {
      this.source = $$0;
   }

   @Override
   public Optional<Float> getBlockExplosionResistance(
      net.minecraft.world.level.Explosion $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, FluidState $$4
   ) {
      return super.getBlockExplosionResistance($$0, $$1, $$2, $$3, $$4).map($$5 -> this.source.getBlockExplosionResistance($$0, $$1, $$2, $$3, $$4, $$5));
   }

   @Override
   public boolean shouldBlockExplode(
      net.minecraft.world.level.Explosion $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, float $$4
   ) {
      return this.source.shouldBlockExplode($$0, $$1, $$2, $$3, $$4);
   }
}
