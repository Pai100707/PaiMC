package net.minecraft.server.level;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PlayerSpawnFinder {
   private static final EntityDimensions PLAYER_DIMENSIONS = EntityType.PLAYER.getDimensions();
   private static final int ABSOLUTE_MAX_ATTEMPTS = 1024;
   private final ServerLevel level;
   private final BlockPos spawnSuggestion;
   private final int radius;
   private final int candidateCount;
   private final int coprime;
   private final int offset;
   private int nextCandidateIndex;
   private final CompletableFuture<Vec3> finishedFuture = new CompletableFuture<>();

   private PlayerSpawnFinder(ServerLevel $$0, BlockPos $$1, int $$2) {
      this.level = $$0;
      this.spawnSuggestion = $$1;
      this.radius = $$2;
      long $$3 = $$2 * 2L + 1L;
      this.candidateCount = (int)Math.min(1024L, $$3 * $$3);
      this.coprime = getCoprime(this.candidateCount);
      this.offset = RandomSource.create().nextInt(this.candidateCount);
   }

   public static CompletableFuture<Vec3> findSpawn(ServerLevel $$0, BlockPos $$1) {
      if ($$0.dimensionType().hasSkyLight() && $$0.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
         int $$2 = Math.max(0, (Integer)$$0.getGameRules().get(GameRules.RESPAWN_RADIUS));
         int $$3 = Mth.floor($$0.getWorldBorder().getDistanceToBorder($$1.getX(), $$1.getZ()));
         if ($$3 < $$2) {
            $$2 = $$3;
         }

         if ($$3 <= 1) {
            $$2 = 1;
         }

         PlayerSpawnFinder $$4 = new PlayerSpawnFinder($$0, $$1, $$2);
         $$4.scheduleNext();
         return $$4.finishedFuture;
      } else {
         return CompletableFuture.completedFuture(fixupSpawnHeight($$0, $$1));
      }
   }

   private void scheduleNext() {
      int $$0 = this.nextCandidateIndex++;
      if ($$0 < this.candidateCount) {
         int $$1 = (this.offset + this.coprime * $$0) % this.candidateCount;
         int $$2 = $$1 % (this.radius * 2 + 1);
         int $$3 = $$1 / (this.radius * 2 + 1);
         int $$4 = this.spawnSuggestion.getX() + $$2 - this.radius;
         int $$5 = this.spawnSuggestion.getZ() + $$3 - this.radius;
         this.scheduleCandidate($$4, $$5, $$0, () -> {
            BlockPos $$2x = getOverworldRespawnPos(this.level, $$4, $$5);
            return $$2x != null && noCollisionNoLiquid(this.level, $$2x) ? Optional.of(Vec3.atBottomCenterOf($$2x)) : Optional.empty();
         });
      } else {
         this.scheduleCandidate(
            this.spawnSuggestion.getX(), this.spawnSuggestion.getZ(), $$0, () -> Optional.of(fixupSpawnHeight(this.level, this.spawnSuggestion))
         );
      }
   }

   private static Vec3 fixupSpawnHeight(CollisionGetter $$0, BlockPos $$1) {
      MutableBlockPos $$2 = $$1.mutable();

      while (!noCollisionNoLiquid($$0, $$2) && $$2.getY() < $$0.getMaxY()) {
         $$2.move(Direction.UP);
      }

      $$2.move(Direction.DOWN);

      while (noCollisionNoLiquid($$0, $$2) && $$2.getY() > $$0.getMinY()) {
         $$2.move(Direction.DOWN);
      }

      $$2.move(Direction.UP);
      return Vec3.atBottomCenterOf($$2);
   }

   private static boolean noCollisionNoLiquid(CollisionGetter $$0, BlockPos $$1) {
      return $$0.noCollision(null, PLAYER_DIMENSIONS.makeBoundingBox($$1.getBottomCenter()), true);
   }

   private static int getCoprime(int $$0) {
      return $$0 <= 16 ? $$0 - 1 : 17;
   }

   private void scheduleCandidate(int $$0, int $$1, int $$2, Supplier<Optional<Vec3>> $$3) {
      if (!this.finishedFuture.isDone()) {
         int $$4 = SectionPos.blockToSectionCoord($$0);
         int $$5 = SectionPos.blockToSectionCoord($$1);
         this.level.getChunkSource().addTicketAndLoadWithRadius(TicketType.SPAWN_SEARCH, new ChunkPos($$4, $$5), 0).whenCompleteAsync(($$4x, $$5x) -> {
            if ($$5x == null) {
               try {
                  Optional<Vec3> $$6 = $$3.get();
                  if ($$6.isPresent()) {
                     this.finishedFuture.complete($$6.get());
                  } else {
                     this.scheduleNext();
                  }
               } catch (Throwable var9) {
                  $$5x = var9;
               }
            }

            if ($$5x != null) {
               CrashReport $$8 = CrashReport.forThrowable($$5x, "Searching for spawn");
               CrashReportCategory $$9 = $$8.addCategory("Spawn Lookup");
               $$9.setDetail("Origin", this.spawnSuggestion::toString);
               $$9.setDetail("Radius", () -> Integer.toString(this.radius));
               $$9.setDetail("Candidate", () -> "[" + $$0 + "," + $$1 + "]");
               $$9.setDetail("Progress", () -> $$2 + " out of " + this.candidateCount);
               this.finishedFuture.completeExceptionally(new ReportedException($$8));
            }
         }, this.level.getServer());
      }
   }

   @Nullable
   protected static BlockPos getOverworldRespawnPos(ServerLevel $$0, int $$1, int $$2) {
      boolean $$3 = $$0.dimensionType().hasCeiling();
      LevelChunk $$4 = $$0.getChunk(SectionPos.blockToSectionCoord($$1), SectionPos.blockToSectionCoord($$2));
      int $$5 = $$3 ? $$0.getChunkSource().getGenerator().getSpawnHeight($$0) : $$4.getHeight(Types.MOTION_BLOCKING, $$1 & 15, $$2 & 15);
      if ($$5 < $$0.getMinY()) {
         return null;
      } else {
         int $$6 = $$4.getHeight(Types.WORLD_SURFACE, $$1 & 15, $$2 & 15);
         if ($$6 <= $$5 && $$6 > $$4.getHeight(Types.OCEAN_FLOOR, $$1 & 15, $$2 & 15)) {
            return null;
         } else {
            MutableBlockPos $$7 = new MutableBlockPos();

            for (int $$8 = $$5 + 1; $$8 >= $$0.getMinY(); $$8--) {
               $$7.set($$1, $$8, $$2);
               BlockState $$9 = $$0.getBlockState($$7);
               if (!$$9.getFluidState().isEmpty()) {
                  break;
               }

               if (Block.isFaceFull($$9.getCollisionShape($$0, $$7), Direction.UP)) {
                  return $$7.above().immutable();
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static BlockPos getSpawnPosInChunk(ServerLevel $$0, ChunkPos $$1) {
      if (SharedConstants.debugVoidTerrain($$1)) {
         return null;
      } else {
         for (int $$2 = $$1.getMinBlockX(); $$2 <= $$1.getMaxBlockX(); $$2++) {
            for (int $$3 = $$1.getMinBlockZ(); $$3 <= $$1.getMaxBlockZ(); $$3++) {
               BlockPos $$4 = getOverworldRespawnPos($$0, $$2, $$3);
               if ($$4 != null) {
                  return $$4;
               }
            }
         }

         return null;
      }
   }
}
