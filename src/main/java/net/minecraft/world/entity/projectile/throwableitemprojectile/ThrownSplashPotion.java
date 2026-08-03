package net.minecraft.world.entity.projectile.throwableitemprojectile;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class ThrownSplashPotion extends AbstractThrownPotion {
   public ThrownSplashPotion(net.minecraft.world.entity.EntityType<? extends ThrownSplashPotion> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownSplashPotion(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.SPLASH_POTION, $$0, $$1, $$2);
   }

   public ThrownSplashPotion(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
      super(net.minecraft.world.entity.EntityType.SPLASH_POTION, $$0, $$1, $$2, $$3, $$4);
   }

   @Override
   protected Item getDefaultItem() {
      return Items.SPLASH_POTION;
   }

   @Override
   public void onHitAsPotion(ServerLevel $$0, ItemStack $$1, HitResult $$2) {
      PotionContents $$3 = (PotionContents)$$1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      float $$4 = (Float)$$1.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
      Iterable<MobEffectInstance> $$5 = $$3.getAllEffects();
      AABB $$6 = this.getBoundingBox().move($$2.getLocation().subtract(this.position()));
      AABB $$7 = $$6.inflate(4.0, 2.0, 4.0);
      List<net.minecraft.world.entity.LivingEntity> $$8 = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, $$7);
      float $$9 = ProjectileUtil.computeMargin(this);
      if (!$$8.isEmpty()) {
         net.minecraft.world.entity.Entity $$10 = this.getEffectSource();

         for (net.minecraft.world.entity.LivingEntity $$11 : $$8) {
            if ($$11.isAffectedByPotions()) {
               double $$12 = $$6.distanceToSqr($$11.getBoundingBox().inflate($$9));
               if ($$12 < 16.0) {
                  double $$13 = 1.0 - Math.sqrt($$12) / 4.0;

                  for (MobEffectInstance $$14 : $$5) {
                     Holder<MobEffect> $$15 = $$14.getEffect();
                     if (((MobEffect)$$15.value()).isInstantenous()) {
                        ((MobEffect)$$15.value()).applyInstantenousEffect($$0, this, this.getOwner(), $$11, $$14.getAmplifier(), $$13);
                     } else {
                        int $$16 = $$14.mapDuration($$2x -> (int)($$13 * $$2x * $$4 + 0.5));
                        MobEffectInstance $$17 = new MobEffectInstance($$15, $$16, $$14.getAmplifier(), $$14.isAmbient(), $$14.isVisible());
                        if (!$$17.endsWithin(20)) {
                           $$11.addEffect($$17, $$10);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
