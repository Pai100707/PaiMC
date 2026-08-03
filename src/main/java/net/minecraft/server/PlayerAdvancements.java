package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger.Listener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancements {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final PlayerList playerList;
   private final Path playerSavePath;
   private AdvancementTree tree;
   private final Map<AdvancementHolder, AdvancementProgress> progress = new LinkedHashMap<>();
   private final Set<AdvancementHolder> visible = new HashSet<>();
   private final Set<AdvancementHolder> progressChanged = new HashSet<>();
   private final Set<AdvancementNode> rootsToUpdate = new HashSet<>();
   private ServerPlayer player;
   @Nullable
   private AdvancementHolder lastSelectedTab;
   private boolean isFirstPacket = true;
   private final Codec<net.minecraft.server.PlayerAdvancements.Data> codec;

   public PlayerAdvancements(DataFixer $$0, PlayerList $$1, net.minecraft.server.ServerAdvancementManager $$2, Path $$3, ServerPlayer $$4) {
      this.playerList = $$1;
      this.playerSavePath = $$3;
      this.player = $$4;
      this.tree = $$2.tree();
      int $$5 = 1343;
      this.codec = DataFixTypes.ADVANCEMENTS.wrapCodec(net.minecraft.server.PlayerAdvancements.Data.CODEC, $$0, 1343);
      this.load($$2);
   }

   public void setPlayer(ServerPlayer $$0) {
      this.player = $$0;
   }

   public void stopListening() {
      for (CriterionTrigger<?> $$0 : BuiltInRegistries.TRIGGER_TYPES) {
         $$0.removePlayerListeners(this);
      }
   }

   public void reload(net.minecraft.server.ServerAdvancementManager $$0) {
      this.stopListening();
      this.progress.clear();
      this.visible.clear();
      this.rootsToUpdate.clear();
      this.progressChanged.clear();
      this.isFirstPacket = true;
      this.lastSelectedTab = null;
      this.tree = $$0.tree();
      this.load($$0);
   }

   private void registerListeners(net.minecraft.server.ServerAdvancementManager $$0) {
      for (AdvancementHolder $$1 : $$0.getAllAdvancements()) {
         this.registerListeners($$1);
      }
   }

   private void checkForAutomaticTriggers(net.minecraft.server.ServerAdvancementManager $$0) {
      for (AdvancementHolder $$1 : $$0.getAllAdvancements()) {
         Advancement $$2 = $$1.value();
         if ($$2.criteria().isEmpty()) {
            this.award($$1, "");
            $$2.rewards().grant(this.player);
         }
      }
   }

   private void load(net.minecraft.server.ServerAdvancementManager $$0) {
      if (Files.isRegularFile(this.playerSavePath)) {
         try (Reader $$1 = Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8)) {
            JsonElement $$2 = StrictJsonParser.parse($$1);
            net.minecraft.server.PlayerAdvancements.Data $$3 = (net.minecraft.server.PlayerAdvancements.Data)this.codec
               .parse(JsonOps.INSTANCE, $$2)
               .getOrThrow(JsonParseException::new);
            this.applyFrom($$0, $$3);
         } catch (JsonIOException | IOException var7) {
            LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, var7);
         } catch (JsonParseException var8) {
            LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, var8);
         }
      }

      this.checkForAutomaticTriggers($$0);
      this.registerListeners($$0);
   }

   public void save() {
      JsonElement $$0 = (JsonElement)this.codec.encodeStart(JsonOps.INSTANCE, this.asData()).getOrThrow();

      try {
         FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());

         try (Writer $$1 = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8)) {
            GSON.toJson($$0, GSON.newJsonWriter($$1));
         }
      } catch (JsonIOException | IOException var7) {
         LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, var7);
      }
   }

   private void applyFrom(net.minecraft.server.ServerAdvancementManager $$0, net.minecraft.server.PlayerAdvancements.Data $$1) {
      $$1.forEach(($$1x, $$2) -> {
         AdvancementHolder $$3 = $$0.get($$1x);
         if ($$3 == null) {
            LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", $$1x, this.playerSavePath);
         } else {
            this.startProgress($$3, $$2);
            this.progressChanged.add($$3);
            this.markForVisibilityUpdate($$3);
         }
      });
   }

   private net.minecraft.server.PlayerAdvancements.Data asData() {
      Map<Identifier, AdvancementProgress> $$0 = new LinkedHashMap<>();
      this.progress.forEach(($$1, $$2) -> {
         if ($$2.hasProgress()) {
            $$0.put($$1.id(), $$2);
         }
      });
      return new net.minecraft.server.PlayerAdvancements.Data($$0);
   }

   public boolean award(AdvancementHolder $$0, String $$1) {
      boolean $$2 = false;
      AdvancementProgress $$3 = this.getOrStartProgress($$0);
      boolean $$4 = $$3.isDone();
      if ($$3.grantProgress($$1)) {
         this.unregisterListeners($$0);
         this.progressChanged.add($$0);
         $$2 = true;
         if (!$$4 && $$3.isDone()) {
            $$0.value().rewards().grant(this.player);
            $$0.value().display().ifPresent($$1x -> {
               if ($$1x.shouldAnnounceChat() && (Boolean)this.player.level().getGameRules().get(GameRules.SHOW_ADVANCEMENT_MESSAGES)) {
                  this.playerList.broadcastSystemMessage($$1x.getType().createAnnouncement($$0, this.player), false);
               }
            });
         }
      }

      if (!$$4 && $$3.isDone()) {
         this.markForVisibilityUpdate($$0);
      }

      return $$2;
   }

   public boolean revoke(AdvancementHolder $$0, String $$1) {
      boolean $$2 = false;
      AdvancementProgress $$3 = this.getOrStartProgress($$0);
      boolean $$4 = $$3.isDone();
      if ($$3.revokeProgress($$1)) {
         this.registerListeners($$0);
         this.progressChanged.add($$0);
         $$2 = true;
      }

      if ($$4 && !$$3.isDone()) {
         this.markForVisibilityUpdate($$0);
      }

      return $$2;
   }

   private void markForVisibilityUpdate(AdvancementHolder $$0) {
      AdvancementNode $$1 = this.tree.get($$0);
      if ($$1 != null) {
         this.rootsToUpdate.add($$1.root());
      }
   }

   private void registerListeners(AdvancementHolder $$0) {
      AdvancementProgress $$1 = this.getOrStartProgress($$0);
      if (!$$1.isDone()) {
         for (Entry<String, Criterion<?>> $$2 : $$0.value().criteria().entrySet()) {
            CriterionProgress $$3 = $$1.getCriterion($$2.getKey());
            if ($$3 != null && !$$3.isDone()) {
               this.registerListener($$0, $$2.getKey(), $$2.getValue());
            }
         }
      }
   }

   private <T extends CriterionTriggerInstance> void registerListener(AdvancementHolder $$0, String $$1, Criterion<T> $$2) {
      $$2.trigger().addPlayerListener(this, new Listener($$2.triggerInstance(), $$0, $$1));
   }

   private void unregisterListeners(AdvancementHolder $$0) {
      AdvancementProgress $$1 = this.getOrStartProgress($$0);

      for (Entry<String, Criterion<?>> $$2 : $$0.value().criteria().entrySet()) {
         CriterionProgress $$3 = $$1.getCriterion($$2.getKey());
         if ($$3 != null && ($$3.isDone() || $$1.isDone())) {
            this.removeListener($$0, $$2.getKey(), $$2.getValue());
         }
      }
   }

   private <T extends CriterionTriggerInstance> void removeListener(AdvancementHolder $$0, String $$1, Criterion<T> $$2) {
      $$2.trigger().removePlayerListener(this, new Listener($$2.triggerInstance(), $$0, $$1));
   }

   public void flushDirty(ServerPlayer $$0, boolean $$1) {
      if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
         Map<Identifier, AdvancementProgress> $$2 = new HashMap<>();
         Set<AdvancementHolder> $$3 = new HashSet<>();
         Set<Identifier> $$4 = new HashSet<>();

         for (AdvancementNode $$5 : this.rootsToUpdate) {
            this.updateTreeVisibility($$5, $$3, $$4);
         }

         this.rootsToUpdate.clear();

         for (AdvancementHolder $$6 : this.progressChanged) {
            if (this.visible.contains($$6)) {
               $$2.put($$6.id(), this.progress.get($$6));
            }
         }

         this.progressChanged.clear();
         if (!$$2.isEmpty() || !$$3.isEmpty() || !$$4.isEmpty()) {
            $$0.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, $$3, $$4, $$2, $$1));
         }
      }

      this.isFirstPacket = false;
   }

   public void setSelectedTab(@Nullable AdvancementHolder $$0) {
      AdvancementHolder $$1 = this.lastSelectedTab;
      if ($$0 != null && $$0.value().isRoot() && $$0.value().display().isPresent()) {
         this.lastSelectedTab = $$0;
      } else {
         this.lastSelectedTab = null;
      }

      if ($$1 != this.lastSelectedTab) {
         this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.id()));
      }
   }

   public AdvancementProgress getOrStartProgress(AdvancementHolder $$0) {
      AdvancementProgress $$1 = this.progress.get($$0);
      if ($$1 == null) {
         $$1 = new AdvancementProgress();
         this.startProgress($$0, $$1);
      }

      return $$1;
   }

   private void startProgress(AdvancementHolder $$0, AdvancementProgress $$1) {
      $$1.update($$0.value().requirements());
      this.progress.put($$0, $$1);
   }

   private void updateTreeVisibility(AdvancementNode $$0, Set<AdvancementHolder> $$1, Set<Identifier> $$2) {
      AdvancementVisibilityEvaluator.evaluateVisibility($$0, $$0x -> this.getOrStartProgress($$0x.holder()).isDone(), ($$2x, $$3) -> {
         AdvancementHolder $$4 = $$2x.holder();
         if ($$3) {
            if (this.visible.add($$4)) {
               $$1.add($$4);
               if (this.progress.containsKey($$4)) {
                  this.progressChanged.add($$4);
               }
            }
         } else if (this.visible.remove($$4)) {
            $$2.add($$4.id());
         }
      });
   }

   record Data(Map<Identifier, AdvancementProgress> map) {
      public static final Codec<net.minecraft.server.PlayerAdvancements.Data> CODEC = Codec.unboundedMap(Identifier.CODEC, AdvancementProgress.CODEC)
         .xmap(net.minecraft.server.PlayerAdvancements.Data::new, net.minecraft.server.PlayerAdvancements.Data::map);

      public void forEach(BiConsumer<Identifier, AdvancementProgress> $$0) {
         this.map.entrySet().stream().sorted(Entry.comparingByValue()).forEach($$1 -> $$0.accept($$1.getKey(), $$1.getValue()));
      }
   }
}
