package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;

public abstract class VehicleEntity extends net.minecraft.world.entity.Entity {
   protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

   public VehicleEntity(net.minecraft.world.entity.EntityType<?> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean hurtClient(DamageSource $$0) {
      return true;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isRemoved()) {
         return true;
      } else if (this.isInvulnerableToBase($$1)) {
         return false;
      } else {
         this.setHurtDir(-this.getHurtDir());
         this.setHurtTime(10);
         this.markHurt();
         this.setDamage(this.getDamage() + $$2 * 10.0F);
         this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
         boolean $$4 = $$1.getEntity() instanceof Player $$3 && $$3.getAbilities().instabuild;
         if (($$4 || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy($$1)) {
            if ($$4) {
               this.discard();
            }
         } else {
            this.destroy($$0, $$1);
         }

         return true;
      }
   }

   protected boolean shouldSourceDestroy(DamageSource $$0) {
      return false;
   }

   @Override
   public boolean ignoreExplosion(Explosion $$0) {
      return $$0.getIndirectSourceEntity() instanceof net.minecraft.world.entity.Mob && !(Boolean)$$0.level().getGameRules().get(GameRules.MOB_GRIEFING);
   }

   public void destroy(ServerLevel $$0, Item $$1) {
      this.kill($$0);
      if ((Boolean)$$0.getGameRules().get(GameRules.ENTITY_DROPS)) {
         ItemStack $$2 = new ItemStack($$1);
         $$2.set(DataComponents.CUSTOM_NAME, this.getCustomName());
         this.spawnAtLocation($$0, $$2);
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_ID_HURT, 0);
      $$0.define(DATA_ID_HURTDIR, 1);
      $$0.define(DATA_ID_DAMAGE, 0.0F);
   }

   public void setHurtTime(int $$0) {
      this.entityData.set(DATA_ID_HURT, $$0);
   }

   public void setHurtDir(int $$0) {
      this.entityData.set(DATA_ID_HURTDIR, $$0);
   }

   public void setDamage(float $$0) {
      this.entityData.set(DATA_ID_DAMAGE, $$0);
   }

   public float getDamage() {
      return (Float)this.entityData.get(DATA_ID_DAMAGE);
   }

   public int getHurtTime() {
      return (Integer)this.entityData.get(DATA_ID_HURT);
   }

   public int getHurtDir() {
      return (Integer)this.entityData.get(DATA_ID_HURTDIR);
   }

   protected void destroy(ServerLevel $$0, DamageSource $$1) {
      this.destroy($$0, this.getDropItem());
   }

   @Override
   public int getDimensionChangingDelay() {
      return 10;
   }

   protected abstract Item getDropItem();
}
