package net.minecraft.sounds;

import net.minecraft.core.Holder;

public class Musics {
   private static final int ONE_SECOND = 20;
   private static final int THIRTY_SECONDS = 600;
   private static final int TEN_MINUTES = 12000;
   private static final int TWENTY_MINUTES = 24000;
   private static final int FIVE_MINUTES = 6000;
   public static final net.minecraft.sounds.Music MENU = new net.minecraft.sounds.Music(net.minecraft.sounds.SoundEvents.MUSIC_MENU, 20, 600, true);
   public static final net.minecraft.sounds.Music CREATIVE = new net.minecraft.sounds.Music(
      net.minecraft.sounds.SoundEvents.MUSIC_CREATIVE, 12000, 24000, false
   );
   public static final net.minecraft.sounds.Music CREDITS = new net.minecraft.sounds.Music(net.minecraft.sounds.SoundEvents.MUSIC_CREDITS, 0, 0, true);
   public static final net.minecraft.sounds.Music END_BOSS = new net.minecraft.sounds.Music(net.minecraft.sounds.SoundEvents.MUSIC_DRAGON, 0, 0, true);
   public static final net.minecraft.sounds.Music END = new net.minecraft.sounds.Music(net.minecraft.sounds.SoundEvents.MUSIC_END, 6000, 24000, true);
   public static final net.minecraft.sounds.Music UNDER_WATER = createGameMusic(net.minecraft.sounds.SoundEvents.MUSIC_UNDER_WATER);
   public static final net.minecraft.sounds.Music GAME = createGameMusic(net.minecraft.sounds.SoundEvents.MUSIC_GAME);

   public static net.minecraft.sounds.Music createGameMusic(Holder<net.minecraft.sounds.SoundEvent> $$0) {
      return new net.minecraft.sounds.Music($$0, 12000, 24000, false);
   }
}
