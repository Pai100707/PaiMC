/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.debug;

import java.lang.runtime.SwitchBootstraps;
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
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
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
import org.jspecify.annotations.Nullable;

public record DebugBrainDump(String name, String profession, int xp, float health, float maxHealth, String inventory, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPos> pois, Set<BlockPos> potentialPois) {
    public static final StreamCodec<FriendlyByteBuf, DebugBrainDump> STREAM_CODEC = StreamCodec.of(($$0, $$1) -> $$1.write((FriendlyByteBuf)((Object)$$0)), DebugBrainDump::new);

    public DebugBrainDump(FriendlyByteBuf $$0) {
        this($$0.readUtf(), $$0.readUtf(), $$0.readInt(), $$0.readFloat(), $$0.readFloat(), $$0.readUtf(), $$0.readBoolean(), $$0.readInt(), $$0.readList(FriendlyByteBuf::readUtf), $$0.readList(FriendlyByteBuf::readUtf), $$0.readList(FriendlyByteBuf::readUtf), $$0.readList(FriendlyByteBuf::readUtf), $$0.readCollection(HashSet::new, BlockPos.STREAM_CODEC), $$0.readCollection(HashSet::new, BlockPos.STREAM_CODEC));
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

    public static DebugBrainDump takeBrainDump(ServerLevel $$02, LivingEntity $$1) {
        List<String> list;
        int n;
        Villager $$16;
        boolean $$17;
        String $$15;
        int $$7;
        String $$6;
        String $$2 = DebugEntityNameGenerator.getEntityName($$1);
        if ($$1 instanceof Villager) {
            Villager $$3 = (Villager)$$1;
            String $$4 = $$3.getVillagerData().profession().getRegisteredName();
            int $$5 = $$3.getVillagerXp();
        } else {
            $$6 = "";
            $$7 = 0;
        }
        float $$8 = $$1.getHealth();
        float $$9 = $$1.getMaxHealth();
        Brain<?> $$10 = $$1.getBrain();
        long $$11 = $$1.level().getGameTime();
        if ($$1 instanceof InventoryCarrier) {
            InventoryCarrier $$12 = (InventoryCarrier)((Object)$$1);
            SimpleContainer $$13 = $$12.getInventory();
            String $$14 = $$13.isEmpty() ? "" : ((Object)$$13).toString();
        } else {
            $$15 = "";
        }
        boolean bl = $$17 = $$1 instanceof Villager && ($$16 = (Villager)$$1).wantsToSpawnGolem($$11);
        if ($$1 instanceof Warden) {
            Warden $$18 = (Warden)$$1;
            n = $$18.getClientAngerLevel();
        } else {
            n = -1;
        }
        int $$19 = n;
        List<String> $$20 = $$10.getActiveActivities().stream().map(Activity::getName).toList();
        List<String> $$21 = $$10.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList();
        List<String> $$22 = DebugBrainDump.getMemoryDescriptions($$02, $$1, $$11).map($$0 -> StringUtil.truncateStringIfNecessary($$0, 255, true)).toList();
        Set<BlockPos> $$23 = DebugBrainDump.getKnownBlockPositions($$10, MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
        Set<BlockPos> $$24 = DebugBrainDump.getKnownBlockPositions($$10, MemoryModuleType.POTENTIAL_JOB_SITE);
        if ($$1 instanceof Villager) {
            Villager $$25 = (Villager)$$1;
            list = DebugBrainDump.getVillagerGossips($$25);
        } else {
            list = List.of();
        }
        List<String> $$26 = list;
        return new DebugBrainDump($$2, $$6, $$7, $$8, $$9, $$15, $$17, $$19, $$20, $$21, $$22, $$26, $$23, $$24);
    }

    @SafeVarargs
    private static Set<BlockPos> getKnownBlockPositions(Brain<?> $$0, MemoryModuleType<GlobalPos> ... $$1) {
        return Stream.of($$1).filter($$0::hasMemoryValue).map($$0::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
    }

    private static List<String> getVillagerGossips(Villager $$0) {
        ArrayList<String> $$12 = new ArrayList<String>();
        $$0.getGossips().getGossipEntries().forEach(($$1, $$22) -> {
            String $$32 = DebugEntityNameGenerator.getEntityName($$1);
            $$22.forEach(($$2, $$3) -> $$12.add($$32 + ": " + String.valueOf($$2) + ": " + $$3));
        });
        return $$12;
    }

    private static Stream<String> getMemoryDescriptions(ServerLevel $$0, LivingEntity $$1, long $$22) {
        return $$1.getBrain().getMemories().entrySet().stream().map($$2 -> {
            MemoryModuleType $$3 = (MemoryModuleType)$$2.getKey();
            Optional $$4 = (Optional)$$2.getValue();
            return DebugBrainDump.getMemoryDescription($$0, $$22, $$3, $$4);
        }).sorted();
    }

    private static String getMemoryDescription(ServerLevel $$0, long $$1, MemoryModuleType<?> $$2, Optional<? extends ExpirableValue<?>> $$3) {
        String $$10;
        if ($$3.isPresent()) {
            ExpirableValue<?> $$4 = $$3.get();
            Object $$5 = $$4.getValue();
            if ($$2 == MemoryModuleType.HEARD_BELL_TIME) {
                long $$6 = $$1 - (Long)$$5;
                String $$7 = $$6 + " ticks ago";
            } else if ($$4.canExpire()) {
                String $$8 = DebugBrainDump.getShortDescription($$0, $$5) + " (ttl: " + $$4.getTimeToLive() + ")";
            } else {
                String $$9 = DebugBrainDump.getShortDescription($$0, $$5);
            }
        } else {
            $$10 = "-";
        }
        return BuiltInRegistries.MEMORY_MODULE_TYPE.getKey($$2).getPath() + ": " + $$10;
    }

    private static String getShortDescription(ServerLevel $$0, @Nullable Object $$12) {
        Object object = $$12;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{UUID.class, Entity.class, WalkTarget.class, EntityTracker.class, GlobalPos.class, BlockPosTracker.class, DamageSource.class, Collection.class}, (Object)object, n)) {
            case -1 -> "-";
            case 0 -> {
                UUID $$2 = (UUID)object;
                yield DebugBrainDump.getShortDescription($$0, $$0.getEntity($$2));
            }
            case 1 -> {
                Entity $$3 = (Entity)object;
                yield DebugEntityNameGenerator.getEntityName($$3);
            }
            case 2 -> {
                WalkTarget $$4 = (WalkTarget)object;
                yield DebugBrainDump.getShortDescription($$0, $$4.getTarget());
            }
            case 3 -> {
                EntityTracker $$5 = (EntityTracker)object;
                yield DebugBrainDump.getShortDescription($$0, $$5.getEntity());
            }
            case 4 -> {
                GlobalPos $$6 = (GlobalPos)object;
                yield DebugBrainDump.getShortDescription($$0, $$6.pos());
            }
            case 5 -> {
                BlockPosTracker $$7 = (BlockPosTracker)object;
                yield DebugBrainDump.getShortDescription($$0, $$7.currentBlockPosition());
            }
            case 6 -> {
                DamageSource $$8 = (DamageSource)object;
                Entity $$9 = $$8.getEntity();
                if ($$9 == null) {
                    yield $$12.toString();
                }
                yield DebugBrainDump.getShortDescription($$0, $$9);
            }
            case 7 -> {
                Collection $$10 = (Collection)object;
                yield "[" + $$10.stream().map($$1 -> DebugBrainDump.getShortDescription($$0, $$1)).collect(Collectors.joining(", ")) + "]";
            }
            default -> $$12.toString();
        };
    }

    public boolean hasPoi(BlockPos $$0) {
        return this.pois.contains($$0);
    }

    public boolean hasPotentialPoi(BlockPos $$0) {
        return this.potentialPois.contains($$0);
    }
}

