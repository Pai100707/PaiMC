package net.minecraft.util.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

public record DebugBrainDump(
   String name,
   String profession,
   int xp,
   float health,
   float maxHealth,
   String inventory,
   boolean wantsGolem,
   int angerLevel,
   List<String> activities,
   List<String> behaviors,
   List<String> memories,
   List<String> gossips,
   Set<BlockPos> pois,
   Set<BlockPos> potentialPois
) {
   public static final StreamCodec<FriendlyByteBuf, DebugBrainDump> STREAM_CODEC = StreamCodec.of(($$0, $$1) -> $$1.write($$0), DebugBrainDump::new);

   public DebugBrainDump(FriendlyByteBuf $$0) {
      this(
         $$0.readUtf(),
         $$0.readUtf(),
         $$0.readInt(),
         $$0.readFloat(),
         $$0.readFloat(),
         $$0.readUtf(),
         $$0.readBoolean(),
         $$0.readInt(),
         $$0.readList(FriendlyByteBuf::readUtf),
         $$0.readList(FriendlyByteBuf::readUtf),
         $$0.readList(FriendlyByteBuf::readUtf),
         $$0.readList(FriendlyByteBuf::readUtf),
         (Set<BlockPos>)$$0.readCollection(HashSet::new, BlockPos.STREAM_CODEC),
         (Set<BlockPos>)$$0.readCollection(HashSet::new, BlockPos.STREAM_CODEC)
      );
   }

   public void write(FriendlyByteBuf $$0) {
      $$0.writeUtf(this.name);
      $$0.writeUtf(this.profession);
      $$0.writeInt(this.xp);
      $$0.writeFloat(this.health);
      $$0.writeFloat(this.maxHealth);
      $$0.writeUtf(this.inventory);
      $$0.writeBoolean(this.wantsGolem);
      $$0.writeInt(this.angerLevel);
      $$0.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
      $$0.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
      $$0.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
      $$0.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
      $$0.writeCollection(this.pois, BlockPos.STREAM_CODEC);
      $$0.writeCollection(this.potentialPois, BlockPos.STREAM_CODEC);
   }

   public static DebugBrainDump takeBrainDump(ServerLevel $$0, LivingEntity $$1) {
      String $$2 = DebugEntityNameGenerator.getEntityName($$1);
      String $$4;
      int $$5;
      if ($$1 instanceof Villager $$3) {
         $$4 = $$3.getVillagerData().profession().getRegisteredName();
         $$5 = $$3.getVillagerXp();
      } else {
         $$4 = "";
         $$5 = 0;
      }

      float $$8 = $$1.getHealth();
      float $$9 = $$1.getMaxHealth();
      Brain<?> $$10 = $$1.getBrain();
      long $$11 = $$1.level().getGameTime();
      String $$14;
      if ($$1 instanceof InventoryCarrier $$12) {
         Container $$13 = $$12.getInventory();
         $$14 = $$13.isEmpty() ? "" : $$13.toString();
      } else {
         $$14 = "";
      }

      boolean $$17 = $$1 instanceof Villager $$16 && $$16.wantsToSpawnGolem($$11);
      int $$19 = $$1 instanceof Warden $$18 ? $$18.getClientAngerLevel() : -1;
      List<String> $$20 = $$10.getActiveActivities().stream().<String>map(Activity::getName).toList();
      List<String> $$21 = $$10.getRunningBehaviors().stream().<String>map(BehaviorControl::debugString).toList();
      List<String> $$22 = getMemoryDescriptions($$0, $$1, $$11).map($$0x -> net.minecraft.util.StringUtil.truncateStringIfNecessary($$0x, 255, true)).toList();
      Set<BlockPos> $$23 = getKnownBlockPositions($$10, MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
      Set<BlockPos> $$24 = getKnownBlockPositions($$10, MemoryModuleType.POTENTIAL_JOB_SITE);
      List<String> $$26 = $$1 instanceof Villager $$25 ? getVillagerGossips($$25) : List.of();
      return new DebugBrainDump($$2, $$4, $$5, $$8, $$9, $$14, $$17, $$19, $$20, $$21, $$22, $$26, $$23, $$24);
   }

   @SafeVarargs
   private static Set<BlockPos> getKnownBlockPositions(Brain<?> $$0, MemoryModuleType<GlobalPos>... $$1) {
      return Stream.of($$1).filter($$0::hasMemoryValue).map($$0::getMemory).flatMap(Optional::stream).<BlockPos>map(GlobalPos::pos).collect(Collectors.toSet());
   }

   private static List<String> getVillagerGossips(Villager $$0) {
      List<String> $$1 = new ArrayList<>();
      $$0.getGossips().getGossipEntries().forEach(($$1x, $$2) -> {
         String $$3 = DebugEntityNameGenerator.getEntityName($$1x);
         $$2.forEach(($$2x, $$3x) -> $$1.add($$3 + ": " + $$2x + ": " + $$3x));
      });
      return $$1;
   }

   private static Stream<String> getMemoryDescriptions(ServerLevel $$0, LivingEntity $$1, long $$2) {
      return $$1.getBrain().getMemories().entrySet().stream().map($$2x -> {
         MemoryModuleType<?> $$3 = (MemoryModuleType<?>)$$2x.getKey();
         Optional<? extends ExpirableValue<?>> $$4 = (Optional<? extends ExpirableValue<?>>)$$2x.getValue();
         return getMemoryDescription($$0, $$2, $$3, $$4);
      }).sorted();
   }

   private static String getMemoryDescription(ServerLevel $$0, long $$1, MemoryModuleType<?> $$2, Optional<? extends ExpirableValue<?>> $$3) {
      String $$7;
      if ($$3.isPresent()) {
         ExpirableValue<?> $$4 = (ExpirableValue<?>)$$3.get();
         Object $$5 = $$4.getValue();
         if ($$2 == MemoryModuleType.HEARD_BELL_TIME) {
            long $$6 = $$1 - (Long)$$5;
            $$7 = $$6 + " ticks ago";
         } else if ($$4.canExpire()) {
            $$7 = getShortDescription($$0, $$5) + " (ttl: " + $$4.getTimeToLive() + ")";
         } else {
            $$7 = getShortDescription($$0, $$5);
         }
      } else {
         $$7 = "-";
      }

      return BuiltInRegistries.MEMORY_MODULE_TYPE.getKey($$2).getPath() + ": " + $$7;
   }

   private static String getShortDescription(ServerLevel $$0, Object $$1) {
      return switch ($$1) {
         case null -> "-";
         case UUID $$2 -> getShortDescription($$0, $$0.getEntity($$2));
         case Entity $$3 -> DebugEntityNameGenerator.getEntityName($$3);
         case WalkTarget $$4 -> getShortDescription($$0, $$4.getTarget());
         case EntityTracker $$5 -> getShortDescription($$0, $$5.getEntity());
         case GlobalPos $$6 -> getShortDescription($$0, $$6.pos());
         case BlockPosTracker $$7 -> getShortDescription($$0, $$7.currentBlockPosition());
         case DamageSource $$8 -> {
            Entity $$9 = $$8.getEntity();
            yield $$9 == null ? $$1.toString() : getShortDescription($$0, $$9);
         }
         case Collection<?> $$10 -> "[" + (String)$$10.stream().map($$1x -> getShortDescription($$0, $$1x)).collect(Collectors.joining(", ")) + "]";
         default -> $$1.toString();
      };
   }

   public boolean hasPoi(BlockPos $$0) {
      return this.pois.contains($$0);
   }

   public boolean hasPotentialPoi(BlockPos $$0) {
      return this.potentialPois.contains($$0);
   }
}
