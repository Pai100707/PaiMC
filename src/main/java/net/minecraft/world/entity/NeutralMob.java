package net.minecraft.world.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface NeutralMob {
   String TAG_ANGER_END_TIME = "anger_end_time";
   String TAG_ANGRY_AT = "angry_at";
   long NO_ANGER_END_TIME = -1L;

   long getPersistentAngerEndTime();

   default void setTimeToRemainAngry(long $$0) {
      this.setPersistentAngerEndTime(this.level().getGameTime() + $$0);
   }

   void setPersistentAngerEndTime(long var1);

   
   net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getPersistentAngerTarget();

   void setPersistentAngerTarget(net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> var1);

   void startPersistentAngerTimer();

   Level level();

   default void addPersistentAngerSaveData(ValueOutput $$0) {
      $$0.putLong("anger_end_time", this.getPersistentAngerEndTime());
      $$0.storeNullable("angry_at", net.minecraft.world.entity.EntityReference.codec(), this.getPersistentAngerTarget());
   }

   default void readPersistentAngerSaveData(Level $$0, ValueInput $$1) {
      Optional<Long> $$2 = $$1.getLong("anger_end_time");
      if ($$2.isPresent()) {
         this.setPersistentAngerEndTime($$2.get());
      } else {
         Optional<Integer> $$3 = $$1.getInt("AngerTime");
         if ($$3.isPresent()) {
            this.setTimeToRemainAngry($$3.get().intValue());
         } else {
            this.setPersistentAngerEndTime(-1L);
         }
      }

      if ($$0 instanceof ServerLevel) {
         this.setPersistentAngerTarget(net.minecraft.world.entity.EntityReference.read($$1, "angry_at"));
         this.setTarget(net.minecraft.world.entity.EntityReference.getLivingEntity(this.getPersistentAngerTarget(), $$0));
      }
   }

   default void updatePersistentAnger(ServerLevel $$0, boolean $$1) {
      net.minecraft.world.entity.LivingEntity $$2 = this.getTarget();
      net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> $$3 = this.getPersistentAngerTarget();
      if ($$2 != null && $$2.isDeadOrDying() && $$3 != null && $$3.matches($$2) && $$2 instanceof net.minecraft.world.entity.Mob) {
         this.stopBeingAngry();
      } else {
         if ($$2 != null) {
            if ($$3 == null || !$$3.matches($$2)) {
               this.setPersistentAngerTarget(net.minecraft.world.entity.EntityReference.of($$2));
            }

            this.startPersistentAngerTimer();
         }

         if ($$3 != null && !this.isAngry() && ($$2 == null || !isValidPlayerTarget($$2) || !$$1)) {
            this.stopBeingAngry();
         }
      }
   }

   private static boolean isValidPlayerTarget(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0 instanceof Player $$1 && !$$1.isCreative() && !$$1.isSpectator();
   }

   default boolean isAngryAt(net.minecraft.world.entity.LivingEntity $$0, ServerLevel $$1) {
      if (!this.canAttack($$0)) {
         return false;
      } else if (isValidPlayerTarget($$0) && this.isAngryAtAllPlayers($$1)) {
         return true;
      } else {
         net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> $$2 = this.getPersistentAngerTarget();
         return $$2 != null && $$2.matches($$0);
      }
   }

   default boolean isAngryAtAllPlayers(ServerLevel $$0) {
      return (Boolean)$$0.getGameRules().get(GameRules.UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
   }

   default boolean isAngry() {
      long $$0 = this.getPersistentAngerEndTime();
      if ($$0 > 0L) {
         long $$1 = $$0 - this.level().getGameTime();
         return $$1 > 0L;
      } else {
         return false;
      }
   }

   default void playerDied(ServerLevel $$0, Player $$1) {
      if ((Boolean)$$0.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
         net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> $$2 = this.getPersistentAngerTarget();
         if ($$2 != null && $$2.matches($$1)) {
            this.stopBeingAngry();
         }
      }
   }

   default void forgetCurrentTargetAndRefreshUniversalAnger() {
      this.stopBeingAngry();
      this.startPersistentAngerTimer();
   }

   default void stopBeingAngry() {
      this.setLastHurtByMob(null);
      this.setPersistentAngerTarget(null);
      this.setTarget(null);
      this.setPersistentAngerEndTime(-1L);
   }

   
   net.minecraft.world.entity.LivingEntity getLastHurtByMob();

   void setLastHurtByMob(net.minecraft.world.entity.LivingEntity var1);

   void setTarget(net.minecraft.world.entity.LivingEntity var1);

   boolean canAttack(net.minecraft.world.entity.LivingEntity var1);

   
   net.minecraft.world.entity.LivingEntity getTarget();
}
