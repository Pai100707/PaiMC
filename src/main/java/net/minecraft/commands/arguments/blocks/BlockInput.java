package net.minecraft.commands.arguments.blocks;

import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Block.UpdateFlags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BlockInput implements Predicate<BlockInWorld> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockState state;
   private final Set<Property<?>> properties;
   @Nullable
   private final CompoundTag tag;

   public BlockInput(BlockState $$0, Set<Property<?>> $$1, @Nullable CompoundTag $$2) {
      this.state = $$0;
      this.properties = $$1;
      this.tag = $$2;
   }

   public BlockState getState() {
      return this.state;
   }

   public Set<Property<?>> getDefinedProperties() {
      return this.properties;
   }

   public boolean test(BlockInWorld $$0) {
      BlockState $$1 = $$0.getState();
      if (!$$1.is(this.state.getBlock())) {
         return false;
      } else {
         for (Property<?> $$2 : this.properties) {
            if ($$1.getValue($$2) != this.state.getValue($$2)) {
               return false;
            }
         }

         if (this.tag == null) {
            return true;
         } else {
            BlockEntity $$3 = $$0.getEntity();
            return $$3 != null && NbtUtils.compareNbt(this.tag, $$3.saveWithFullMetadata($$0.getLevel().registryAccess()), true);
         }
      }
   }

   public boolean test(ServerLevel $$0, BlockPos $$1) {
      return this.test(new BlockInWorld($$0, $$1, false));
   }

   public boolean place(ServerLevel $$0, BlockPos $$1, @UpdateFlags int $$2) {
      BlockState $$3 = ($$2 & 16) != 0 ? this.state : Block.updateFromNeighbourShapes(this.state, $$0, $$1);
      if ($$3.isAir()) {
         $$3 = this.state;
      }

      $$3 = this.overwriteWithDefinedProperties($$3);
      boolean $$4 = false;
      if ($$0.setBlock($$1, $$3, $$2)) {
         $$4 = true;
      }

      if (this.tag != null) {
         BlockEntity $$5 = $$0.getBlockEntity($$1);
         if ($$5 != null) {
            ScopedCollector $$6 = new ScopedCollector(LOGGER);

            try {
               Provider $$7 = $$0.registryAccess();
               ProblemReporter $$8 = $$6.forChild($$5.problemPath());
               TagValueOutput $$9 = TagValueOutput.createWithContext($$8.forChild(() -> "(before)"), $$7);
               $$5.saveWithoutMetadata($$9);
               CompoundTag $$10 = $$9.buildResult();
               $$5.loadWithComponents(TagValueInput.create($$6, $$7, this.tag));
               TagValueOutput $$11 = TagValueOutput.createWithContext($$8.forChild(() -> "(after)"), $$7);
               $$5.saveWithoutMetadata($$11);
               CompoundTag $$12 = $$11.buildResult();
               if (!$$12.equals($$10)) {
                  $$4 = true;
                  $$5.setChanged();
                  $$0.getChunkSource().blockChanged($$1);
               }
            } catch (Throwable var15) {
               try {
                  $$6.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }

               throw var15;
            }

            $$6.close();
         }
      }

      return $$4;
   }

   private BlockState overwriteWithDefinedProperties(BlockState $$0) {
      if ($$0 == this.state) {
         return $$0;
      } else {
         for (Property<?> $$1 : this.properties) {
            $$0 = copyProperty($$0, this.state, $$1);
         }

         return $$0;
      }
   }

   private static <T extends Comparable<T>> BlockState copyProperty(BlockState $$0, BlockState $$1, Property<T> $$2) {
      return (BlockState)$$0.trySetValue($$2, $$1.getValue($$2));
   }
}
