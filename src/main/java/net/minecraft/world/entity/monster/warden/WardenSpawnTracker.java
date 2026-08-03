package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
   public static final Codec<WardenSpawnTracker> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter($$0x -> $$0x.ticksSinceLastWarning),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter($$0x -> $$0x.warningLevel),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter($$0x -> $$0x.cooldownTicks)
         )
         .apply($$0, WardenSpawnTracker::new)
   );
   public static final int MAX_WARNING_LEVEL = 4;
   private static final double PLAYER_SEARCH_RADIUS = 16.0;
   private static final int WARNING_CHECK_DIAMETER = 48;
   private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
   private static final int WARNING_LEVEL_INCREASE_COOLDOWN = 200;
   private int ticksSinceLastWarning;
   private int warningLevel;
   private int cooldownTicks;

   public WardenSpawnTracker(int $$0, int $$1, int $$2) {
      this.ticksSinceLastWarning = $$0;
      this.warningLevel = $$1;
      this.cooldownTicks = $$2;
   }

   public WardenSpawnTracker() {
      this(0, 0, 0);
   }

   public void tick() {
      if (this.ticksSinceLastWarning >= 12000) {
         this.decreaseWarningLevel();
         this.ticksSinceLastWarning = 0;
      } else {
         this.ticksSinceLastWarning++;
      }

      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
      }
   }

   public void reset() {
      this.ticksSinceLastWarning = 0;
      this.warningLevel = 0;
      this.cooldownTicks = 0;
   }

   public static OptionalInt tryWarn(ServerLevel $$0, BlockPos $$1, ServerPlayer $$2) {
      if (hasNearbyWarden($$0, $$1)) {
         return OptionalInt.empty();
      } else {
         List<ServerPlayer> $$3 = getNearbyPlayers($$0, $$1);
         if (!$$3.contains($$2)) {
            $$3.add($$2);
         }

         if ($$3.stream().anyMatch($$0x -> $$0x.getWardenSpawnTracker().map(WardenSpawnTracker::onCooldown).orElse(false))) {
            return OptionalInt.empty();
         } else {
            Optional<WardenSpawnTracker> $$4 = $$3.stream()
               .flatMap($$0x -> $$0x.getWardenSpawnTracker().stream())
               .max(Comparator.comparingInt(WardenSpawnTracker::getWarningLevel));
            if ($$4.isPresent()) {
               WardenSpawnTracker $$5 = $$4.get();
               $$5.increaseWarningLevel();
               $$3.forEach($$1x -> $$1x.getWardenSpawnTracker().ifPresent($$1xx -> $$1xx.copyData($$5)));
               return OptionalInt.of($$5.warningLevel);
            } else {
               return OptionalInt.empty();
            }
         }
      }
   }

   private boolean onCooldown() {
      return this.cooldownTicks > 0;
   }

   private static boolean hasNearbyWarden(ServerLevel $$0, BlockPos $$1) {
      AABB $$2 = AABB.ofSize(Vec3.atCenterOf($$1), 48.0, 48.0, 48.0);
      return !$$0.getEntitiesOfClass(Warden.class, $$2).isEmpty();
   }

   private static List<ServerPlayer> getNearbyPlayers(ServerLevel $$0, BlockPos $$1) {
      Vec3 $$2 = Vec3.atCenterOf($$1);
      return $$0.getPlayers($$1x -> !$$1x.isSpectator() && $$1x.position().closerThan($$2, 16.0) && $$1x.isAlive());
   }

   private void increaseWarningLevel() {
      if (!this.onCooldown()) {
         this.ticksSinceLastWarning = 0;
         this.cooldownTicks = 200;
         this.setWarningLevel(this.getWarningLevel() + 1);
      }
   }

   private void decreaseWarningLevel() {
      this.setWarningLevel(this.getWarningLevel() - 1);
   }

   public void setWarningLevel(int $$0) {
      this.warningLevel = Mth.clamp($$0, 0, 4);
   }

   public int getWarningLevel() {
      return this.warningLevel;
   }

   private void copyData(WardenSpawnTracker $$0) {
      this.warningLevel = $$0.warningLevel;
      this.cooldownTicks = $$0.cooldownTicks;
      this.ticksSinceLastWarning = $$0.ticksSinceLastWarning;
   }
}
