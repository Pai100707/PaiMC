package net.minecraft.world.level.block.state.pattern;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockInWorld {
   private final net.minecraft.world.level.LevelReader level;
   private final BlockPos pos;
   private final boolean loadChunks;
   @Nullable
   private BlockState state;
   @Nullable
   private BlockEntity entity;
   private boolean cachedEntity;

   public BlockInWorld(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, boolean $$2) {
      this.level = $$0;
      this.pos = $$1.immutable();
      this.loadChunks = $$2;
   }

   public BlockState getState() {
      if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos))) {
         this.state = this.level.getBlockState(this.pos);
      }

      return this.state;
   }

   @Nullable
   public BlockEntity getEntity() {
      if (this.entity == null && !this.cachedEntity) {
         this.entity = this.level.getBlockEntity(this.pos);
         this.cachedEntity = true;
      }

      return this.entity;
   }

   public net.minecraft.world.level.LevelReader getLevel() {
      return this.level;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public static Predicate<BlockInWorld> hasState(Predicate<BlockState> $$0) {
      return $$1 -> $$1 != null && $$0.test($$1.getState());
   }
}
