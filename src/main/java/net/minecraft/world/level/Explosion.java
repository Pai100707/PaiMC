package net.minecraft.world.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public interface Explosion {
   static DamageSource getDefaultDamageSource(net.minecraft.world.level.Level $$0, Entity $$1) {
      return $$0.damageSources().explosion($$1, getIndirectSourceEntity($$1));
   }

   
   static LivingEntity getIndirectSourceEntity(Entity $$0) {
      return switch ($$0) {
         case PrimedTnt $$1 -> $$1.getOwner();
         case LivingEntity $$2 -> $$2;
         case Projectile $$3 when $$3.getOwner() instanceof LivingEntity $$4 -> $$4;
         case null, default -> null;
      };
   }

   ServerLevel level();

   net.minecraft.world.level.Explosion.BlockInteraction getBlockInteraction();

   
   LivingEntity getIndirectSourceEntity();

   
   Entity getDirectSourceEntity();

   float radius();

   Vec3 center();

   boolean canTriggerBlocks();

   boolean shouldAffectBlocklikeEntities();

   public static enum BlockInteraction {
      KEEP(false),
      DESTROY(true),
      DESTROY_WITH_DECAY(true),
      TRIGGER_BLOCK(false);

      private final boolean shouldAffectBlocklikeEntities;

      private BlockInteraction(final boolean $$0) {
         this.shouldAffectBlocklikeEntities = $$0;
      }

      public boolean shouldAffectBlocklikeEntities() {
         return this.shouldAffectBlocklikeEntities;
      }
   }
}
