package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;

public enum ConversionType {
   SINGLE(true) {
      @Override
      void convert(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.Mob $$1, net.minecraft.world.entity.ConversionParams $$2) {
         net.minecraft.world.entity.Entity $$3 = $$0.getFirstPassenger();
         $$1.copyPosition($$0);
         $$1.setDeltaMovement($$0.getDeltaMovement());
         if ($$3 != null) {
            $$3.stopRiding();
            $$3.boardingCooldown = 0;

            for (net.minecraft.world.entity.Entity $$4 : $$1.getPassengers()) {
               $$4.stopRiding();
               $$4.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }

            $$3.startRiding($$1);
         }

         net.minecraft.world.entity.Entity $$5 = $$0.getVehicle();
         if ($$5 != null) {
            $$0.stopRiding();
            $$1.startRiding($$5, false, false);
         }

         if ($$2.keepEquipment()) {
            for (net.minecraft.world.entity.EquipmentSlot $$6 : net.minecraft.world.entity.EquipmentSlot.VALUES) {
               ItemStack $$7 = $$0.getItemBySlot($$6);
               if (!$$7.isEmpty()) {
                  $$1.setItemSlot($$6, $$7.copyAndClear());
                  $$1.setDropChance($$6, $$0.getDropChances().byEquipment($$6));
               }
            }
         }

         $$1.fallDistance = $$0.fallDistance;
         $$1.setSharedFlag(7, $$0.isFallFlying());
         $$1.lastHurtByPlayerMemoryTime = $$0.lastHurtByPlayerMemoryTime;
         $$1.hurtTime = $$0.hurtTime;
         $$1.yBodyRot = $$0.yBodyRot;
         $$1.setOnGround($$0.onGround());
         $$0.getSleepingPos().ifPresent($$1::setSleepingPos);
         net.minecraft.world.entity.Entity $$8 = $$0.getLeashHolder();
         if ($$8 != null) {
            $$1.setLeashedTo($$8, true);
         }

         this.convertCommon($$0, $$1, $$2);
      }
   },
   SPLIT_ON_DEATH(false) {
      @Override
      void convert(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.Mob $$1, net.minecraft.world.entity.ConversionParams $$2) {
         net.minecraft.world.entity.Entity $$3 = $$0.getFirstPassenger();
         if ($$3 != null) {
            $$3.stopRiding();
         }

         net.minecraft.world.entity.Entity $$4 = $$0.getLeashHolder();
         if ($$4 != null) {
            $$0.dropLeash();
         }

         this.convertCommon($$0, $$1, $$2);
      }
   };

   private static final Set<DataComponentType<?>> COMPONENTS_TO_COPY = Set.of(DataComponents.CUSTOM_NAME, DataComponents.CUSTOM_DATA);
   private final boolean discardAfterConversion;

   ConversionType(final boolean $$0) {
      this.discardAfterConversion = $$0;
   }

   public boolean shouldDiscardAfterConversion() {
      return this.discardAfterConversion;
   }

   abstract void convert(net.minecraft.world.entity.Mob var1, net.minecraft.world.entity.Mob var2, net.minecraft.world.entity.ConversionParams var3);

   void convertCommon(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.Mob $$1, net.minecraft.world.entity.ConversionParams $$2) {
      $$1.setAbsorptionAmount($$0.getAbsorptionAmount());

      for (MobEffectInstance $$3 : $$0.getActiveEffects()) {
         $$1.addEffect(new MobEffectInstance($$3));
      }

      if ($$0.isBaby()) {
         $$1.setBaby(true);
      }

      if ($$0 instanceof net.minecraft.world.entity.AgeableMob $$4 && $$1 instanceof net.minecraft.world.entity.AgeableMob $$5) {
         $$5.setAge($$4.getAge());
         $$5.forcedAge = $$4.forcedAge;
         $$5.forcedAgeTimer = $$4.forcedAgeTimer;
      }

      Brain<?> $$6 = $$0.getBrain();
      Brain<?> $$7 = $$1.getBrain();
      if ($$6.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED) && $$6.hasMemoryValue(MemoryModuleType.ANGRY_AT)) {
         $$7.setMemory(MemoryModuleType.ANGRY_AT, $$6.getMemory(MemoryModuleType.ANGRY_AT));
      }

      if ($$2.preserveCanPickUpLoot()) {
         $$1.setCanPickUpLoot($$0.canPickUpLoot());
      }

      $$1.setLeftHanded($$0.isLeftHanded());
      $$1.setNoAi($$0.isNoAi());
      if ($$0.isPersistenceRequired()) {
         $$1.setPersistenceRequired();
      }

      $$1.setCustomNameVisible($$0.isCustomNameVisible());
      $$1.setSharedFlagOnFire($$0.isOnFire());
      $$1.setInvulnerable($$0.isInvulnerable());
      $$1.setNoGravity($$0.isNoGravity());
      $$1.setPortalCooldown($$0.getPortalCooldown());
      $$1.setSilent($$0.isSilent());
      $$0.getTags().forEach($$1::addTag);

      for (DataComponentType<?> $$8 : COMPONENTS_TO_COPY) {
         copyComponent($$0, $$1, $$8);
      }

      if ($$2.team() != null) {
         Scoreboard $$9 = $$1.level().getScoreboard();
         $$9.addPlayerToTeam($$1.getStringUUID(), $$2.team());
         if ($$0.getTeam() != null && $$0.getTeam() == $$2.team()) {
            $$9.removePlayerFromTeam($$0.getStringUUID(), $$0.getTeam());
         }
      }

      if ($$0 instanceof Zombie $$10 && $$10.canBreakDoors() && $$1 instanceof Zombie $$11) {
         $$11.setCanBreakDoors(true);
      }
   }

   private static <T> void copyComponent(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.Mob $$1, DataComponentType<T> $$2) {
      T $$3 = $$0.get($$2);
      if ($$3 != null) {
         $$1.setComponent($$2, $$3);
      }
   }
}
