package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class GiveGiftToHero extends Behavior<Villager> {
   private static final int THROW_GIFT_AT_DISTANCE = 5;
   private static final int MIN_TIME_BETWEEN_GIFTS = 600;
   private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
   private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
   private static final Map<ResourceKey<VillagerProfession>, ResourceKey<LootTable>> GIFTS = ImmutableMap.builder()
      .put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT)
      .put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT)
      .put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT)
      .put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT)
      .put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT)
      .put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT)
      .put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT)
      .put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT)
      .put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT)
      .put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT)
      .put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT)
      .put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT)
      .put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT)
      .build();
   private static final float SPEED_MODIFIER = 0.5F;
   private int timeUntilNextGift = 600;
   private boolean giftGivenDuringThisRun;
   private long timeSinceStart;

   public GiveGiftToHero(int $$0) {
      super(
         ImmutableMap.of(
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryStatus.VALUE_PRESENT
         ),
         $$0
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      if (!this.isHeroVisible($$1)) {
         return false;
      } else if (this.timeUntilNextGift > 0) {
         this.timeUntilNextGift--;
         return false;
      } else {
         return true;
      }
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      this.giftGivenDuringThisRun = false;
      this.timeSinceStart = $$2;
      Player $$3 = this.getNearestTargetableHero($$1).get();
      $$1.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, $$3);
      BehaviorUtils.lookAtEntity($$1, $$3);
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return this.isHeroVisible($$1) && !this.giftGivenDuringThisRun;
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      Player $$3 = this.getNearestTargetableHero($$1).get();
      BehaviorUtils.lookAtEntity($$1, $$3);
      if (this.isWithinThrowingDistance($$1, $$3)) {
         if ($$2 - this.timeSinceStart > 20L) {
            this.throwGift($$0, $$1, $$3);
            this.giftGivenDuringThisRun = true;
         }
      } else {
         BehaviorUtils.setWalkAndLookTargetMemories($$1, $$3, 0.5F, 5);
      }
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      this.timeUntilNextGift = calculateTimeUntilNextGift($$0);
      $$1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   private void throwGift(ServerLevel $$0, Villager $$1, net.minecraft.world.entity.LivingEntity $$2) {
      $$1.dropFromGiftLootTable($$0, getLootTableToThrow($$1), ($$2x, $$3) -> BehaviorUtils.throwItem($$1, $$3, $$2.position()));
   }

   private static ResourceKey<LootTable> getLootTableToThrow(Villager $$0) {
      if ($$0.isBaby()) {
         return BuiltInLootTables.BABY_VILLAGER_GIFT;
      } else {
         Optional<ResourceKey<VillagerProfession>> $$1 = $$0.getVillagerData().profession().unwrapKey();
         return $$1.isEmpty() ? BuiltInLootTables.UNEMPLOYED_GIFT : GIFTS.getOrDefault($$1.get(), BuiltInLootTables.UNEMPLOYED_GIFT);
      }
   }

   private boolean isHeroVisible(Villager $$0) {
      return this.getNearestTargetableHero($$0).isPresent();
   }

   private Optional<Player> getNearestTargetableHero(Villager $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
   }

   private boolean isHero(Player $$0) {
      return $$0.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
   }

   private boolean isWithinThrowingDistance(Villager $$0, Player $$1) {
      BlockPos $$2 = $$1.blockPosition();
      BlockPos $$3 = $$0.blockPosition();
      return $$3.closerThan($$2, 5.0);
   }

   private static int calculateTimeUntilNextGift(ServerLevel $$0) {
      return 600 + $$0.random.nextInt(6001);
   }
}
