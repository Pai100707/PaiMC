package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

public interface Instruments {
   int GOAT_HORN_RANGE_BLOCKS = 256;
   float GOAT_HORN_DURATION = 7.0F;
   ResourceKey<net.minecraft.world.item.Instrument> PONDER_GOAT_HORN = create("ponder_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> SING_GOAT_HORN = create("sing_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> SEEK_GOAT_HORN = create("seek_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> FEEL_GOAT_HORN = create("feel_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> ADMIRE_GOAT_HORN = create("admire_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> CALL_GOAT_HORN = create("call_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> YEARN_GOAT_HORN = create("yearn_goat_horn");
   ResourceKey<net.minecraft.world.item.Instrument> DREAM_GOAT_HORN = create("dream_goat_horn");

   private static ResourceKey<net.minecraft.world.item.Instrument> create(String $$0) {
      return ResourceKey.create(Registries.INSTRUMENT, Identifier.withDefaultNamespace($$0));
   }

   static void bootstrap(BootstrapContext<net.minecraft.world.item.Instrument> $$0) {
      register($$0, PONDER_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0), 7.0F, 256.0F);
      register($$0, SING_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1), 7.0F, 256.0F);
      register($$0, SEEK_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2), 7.0F, 256.0F);
      register($$0, FEEL_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3), 7.0F, 256.0F);
      register($$0, ADMIRE_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(4), 7.0F, 256.0F);
      register($$0, CALL_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5), 7.0F, 256.0F);
      register($$0, YEARN_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(6), 7.0F, 256.0F);
      register($$0, DREAM_GOAT_HORN, (Holder<SoundEvent>)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(7), 7.0F, 256.0F);
   }

   static void register(
      BootstrapContext<net.minecraft.world.item.Instrument> $$0,
      ResourceKey<net.minecraft.world.item.Instrument> $$1,
      Holder<SoundEvent> $$2,
      float $$3,
      float $$4
   ) {
      MutableComponent $$5 = Component.translatable(Util.makeDescriptionId("instrument", $$1.identifier()));
      $$0.register($$1, new net.minecraft.world.item.Instrument($$2, $$3, $$4, $$5));
   }
}
