/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStandItem
extends Item {
    public ArmorStandItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        Direction $$1 = $$0.getClickedFace();
        if ($$1 == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level $$2 = $$0.getLevel();
        BlockPlaceContext $$3 = new BlockPlaceContext($$0);
        BlockPos $$4 = $$3.getClickedPos();
        ItemStack $$5 = $$0.getItemInHand();
        Vec3 $$6 = Vec3.atBottomCenterOf($$4);
        AABB $$7 = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox($$6.x(), $$6.y(), $$6.z());
        if (!$$2.noCollision(null, $$7) || !$$2.getEntities(null, $$7).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if ($$2 instanceof ServerLevel) {
            ServerLevel $$8 = (ServerLevel)$$2;
            Consumer $$9 = EntityType.createDefaultStackConfig($$8, $$5, $$0.getPlayer());
            ArmorStand $$10 = EntityType.ARMOR_STAND.create($$8, $$9, $$4, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if ($$10 == null) {
                return InteractionResult.FAIL;
            }
            float $$11 = (float)Mth.floor((Mth.wrapDegrees($$0.getRotation() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
            $$10.snapTo($$10.getX(), $$10.getY(), $$10.getZ(), $$11, 0.0f);
            $$8.addFreshEntityWithPassengers($$10);
            $$2.playSound(null, $$10.getX(), $$10.getY(), $$10.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75f, 0.8f);
            $$10.gameEvent(GameEvent.ENTITY_PLACE, $$0.getPlayer());
        }
        $$5.shrink(1);
        return InteractionResult.SUCCESS;
    }
}

