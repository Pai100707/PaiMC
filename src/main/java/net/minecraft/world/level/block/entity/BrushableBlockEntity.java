package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BrushableBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String LOOT_TABLE_TAG = "LootTable";
   private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
   private static final String HIT_DIRECTION_TAG = "hit_direction";
   private static final String ITEM_TAG = "item";
   private static final int BRUSH_COOLDOWN_TICKS = 10;
   private static final int BRUSH_RESET_TICKS = 40;
   private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
   private int brushCount;
   private long brushCountResetsAtTick;
   private long coolDownEndsAtTick;
   private ItemStack item = ItemStack.EMPTY;
   @Nullable
   private Direction hitDirection;
   @Nullable
   private ResourceKey<LootTable> lootTable;
   private long lootTableSeed;

   public BrushableBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.BRUSHABLE_BLOCK, $$0, $$1);
   }

   public boolean brush(long $$0, ServerLevel $$1, LivingEntity $$2, Direction $$3, ItemStack $$4) {
      if (this.hitDirection == null) {
         this.hitDirection = $$3;
      }

      this.brushCountResetsAtTick = $$0 + 40L;
      if ($$0 < this.coolDownEndsAtTick) {
         return false;
      } else {
         this.coolDownEndsAtTick = $$0 + 10L;
         this.unpackLootTable($$1, $$2, $$4);
         int $$5 = this.getCompletionState();
         if (++this.brushCount >= 10) {
            this.brushingCompleted($$1, $$2, $$4);
            return true;
         } else {
            $$1.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
            int $$6 = this.getCompletionState();
            if ($$5 != $$6) {
               BlockState $$7 = this.getBlockState();
               BlockState $$8 = $$7.setValue(BlockStateProperties.DUSTED, $$6);
               $$1.setBlock(this.getBlockPos(), $$8, 3);
            }

            return false;
         }
      }
   }

   private void unpackLootTable(ServerLevel $$0, LivingEntity $$1, ItemStack $$2) {
      if (this.lootTable != null) {
         LootTable $$3 = $$0.getServer().reloadableRegistries().getLootTable(this.lootTable);
         if ($$1 instanceof ServerPlayer $$4) {
            CriteriaTriggers.GENERATE_LOOT.trigger($$4, this.lootTable);
         }

         LootParams $$5 = new LootParams.Builder($$0)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
            .withLuck($$1.getLuck())
            .withParameter(LootContextParams.THIS_ENTITY, $$1)
            .withParameter(LootContextParams.TOOL, $$2)
            .create(LootContextParamSets.ARCHAEOLOGY);
         ObjectArrayList<ItemStack> $$6 = $$3.getRandomItems($$5, this.lootTableSeed);

         this.item = switch ($$6.size()) {
            case 0 -> ItemStack.EMPTY;
            case 1 -> (ItemStack)$$6.getFirst();
            default -> {
               LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", this.lootTable.identifier(), $$6.size());
               yield (ItemStack)$$6.getFirst();
            }
         };
         this.lootTable = null;
         this.setChanged();
      }
   }

   private void brushingCompleted(ServerLevel $$0, LivingEntity $$1, ItemStack $$2) {
      this.dropContent($$0, $$1, $$2);
      BlockState $$3 = this.getBlockState();
      $$0.levelEvent(3008, this.getBlockPos(), Block.getId($$3));
      Block $$6;
      if (this.getBlockState().getBlock() instanceof BrushableBlock $$5) {
         $$6 = $$5.getTurnsInto();
      } else {
         $$6 = Blocks.AIR;
      }

      $$0.setBlock(this.worldPosition, $$6.defaultBlockState(), 3);
   }

   private void dropContent(ServerLevel $$0, LivingEntity $$1, ItemStack $$2) {
      this.unpackLootTable($$0, $$1, $$2);
      if (!this.item.isEmpty()) {
         double $$3 = EntityType.ITEM.getWidth();
         double $$4 = 1.0 - $$3;
         double $$5 = $$3 / 2.0;
         Direction $$6 = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
         BlockPos $$7 = this.worldPosition.relative($$6, 1);
         double $$8 = $$7.getX() + 0.5 * $$4 + $$5;
         double $$9 = $$7.getY() + 0.5 + EntityType.ITEM.getHeight() / 2.0F;
         double $$10 = $$7.getZ() + 0.5 * $$4 + $$5;
         ItemEntity $$11 = new ItemEntity($$0, $$8, $$9, $$10, this.item.split($$0.random.nextInt(21) + 10));
         $$11.setDeltaMovement(Vec3.ZERO);
         $$0.addFreshEntity($$11);
         this.item = ItemStack.EMPTY;
      }
   }

   public void checkReset(ServerLevel $$0) {
      if (this.brushCount != 0 && $$0.getGameTime() >= this.brushCountResetsAtTick) {
         int $$1 = this.getCompletionState();
         this.brushCount = Math.max(0, this.brushCount - 2);
         int $$2 = this.getCompletionState();
         if ($$1 != $$2) {
            $$0.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.DUSTED, $$2), 3);
         }

         int $$3 = 4;
         this.brushCountResetsAtTick = $$0.getGameTime() + 4L;
      }

      if (this.brushCount == 0) {
         this.hitDirection = null;
         this.brushCountResetsAtTick = 0L;
         this.coolDownEndsAtTick = 0L;
      } else {
         $$0.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
      }
   }

   private boolean tryLoadLootTable(ValueInput $$0) {
      this.lootTable = $$0.<ResourceKey<LootTable>>read("LootTable", LootTable.KEY_CODEC).orElse(null);
      this.lootTableSeed = $$0.getLongOr("LootTableSeed", 0L);
      return this.lootTable != null;
   }

   private boolean trySaveLootTable(ValueOutput $$0) {
      if (this.lootTable == null) {
         return false;
      } else {
         $$0.store("LootTable", LootTable.KEY_CODEC, this.lootTable);
         if (this.lootTableSeed != 0L) {
            $$0.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      CompoundTag $$1 = super.getUpdateTag($$0);
      $$1.storeNullable("hit_direction", Direction.LEGACY_ID_CODEC, this.hitDirection);
      if (!this.item.isEmpty()) {
         RegistryOps<Tag> $$2 = $$0.createSerializationContext(NbtOps.INSTANCE);
         $$1.store("item", ItemStack.CODEC, $$2, this.item);
      }

      return $$1;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      if (!this.tryLoadLootTable($$0)) {
         this.item = $$0.<ItemStack>read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
      } else {
         this.item = ItemStack.EMPTY;
      }

      this.hitDirection = $$0.<Direction>read("hit_direction", Direction.LEGACY_ID_CODEC).orElse(null);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0) && !this.item.isEmpty()) {
         $$0.store("item", ItemStack.CODEC, this.item);
      }
   }

   public void setLootTable(ResourceKey<LootTable> $$0, long $$1) {
      this.lootTable = $$0;
      this.lootTableSeed = $$1;
   }

   private int getCompletionState() {
      if (this.brushCount == 0) {
         return 0;
      } else if (this.brushCount < 3) {
         return 1;
      } else {
         return this.brushCount < 6 ? 2 : 3;
      }
   }

   @Nullable
   public Direction getHitDirection() {
      return this.hitDirection;
   }

   public ItemStack getItem() {
      return this.item;
   }
}
