/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public class LeavesFix
extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    private static final int[][] DIRECTIONS = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    private static final int DECAY_DISTANCE = 7;
    private static final int SIZE_BITS = 12;
    private static final int SIZE = 4096;
    static final Object2IntMap<String> LEAVES = (Object2IntMap)DataFixUtils.make((Object)new Object2IntOpenHashMap(), $$0 -> {
        $$0.put((Object)"minecraft:acacia_leaves", 0);
        $$0.put((Object)"minecraft:birch_leaves", 1);
        $$0.put((Object)"minecraft:dark_oak_leaves", 2);
        $$0.put((Object)"minecraft:jungle_leaves", 3);
        $$0.put((Object)"minecraft:oak_leaves", 4);
        $$0.put((Object)"minecraft:spruce_leaves", 5);
    });
    static final Set<String> LOGS = ImmutableSet.of((Object)"minecraft:acacia_bark", (Object)"minecraft:birch_bark", (Object)"minecraft:dark_oak_bark", (Object)"minecraft:jungle_bark", (Object)"minecraft:oak_bark", (Object)"minecraft:spruce_bark", (Object[])new String[]{"minecraft:acacia_log", "minecraft:birch_log", "minecraft:dark_oak_log", "minecraft:jungle_log", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:stripped_acacia_log", "minecraft:stripped_birch_log", "minecraft:stripped_dark_oak_log", "minecraft:stripped_jungle_log", "minecraft:stripped_oak_log", "minecraft:stripped_spruce_log"});

    public LeavesFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder $$1 = $$0.findField("Level");
        OpticFinder $$2 = $$1.type().findField("Sections");
        Type $$32 = $$2.type();
        if (!($$32 instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        }
        Type $$4 = ((List.ListType)$$32).getElement();
        OpticFinder $$5 = DSL.typeFinder((Type)$$4);
        return this.fixTypeEverywhereTyped("Leaves fix", $$0, $$3 -> $$3.updateTyped($$1, $$22 -> {
            Object $$3 = new int[]{0};
            Typed $$4 = $$22.updateTyped($$2, $$2 -> {
                Object $$3 = new Int2ObjectOpenHashMap($$2.getAllTyped($$5).stream().map($$0 -> new LeavesSection((Typed<?>)$$0, this.getInputSchema())).collect(Collectors.toMap(Section::getIndex, $$0 -> $$0)));
                if ($$3.values().stream().allMatch(Section::isSkippable)) {
                    return $$2;
                }
                ArrayList $$4 = Lists.newArrayList();
                for (int $$5 = 0; $$5 < 7; ++$$5) {
                    $$4.add(new IntOpenHashSet());
                }
                for (LeavesSection $$6 : $$3.values()) {
                    if ($$6.isSkippable()) continue;
                    for (int $$7 = 0; $$7 < 4096; ++$$7) {
                        int $$8 = $$6.getBlock($$7);
                        if ($$6.isLog($$8)) {
                            ((IntSet)$$4.get(0)).add($$6.getIndex() << 12 | $$7);
                            continue;
                        }
                        if (!$$6.isLeaf($$8)) continue;
                        int $$9 = this.getX($$7);
                        int $$10 = this.getZ($$7);
                        $$1[0] = $$3[0] | LeavesFix.getSideMask($$9 == 0, $$9 == 15, $$10 == 0, $$10 == 15);
                    }
                }
                for (int $$11 = 1; $$11 < 7; ++$$11) {
                    IntSet $$12 = (IntSet)$$4.get($$11 - 1);
                    IntSet $$13 = (IntSet)$$4.get($$11);
                    IntIterator $$14 = $$12.iterator();
                    while ($$14.hasNext()) {
                        int $$15 = $$14.nextInt();
                        int $$16 = this.getX($$15);
                        int $$17 = this.getY($$15);
                        int $$18 = this.getZ($$15);
                        for (int[] $$19 : DIRECTIONS) {
                            int $$26;
                            int $$24;
                            int $$25;
                            LeavesSection $$23;
                            int $$20 = $$16 + $$19[0];
                            int $$21 = $$17 + $$19[1];
                            int $$22 = $$18 + $$19[2];
                            if ($$20 < 0 || $$20 > 15 || $$22 < 0 || $$22 > 15 || $$21 < 0 || $$21 > 255 || ($$23 = (LeavesSection)$$3.get($$21 >> 4)) == null || $$23.isSkippable() || !$$23.isLeaf($$25 = $$23.getBlock($$24 = LeavesFix.getIndex($$20, $$21 & 0xF, $$22))) || ($$26 = $$23.getDistance($$25)) <= $$11) continue;
                            $$23.setDistance($$24, $$25, $$11);
                            $$13.add(LeavesFix.getIndex($$20, $$21, $$22));
                        }
                    }
                }
                return $$2.updateTyped($$5, arg_0 -> LeavesFix.lambda$makeRule$3((Int2ObjectMap)$$3, arg_0));
            });
            if ($$3[0] != 0) {
                $$4 = $$4.update(DSL.remainderFinder(), $$1 -> {
                    Dynamic $$2 = (Dynamic)DataFixUtils.orElse((Optional)$$1.get("UpgradeData").result(), (Object)$$1.emptyMap());
                    return $$1.set("UpgradeData", $$2.set("Sides", $$1.createByte((byte)($$2.get("Sides").asByte((byte)0) | $$3[0]))));
                });
            }
            return $$4;
        }));
    }

    public static int getIndex(int $$0, int $$1, int $$2) {
        return $$1 << 8 | $$2 << 4 | $$0;
    }

    private int getX(int $$0) {
        return $$0 & 0xF;
    }

    private int getY(int $$0) {
        return $$0 >> 8 & 0xFF;
    }

    private int getZ(int $$0) {
        return $$0 >> 4 & 0xF;
    }

    public static int getSideMask(boolean $$0, boolean $$1, boolean $$2, boolean $$3) {
        int $$4 = 0;
        if ($$2) {
            $$4 = $$1 ? ($$4 |= 2) : ($$0 ? ($$4 |= 0x80) : ($$4 |= 1));
        } else if ($$3) {
            $$4 = $$0 ? ($$4 |= 0x20) : ($$1 ? ($$4 |= 8) : ($$4 |= 0x10));
        } else if ($$1) {
            $$4 |= 4;
        } else if ($$0) {
            $$4 |= 0x40;
        }
        return $$4;
    }

    private static /* synthetic */ Typed lambda$makeRule$3(Int2ObjectMap $$0, Typed $$1) {
        return ((LeavesSection)$$0.get(((Dynamic)$$1.get(DSL.remainderFinder())).get("Y").asInt(0))).write($$1);
    }

    public static final class LeavesSection
    extends Section {
        private static final String PERSISTENT = "persistent";
        private static final String DECAYABLE = "decayable";
        private static final String DISTANCE = "distance";
        private @Nullable IntSet leaveIds;
        private @Nullable IntSet logIds;
        private @Nullable Int2IntMap stateToIdMap;

        public LeavesSection(Typed<?> $$0, Schema $$1) {
            super($$0, $$1);
        }

        @Override
        protected boolean skippable() {
            this.leaveIds = new IntOpenHashSet();
            this.logIds = new IntOpenHashSet();
            this.stateToIdMap = new Int2IntOpenHashMap();
            for (int $$0 = 0; $$0 < this.palette.size(); ++$$0) {
                Dynamic $$1 = (Dynamic)this.palette.get($$0);
                String $$2 = $$1.get("Name").asString("");
                if (LEAVES.containsKey((Object)$$2)) {
                    boolean $$3 = Objects.equals($$1.get("Properties").get(DECAYABLE).asString(""), "false");
                    this.leaveIds.add($$0);
                    this.stateToIdMap.put(this.getStateId($$2, $$3, 7), $$0);
                    this.palette.set($$0, this.makeLeafTag($$1, $$2, $$3, 7));
                }
                if (!LOGS.contains($$2)) continue;
                this.logIds.add($$0);
            }
            return this.leaveIds.isEmpty() && this.logIds.isEmpty();
        }

        private Dynamic<?> makeLeafTag(Dynamic<?> $$0, String $$1, boolean $$2, int $$3) {
            Dynamic $$4 = $$0.emptyMap();
            $$4 = $$4.set(PERSISTENT, $$4.createString($$2 ? "true" : "false"));
            $$4 = $$4.set(DISTANCE, $$4.createString(Integer.toString($$3)));
            Dynamic $$5 = $$0.emptyMap();
            $$5 = $$5.set("Properties", $$4);
            $$5 = $$5.set("Name", $$5.createString($$1));
            return $$5;
        }

        public boolean isLog(int $$0) {
            return this.logIds.contains($$0);
        }

        public boolean isLeaf(int $$0) {
            return this.leaveIds.contains($$0);
        }

        int getDistance(int $$0) {
            if (this.isLog($$0)) {
                return 0;
            }
            return Integer.parseInt(((Dynamic)this.palette.get($$0)).get("Properties").get(DISTANCE).asString(""));
        }

        void setDistance(int $$0, int $$1, int $$2) {
            boolean $$5;
            Dynamic $$3 = (Dynamic)this.palette.get($$1);
            String $$4 = $$3.get("Name").asString("");
            int $$6 = this.getStateId($$4, $$5 = Objects.equals($$3.get("Properties").get(PERSISTENT).asString(""), "true"), $$2);
            if (!this.stateToIdMap.containsKey($$6)) {
                int $$7 = this.palette.size();
                this.leaveIds.add($$7);
                this.stateToIdMap.put($$6, $$7);
                this.palette.add(this.makeLeafTag($$3, $$4, $$5, $$2));
            }
            int $$8 = this.stateToIdMap.get($$6);
            if (1 << this.storage.getBits() <= $$8) {
                PackedBitStorage $$9 = new PackedBitStorage(this.storage.getBits() + 1, 4096);
                for (int $$10 = 0; $$10 < 4096; ++$$10) {
                    $$9.set($$10, this.storage.get($$10));
                }
                this.storage = $$9;
            }
            this.storage.set($$0, $$8);
        }
    }

    public static abstract class Section {
        protected static final String BLOCK_STATES_TAG = "BlockStates";
        protected static final String NAME_TAG = "Name";
        protected static final String PROPERTIES_TAG = "Properties";
        private final Type<Pair<String, Dynamic<?>>> blockStateType = DSL.named((String)References.BLOCK_STATE.typeName(), (Type)DSL.remainderType());
        protected final OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder = DSL.fieldFinder((String)"Palette", (Type)DSL.list(this.blockStateType));
        protected final List<Dynamic<?>> palette;
        protected final int index;
        protected @Nullable PackedBitStorage storage;

        public Section(Typed<?> $$02, Schema $$1) {
            if (!Objects.equals($$1.getType(References.BLOCK_STATE), this.blockStateType)) {
                throw new IllegalStateException("Block state type is not what was expected.");
            }
            Optional $$2 = $$02.getOptional(this.paletteFinder);
            this.palette = $$2.map($$0 -> $$0.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse((List)ImmutableList.of());
            Dynamic $$3 = (Dynamic)$$02.get(DSL.remainderFinder());
            this.index = $$3.get("Y").asInt(0);
            this.readStorage($$3);
        }

        protected void readStorage(Dynamic<?> $$0) {
            if (this.skippable()) {
                this.storage = null;
            } else {
                long[] $$1 = $$0.get(BLOCK_STATES_TAG).asLongStream().toArray();
                int $$2 = Math.max(4, DataFixUtils.ceillog2((int)this.palette.size()));
                this.storage = new PackedBitStorage($$2, 4096, $$1);
            }
        }

        public Typed<?> write(Typed<?> $$02) {
            if (this.isSkippable()) {
                return $$02;
            }
            return $$02.update(DSL.remainderFinder(), $$0 -> $$0.set(BLOCK_STATES_TAG, $$0.createLongList(Arrays.stream(this.storage.getRaw())))).set(this.paletteFinder, this.palette.stream().map($$0 -> Pair.of((Object)References.BLOCK_STATE.typeName(), (Object)$$0)).collect(Collectors.toList()));
        }

        public boolean isSkippable() {
            return this.storage == null;
        }

        public int getBlock(int $$0) {
            return this.storage.get($$0);
        }

        protected int getStateId(String $$0, boolean $$1, int $$2) {
            return LEAVES.get((Object)$$0) << 5 | ($$1 ? 16 : 0) | $$2;
        }

        int getIndex() {
            return this.index;
        }

        protected abstract boolean skippable();
    }
}

