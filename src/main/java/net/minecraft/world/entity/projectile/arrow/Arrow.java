package net.minecraft.world.entity.projectile.arrow;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class Arrow extends AbstractArrow {
   private static final int EXPOSED_POTION_DECAY_TIME = 600;
   private static final int NO_EFFECT_COLOR = -1;
   private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
   private static final byte EVENT_POTION_PUFF = 0;

   public Arrow(net.minecraft.world.entity.EntityType<? extends Arrow> $$0, Level $$1) {
      super($$0, $$1);
   }

   public Arrow(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4, ItemStack $$5) {
      super(net.minecraft.world.entity.EntityType.ARROW, $$1, $$2, $$3, $$0, $$4, $$5);
      this.updateColor();
   }

   public Arrow(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2, ItemStack $$3) {
      super(net.minecraft.world.entity.EntityType.ARROW, $$1, $$0, $$2, $$3);
      this.updateColor();
   }

   private PotionContents getPotionContents() {
      return (PotionContents)this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
   }

   private float getPotionDurationScale() {
      return (Float)this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
   }

   private void setPotionContents(PotionContents $$0) {
      this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, $$0);
      this.updateColor();
   }

   @Override
   protected void setPickupItemStack(ItemStack $$0) {
      super.setPickupItemStack($$0);
      this.updateColor();
   }

   private void updateColor() {
      PotionContents $$0 = this.getPotionContents();
      this.entityData.set(ID_EFFECT_COLOR, $$0.equals(PotionContents.EMPTY) ? -1 : $$0.getColor());
   }

   public void addEffect(MobEffectInstance $$0) {
      this.setPotionContents(this.getPotionContents().withEffectAdded($$0));
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(ID_EFFECT_COLOR, -1);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         if (this.isInGround()) {
            if (this.inGroundTime % 5 == 0) {
               this.makeParticle(1);
            }
         } else {
            this.makeParticle(2);
         }
      } else if (this.isInGround() && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
         this.level().broadcastEntityEvent(this, (byte)0);
         this.setPickupItemStack(new ItemStack(Items.ARROW));
      }
   }

   private void makeParticle(int $$0) {
      int $$1 = this.getColor();
      if ($$1 != -1 && $$0 > 0) {
         for (int $$2 = 0; $$2 < $$0; $$2++) {
            this.level()
               .addParticle(
                  ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$1), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0
               );
         }
      }
   }

   public int getColor() {
      return (Integer)this.entityData.get(ID_EFFECT_COLOR);
   }

   @Override
   protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity $$0) {
      super.doPostHurtEffects($$0);
      net.minecraft.world.entity.Entity $$1 = this.getEffectSource();
      PotionContents $$2 = this.getPotionContents();
      float $$3 = this.getPotionDurationScale();
      $$2.forEachEffect($$2x -> $$0.addEffect($$2x, $$1), $$3);
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(Items.ARROW);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 0) {
         int $$1 = this.getColor();
         if ($$1 != -1) {
            float $$2 = ($$1 >> 16 & 0xFF) / 255.0F;
            float $$3 = ($$1 >> 8 & 0xFF) / 255.0F;
            float $$4 = ($$1 >> 0 & 0xFF) / 255.0F;

            for (int $$5 = 0; $$5 < 20; $$5++) {
               this.level()
                  .addParticle(
                     ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$2, $$3, $$4),
                     this.getRandomX(0.5),
                     this.getRandomY(),
                     this.getRandomZ(0.5),
                     0.0,
                     0.0,
                     0.0
                  );
            }
         }
      } else {
         super.handleEntityEvent($$0);
      }
   }
}
