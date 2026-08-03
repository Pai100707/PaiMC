package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Scoreboard {
   public static final String HIDDEN_SCORE_PREFIX = "#";
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Object2ObjectMap<String, net.minecraft.world.scores.Objective> objectivesByName = new Object2ObjectOpenHashMap(16, 0.5F);
   private final Reference2ObjectMap<ObjectiveCriteria, List<net.minecraft.world.scores.Objective>> objectivesByCriteria = new Reference2ObjectOpenHashMap();
   private final Map<String, net.minecraft.world.scores.PlayerScores> playerScores = new Object2ObjectOpenHashMap(16, 0.5F);
   private final Map<net.minecraft.world.scores.DisplaySlot, net.minecraft.world.scores.Objective> displayObjectives = new EnumMap<>(
      net.minecraft.world.scores.DisplaySlot.class
   );
   private final Object2ObjectMap<String, net.minecraft.world.scores.PlayerTeam> teamsByName = new Object2ObjectOpenHashMap();
   private final Object2ObjectMap<String, net.minecraft.world.scores.PlayerTeam> teamsByPlayer = new Object2ObjectOpenHashMap();

   @Nullable
   public net.minecraft.world.scores.Objective getObjective(@Nullable String $$0) {
      return (net.minecraft.world.scores.Objective)this.objectivesByName.get($$0);
   }

   public net.minecraft.world.scores.Objective addObjective(
      String $$0, ObjectiveCriteria $$1, Component $$2, ObjectiveCriteria.RenderType $$3, boolean $$4, @Nullable NumberFormat $$5
   ) {
      if (this.objectivesByName.containsKey($$0)) {
         throw new IllegalArgumentException("An objective with the name '" + $$0 + "' already exists!");
      } else {
         net.minecraft.world.scores.Objective $$6 = new net.minecraft.world.scores.Objective(this, $$0, $$1, $$2, $$3, $$4, $$5);
         ((List)this.objectivesByCriteria.computeIfAbsent($$1, $$0x -> Lists.newArrayList())).add($$6);
         this.objectivesByName.put($$0, $$6);
         this.onObjectiveAdded($$6);
         return $$6;
      }
   }

   public final void forAllObjectives(ObjectiveCriteria $$0, net.minecraft.world.scores.ScoreHolder $$1, Consumer<net.minecraft.world.scores.ScoreAccess> $$2) {
      ((List)this.objectivesByCriteria.getOrDefault($$0, Collections.emptyList())).forEach($$2x -> $$2.accept(this.getOrCreatePlayerScore($$1, $$2x, true)));
   }

   private net.minecraft.world.scores.PlayerScores getOrCreatePlayerInfo(String $$0) {
      return this.playerScores.computeIfAbsent($$0, $$0x -> new net.minecraft.world.scores.PlayerScores());
   }

   public net.minecraft.world.scores.ScoreAccess getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1) {
      return this.getOrCreatePlayerScore($$0, $$1, false);
   }

   public net.minecraft.world.scores.ScoreAccess getOrCreatePlayerScore(
      final net.minecraft.world.scores.ScoreHolder $$0, final net.minecraft.world.scores.Objective $$1, boolean $$2
   ) {
      final boolean $$3 = $$2 || !$$1.getCriteria().isReadOnly();
      net.minecraft.world.scores.PlayerScores $$4 = this.getOrCreatePlayerInfo($$0.getScoreboardName());
      final MutableBoolean $$5 = new MutableBoolean();
      final net.minecraft.world.scores.Score $$6 = $$4.getOrCreate($$1, $$1x -> $$5.setTrue());
      return new net.minecraft.world.scores.ScoreAccess() {
         @Override
         public int get() {
            return $$6.value();
         }

         @Override
         public void set(int $$0x) {
            if (!$$3) {
               throw new IllegalStateException("Cannot modify read-only score");
            } else {
               boolean $$1x = $$5.isTrue();
               if ($$1.displayAutoUpdate()) {
                  Component $$2x = $$0.getDisplayName();
                  if ($$2x != null && !$$2x.equals($$6.display())) {
                     $$6.display($$2x);
                     $$1x = true;
                  }
               }

               if ($$0 != $$6.value()) {
                  $$6.value($$0);
                  $$1x = true;
               }

               if ($$1x) {
                  this.sendScoreToPlayers();
               }
            }
         }

         @Nullable
         @Override
         public Component display() {
            return $$6.display();
         }

         @Override
         public void display(@Nullable Component $$0x) {
            if ($$5.isTrue() || !Objects.equals($$0, $$6.display())) {
               $$6.display($$0);
               this.sendScoreToPlayers();
            }
         }

         @Override
         public void numberFormatOverride(@Nullable NumberFormat $$0x) {
            $$6.numberFormat($$0);
            this.sendScoreToPlayers();
         }

         @Override
         public boolean locked() {
            return $$6.isLocked();
         }

         @Override
         public void unlock() {
            this.setLocked(false);
         }

         @Override
         public void lock() {
            this.setLocked(true);
         }

         private void setLocked(boolean $$0x) {
            $$6.setLocked($$0);
            if ($$5.isTrue()) {
               this.sendScoreToPlayers();
            }

            Scoreboard.this.onScoreLockChanged($$0, $$1);
         }

         private void sendScoreToPlayers() {
            Scoreboard.this.onScoreChanged($$0, $$1, $$6);
            $$5.setFalse();
         }
      };
   }

   @Nullable
   public net.minecraft.world.scores.ReadOnlyScoreInfo getPlayerScoreInfo(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1) {
      net.minecraft.world.scores.PlayerScores $$2 = this.playerScores.get($$0.getScoreboardName());
      return $$2 != null ? $$2.get($$1) : null;
   }

   public Collection<net.minecraft.world.scores.PlayerScoreEntry> listPlayerScores(net.minecraft.world.scores.Objective $$0) {
      List<net.minecraft.world.scores.PlayerScoreEntry> $$1 = new ArrayList<>();
      this.playerScores.forEach(($$2, $$3) -> {
         net.minecraft.world.scores.Score $$4 = $$3.get($$0);
         if ($$4 != null) {
            $$1.add(new net.minecraft.world.scores.PlayerScoreEntry($$2, $$4.value(), $$4.display(), $$4.numberFormat()));
         }
      });
      return $$1;
   }

   public Collection<net.minecraft.world.scores.Objective> getObjectives() {
      return this.objectivesByName.values();
   }

   public Collection<String> getObjectiveNames() {
      return this.objectivesByName.keySet();
   }

   public Collection<net.minecraft.world.scores.ScoreHolder> getTrackedPlayers() {
      return this.playerScores.keySet().stream().map(net.minecraft.world.scores.ScoreHolder::forNameOnly).toList();
   }

   public void resetAllPlayerScores(net.minecraft.world.scores.ScoreHolder $$0) {
      net.minecraft.world.scores.PlayerScores $$1 = this.playerScores.remove($$0.getScoreboardName());
      if ($$1 != null) {
         this.onPlayerRemoved($$0);
      }
   }

   public void resetSinglePlayerScore(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1) {
      net.minecraft.world.scores.PlayerScores $$2 = this.playerScores.get($$0.getScoreboardName());
      if ($$2 != null) {
         boolean $$3 = $$2.remove($$1);
         if (!$$2.hasScores()) {
            net.minecraft.world.scores.PlayerScores $$4 = this.playerScores.remove($$0.getScoreboardName());
            if ($$4 != null) {
               this.onPlayerRemoved($$0);
            }
         } else if ($$3) {
            this.onPlayerScoreRemoved($$0, $$1);
         }
      }
   }

   public Object2IntMap<net.minecraft.world.scores.Objective> listPlayerScores(net.minecraft.world.scores.ScoreHolder $$0) {
      net.minecraft.world.scores.PlayerScores $$1 = this.playerScores.get($$0.getScoreboardName());
      return $$1 != null ? $$1.listScores() : Object2IntMaps.emptyMap();
   }

   public void removeObjective(net.minecraft.world.scores.Objective $$0) {
      this.objectivesByName.remove($$0.getName());

      for (net.minecraft.world.scores.DisplaySlot $$1 : net.minecraft.world.scores.DisplaySlot.values()) {
         if (this.getDisplayObjective($$1) == $$0) {
            this.setDisplayObjective($$1, null);
         }
      }

      List<net.minecraft.world.scores.Objective> $$2 = (List<net.minecraft.world.scores.Objective>)this.objectivesByCriteria.get($$0.getCriteria());
      if ($$2 != null) {
         $$2.remove($$0);
      }

      for (net.minecraft.world.scores.PlayerScores $$3 : this.playerScores.values()) {
         $$3.remove($$0);
      }

      this.onObjectiveRemoved($$0);
   }

   public void setDisplayObjective(net.minecraft.world.scores.DisplaySlot $$0, @Nullable net.minecraft.world.scores.Objective $$1) {
      this.displayObjectives.put($$0, $$1);
   }

   @Nullable
   public net.minecraft.world.scores.Objective getDisplayObjective(net.minecraft.world.scores.DisplaySlot $$0) {
      return this.displayObjectives.get($$0);
   }

   @Nullable
   public net.minecraft.world.scores.PlayerTeam getPlayerTeam(String $$0) {
      return (net.minecraft.world.scores.PlayerTeam)this.teamsByName.get($$0);
   }

   public net.minecraft.world.scores.PlayerTeam addPlayerTeam(String $$0) {
      net.minecraft.world.scores.PlayerTeam $$1 = this.getPlayerTeam($$0);
      if ($$1 != null) {
         LOGGER.warn("Requested creation of existing team '{}'", $$0);
         return $$1;
      } else {
         $$1 = new net.minecraft.world.scores.PlayerTeam(this, $$0);
         this.teamsByName.put($$0, $$1);
         this.onTeamAdded($$1);
         return $$1;
      }
   }

   public void removePlayerTeam(net.minecraft.world.scores.PlayerTeam $$0) {
      this.teamsByName.remove($$0.getName());

      for (String $$1 : $$0.getPlayers()) {
         this.teamsByPlayer.remove($$1);
      }

      this.onTeamRemoved($$0);
   }

   public boolean addPlayerToTeam(String $$0, net.minecraft.world.scores.PlayerTeam $$1) {
      if (this.getPlayersTeam($$0) != null) {
         this.removePlayerFromTeam($$0);
      }

      this.teamsByPlayer.put($$0, $$1);
      return $$1.getPlayers().add($$0);
   }

   public boolean removePlayerFromTeam(String $$0) {
      net.minecraft.world.scores.PlayerTeam $$1 = this.getPlayersTeam($$0);
      if ($$1 != null) {
         this.removePlayerFromTeam($$0, $$1);
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String $$0, net.minecraft.world.scores.PlayerTeam $$1) {
      if (this.getPlayersTeam($$0) != $$1) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + $$1.getName() + "'.");
      } else {
         this.teamsByPlayer.remove($$0);
         $$1.getPlayers().remove($$0);
      }
   }

   public Collection<String> getTeamNames() {
      return this.teamsByName.keySet();
   }

   public Collection<net.minecraft.world.scores.PlayerTeam> getPlayerTeams() {
      return this.teamsByName.values();
   }

   @Nullable
   public net.minecraft.world.scores.PlayerTeam getPlayersTeam(String $$0) {
      return (net.minecraft.world.scores.PlayerTeam)this.teamsByPlayer.get($$0);
   }

   public void onObjectiveAdded(net.minecraft.world.scores.Objective $$0) {
   }

   public void onObjectiveChanged(net.minecraft.world.scores.Objective $$0) {
   }

   public void onObjectiveRemoved(net.minecraft.world.scores.Objective $$0) {
   }

   protected void onScoreChanged(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1, net.minecraft.world.scores.Score $$2) {
   }

   protected void onScoreLockChanged(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1) {
   }

   public void onPlayerRemoved(net.minecraft.world.scores.ScoreHolder $$0) {
   }

   public void onPlayerScoreRemoved(net.minecraft.world.scores.ScoreHolder $$0, net.minecraft.world.scores.Objective $$1) {
   }

   public void onTeamAdded(net.minecraft.world.scores.PlayerTeam $$0) {
   }

   public void onTeamChanged(net.minecraft.world.scores.PlayerTeam $$0) {
   }

   public void onTeamRemoved(net.minecraft.world.scores.PlayerTeam $$0) {
   }

   public void entityRemoved(Entity $$0) {
      if (!($$0 instanceof Player) && !$$0.isAlive()) {
         this.resetAllPlayerScores($$0);
         this.removePlayerFromTeam($$0.getScoreboardName());
      }
   }

   protected List<net.minecraft.world.scores.Scoreboard.PackedScore> packPlayerScores() {
      return this.playerScores
         .entrySet()
         .stream()
         .flatMap(
            $$0 -> {
               String $$1 = $$0.getKey();
               return $$0.getValue()
                  .listRawScores()
                  .entrySet()
                  .stream()
                  .map(
                     $$1x -> new net.minecraft.world.scores.Scoreboard.PackedScore(
                        $$1, ((net.minecraft.world.scores.Objective)$$1x.getKey()).getName(), ((net.minecraft.world.scores.Score)$$1x.getValue()).pack()
                     )
                  );
            }
         )
         .toList();
   }

   protected void loadPlayerScore(net.minecraft.world.scores.Scoreboard.PackedScore $$0) {
      net.minecraft.world.scores.Objective $$1 = this.getObjective($$0.objective);
      if ($$1 == null) {
         LOGGER.error("Unknown objective {} for name {}, ignoring", $$0.objective, $$0.owner);
      } else {
         this.getOrCreatePlayerInfo($$0.owner).setScore($$1, new net.minecraft.world.scores.Score($$0.score));
      }
   }

   protected List<net.minecraft.world.scores.PlayerTeam.Packed> packPlayerTeams() {
      return this.getPlayerTeams().stream().map(net.minecraft.world.scores.PlayerTeam::pack).toList();
   }

   protected void loadPlayerTeam(net.minecraft.world.scores.PlayerTeam.Packed $$0) {
      net.minecraft.world.scores.PlayerTeam $$1 = this.addPlayerTeam($$0.name());
      $$0.displayName().ifPresent($$1::setDisplayName);
      $$0.color().ifPresent($$1::setColor);
      $$1.setAllowFriendlyFire($$0.allowFriendlyFire());
      $$1.setSeeFriendlyInvisibles($$0.seeFriendlyInvisibles());
      $$1.setPlayerPrefix($$0.memberNamePrefix());
      $$1.setPlayerSuffix($$0.memberNameSuffix());
      $$1.setNameTagVisibility($$0.nameTagVisibility());
      $$1.setDeathMessageVisibility($$0.deathMessageVisibility());
      $$1.setCollisionRule($$0.collisionRule());

      for (String $$2 : $$0.players()) {
         this.addPlayerToTeam($$2, $$1);
      }
   }

   protected List<net.minecraft.world.scores.Objective.Packed> packObjectives() {
      return this.getObjectives().stream().map(net.minecraft.world.scores.Objective::pack).toList();
   }

   protected void loadObjective(net.minecraft.world.scores.Objective.Packed $$0) {
      this.addObjective($$0.name(), $$0.criteria(), $$0.displayName(), $$0.renderType(), $$0.displayAutoUpdate(), $$0.numberFormat().orElse(null));
   }

   protected Map<net.minecraft.world.scores.DisplaySlot, String> packDisplaySlots() {
      Map<net.minecraft.world.scores.DisplaySlot, String> $$0 = new EnumMap<>(net.minecraft.world.scores.DisplaySlot.class);

      for (net.minecraft.world.scores.DisplaySlot $$1 : net.minecraft.world.scores.DisplaySlot.values()) {
         net.minecraft.world.scores.Objective $$2 = this.getDisplayObjective($$1);
         if ($$2 != null) {
            $$0.put($$1, $$2.getName());
         }
      }

      return $$0;
   }

   public record PackedScore(String owner, String objective, net.minecraft.world.scores.Score.Packed score) {
      public static final Codec<net.minecraft.world.scores.Scoreboard.PackedScore> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("Name").forGetter(net.minecraft.world.scores.Scoreboard.PackedScore::owner),
               Codec.STRING.fieldOf("Objective").forGetter(net.minecraft.world.scores.Scoreboard.PackedScore::objective),
               net.minecraft.world.scores.Score.Packed.MAP_CODEC.forGetter(net.minecraft.world.scores.Scoreboard.PackedScore::score)
            )
            .apply($$0, net.minecraft.world.scores.Scoreboard.PackedScore::new)
      );
   }
}
