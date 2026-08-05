/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class DyeItem
extends Item
implements SignApplicator {
    private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
    private final DyeColor dyeColor;

    public DyeItem(DyeColor $$0, Item.Properties $$1) {
        super($$1);
        this.dyeColor = $$0;
        ITEM_BY_COLOR.put($$0, this);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack $$0, Player $$1, LivingEntity $$2, InteractionHand $$3) {
        Sheep $$4;
        if ($$2 instanceof Sheep && ($$4 = (Sheep)$$2).isAlive() && !$$4.isSheared() && $$4.getColor() != this.dyeColor) {
            $$4.level().playSound((Entity)$$1, $$4, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            if (!$$1.level().isClientSide()) {
                $$4.setColor(this.dyeColor);
                $$0.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public static DyeItem byColor(DyeColor $$0) {
        return ITEM_BY_COLOR.get($$0);
    }

    @Override
    public boolean tryApplyToSign(Level $$02, SignBlockEntity $$1, boolean $$2, Player $$3) {
        if ($$1.updateText($$0 -> $$0.setColor(this.getDyeColor()), $$2)) {
            $$02.playSound(null, $$1.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

