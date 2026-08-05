package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor
   extends net.minecraft.world.level.CommonLevelAccessor,
   net.minecraft.world.level.LevelReader,
   net.minecraft.world.level.ScheduledTickAccess {
   long nextSubTickCount();

   @Override
   default <T> ScheduledTick<T> createTick(BlockPos $$0, T $$1, int $$2, TickPriority $$3) {
      return new ScheduledTick($$1, $$0, this.getGameTime() + $$2, $$3, this.nextSubTickCount());
   }

   @Override
   default <T> ScheduledTick<T> createTick(BlockPos $$0, T $$1, int $$2) {
      return new ScheduledTick($$1, $$0, this.getGameTime() + $$2, this.nextSubTickCount());
   }

   LevelData getLevelData();

   default long getGameTime() {
      return this.getLevelData().getGameTime();
   }

   
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   ChunkSource getChunkSource();

   @Override
   default boolean hasChunk(int $$0, int $$1) {
      return this.getChunkSource().hasChunk($$0, $$1);
   }

   RandomSource getRandom();

   default void updateNeighborsAt(BlockPos $$0, Block $$1) {
   }

   default void neighborShapeChanged(Direction $$0, BlockPos $$1, BlockPos $$2, BlockState $$3, @Block.UpdateFlags int $$4, int $$5) {
      NeighborUpdater.executeShapeUpdate(this, $$0, $$1, $$2, $$3, $$4, $$5 - 1);
   }

   default void playSound(Entity $$0, BlockPos $$1, SoundEvent $$2, SoundSource $$3) {
      this.playSound($$0, $$1, $$2, $$3, 1.0F, 1.0F);
   }

   void playSound(Entity var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

   void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

   void levelEvent(Entity var1, int var2, BlockPos var3, int var4);

   default void levelEvent(int $$0, BlockPos $$1, int $$2) {
      this.levelEvent(null, $$0, $$1, $$2);
   }

   void gameEvent(Holder<GameEvent> var1, Vec3 var2, GameEvent.Context var3);

   default void gameEvent(Entity $$0, Holder<GameEvent> $$1, Vec3 $$2) {
      this.gameEvent($$1, $$2, new GameEvent.Context($$0, null));
   }

   default void gameEvent(Entity $$0, Holder<GameEvent> $$1, BlockPos $$2) {
      this.gameEvent($$1, $$2, new GameEvent.Context($$0, null));
   }

   default void gameEvent(Holder<GameEvent> $$0, BlockPos $$1, GameEvent.Context $$2) {
      this.gameEvent($$0, Vec3.atCenterOf($$1), $$2);
   }

   default void gameEvent(ResourceKey<GameEvent> $$0, BlockPos $$1, GameEvent.Context $$2) {
      this.gameEvent(this.registryAccess().lookupOrThrow(Registries.GAME_EVENT).getOrThrow($$0), $$1, $$2);
   }
}
