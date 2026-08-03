package net.minecraft.world;

import com.mojang.serialization.Codec;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public abstract class BossEvent {
   private final UUID id;
   protected Component name;
   protected float progress;
   protected BossEvent.BossBarColor color;
   protected BossEvent.BossBarOverlay overlay;
   protected boolean darkenScreen;
   protected boolean playBossMusic;
   protected boolean createWorldFog;

   public BossEvent(UUID $$0, Component $$1, BossEvent.BossBarColor $$2, BossEvent.BossBarOverlay $$3) {
      this.id = $$0;
      this.name = $$1;
      this.color = $$2;
      this.overlay = $$3;
      this.progress = 1.0F;
   }

   public UUID getId() {
      return this.id;
   }

   public Component getName() {
      return this.name;
   }

   public void setName(Component $$0) {
      this.name = $$0;
   }

   public float getProgress() {
      return this.progress;
   }

   public void setProgress(float $$0) {
      this.progress = $$0;
   }

   public BossEvent.BossBarColor getColor() {
      return this.color;
   }

   public void setColor(BossEvent.BossBarColor $$0) {
      this.color = $$0;
   }

   public BossEvent.BossBarOverlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossEvent.BossBarOverlay $$0) {
      this.overlay = $$0;
   }

   public boolean shouldDarkenScreen() {
      return this.darkenScreen;
   }

   public BossEvent setDarkenScreen(boolean $$0) {
      this.darkenScreen = $$0;
      return this;
   }

   public boolean shouldPlayBossMusic() {
      return this.playBossMusic;
   }

   public BossEvent setPlayBossMusic(boolean $$0) {
      this.playBossMusic = $$0;
      return this;
   }

   public BossEvent setCreateWorldFog(boolean $$0) {
      this.createWorldFog = $$0;
      return this;
   }

   public boolean shouldCreateWorldFog() {
      return this.createWorldFog;
   }

   public static enum BossBarColor implements StringRepresentable {
      PINK("pink", ChatFormatting.RED),
      BLUE("blue", ChatFormatting.BLUE),
      RED("red", ChatFormatting.DARK_RED),
      GREEN("green", ChatFormatting.GREEN),
      YELLOW("yellow", ChatFormatting.YELLOW),
      PURPLE("purple", ChatFormatting.DARK_BLUE),
      WHITE("white", ChatFormatting.WHITE);

      public static final Codec<BossEvent.BossBarColor> CODEC = StringRepresentable.fromEnum(BossEvent.BossBarColor::values);
      private final String name;
      private final ChatFormatting formatting;

      private BossBarColor(final String $$0, final ChatFormatting $$1) {
         this.name = $$0;
         this.formatting = $$1;
      }

      public ChatFormatting getFormatting() {
         return this.formatting;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static enum BossBarOverlay implements StringRepresentable {
      PROGRESS("progress"),
      NOTCHED_6("notched_6"),
      NOTCHED_10("notched_10"),
      NOTCHED_12("notched_12"),
      NOTCHED_20("notched_20");

      public static final Codec<BossEvent.BossBarOverlay> CODEC = StringRepresentable.fromEnum(BossEvent.BossBarOverlay::values);
      private final String name;

      private BossBarOverlay(final String $$0) {
         this.name = $$0;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
