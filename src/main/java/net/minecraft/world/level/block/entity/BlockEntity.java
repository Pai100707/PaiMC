package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.debug.DebugValueSource.Registration;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockEntity implements DebugValueSource {
   private static final Codec<BlockEntityType<?>> TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockEntityType<?> type;
   @Nullable
   protected net.minecraft.world.level.Level level;
   protected final BlockPos worldPosition;
   protected boolean remove;
   private BlockState blockState;
   private DataComponentMap components = DataComponentMap.EMPTY;

   public BlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      this.type = $$0;
      this.worldPosition = $$1.immutable();
      this.validateBlockState($$2);
      this.blockState = $$2;
   }

   private void validateBlockState(BlockState $$0) {
      if (!this.isValidBlockState($$0)) {
         throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + this.worldPosition + ", got " + $$0);
      }
   }

   public boolean isValidBlockState(BlockState $$0) {
      return this.type.isValid($$0);
   }

   public static BlockPos getPosFromTag(net.minecraft.world.level.ChunkPos $$0, CompoundTag $$1) {
      int $$2 = $$1.getIntOr("x", 0);
      int $$3 = $$1.getIntOr("y", 0);
      int $$4 = $$1.getIntOr("z", 0);
      int $$5 = SectionPos.blockToSectionCoord($$2);
      int $$6 = SectionPos.blockToSectionCoord($$4);
      if ($$5 != $$0.x || $$6 != $$0.z) {
         LOGGER.warn("Block entity {} found in a wrong chunk, expected position from chunk {}", $$1, $$0);
         $$2 = $$0.getBlockX(SectionPos.sectionRelative($$2));
         $$4 = $$0.getBlockZ(SectionPos.sectionRelative($$4));
      }

      return new BlockPos($$2, $$3, $$4);
   }

   @Nullable
   public net.minecraft.world.level.Level getLevel() {
      return this.level;
   }

   public void setLevel(net.minecraft.world.level.Level $$0) {
      this.level = $$0;
   }

   public boolean hasLevel() {
      return this.level != null;
   }

   protected void loadAdditional(ValueInput $$0) {
   }

   public final void loadWithComponents(ValueInput $$0) {
      this.loadAdditional($$0);
      this.components = $$0.<DataComponentMap>read("components", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
   }

   public final void loadCustomOnly(ValueInput $$0) {
      this.loadAdditional($$0);
   }

   protected void saveAdditional(ValueOutput $$0) {
   }

   public final CompoundTag saveWithFullMetadata(Provider $$0) {
      ScopedCollector $$1 = new ScopedCollector(this.problemPath(), LOGGER);

      CompoundTag var4;
      try {
         TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0);
         this.saveWithFullMetadata($$2);
         var4 = $$2.buildResult();
      } catch (Throwable var6) {
         try {
            $$1.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      $$1.close();
      return var4;
   }

   public void saveWithFullMetadata(ValueOutput $$0) {
      this.saveWithoutMetadata($$0);
      this.saveMetadata($$0);
   }

   public void saveWithId(ValueOutput $$0) {
      this.saveWithoutMetadata($$0);
      this.saveId($$0);
   }

   public final CompoundTag saveWithoutMetadata(Provider $$0) {
      ScopedCollector $$1 = new ScopedCollector(this.problemPath(), LOGGER);

      CompoundTag var4;
      try {
         TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0);
         this.saveWithoutMetadata($$2);
         var4 = $$2.buildResult();
      } catch (Throwable var6) {
         try {
            $$1.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      $$1.close();
      return var4;
   }

   public void saveWithoutMetadata(ValueOutput $$0) {
      this.saveAdditional($$0);
      $$0.store("components", DataComponentMap.CODEC, this.components);
   }

   public final CompoundTag saveCustomOnly(Provider $$0) {
      ScopedCollector $$1 = new ScopedCollector(this.problemPath(), LOGGER);

      CompoundTag var4;
      try {
         TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0);
         this.saveCustomOnly($$2);
         var4 = $$2.buildResult();
      } catch (Throwable var6) {
         try {
            $$1.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      $$1.close();
      return var4;
   }

   public void saveCustomOnly(ValueOutput $$0) {
      this.saveAdditional($$0);
   }

   private void saveId(ValueOutput $$0) {
      addEntityType($$0, this.getType());
   }

   public static void addEntityType(ValueOutput $$0, BlockEntityType<?> $$1) {
      $$0.store("id", TYPE_CODEC, $$1);
   }

   private void saveMetadata(ValueOutput $$0) {
      this.saveId($$0);
      $$0.putInt("x", this.worldPosition.getX());
      $$0.putInt("y", this.worldPosition.getY());
      $$0.putInt("z", this.worldPosition.getZ());
   }

   @Nullable
   public static BlockEntity loadStatic(BlockPos $$0, BlockState $$1, CompoundTag $$2, Provider $$3) {
      BlockEntityType<?> $$4 = (BlockEntityType<?>)$$2.read("id", TYPE_CODEC).orElse(null);
      if ($$4 == null) {
         LOGGER.error("Skipping block entity with invalid type: {}", $$2.get("id"));
         return null;
      } else {
         BlockEntity $$5;
         try {
            $$5 = $$4.create($$0, $$1);
         } catch (Throwable var12) {
            LOGGER.error("Failed to create block entity {} for block {} at position {} ", new Object[]{$$4, $$0, $$1, var12});
            return null;
         }

         try {
            ScopedCollector $$8 = new ScopedCollector($$5.problemPath(), LOGGER);

            BlockEntity var7;
            try {
               $$5.loadWithComponents(TagValueInput.create($$8, $$3, $$2));
               var7 = $$5;
            } catch (Throwable var10) {
               try {
                  $$8.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            $$8.close();
            return var7;
         } catch (Throwable var11) {
            LOGGER.error("Failed to load data for block entity {} for block {} at position {}", new Object[]{$$4, $$0, $$1, var11});
            return null;
         }
      }
   }

   public void setChanged() {
      if (this.level != null) {
         setChanged(this.level, this.worldPosition, this.blockState);
      }
   }

   protected static void setChanged(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      $$0.blockEntityChanged($$1);
      if (!$$2.isAir()) {
         $$0.updateNeighbourForOutputSignal($$1, $$2.getBlock());
      }
   }

   public BlockPos getBlockPos() {
      return this.worldPosition;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return null;
   }

   public CompoundTag getUpdateTag(Provider $$0) {
      return new CompoundTag();
   }

   public boolean isRemoved() {
      return this.remove;
   }

   public void setRemoved() {
      this.remove = true;
   }

   public void clearRemoved() {
      this.remove = false;
   }

   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      if (this instanceof Container $$2 && this.level != null) {
         Containers.dropContents(this.level, $$0, $$2);
      }
   }

   public boolean triggerEvent(int $$0, int $$1) {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory $$0) {
      $$0.setDetail("Name", this::getNameForReporting);
      $$0.setDetail("Cached block", this.getBlockState()::toString);
      if (this.level == null) {
         $$0.setDetail("Block location", () -> this.worldPosition + " (world missing)");
      } else {
         $$0.setDetail("Actual block", this.level.getBlockState(this.worldPosition)::toString);
         CrashReportCategory.populateBlockLocationDetails($$0, this.level, this.worldPosition);
      }
   }

   public String getNameForReporting() {
      return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
   }

   public BlockEntityType<?> getType() {
      return this.type;
   }

   @Deprecated
   public void setBlockState(BlockState $$0) {
      this.validateBlockState($$0);
      this.blockState = $$0;
   }

   protected void applyImplicitComponents(DataComponentGetter $$0) {
   }

   public final void applyComponentsFromItemStack(ItemStack $$0) {
      this.applyComponents($$0.getPrototype(), $$0.getComponentsPatch());
   }

   public final void applyComponents(DataComponentMap $$0, DataComponentPatch $$1) {
      final Set<DataComponentType<?>> $$2 = new HashSet<>();
      $$2.add(DataComponents.BLOCK_ENTITY_DATA);
      $$2.add(DataComponents.BLOCK_STATE);
      final DataComponentMap $$3 = PatchedDataComponentMap.fromPatch($$0, $$1);
      this.applyImplicitComponents(new DataComponentGetter() {
         @Nullable
         public <T> T get(DataComponentType<? extends T> $$0) {
            $$2.add($$0);
            return (T)$$3.get($$0);
         }

         public <T> T getOrDefault(DataComponentType<? extends T> $$0, T $$1x) {
            $$2.add($$0);
            return (T)$$3.getOrDefault($$0, $$1x);
         }
      });
      DataComponentPatch $$4 = $$1.forget($$2::contains);
      this.components = $$4.split().added();
   }

   protected void collectImplicitComponents(Builder $$0) {
   }

   @Deprecated
   public void removeComponentsFromTag(ValueOutput $$0) {
   }

   public final DataComponentMap collectComponents() {
      Builder $$0 = DataComponentMap.builder();
      $$0.addAll(this.components);
      this.collectImplicitComponents($$0);
      return $$0.build();
   }

   public DataComponentMap components() {
      return this.components;
   }

   public void setComponents(DataComponentMap $$0) {
      this.components = $$0;
   }

   @Nullable
   public static Component parseCustomNameSafe(ValueInput $$0, String $$1) {
      return $$0.<Component>read($$1, ComponentSerialization.CODEC).orElse(null);
   }

   public PathElement problemPath() {
      return new BlockEntity.BlockEntityPathElement(this);
   }

   public void registerDebugValues(ServerLevel $$0, Registration $$1) {
   }

   record BlockEntityPathElement(BlockEntity blockEntity) implements PathElement {
      public String get() {
         return this.blockEntity.getNameForReporting() + "@" + this.blockEntity.getBlockPos();
      }
   }
}
