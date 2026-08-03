package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class ExplosionDamageCalculator {
   public Optional<Float> getBlockExplosionResistance(
      net.minecraft.world.level.Explosion $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, FluidState $$4
   ) {
      return $$3.isAir() && $$4.isEmpty() ? Optional.empty() : Optional.of(Math.max($$3.getBlock().getExplosionResistance(), $$4.getExplosionResistance()));
   }

   public boolean shouldBlockExplode(
      net.minecraft.world.level.Explosion $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, float $$4
   ) {
      return true;
   }

   public boolean shouldDamageEntity(net.minecraft.world.level.Explosion $$0, Entity $$1) {
      return true;
   }

   public float getKnockbackMultiplier(Entity $$0) {
      return 1.0F;
   }

   public float getEntityDamageAmount(net.minecraft.world.level.Explosion $$0, Entity $$1, float $$2) {
      float $$3 = $$0.radius() * 2.0F;
      Vec3 $$4 = $$0.center();
      double $$5 = Math.sqrt($$1.distanceToSqr($$4)) / $$3;
      double $$6 = (1.0 - $$5) * $$2;
      return (float)(($$6 * $$6 + $$6) / 2.0 * 7.0 * $$3 + 1.0);
   }
}
