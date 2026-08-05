/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import org.jspecify.annotations.Nullable;

public class SkeletonTrapGoal
extends Goal {
    private final SkeletonHorse horse;

    public SkeletonTrapGoal(SkeletonHorse $$0) {
        this.horse = $$0;
    }

    @Override
    public boolean canUse() {
        return this.horse.level().hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        ServerLevel $$0 = (ServerLevel)this.horse.level();
        DifficultyInstance $$1 = $$0.getCurrentDifficultyAt(this.horse.blockPosition());
        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        LightningBolt $$2 = EntityType.LIGHTNING_BOLT.create($$0, EntitySpawnReason.TRIGGERED);
        if ($$2 == null) {
            return;
        }
        $$2.snapTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        $$2.setVisualOnly(true);
        $$0.addFreshEntity($$2);
        Skeleton $$3 = this.createSkeleton($$1, this.horse);
        if ($$3 == null) {
            return;
        }
        $$3.startRiding(this.horse);
        $$0.addFreshEntityWithPassengers($$3);
        for (int $$4 = 0; $$4 < 3; ++$$4) {
            Skeleton $$6;
            AbstractHorse $$5 = this.createHorse($$1);
            if ($$5 == null || ($$6 = this.createSkeleton($$1, $$5)) == null) continue;
            $$6.startRiding($$5);
            $$5.push(this.horse.getRandom().triangle(0.0, 1.1485), 0.0, this.horse.getRandom().triangle(0.0, 1.1485));
            $$0.addFreshEntityWithPassengers($$5);
        }
    }

    private @Nullable AbstractHorse createHorse(DifficultyInstance $$0) {
        SkeletonHorse $$1 = EntityType.SKELETON_HORSE.create(this.horse.level(), EntitySpawnReason.TRIGGERED);
        if ($$1 != null) {
            $$1.finalizeSpawn((ServerLevel)this.horse.level(), $$0, EntitySpawnReason.TRIGGERED, null);
            $$1.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            $$1.invulnerableTime = 60;
            $$1.setPersistenceRequired();
            $$1.setTamed(true);
            $$1.setAge(0);
        }
        return $$1;
    }

    private @Nullable Skeleton createSkeleton(DifficultyInstance $$0, AbstractHorse $$1) {
        Skeleton $$2 = EntityType.SKELETON.create($$1.level(), EntitySpawnReason.TRIGGERED);
        if ($$2 != null) {
            $$2.finalizeSpawn((ServerLevel)$$1.level(), $$0, EntitySpawnReason.TRIGGERED, null);
            $$2.setPos($$1.getX(), $$1.getY(), $$1.getZ());
            $$2.invulnerableTime = 60;
            $$2.setPersistenceRequired();
            if ($$2.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                $$2.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
            this.enchant($$2, EquipmentSlot.MAINHAND, $$0);
            this.enchant($$2, EquipmentSlot.HEAD, $$0);
        }
        return $$2;
    }

    private void enchant(Skeleton $$0, EquipmentSlot $$1, DifficultyInstance $$2) {
        ItemStack $$3 = $$0.getItemBySlot($$1);
        $$3.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        EnchantmentHelper.enchantItemFromProvider($$3, $$0.level().registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, $$2, $$0.getRandom());
        $$0.setItemSlot($$1, $$3);
    }
}

