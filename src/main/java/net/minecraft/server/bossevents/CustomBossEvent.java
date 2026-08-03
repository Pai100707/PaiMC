package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

public class CustomBossEvent extends ServerBossEvent {
   private static final int DEFAULT_MAX = 100;
   private final Identifier id;
   private final Set<UUID> players = Sets.newHashSet();
   private int value;
   private int max = 100;

   public CustomBossEvent(Identifier $$0, Component $$1) {
      super($$1, BossBarColor.WHITE, BossBarOverlay.PROGRESS);
      this.id = $$0;
      this.setProgress(0.0F);
   }

   public Identifier getTextId() {
      return this.id;
   }

   @Override
   public void addPlayer(ServerPlayer $$0) {
      super.addPlayer($$0);
      this.players.add($$0.getUUID());
   }

   public void addOfflinePlayer(UUID $$0) {
      this.players.add($$0);
   }

   @Override
   public void removePlayer(ServerPlayer $$0) {
      super.removePlayer($$0);
      this.players.remove($$0.getUUID());
   }

   @Override
   public void removeAllPlayers() {
      super.removeAllPlayers();
      this.players.clear();
   }

   public int getValue() {
      return this.value;
   }

   public int getMax() {
      return this.max;
   }

   public void setValue(int $$0) {
      this.value = $$0;
      this.setProgress(Mth.clamp((float)$$0 / this.max, 0.0F, 1.0F));
   }

   public void setMax(int $$0) {
      this.max = $$0;
      this.setProgress(Mth.clamp((float)this.value / $$0, 0.0F, 1.0F));
   }

   public final Component getDisplayName() {
      return ComponentUtils.wrapInSquareBrackets(this.getName())
         .withStyle(
            $$0 -> $$0.withColor(this.getColor().getFormatting())
               .withHoverEvent(new ShowText(Component.literal(this.getTextId().toString())))
               .withInsertion(this.getTextId().toString())
         );
   }

   public boolean setPlayers(Collection<ServerPlayer> $$0) {
      Set<UUID> $$1 = Sets.newHashSet();
      Set<ServerPlayer> $$2 = Sets.newHashSet();

      for (UUID $$3 : this.players) {
         boolean $$4 = false;

         for (ServerPlayer $$5 : $$0) {
            if ($$5.getUUID().equals($$3)) {
               $$4 = true;
               break;
            }
         }

         if (!$$4) {
            $$1.add($$3);
         }
      }

      for (ServerPlayer $$6 : $$0) {
         boolean $$7 = false;

         for (UUID $$8 : this.players) {
            if ($$6.getUUID().equals($$8)) {
               $$7 = true;
               break;
            }
         }

         if (!$$7) {
            $$2.add($$6);
         }
      }

      for (UUID $$9 : $$1) {
         for (ServerPlayer $$10 : this.getPlayers()) {
            if ($$10.getUUID().equals($$9)) {
               this.removePlayer($$10);
               break;
            }
         }

         this.players.remove($$9);
      }

      for (ServerPlayer $$11 : $$2) {
         this.addPlayer($$11);
      }

      return !$$1.isEmpty() || !$$2.isEmpty();
   }

   public static CustomBossEvent load(Identifier $$0, CustomBossEvent.Packed $$1) {
      CustomBossEvent $$2 = new CustomBossEvent($$0, $$1.name);
      $$2.setVisible($$1.visible);
      $$2.setValue($$1.value);
      $$2.setMax($$1.max);
      $$2.setColor($$1.color);
      $$2.setOverlay($$1.overlay);
      $$2.setDarkenScreen($$1.darkenScreen);
      $$2.setPlayBossMusic($$1.playBossMusic);
      $$2.setCreateWorldFog($$1.createWorldFog);
      $$1.players.forEach($$2::addOfflinePlayer);
      return $$2;
   }

   public CustomBossEvent.Packed pack() {
      return new CustomBossEvent.Packed(
         this.getName(),
         this.isVisible(),
         this.getValue(),
         this.getMax(),
         this.getColor(),
         this.getOverlay(),
         this.shouldDarkenScreen(),
         this.shouldPlayBossMusic(),
         this.shouldCreateWorldFog(),
         Set.copyOf(this.players)
      );
   }

   public void onPlayerConnect(ServerPlayer $$0) {
      if (this.players.contains($$0.getUUID())) {
         this.addPlayer($$0);
      }
   }

   public void onPlayerDisconnect(ServerPlayer $$0) {
      super.removePlayer($$0);
   }

   public record Packed(
      Component name,
      boolean visible,
      int value,
      int max,
      BossBarColor color,
      BossBarOverlay overlay,
      boolean darkenScreen,
      boolean playBossMusic,
      boolean createWorldFog,
      Set<UUID> players
   ) {
      public static final Codec<CustomBossEvent.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ComponentSerialization.CODEC.fieldOf("Name").forGetter(CustomBossEvent.Packed::name),
               Codec.BOOL.optionalFieldOf("Visible", false).forGetter(CustomBossEvent.Packed::visible),
               Codec.INT.optionalFieldOf("Value", 0).forGetter(CustomBossEvent.Packed::value),
               Codec.INT.optionalFieldOf("Max", 100).forGetter(CustomBossEvent.Packed::max),
               BossBarColor.CODEC.optionalFieldOf("Color", BossBarColor.WHITE).forGetter(CustomBossEvent.Packed::color),
               BossBarOverlay.CODEC.optionalFieldOf("Overlay", BossBarOverlay.PROGRESS).forGetter(CustomBossEvent.Packed::overlay),
               Codec.BOOL.optionalFieldOf("DarkenScreen", false).forGetter(CustomBossEvent.Packed::darkenScreen),
               Codec.BOOL.optionalFieldOf("PlayBossMusic", false).forGetter(CustomBossEvent.Packed::playBossMusic),
               Codec.BOOL.optionalFieldOf("CreateWorldFog", false).forGetter(CustomBossEvent.Packed::createWorldFog),
               UUIDUtil.CODEC_SET.optionalFieldOf("Players", Set.of()).forGetter(CustomBossEvent.Packed::players)
            )
            .apply($$0, CustomBossEvent.Packed::new)
      );
   }
}
