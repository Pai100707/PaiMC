/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;

public enum ConversionType {
    SINGLE(true){

        @Override
        void convert(Mob $$0, Mob $$1, ConversionParams $$2) {
            Entity $$5;
            Entity $$3 = $$0.getFirstPassenger();
            $$1.copyPosition($$0);
            $$1.setDeltaMovement($$0.getDeltaMovement());
            if ($$3 != null) {
                $$3.stopRiding();
                $$3.boardingCooldown = 0;
                for (Entity entity : $$1.getPassengers()) {
                    entity.stopRiding();
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                $$3.startRiding($$1);
            }
            if (($$5 = $$0.getVehicle()) != null) {
                $$0.stopRiding();
                $$1.startRiding($$5, false, false);
            }
            if ($$2.keepEquipment()) {
                for (EquipmentSlot $$6 : EquipmentSlot.VALUES) {
                    ItemStack $$7 = $$0.getItemBySlot($$6);
                    if ($$7.isEmpty()) continue;
                    $$1.setItemSlot($$6, $$7.copyAndClear());
                    $$1.setDropChance($$6, $$0.getDropChances().byEquipment($$6));
                }
            }
            $$1.fallDistance = $$0.fallDistance;
            $$1.setSharedFlag(7, $$0.isFallFlying());
            $$1.lastHurtByPlayerMemoryTime = $$0.lastHurtByPlayerMemoryTime;
            $$1.hurtTime = $$0.hurtTime;
            $$1.yBodyRot = $$0.yBodyRot;
            $$1.setOnGround($$0.onGround());
            $$0.getSleepingPos().ifPresent($$1::setSleepingPos);
            Entity entity = $$0.getLeashHolder();
            if (entity != null) {
                $$1.setLeashedTo(entity, true);
            }
            this.convertCommon($$0, $$1, $$2);
        }
    }
    ,
    SPLIT_ON_DEATH(false){

        @Override
        void convert(Mob $$0, Mob $$1, ConversionParams $$2) {
            Entity $$4;
            Entity $$3 = $$0.getFirstPassenger();
            if ($$3 != null) {
                $$3.stopRiding();
            }
            if (($$4 = $$0.getLeashHolder()) != null) {
                $$0.dropLeash();
            }
            this.convertCommon($$0, $$1, $$2);
        }
    };

    private static final Set<DataComponentType<?>> COMPONENTS_TO_COPY;
    private final boolean discardAfterConversion;

    ConversionType(boolean $$0) {
        this.discardAfterConversion = $$0;
    }

    public boolean shouldDiscardAfterConversion() {
        return this.discardAfterConversion;
    }

    abstract void convert(Mob var1, Mob var2, ConversionParams var3);

    void convertCommon(Mob $$0, Mob $$1, ConversionParams $$2) {
        Zombie $$10;
        $$1.setAbsorptionAmount($$0.getAbsorptionAmount());
        for (MobEffectInstance $$3 : $$0.getActiveEffects()) {
            $$1.addEffect(new MobEffectInstance($$3));
        }
        if ($$0.isBaby()) {
            $$1.setBaby(true);
        }
        if ($$0 instanceof AgeableMob) {
            AgeableMob $$4 = (AgeableMob)$$0;
            if ($$1 instanceof AgeableMob) {
                AgeableMob $$5 = (AgeableMob)$$1;
                $$5.setAge($$4.getAge());
                $$5.forcedAge = $$4.forcedAge;
                $$5.forcedAgeTimer = $$4.forcedAgeTimer;
            }
        }
        Brain<UUID> $$6 = $$0.getBrain();
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
            ConversionType.copyComponent($$0, $$1, $$8);
        }
        if ($$2.team() != null) {
            Scoreboard $$9 = $$1.level().getScoreboard();
            $$9.addPlayerToTeam($$1.getStringUUID(), $$2.team());
            if ($$0.getTeam() != null && $$0.getTeam() == $$2.team()) {
                $$9.removePlayerFromTeam($$0.getStringUUID(), $$0.getTeam());
            }
        }
        if ($$0 instanceof Zombie && ($$10 = (Zombie)$$0).canBreakDoors() && $$1 instanceof Zombie) {
            Zombie $$11 = (Zombie)$$1;
            $$11.setCanBreakDoors(true);
        }
    }

    private static <T> void copyComponent(Mob $$0, Mob $$1, DataComponentType<T> $$2) {
        T $$3 = $$0.get($$2);
        if ($$3 != null) {
            $$1.setComponent($$2, $$3);
        }
    }

    static {
        COMPONENTS_TO_COPY = Set.of(DataComponents.CUSTOM_NAME, DataComponents.CUSTOM_DATA);
    }
}

