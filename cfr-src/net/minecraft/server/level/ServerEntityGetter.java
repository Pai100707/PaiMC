/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public interface ServerEntityGetter
extends EntityGetter {
    public ServerLevel getLevel();

    default public @Nullable Player getNearestPlayer(TargetingConditions $$0, LivingEntity $$1) {
        return this.getNearestEntity(this.players(), $$0, $$1, $$1.getX(), $$1.getY(), $$1.getZ());
    }

    default public @Nullable Player getNearestPlayer(TargetingConditions $$0, LivingEntity $$1, double $$2, double $$3, double $$4) {
        return this.getNearestEntity(this.players(), $$0, $$1, $$2, $$3, $$4);
    }

    default public @Nullable Player getNearestPlayer(TargetingConditions $$0, double $$1, double $$2, double $$3) {
        return this.getNearestEntity(this.players(), $$0, null, $$1, $$2, $$3);
    }

    default public <T extends LivingEntity> @Nullable T getNearestEntity(Class<? extends T> $$02, TargetingConditions $$1, @Nullable LivingEntity $$2, double $$3, double $$4, double $$5, AABB $$6) {
        return (T)this.getNearestEntity(this.getEntitiesOfClass($$02, $$6, $$0 -> true), $$1, $$2, $$3, $$4, $$5);
    }

    default public @Nullable LivingEntity getNearestEntity(TagKey<EntityType<?>> $$0, TargetingConditions $$12, @Nullable LivingEntity $$2, double $$3, double $$4, double $$5, AABB $$6) {
        double $$7 = Double.MAX_VALUE;
        LivingEntity $$8 = null;
        for (LivingEntity $$9 : this.getEntitiesOfClass(LivingEntity.class, $$6, $$1 -> $$1.getType().is($$0))) {
            double $$10;
            if (!$$12.test(this.getLevel(), $$2, $$9) || !(($$10 = $$9.distanceToSqr($$3, $$4, $$5)) < $$7)) continue;
            $$7 = $$10;
            $$8 = $$9;
        }
        return $$8;
    }

    default public <T extends LivingEntity> @Nullable T getNearestEntity(List<? extends T> $$0, TargetingConditions $$1, @Nullable LivingEntity $$2, double $$3, double $$4, double $$5) {
        double $$6 = -1.0;
        LivingEntity $$7 = null;
        for (LivingEntity $$8 : $$0) {
            if (!$$1.test(this.getLevel(), $$2, $$8)) continue;
            double $$9 = $$8.distanceToSqr($$3, $$4, $$5);
            if ($$6 != -1.0 && !($$9 < $$6)) continue;
            $$6 = $$9;
            $$7 = $$8;
        }
        return (T)$$7;
    }

    default public List<Player> getNearbyPlayers(TargetingConditions $$0, LivingEntity $$1, AABB $$2) {
        ArrayList<Player> $$3 = new ArrayList<Player>();
        for (Player player : this.players()) {
            if (!$$2.contains(player.getX(), player.getY(), player.getZ()) || !$$0.test(this.getLevel(), $$1, player)) continue;
            $$3.add(player);
        }
        return $$3;
    }

    default public <T extends LivingEntity> List<T> getNearbyEntities(Class<T> $$02, TargetingConditions $$1, LivingEntity $$2, AABB $$3) {
        List<LivingEntity> $$4 = this.getEntitiesOfClass($$02, $$3, $$0 -> true);
        ArrayList<LivingEntity> $$5 = new ArrayList<LivingEntity>();
        for (LivingEntity $$6 : $$4) {
            if (!$$1.test(this.getLevel(), $$2, $$6)) continue;
            $$5.add($$6);
        }
        return $$5;
    }
}

