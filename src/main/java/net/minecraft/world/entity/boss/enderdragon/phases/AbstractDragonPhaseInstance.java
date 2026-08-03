package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDragonPhaseInstance implements DragonPhaseInstance {
   protected final EnderDragon dragon;

   public AbstractDragonPhaseInstance(EnderDragon $$0) {
      this.dragon = $$0;
   }

   @Override
   public boolean isSitting() {
      return false;
   }

   @Override
   public void doClientTick() {
   }

   @Override
   public void doServerTick(ServerLevel $$0) {
   }

   @Override
   public void onCrystalDestroyed(EndCrystal $$0, BlockPos $$1, DamageSource $$2, @Nullable Player $$3) {
   }

   @Override
   public void begin() {
   }

   @Override
   public void end() {
   }

   @Override
   public float getFlySpeed() {
      return 0.6F;
   }

   @Nullable
   @Override
   public Vec3 getFlyTargetLocation() {
      return null;
   }

   @Override
   public float onHurt(DamageSource $$0, float $$1) {
      return $$1;
   }

   @Override
   public float getTurnSpeed() {
      float $$0 = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float $$1 = Math.min($$0, 40.0F);
      return 0.7F / $$1 / $$0;
   }
}
