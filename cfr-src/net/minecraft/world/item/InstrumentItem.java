/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem
extends Item {
    public InstrumentItem(Item.Properties $$0) {
        super($$0);
    }

    public static ItemStack create(Item $$0, Holder<Instrument> $$1) {
        ItemStack $$2 = new ItemStack($$0);
        $$2.set(DataComponents.INSTRUMENT, new InstrumentComponent($$1));
        return $$2;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        Optional<Holder<Instrument>> $$4 = this.getInstrument($$3, $$1.registryAccess());
        if ($$4.isPresent()) {
            Instrument $$5 = $$4.get().value();
            $$1.startUsingItem($$2);
            InstrumentItem.play($$0, $$1, $$5);
            $$1.getCooldowns().addCooldown($$3, Mth.floor($$5.useDuration() * 20.0f));
            $$1.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack $$02, LivingEntity $$1) {
        Optional<Holder<Instrument>> $$2 = this.getInstrument($$02, $$1.registryAccess());
        return $$2.map($$0 -> Mth.floor(((Instrument)$$0.value()).useDuration() * 20.0f)).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack $$0, HolderLookup.Provider $$1) {
        InstrumentComponent $$2 = $$0.get(DataComponents.INSTRUMENT);
        return $$2 != null ? $$2.unwrap($$1) : Optional.empty();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack $$0) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static void play(Level $$0, Player $$1, Instrument $$2) {
        SoundEvent $$3 = $$2.soundEvent().value();
        float $$4 = $$2.range() / 16.0f;
        $$0.playSound((Entity)$$1, $$1, $$3, SoundSource.RECORDS, $$4, 1.0f);
        $$0.gameEvent(GameEvent.INSTRUMENT_PLAY, $$1.position(), GameEvent.Context.of($$1));
    }
}

