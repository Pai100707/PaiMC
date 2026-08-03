package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;

public class LocalMobCapCalculator {
   private final Long2ObjectMap<List<ServerPlayer>> playersNearChunk = new Long2ObjectOpenHashMap();
   private final Map<ServerPlayer, net.minecraft.world.level.LocalMobCapCalculator.MobCounts> playerMobCounts = Maps.newHashMap();
   private final ChunkMap chunkMap;

   public LocalMobCapCalculator(ChunkMap $$0) {
      this.chunkMap = $$0;
   }

   private List<ServerPlayer> getPlayersNear(net.minecraft.world.level.ChunkPos $$0) {
      return (List<ServerPlayer>)this.playersNearChunk.computeIfAbsent($$0.toLong(), $$1 -> this.chunkMap.getPlayersCloseForSpawning($$0));
   }

   public void addMob(net.minecraft.world.level.ChunkPos $$0, MobCategory $$1) {
      for (ServerPlayer $$2 : this.getPlayersNear($$0)) {
         this.playerMobCounts.computeIfAbsent($$2, $$0x -> new net.minecraft.world.level.LocalMobCapCalculator.MobCounts()).add($$1);
      }
   }

   public boolean canSpawn(MobCategory $$0, net.minecraft.world.level.ChunkPos $$1) {
      for (ServerPlayer $$2 : this.getPlayersNear($$1)) {
         net.minecraft.world.level.LocalMobCapCalculator.MobCounts $$3 = this.playerMobCounts.get($$2);
         if ($$3 == null || $$3.canSpawn($$0)) {
            return true;
         }
      }

      return false;
   }

   static class MobCounts {
      private final Object2IntMap<MobCategory> counts = new Object2IntOpenHashMap(MobCategory.values().length);

      public void add(MobCategory $$0) {
         this.counts.computeInt($$0, ($$0x, $$1) -> $$1 == null ? 1 : $$1 + 1);
      }

      public boolean canSpawn(MobCategory $$0) {
         return this.counts.getOrDefault($$0, 0) < $$0.getMaxInstancesPerChunk();
      }
   }
}
