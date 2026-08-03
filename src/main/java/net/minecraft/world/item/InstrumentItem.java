package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class InstrumentItem extends net.minecraft.world.item.Item {
   public InstrumentItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static net.minecraft.world.item.ItemStack create(net.minecraft.world.item.Item $$0, Holder<net.minecraft.world.item.Instrument> $$1) {
      net.minecraft.world.item.ItemStack $$2 = new net.minecraft.world.item.ItemStack($$0);
      $$2.set(DataComponents.INSTRUMENT, new InstrumentComponent($$1));
      return $$2;
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      Optional<? extends Holder<net.minecraft.world.item.Instrument>> $$4 = this.getInstrument($$3, $$1.registryAccess());
      if ($$4.isPresent()) {
         net.minecraft.world.item.Instrument $$5 = (net.minecraft.world.item.Instrument)$$4.get().value();
         $$1.startUsingItem($$2);
         play($$0, $$1, $$5);
         $$1.getCooldowns().addCooldown($$3, Mth.floor($$5.useDuration() * 20.0F));
         $$1.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.FAIL;
      }
   }

   @Override
   public int getUseDuration(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      Optional<Holder<net.minecraft.world.item.Instrument>> $$2 = this.getInstrument($$0, $$1.registryAccess());
      return $$2.<Integer>map($$0x -> Mth.floor(((net.minecraft.world.item.Instrument)$$0x.value()).useDuration() * 20.0F)).orElse(0);
   }

   private Optional<Holder<net.minecraft.world.item.Instrument>> getInstrument(net.minecraft.world.item.ItemStack $$0, Provider $$1) {
      InstrumentComponent $$2 = (InstrumentComponent)$$0.get(DataComponents.INSTRUMENT);
      return $$2 != null ? $$2.unwrap($$1) : Optional.empty();
   }

   @Override
   public net.minecraft.world.item.ItemUseAnimation getUseAnimation(net.minecraft.world.item.ItemStack $$0) {
      return net.minecraft.world.item.ItemUseAnimation.TOOT_HORN;
   }

   private static void play(Level $$0, Player $$1, net.minecraft.world.item.Instrument $$2) {
      SoundEvent $$3 = (SoundEvent)$$2.soundEvent().value();
      float $$4 = $$2.range() / 16.0F;
      $$0.playSound($$1, $$1, $$3, SoundSource.RECORDS, $$4, 1.0F);
      $$0.gameEvent(GameEvent.INSTRUMENT_PLAY, $$1.position(), Context.of($$1));
   }
}
