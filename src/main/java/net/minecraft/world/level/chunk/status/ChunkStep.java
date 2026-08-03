package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.jspecify.annotations.Nullable;

public record ChunkStep(
   ChunkStatus targetStatus, ChunkDependencies directDependencies, ChunkDependencies accumulatedDependencies, int blockStateWriteRadius, ChunkStatusTask task
) {

   public int getAccumulatedRadiusOf(ChunkStatus $$0) {
      return $$0 == this.targetStatus ? 0 : this.accumulatedDependencies.getRadiusOf($$0);
   }

   public CompletableFuture<ChunkAccess> apply(WorldGenContext $$0, StaticCache2D<GenerationChunkHolder> $$1, ChunkAccess $$2) {
      if ($$2.getPersistedStatus().isBefore(this.targetStatus)) {
         ProfiledDuration $$3 = JvmProfiler.INSTANCE.onChunkGenerate($$2.getPos(), $$0.level().dimension(), this.targetStatus.getName());
         return this.task.doWork($$0, this, $$1, $$2).thenApply($$1x -> this.completeChunkGeneration($$1x, $$3));
      } else {
         return this.task.doWork($$0, this, $$1, $$2);
      }
   }

   private ChunkAccess completeChunkGeneration(ChunkAccess $$0, @Nullable ProfiledDuration $$1) {
      if ($$0 instanceof ProtoChunk $$2 && $$2.getPersistedStatus().isBefore(this.targetStatus)) {
         $$2.setPersistedStatus(this.targetStatus);
      }

      if ($$1 != null) {
         $$1.finish(true);
      }

      return $$0;
   }

   public static class Builder {
      private final ChunkStatus status;
      @Nullable
      private final ChunkStep parent;
      private ChunkStatus[] directDependenciesByRadius;
      private int blockStateWriteRadius = -1;
      private ChunkStatusTask task = ChunkStatusTasks::passThrough;

      protected Builder(ChunkStatus $$0) {
         if ($$0.getParent() != $$0) {
            throw new IllegalArgumentException("Not starting with the first status: " + $$0);
         } else {
            this.status = $$0;
            this.parent = null;
            this.directDependenciesByRadius = new ChunkStatus[0];
         }
      }

      protected Builder(ChunkStatus $$0, ChunkStep $$1) {
         if ($$1.targetStatus.getIndex() != $$0.getIndex() - 1) {
            throw new IllegalArgumentException("Out of order status: " + $$0);
         } else {
            this.status = $$0;
            this.parent = $$1;
            this.directDependenciesByRadius = new ChunkStatus[]{$$1.targetStatus};
         }
      }

      public ChunkStep.Builder addRequirement(ChunkStatus $$0, int $$1) {
         if ($$0.isOrAfter(this.status)) {
            throw new IllegalArgumentException("Status " + $$0 + " can not be required by " + this.status);
         } else {
            ChunkStatus[] $$2 = this.directDependenciesByRadius;
            int $$3 = $$1 + 1;
            if ($$3 > $$2.length) {
               this.directDependenciesByRadius = new ChunkStatus[$$3];
               Arrays.fill(this.directDependenciesByRadius, $$0);
            }

            for (int $$4 = 0; $$4 < Math.min($$3, $$2.length); $$4++) {
               this.directDependenciesByRadius[$$4] = ChunkStatus.max($$2[$$4], $$0);
            }

            return this;
         }
      }

      public ChunkStep.Builder blockStateWriteRadius(int $$0) {
         this.blockStateWriteRadius = $$0;
         return this;
      }

      public ChunkStep.Builder setTask(ChunkStatusTask $$0) {
         this.task = $$0;
         return this;
      }

      public ChunkStep build() {
         return new ChunkStep(
            this.status,
            new ChunkDependencies(ImmutableList.copyOf(this.directDependenciesByRadius)),
            new ChunkDependencies(ImmutableList.copyOf(this.buildAccumulatedDependencies())),
            this.blockStateWriteRadius,
            this.task
         );
      }

      private ChunkStatus[] buildAccumulatedDependencies() {
         if (this.parent == null) {
            return this.directDependenciesByRadius;
         } else {
            int $$0 = this.getRadiusOfParent(this.parent.targetStatus);
            ChunkDependencies $$1 = this.parent.accumulatedDependencies;
            ChunkStatus[] $$2 = new ChunkStatus[Math.max($$0 + $$1.size(), this.directDependenciesByRadius.length)];

            for (int $$3 = 0; $$3 < $$2.length; $$3++) {
               int $$4 = $$3 - $$0;
               if ($$4 < 0 || $$4 >= $$1.size()) {
                  $$2[$$3] = this.directDependenciesByRadius[$$3];
               } else if ($$3 >= this.directDependenciesByRadius.length) {
                  $$2[$$3] = $$1.get($$4);
               } else {
                  $$2[$$3] = ChunkStatus.max(this.directDependenciesByRadius[$$3], $$1.get($$4));
               }
            }

            return $$2;
         }
      }

      private int getRadiusOfParent(ChunkStatus $$0) {
         for (int $$1 = this.directDependenciesByRadius.length - 1; $$1 >= 0; $$1--) {
            if (this.directDependenciesByRadius[$$1].isOrAfter($$0)) {
               return $$1;
            }
         }

         return 0;
      }
   }
}
