package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VillagerGoalPackages {
   private static final float STROLL_SPEED_MODIFIER = 0.4F;
   public static final int INTERACT_DIST_SQR = 5;
   public static final int INTERACT_WALKUP_DIST = 2;
   public static final float INTERACT_SPEED_MODIFIER = 0.5F;

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getCorePackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(0, new Swim(0.8F)),
         Pair.of(0, InteractWithDoor.create()),
         Pair.of(0, new LookAtTargetSink(45, 90)),
         Pair.of(0, new VillagerPanicTrigger()),
         Pair.of(0, WakeUp.create()),
         Pair.of(0, ReactToBell.create()),
         Pair.of(0, SetRaidStatus.create()),
         Pair.of(0, ValidateNearbyPoi.create(((VillagerProfession)$$0.value()).heldJobSite(), MemoryModuleType.JOB_SITE)),
         Pair.of(0, ValidateNearbyPoi.create(((VillagerProfession)$$0.value()).acquirableJobSite(), MemoryModuleType.POTENTIAL_JOB_SITE)),
         Pair.of(1, new MoveToTargetSink()),
         Pair.of(2, PoiCompetitorScan.create()),
         Pair.of(3, new LookAndFollowTradingPlayerSink($$1)),
         new Pair[]{
            Pair.of(5, GoToWantedItem.create($$1, false, 4)),
            Pair.of(
               6,
               AcquirePoi.create(
                  ((VillagerProfession)$$0.value()).acquirableJobSite(),
                  MemoryModuleType.JOB_SITE,
                  MemoryModuleType.POTENTIAL_JOB_SITE,
                  true,
                  Optional.empty(),
                  ($$0x, $$1x) -> true
               )
            ),
            Pair.of(7, new GoToPotentialJobSite($$1)),
            Pair.of(8, YieldJobSite.create($$1)),
            Pair.of(
               10, AcquirePoi.create($$0x -> $$0x.is(PoiTypes.HOME), MemoryModuleType.HOME, false, Optional.of((byte)14), VillagerGoalPackages::validateBedPoi)
            ),
            Pair.of(10, AcquirePoi.create($$0x -> $$0x.is(PoiTypes.MEETING), MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
            Pair.of(10, AssignProfessionFromJobSite.create()),
            Pair.of(10, ResetProfession.create())
         }
      );
   }

   private static boolean validateBedPoi(ServerLevel $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      return $$2.is(BlockTags.BEDS) && !(Boolean)$$2.getValue(BedBlock.OCCUPIED);
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getWorkPackage(Holder<VillagerProfession> $$0, float $$1) {
      WorkAtPoi $$2;
      if ($$0.is(VillagerProfession.FARMER)) {
         $$2 = new WorkAtComposter();
      } else {
         $$2 = new WorkAtPoi();
      }

      return ImmutableList.of(
         getMinimalLookBehavior(),
         Pair.of(
            5,
            new RunOne(
               ImmutableList.of(
                  Pair.of($$2, 7),
                  Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                  Pair.of(StrollToPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                  Pair.of(StrollToPoiList.create(MemoryModuleType.SECONDARY_JOB_SITE, $$1, 1, 6, MemoryModuleType.JOB_SITE), 5),
                  Pair.of(new HarvestFarmland(), $$0.is(VillagerProfession.FARMER) ? 2 : 5),
                  Pair.of(new UseBonemeal(), $$0.is(VillagerProfession.FARMER) ? 4 : 7)
               )
            )
         ),
         Pair.of(10, new ShowTradesToPlayer(400, 1600)),
         Pair.of(10, SetLookAndInteract.create(net.minecraft.world.entity.EntityType.PLAYER, 4)),
         Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.JOB_SITE, $$1, 9, 100, 1200)),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(99, UpdateActivityFromSchedule.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPlayPackage(float $$0) {
      return ImmutableList.of(
         Pair.of(0, new MoveToTargetSink(80, 120)),
         getFullLookBehavior(),
         Pair.of(5, PlayTagWithOtherKids.create()),
         Pair.of(
            5,
            new RunOne(
               ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(
                  Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, $$0, 2), 2),
                  Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, $$0, 2), 1),
                  Pair.of(VillageBoundRandomStroll.create($$0), 1),
                  Pair.of(SetWalkTargetFromLookTarget.create($$0, 2), 1),
                  Pair.of(new JumpOnBed($$0), 2),
                  Pair.of(new DoNothing(20, 40), 2)
               )
            )
         ),
         Pair.of(99, UpdateActivityFromSchedule.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRestPackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.HOME, $$1, 1, 150, 1200)),
         Pair.of(3, ValidateNearbyPoi.create($$0x -> $$0x.is(PoiTypes.HOME), MemoryModuleType.HOME)),
         Pair.of(3, new SleepInBed()),
         Pair.of(
            5,
            new RunOne(
               ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(
                  Pair.of(SetClosestHomeAsWalkTarget.create($$1), 1),
                  Pair.of(InsideBrownianWalk.create($$1), 4),
                  Pair.of(GoToClosestVillage.create($$1, 4), 2),
                  Pair.of(new DoNothing(20, 40), 2)
               )
            )
         ),
         getMinimalLookBehavior(),
         Pair.of(99, UpdateActivityFromSchedule.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getMeetPackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(
            2,
            TriggerGate.triggerOneShuffled(
               ImmutableList.of(Pair.of(StrollAroundPoi.create(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2), Pair.of(SocializeAtBell.create(), 2))
            )
         ),
         Pair.of(10, new ShowTradesToPlayer(400, 1600)),
         Pair.of(10, SetLookAndInteract.create(net.minecraft.world.entity.EntityType.PLAYER, 4)),
         Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, $$1, 6, 100, 200)),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(3, ValidateNearbyPoi.create($$0x -> $$0x.is(PoiTypes.MEETING), MemoryModuleType.MEETING_POINT)),
         Pair.of(
            3,
            new GateBehavior(
               ImmutableMap.of(),
               ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
               GateBehavior.OrderPolicy.ORDERED,
               GateBehavior.RunningPolicy.RUN_ONE,
               ImmutableList.of(Pair.of(new TradeWithVillager(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, UpdateActivityFromSchedule.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getIdlePackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(
            2,
            new RunOne(
               ImmutableList.of(
                  Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, $$1, 2), 2),
                  Pair.of(
                     InteractWith.of(
                        net.minecraft.world.entity.EntityType.VILLAGER,
                        8,
                        net.minecraft.world.entity.AgeableMob::canBreed,
                        net.minecraft.world.entity.AgeableMob::canBreed,
                        MemoryModuleType.BREED_TARGET,
                        $$1,
                        2
                     ),
                     1
                  ),
                  Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, $$1, 2), 1),
                  Pair.of(VillageBoundRandomStroll.create($$1), 1),
                  Pair.of(SetWalkTargetFromLookTarget.create($$1, 2), 1),
                  Pair.of(new JumpOnBed($$1), 1),
                  Pair.of(new DoNothing(30, 60), 1)
               )
            )
         ),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(3, SetLookAndInteract.create(net.minecraft.world.entity.EntityType.PLAYER, 4)),
         Pair.of(3, new ShowTradesToPlayer(400, 1600)),
         Pair.of(
            3,
            new GateBehavior(
               ImmutableMap.of(),
               ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
               GateBehavior.OrderPolicy.ORDERED,
               GateBehavior.RunningPolicy.RUN_ONE,
               ImmutableList.of(Pair.of(new TradeWithVillager(), 1))
            )
         ),
         Pair.of(
            3,
            new GateBehavior(
               ImmutableMap.of(),
               ImmutableSet.of(MemoryModuleType.BREED_TARGET),
               GateBehavior.OrderPolicy.ORDERED,
               GateBehavior.RunningPolicy.RUN_ONE,
               ImmutableList.of(Pair.of(new VillagerMakeLove(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, UpdateActivityFromSchedule.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPanicPackage(Holder<VillagerProfession> $$0, float $$1) {
      float $$2 = $$1 * 1.5F;
      return ImmutableList.of(
         Pair.of(0, VillagerCalmDown.create()),
         Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, $$2, 6, false)),
         Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, $$2, 6, false)),
         Pair.of(3, VillageBoundRandomStroll.create($$2, 2, 2)),
         getMinimalLookBehavior()
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPreRaidPackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(0, RingBell.create()),
         Pair.of(
            0,
            TriggerGate.triggerOneShuffled(
               ImmutableList.of(
                  Pair.of(SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, $$1 * 1.5F, 2, 150, 200), 6),
                  Pair.of(VillageBoundRandomStroll.create($$1 * 1.5F), 2)
               )
            )
         ),
         getMinimalLookBehavior(),
         Pair.of(99, ResetRaidStatus.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRaidPackage(Holder<VillagerProfession> $$0, float $$1) {
      return ImmutableList.of(
         Pair.of(
            0,
            BehaviorBuilder.sequence(
               BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndNotVictory),
               TriggerGate.triggerOneShuffled(
                  ImmutableList.of(Pair.of(MoveToSkySeeingSpot.create($$1), 5), Pair.of(VillageBoundRandomStroll.create($$1 * 1.1F), 2))
               )
            )
         ),
         Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)),
         Pair.of(2, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndActive), LocateHidingPlace.create(24, $$1 * 1.4F, 1))),
         getMinimalLookBehavior(),
         Pair.of(99, ResetRaidStatus.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getHidePackage(Holder<VillagerProfession> $$0, float $$1) {
      int $$2 = 2;
      return ImmutableList.of(Pair.of(0, SetHiddenState.create(15, 3)), Pair.of(1, LocateHidingPlace.create(32, $$1 * 1.25F, 2)), getMinimalLookBehavior());
   }

   private static Pair<Integer, BehaviorControl<net.minecraft.world.entity.LivingEntity>> getFullLookBehavior() {
      return Pair.of(
         5,
         new RunOne(
            ImmutableList.of(
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.CAT, 8.0F), 8),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.VILLAGER, 8.0F), 2),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PLAYER, 8.0F), 2),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.CREATURE, 8.0F), 1),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.WATER_CREATURE, 8.0F), 1),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.AXOLOTLS, 8.0F), 1),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.WATER_AMBIENT, 8.0F), 1),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.MobCategory.MONSTER, 8.0F), 1),
               Pair.of(new DoNothing(30, 60), 2)
            )
         )
      );
   }

   private static Pair<Integer, BehaviorControl<net.minecraft.world.entity.LivingEntity>> getMinimalLookBehavior() {
      return Pair.of(
         5,
         new RunOne(
            ImmutableList.of(
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.VILLAGER, 8.0F), 2),
               Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PLAYER, 8.0F), 2),
               Pair.of(new DoNothing(30, 60), 8)
            )
         )
      );
   }

   private static boolean raidExistsAndActive(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Raid $$2 = $$0.getRaidAt($$1.blockPosition());
      return $$2 != null && $$2.isActive() && !$$2.isVictory() && !$$2.isLoss();
   }

   private static boolean raidExistsAndNotVictory(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Raid $$2 = $$0.getRaidAt($$1.blockPosition());
      return $$2 != null && $$2.isVictory();
   }
}
