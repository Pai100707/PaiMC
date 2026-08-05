package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;

public class HarvestFarmland extends Behavior<Villager> {
   private static final int HARVEST_DURATION = 200;
   public static final float SPEED_MODIFIER = 0.5F;
   
   private BlockPos aboveFarmlandPos;
   private long nextOkStartTime;
   private int timeWorkedSoFar;
   private final List<BlockPos> validFarmlandAroundVillager = Lists.newArrayList();

   public HarvestFarmland() {
      super(
         ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.SECONDARY_JOB_SITE,
            MemoryStatus.VALUE_PRESENT
         )
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      if (!(Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING)) {
         return false;
      } else if (!$$1.getVillagerData().profession().is(VillagerProfession.FARMER)) {
         return false;
      } else {
         MutableBlockPos $$2 = $$1.blockPosition().mutable();
         this.validFarmlandAroundVillager.clear();

         for (int $$3 = -1; $$3 <= 1; $$3++) {
            for (int $$4 = -1; $$4 <= 1; $$4++) {
               for (int $$5 = -1; $$5 <= 1; $$5++) {
                  $$2.set($$1.getX() + $$3, $$1.getY() + $$4, $$1.getZ() + $$5);
                  if (this.validPos($$2, $$0)) {
                     this.validFarmlandAroundVillager.add(new BlockPos($$2));
                  }
               }
            }
         }

         this.aboveFarmlandPos = this.getValidFarmland($$0);
         return this.aboveFarmlandPos != null;
      }
   }

   
   private BlockPos getValidFarmland(ServerLevel $$0) {
      return this.validFarmlandAroundVillager.isEmpty()
         ? null
         : this.validFarmlandAroundVillager.get($$0.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
   }

   private boolean validPos(BlockPos $$0, ServerLevel $$1) {
      BlockState $$2 = $$1.getBlockState($$0);
      Block $$3 = $$2.getBlock();
      Block $$4 = $$1.getBlockState($$0.below()).getBlock();
      return $$3 instanceof CropBlock && ((CropBlock)$$3).isMaxAge($$2) || $$2.isAir() && $$4 instanceof FarmBlock;
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      if ($$2 > this.nextOkStartTime && this.aboveFarmlandPos != null) {
         $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
         $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
      }
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      this.timeWorkedSoFar = 0;
      this.nextOkStartTime = $$2 + 40L;
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan($$1.position(), 1.0)) {
         if (this.aboveFarmlandPos != null && $$2 > this.nextOkStartTime) {
            BlockState $$3 = $$0.getBlockState(this.aboveFarmlandPos);
            Block $$4 = $$3.getBlock();
            Block $$5 = $$0.getBlockState(this.aboveFarmlandPos.below()).getBlock();
            if ($$4 instanceof CropBlock && ((CropBlock)$$4).isMaxAge($$3)) {
               $$0.destroyBlock(this.aboveFarmlandPos, true, $$1);
            }

            if ($$3.isAir() && $$5 instanceof FarmBlock && $$1.hasFarmSeeds()) {
               SimpleContainer $$6 = $$1.getInventory();

               for (int $$7 = 0; $$7 < $$6.getContainerSize(); $$7++) {
                  ItemStack $$8 = $$6.getItem($$7);
                  boolean $$9 = false;
                  if (!$$8.isEmpty() && $$8.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && $$8.getItem() instanceof BlockItem $$10) {
                     BlockState $$11 = $$10.getBlock().defaultBlockState();
                     $$0.setBlockAndUpdate(this.aboveFarmlandPos, $$11);
                     $$0.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, Context.of($$1, $$11));
                     $$9 = true;
                  }

                  if ($$9) {
                     $$0.playSound(
                        null,
                        this.aboveFarmlandPos.getX(),
                        this.aboveFarmlandPos.getY(),
                        this.aboveFarmlandPos.getZ(),
                        SoundEvents.CROP_PLANTED,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                     );
                     $$8.shrink(1);
                     if ($$8.isEmpty()) {
                        $$6.setItem($$7, ItemStack.EMPTY);
                     }
                     break;
                  }
               }
            }

            if ($$4 instanceof CropBlock && !((CropBlock)$$4).isMaxAge($$3)) {
               this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
               this.aboveFarmlandPos = this.getValidFarmland($$0);
               if (this.aboveFarmlandPos != null) {
                  this.nextOkStartTime = $$2 + 20L;
                  $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                  $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
               }
            }
         }

         this.timeWorkedSoFar++;
      }
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return this.timeWorkedSoFar < 200;
   }
}
