package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jspecify.annotations.Nullable;

public class ShowTradesToPlayer extends Behavior<Villager> {
   private static final int MAX_LOOK_TIME = 900;
   private static final int STARTING_LOOK_TIME = 40;
   @Nullable
   private ItemStack playerItemStack;
   private final List<ItemStack> displayItems = Lists.newArrayList();
   private int cycleCounter;
   private int displayIndex;
   private int lookTime;

   public ShowTradesToPlayer(int $$0, int $$1) {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT), $$0, $$1);
   }

   public boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      Brain<?> $$2 = $$1.getBrain();
      if ($$2.getMemory(MemoryModuleType.INTERACTION_TARGET).isEmpty()) {
         return false;
      } else {
         net.minecraft.world.entity.LivingEntity $$3 = $$2.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
         return $$3.getType() == net.minecraft.world.entity.EntityType.PLAYER
            && $$1.isAlive()
            && $$3.isAlive()
            && !$$1.isBaby()
            && $$1.distanceToSqr($$3) <= 17.0;
      }
   }

   public boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return this.checkExtraStartConditions($$0, $$1) && this.lookTime > 0 && $$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   public void start(ServerLevel $$0, Villager $$1, long $$2) {
      super.start($$0, $$1, $$2);
      this.lookAtTarget($$1);
      this.cycleCounter = 0;
      this.displayIndex = 0;
      this.lookTime = 40;
   }

   public void tick(ServerLevel $$0, Villager $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = this.lookAtTarget($$1);
      this.findItemsToDisplay($$3, $$1);
      if (!this.displayItems.isEmpty()) {
         this.displayCyclingItems($$1);
      } else {
         clearHeldItem($$1);
         this.lookTime = Math.min(this.lookTime, 40);
      }

      this.lookTime--;
   }

   public void stop(ServerLevel $$0, Villager $$1, long $$2) {
      super.stop($$0, $$1, $$2);
      $$1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      clearHeldItem($$1);
      this.playerItemStack = null;
   }

   private void findItemsToDisplay(net.minecraft.world.entity.LivingEntity $$0, Villager $$1) {
      boolean $$2 = false;
      ItemStack $$3 = $$0.getMainHandItem();
      if (this.playerItemStack == null || !ItemStack.isSameItem(this.playerItemStack, $$3)) {
         this.playerItemStack = $$3;
         $$2 = true;
         this.displayItems.clear();
      }

      if ($$2 && !this.playerItemStack.isEmpty()) {
         this.updateDisplayItems($$1);
         if (!this.displayItems.isEmpty()) {
            this.lookTime = 900;
            this.displayFirstItem($$1);
         }
      }
   }

   private void displayFirstItem(Villager $$0) {
      displayAsHeldItem($$0, this.displayItems.get(0));
   }

   private void updateDisplayItems(Villager $$0) {
      for (MerchantOffer $$1 : $$0.getOffers()) {
         if (!$$1.isOutOfStock() && this.playerItemStackMatchesCostOfOffer($$1)) {
            this.displayItems.add($$1.assemble());
         }
      }
   }

   private boolean playerItemStackMatchesCostOfOffer(MerchantOffer $$0) {
      return ItemStack.isSameItem(this.playerItemStack, $$0.getCostA()) || ItemStack.isSameItem(this.playerItemStack, $$0.getCostB());
   }

   private static void clearHeldItem(Villager $$0) {
      $$0.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      $$0.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.085F);
   }

   private static void displayAsHeldItem(Villager $$0, ItemStack $$1) {
      $$0.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$1);
      $$0.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
   }

   private net.minecraft.world.entity.LivingEntity lookAtTarget(Villager $$0) {
      Brain<?> $$1 = $$0.getBrain();
      net.minecraft.world.entity.LivingEntity $$2 = $$1.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      $$1.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$2, true));
      return $$2;
   }

   private void displayCyclingItems(Villager $$0) {
      if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
         this.displayIndex++;
         this.cycleCounter = 0;
         if (this.displayIndex > this.displayItems.size() - 1) {
            this.displayIndex = 0;
         }

         displayAsHeldItem($$0, this.displayItems.get(this.displayIndex));
      }
   }
}
