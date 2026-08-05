/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BoatItem
extends Item {
    private final EntityType<? extends AbstractBoat> entityType;

    public BoatItem(EntityType<? extends AbstractBoat> $$0, Item.Properties $$1) {
        super($$1);
        this.entityType = $$0;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        BlockHitResult $$4 = BoatItem.getPlayerPOVHitResult($$0, $$1, ClipContext.Fluid.ANY);
        if (((HitResult)$$4).getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        Vec3 $$5 = $$1.getViewVector(1.0f);
        double $$6 = 5.0;
        List<Entity> $$7 = $$0.getEntities($$1, $$1.getBoundingBox().expandTowards($$5.scale(5.0)).inflate(1.0), EntitySelector.CAN_BE_PICKED);
        if (!$$7.isEmpty()) {
            Vec3 $$8 = $$1.getEyePosition();
            for (Entity $$9 : $$7) {
                AABB $$10 = $$9.getBoundingBox().inflate($$9.getPickRadius());
                if (!$$10.contains($$8)) continue;
                return InteractionResult.PASS;
            }
        }
        if (((HitResult)$$4).getType() == HitResult.Type.BLOCK) {
            AbstractBoat $$11 = this.getBoat($$0, $$4, $$3, $$1);
            if ($$11 == null) {
                return InteractionResult.FAIL;
            }
            $$11.setYRot($$1.getYRot());
            if (!$$0.noCollision($$11, $$11.getBoundingBox())) {
                return InteractionResult.FAIL;
            }
            if (!$$0.isClientSide()) {
                $$0.addFreshEntity($$11);
                $$0.gameEvent((Entity)$$1, GameEvent.ENTITY_PLACE, $$4.getLocation());
                $$3.consume(1, $$1);
            }
            $$1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private @Nullable AbstractBoat getBoat(Level $$0, HitResult $$1, ItemStack $$2, Player $$3) {
        AbstractBoat $$4 = this.entityType.create($$0, EntitySpawnReason.SPAWN_ITEM_USE);
        if ($$4 != null) {
            Vec3 $$5 = $$1.getLocation();
            $$4.setInitialPos($$5.x, $$5.y, $$5.z);
            if ($$0 instanceof ServerLevel) {
                ServerLevel $$6 = (ServerLevel)$$0;
                EntityType.createDefaultStackConfig($$6, $$2, $$3).accept($$4);
            }
        }
        return $$4;
    }
}

