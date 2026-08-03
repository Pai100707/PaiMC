package net.minecraft.world.item;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

public interface JukeboxSongs {
   ResourceKey<net.minecraft.world.item.JukeboxSong> THIRTEEN = create("13");
   ResourceKey<net.minecraft.world.item.JukeboxSong> CAT = create("cat");
   ResourceKey<net.minecraft.world.item.JukeboxSong> BLOCKS = create("blocks");
   ResourceKey<net.minecraft.world.item.JukeboxSong> CHIRP = create("chirp");
   ResourceKey<net.minecraft.world.item.JukeboxSong> FAR = create("far");
   ResourceKey<net.minecraft.world.item.JukeboxSong> MALL = create("mall");
   ResourceKey<net.minecraft.world.item.JukeboxSong> MELLOHI = create("mellohi");
   ResourceKey<net.minecraft.world.item.JukeboxSong> STAL = create("stal");
   ResourceKey<net.minecraft.world.item.JukeboxSong> STRAD = create("strad");
   ResourceKey<net.minecraft.world.item.JukeboxSong> WARD = create("ward");
   ResourceKey<net.minecraft.world.item.JukeboxSong> ELEVEN = create("11");
   ResourceKey<net.minecraft.world.item.JukeboxSong> WAIT = create("wait");
   ResourceKey<net.minecraft.world.item.JukeboxSong> PIGSTEP = create("pigstep");
   ResourceKey<net.minecraft.world.item.JukeboxSong> OTHERSIDE = create("otherside");
   ResourceKey<net.minecraft.world.item.JukeboxSong> FIVE = create("5");
   ResourceKey<net.minecraft.world.item.JukeboxSong> RELIC = create("relic");
   ResourceKey<net.minecraft.world.item.JukeboxSong> PRECIPICE = create("precipice");
   ResourceKey<net.minecraft.world.item.JukeboxSong> CREATOR = create("creator");
   ResourceKey<net.minecraft.world.item.JukeboxSong> CREATOR_MUSIC_BOX = create("creator_music_box");
   ResourceKey<net.minecraft.world.item.JukeboxSong> TEARS = create("tears");
   ResourceKey<net.minecraft.world.item.JukeboxSong> LAVA_CHICKEN = create("lava_chicken");

   private static ResourceKey<net.minecraft.world.item.JukeboxSong> create(String $$0) {
      return ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.withDefaultNamespace($$0));
   }

   private static void register(
      BootstrapContext<net.minecraft.world.item.JukeboxSong> $$0,
      ResourceKey<net.minecraft.world.item.JukeboxSong> $$1,
      Reference<SoundEvent> $$2,
      int $$3,
      int $$4
   ) {
      $$0.register(
         $$1, new net.minecraft.world.item.JukeboxSong($$2, Component.translatable(Util.makeDescriptionId("jukebox_song", $$1.identifier())), $$3, $$4)
      );
   }

   static void bootstrap(BootstrapContext<net.minecraft.world.item.JukeboxSong> $$0) {
      register($$0, THIRTEEN, SoundEvents.MUSIC_DISC_13, 178, 1);
      register($$0, CAT, SoundEvents.MUSIC_DISC_CAT, 185, 2);
      register($$0, BLOCKS, SoundEvents.MUSIC_DISC_BLOCKS, 345, 3);
      register($$0, CHIRP, SoundEvents.MUSIC_DISC_CHIRP, 185, 4);
      register($$0, FAR, SoundEvents.MUSIC_DISC_FAR, 174, 5);
      register($$0, MALL, SoundEvents.MUSIC_DISC_MALL, 197, 6);
      register($$0, MELLOHI, SoundEvents.MUSIC_DISC_MELLOHI, 96, 7);
      register($$0, STAL, SoundEvents.MUSIC_DISC_STAL, 150, 8);
      register($$0, STRAD, SoundEvents.MUSIC_DISC_STRAD, 188, 9);
      register($$0, WARD, SoundEvents.MUSIC_DISC_WARD, 251, 10);
      register($$0, ELEVEN, SoundEvents.MUSIC_DISC_11, 71, 11);
      register($$0, WAIT, SoundEvents.MUSIC_DISC_WAIT, 238, 12);
      register($$0, PIGSTEP, SoundEvents.MUSIC_DISC_PIGSTEP, 149, 13);
      register($$0, OTHERSIDE, SoundEvents.MUSIC_DISC_OTHERSIDE, 195, 14);
      register($$0, FIVE, SoundEvents.MUSIC_DISC_5, 178, 15);
      register($$0, RELIC, SoundEvents.MUSIC_DISC_RELIC, 218, 14);
      register($$0, PRECIPICE, SoundEvents.MUSIC_DISC_PRECIPICE, 299, 13);
      register($$0, CREATOR, SoundEvents.MUSIC_DISC_CREATOR, 176, 12);
      register($$0, CREATOR_MUSIC_BOX, SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
      register($$0, TEARS, SoundEvents.MUSIC_DISC_TEARS, 175, 10);
      register($$0, LAVA_CHICKEN, SoundEvents.MUSIC_DISC_LAVA_CHICKEN, 134, 9);
   }
}
