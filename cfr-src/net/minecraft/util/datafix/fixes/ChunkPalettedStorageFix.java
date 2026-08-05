/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix
extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 4096;

    public ChunkPalettedStorageFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public static String getName(Dynamic<?> $$0) {
        return $$0.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> $$0, String $$1) {
        return $$0.get("Properties").get($$1).asString("");
    }

    public static int idFor(CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> $$0, Dynamic<?> $$1) {
        int $$2 = $$0.getId($$1);
        if ($$2 == -1) {
            $$2 = $$0.add($$1);
        }
        return $$2;
    }

    private Dynamic<?> fix(Dynamic<?> $$0) {
        Optional $$1 = $$0.get("Level").result();
        if ($$1.isPresent() && ((Dynamic)$$1.get()).get("Sections").asStreamOpt().result().isPresent()) {
            return $$0.set("Level", new UpgradeChunk((Dynamic)$$1.get()).write());
        }
        return $$0;
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        Type $$1 = this.getOutputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("ChunkPalettedStorageFix", $$0, $$1, this::fix);
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

    static final class UpgradeChunk {
        private int sides;
        private final @Nullable Section[] sections = new Section[16];
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap(16);

        public UpgradeChunk(Dynamic<?> $$0) {
            this.level = $$0;
            this.x = $$0.get("xPos").asInt(0) << 4;
            this.z = $$0.get("zPos").asInt(0) << 4;
            $$0.get("TileEntities").asStreamOpt().ifSuccess($$02 -> $$02.forEach($$0 -> {
                int $$3;
                int $$1 = $$0.get("x").asInt(0) - this.x & 0xF;
                int $$2 = $$0.get("y").asInt(0);
                int $$4 = $$2 << 8 | ($$3 = $$0.get("z").asInt(0) - this.z & 0xF) << 4 | $$1;
                if (this.blockEntities.put($$4, $$0) != null) {
                    LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", new Object[]{this.x, this.z, $$1, $$2, $$3});
                }
            }));
            boolean $$1 = $$0.get("convertedFromAlphaFormat").asBoolean(false);
            $$0.get("Sections").asStreamOpt().ifSuccess($$02 -> $$02.forEach($$0 -> {
                Section $$1 = new Section((Dynamic<?>)$$0);
                this.sides = $$1.upgrade(this.sides);
                this.sections[$$1.y] = $$1;
            }));
            for (Section $$2 : this.sections) {
                if ($$2 == null) continue;
                block30: for (Int2ObjectMap.Entry $$3 : $$2.toFix.int2ObjectEntrySet()) {
                    int $$4 = $$2.y << 12;
                    switch ($$3.getIntKey()) {
                        case 2: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$7;
                                int $$5 = (Integer)intListIterator.next();
                                Dynamic<?> $$6 = this.getBlock($$5 |= $$4);
                                if (!"minecraft:grass_block".equals(ChunkPalettedStorageFix.getName($$6)) || !"minecraft:snow".equals($$7 = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative($$5, Direction.UP)))) && !"minecraft:snow_layer".equals($$7)) continue;
                                this.setBlock($$5, MappingConstants.SNOWY_GRASS);
                            }
                            continue block30;
                        }
                        case 3: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$10;
                                int $$8 = (Integer)intListIterator.next();
                                Dynamic<?> $$9 = this.getBlock($$8 |= $$4);
                                if (!"minecraft:podzol".equals(ChunkPalettedStorageFix.getName($$9)) || !"minecraft:snow".equals($$10 = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative($$8, Direction.UP)))) && !"minecraft:snow_layer".equals($$10)) continue;
                                this.setBlock($$8, MappingConstants.SNOWY_PODZOL);
                            }
                            continue block30;
                        }
                        case 110: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$13;
                                int $$11 = (Integer)intListIterator.next();
                                Dynamic<?> $$12 = this.getBlock($$11 |= $$4);
                                if (!"minecraft:mycelium".equals(ChunkPalettedStorageFix.getName($$12)) || !"minecraft:snow".equals($$13 = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative($$11, Direction.UP)))) && !"minecraft:snow_layer".equals($$13)) continue;
                                this.setBlock($$11, MappingConstants.SNOWY_MYCELIUM);
                            }
                            continue block30;
                        }
                        case 25: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int $$14 = (Integer)intListIterator.next();
                                Dynamic<?> $$15 = this.removeBlockEntity($$14 |= $$4);
                                if ($$15 == null) continue;
                                String $$16 = Boolean.toString($$15.get("powered").asBoolean(false)) + (byte)Math.min(Math.max($$15.get("note").asInt(0), 0), 24);
                                this.setBlock($$14, MappingConstants.NOTE_BLOCK_MAP.getOrDefault($$16, MappingConstants.NOTE_BLOCK_MAP.get("false0")));
                            }
                            continue block30;
                        }
                        case 26: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$21;
                                int $$20;
                                int $$17 = (Integer)intListIterator.next();
                                Dynamic<?> $$18 = this.getBlockEntity($$17 |= $$4);
                                Dynamic<?> $$19 = this.getBlock($$17);
                                if ($$18 == null || ($$20 = $$18.get("color").asInt(0)) == 14 || $$20 < 0 || $$20 >= 16 || !MappingConstants.BED_BLOCK_MAP.containsKey($$21 = ChunkPalettedStorageFix.getProperty($$19, "facing") + ChunkPalettedStorageFix.getProperty($$19, "occupied") + ChunkPalettedStorageFix.getProperty($$19, "part") + $$20)) continue;
                                this.setBlock($$17, MappingConstants.BED_BLOCK_MAP.get($$21));
                            }
                            continue block30;
                        }
                        case 176: 
                        case 177: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$26;
                                int $$25;
                                int $$22 = (Integer)intListIterator.next();
                                Dynamic<?> $$23 = this.getBlockEntity($$22 |= $$4);
                                Dynamic<?> $$24 = this.getBlock($$22);
                                if ($$23 == null || ($$25 = $$23.get("Base").asInt(0)) == 15 || $$25 < 0 || $$25 >= 16 || !MappingConstants.BANNER_BLOCK_MAP.containsKey($$26 = ChunkPalettedStorageFix.getProperty($$24, $$3.getIntKey() == 176 ? "rotation" : "facing") + "_" + $$25)) continue;
                                this.setBlock($$22, MappingConstants.BANNER_BLOCK_MAP.get($$26));
                            }
                            continue block30;
                        }
                        case 86: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$29;
                                int $$27 = (Integer)intListIterator.next();
                                Dynamic<?> $$28 = this.getBlock($$27 |= $$4);
                                if (!"minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName($$28)) || !"minecraft:grass_block".equals($$29 = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative($$27, Direction.DOWN)))) && !"minecraft:dirt".equals($$29)) continue;
                                this.setBlock($$27, MappingConstants.PUMPKIN);
                            }
                            continue block30;
                        }
                        case 140: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int $$30 = (Integer)intListIterator.next();
                                Dynamic<?> $$31 = this.removeBlockEntity($$30 |= $$4);
                                if ($$31 == null) continue;
                                String $$32 = $$31.get("Item").asString("") + $$31.get("Data").asInt(0);
                                this.setBlock($$30, MappingConstants.FLOWER_POT_MAP.getOrDefault($$32, MappingConstants.FLOWER_POT_MAP.get("minecraft:air0")));
                            }
                            continue block30;
                        }
                        case 144: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$38;
                                int $$33 = (Integer)intListIterator.next();
                                Dynamic<?> $$34 = this.getBlockEntity($$33 |= $$4);
                                if ($$34 == null) continue;
                                String $$35 = String.valueOf($$34.get("SkullType").asInt(0));
                                String $$36 = ChunkPalettedStorageFix.getProperty(this.getBlock($$33), "facing");
                                if ("up".equals($$36) || "down".equals($$36)) {
                                    String $$37 = $$35 + $$34.get("Rot").asInt(0);
                                } else {
                                    $$38 = $$35 + $$36;
                                }
                                $$34.remove("SkullType");
                                $$34.remove("facing");
                                $$34.remove("Rot");
                                this.setBlock($$33, MappingConstants.SKULL_MAP.getOrDefault($$38, MappingConstants.SKULL_MAP.get("0north")));
                            }
                            continue block30;
                        }
                        case 64: 
                        case 71: 
                        case 193: 
                        case 194: 
                        case 195: 
                        case 196: 
                        case 197: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                Dynamic<?> $$41;
                                int $$39 = (Integer)intListIterator.next();
                                Dynamic<?> $$40 = this.getBlock($$39 |= $$4);
                                if (!ChunkPalettedStorageFix.getName($$40).endsWith("_door") || !"lower".equals(ChunkPalettedStorageFix.getProperty($$41 = this.getBlock($$39), "half"))) continue;
                                int $$42 = UpgradeChunk.relative($$39, Direction.UP);
                                Dynamic<?> $$43 = this.getBlock($$42);
                                String $$44 = ChunkPalettedStorageFix.getName($$41);
                                if (!$$44.equals(ChunkPalettedStorageFix.getName($$43))) continue;
                                String $$45 = ChunkPalettedStorageFix.getProperty($$41, "facing");
                                String $$46 = ChunkPalettedStorageFix.getProperty($$41, "open");
                                String $$47 = $$1 ? "left" : ChunkPalettedStorageFix.getProperty($$43, "hinge");
                                String $$48 = $$1 ? "false" : ChunkPalettedStorageFix.getProperty($$43, "powered");
                                this.setBlock($$39, MappingConstants.DOOR_MAP.get($$44 + $$45 + "lower" + $$47 + $$46 + $$48));
                                this.setBlock($$42, MappingConstants.DOOR_MAP.get($$44 + $$45 + "upper" + $$47 + $$46 + $$48));
                            }
                            continue block30;
                        }
                        case 175: {
                            IntListIterator intListIterator = ((IntList)$$3.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String $$52;
                                int $$49 = (Integer)intListIterator.next();
                                Dynamic<?> $$50 = this.getBlock($$49 |= $$4);
                                if (!"upper".equals(ChunkPalettedStorageFix.getProperty($$50, "half"))) continue;
                                Dynamic<?> $$51 = this.getBlock(UpgradeChunk.relative($$49, Direction.DOWN));
                                switch ($$52 = ChunkPalettedStorageFix.getName($$51)) {
                                    case "minecraft:sunflower": {
                                        this.setBlock($$49, MappingConstants.UPPER_SUNFLOWER);
                                        break;
                                    }
                                    case "minecraft:lilac": {
                                        this.setBlock($$49, MappingConstants.UPPER_LILAC);
                                        break;
                                    }
                                    case "minecraft:tall_grass": {
                                        this.setBlock($$49, MappingConstants.UPPER_TALL_GRASS);
                                        break;
                                    }
                                    case "minecraft:large_fern": {
                                        this.setBlock($$49, MappingConstants.UPPER_LARGE_FERN);
                                        break;
                                    }
                                    case "minecraft:rose_bush": {
                                        this.setBlock($$49, MappingConstants.UPPER_ROSE_BUSH);
                                        break;
                                    }
                                    case "minecraft:peony": {
                                        this.setBlock($$49, MappingConstants.UPPER_PEONY);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        private @Nullable Dynamic<?> getBlockEntity(int $$0) {
            return (Dynamic)this.blockEntities.get($$0);
        }

        private @Nullable Dynamic<?> removeBlockEntity(int $$0) {
            return (Dynamic)this.blockEntities.remove($$0);
        }

        public static int relative(int $$0, Direction $$1) {
            return switch ($$1.getAxis().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    int $$2 = ($$0 & 0xF) + $$1.getAxisDirection().getStep();
                    if ($$2 < 0 || $$2 > 15) {
                        yield -1;
                    }
                    yield $$0 & 0xFFFFFFF0 | $$2;
                }
                case 1 -> {
                    int $$3 = ($$0 >> 8) + $$1.getAxisDirection().getStep();
                    if ($$3 < 0 || $$3 > 255) {
                        yield -1;
                    }
                    yield $$0 & 0xFF | $$3 << 8;
                }
                case 2 -> {
                    int $$4 = ($$0 >> 4 & 0xF) + $$1.getAxisDirection().getStep();
                    if ($$4 < 0 || $$4 > 15) {
                        yield -1;
                    }
                    yield $$0 & 0xFFFFFF0F | $$4 << 4;
                }
            };
        }

        private void setBlock(int $$0, Dynamic<?> $$1) {
            if ($$0 < 0 || $$0 > 65535) {
                return;
            }
            Section $$2 = this.getSection($$0);
            if ($$2 == null) {
                return;
            }
            $$2.setBlock($$0 & 0xFFF, $$1);
        }

        private @Nullable Section getSection(int $$0) {
            int $$1 = $$0 >> 12;
            return $$1 < this.sections.length ? this.sections[$$1] : null;
        }

        public Dynamic<?> getBlock(int $$0) {
            if ($$0 < 0 || $$0 > 65535) {
                return MappingConstants.AIR;
            }
            Section $$1 = this.getSection($$0);
            if ($$1 == null) {
                return MappingConstants.AIR;
            }
            return $$1.getBlock($$0 & 0xFFF);
        }

        public Dynamic<?> write() {
            Dynamic $$0 = this.level;
            $$0 = this.blockEntities.isEmpty() ? $$0.remove("TileEntities") : $$0.set("TileEntities", $$0.createList(this.blockEntities.values().stream()));
            Dynamic $$1 = $$0.emptyMap();
            ArrayList $$2 = Lists.newArrayList();
            for (Section $$3 : this.sections) {
                if ($$3 == null) continue;
                $$2.add($$3.write());
                $$1 = $$1.set(String.valueOf($$3.y), $$1.createIntList(Arrays.stream($$3.update.toIntArray())));
            }
            Dynamic $$4 = $$0.emptyMap();
            $$4 = $$4.set("Sides", $$4.createByte((byte)this.sides));
            $$4 = $$4.set("Indices", $$1);
            return $$0.set("UpgradeData", $$4).set("Sections", $$4.createList($$2.stream()));
        }
    }

    public static enum Direction {
        DOWN(AxisDirection.NEGATIVE, Axis.Y),
        UP(AxisDirection.POSITIVE, Axis.Y),
        NORTH(AxisDirection.NEGATIVE, Axis.Z),
        SOUTH(AxisDirection.POSITIVE, Axis.Z),
        WEST(AxisDirection.NEGATIVE, Axis.X),
        EAST(AxisDirection.POSITIVE, Axis.X);

        private final Axis axis;
        private final AxisDirection axisDirection;

        private Direction(AxisDirection $$0, Axis $$1) {
            this.axis = $$1;
            this.axisDirection = $$0;
        }

        public AxisDirection getAxisDirection() {
            return this.axisDirection;
        }

        public Axis getAxis() {
            return this.axis;
        }

        public static enum Axis {
            X,
            Y,
            Z;

        }

        public static enum AxisDirection {
            POSITIVE(1),
            NEGATIVE(-1);

            private final int step;

            private AxisDirection(int $$0) {
                this.step = $$0;
            }

            public int getStep() {
                return this.step;
            }
        }
    }

    static class DataLayer {
        private static final int SIZE = 2048;
        private static final int NIBBLE_SIZE = 4;
        private final byte[] data;

        public DataLayer() {
            this.data = new byte[2048];
        }

        public DataLayer(byte[] $$0) {
            this.data = $$0;
            if ($$0.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + $$0.length);
            }
        }

        public int get(int $$0, int $$1, int $$2) {
            int $$3 = this.getPosition($$1 << 8 | $$2 << 4 | $$0);
            if (this.isFirst($$1 << 8 | $$2 << 4 | $$0)) {
                return this.data[$$3] & 0xF;
            }
            return this.data[$$3] >> 4 & 0xF;
        }

        private boolean isFirst(int $$0) {
            return ($$0 & 1) == 0;
        }

        private int getPosition(int $$0) {
            return $$0 >> 1;
        }
    }

    static class Section {
        private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = CrudeIncrementalIntIdentityHashBiMap.create(32);
        private final List<Dynamic<?>> listTag;
        private final Dynamic<?> section;
        private final boolean hasData;
        final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap();
        final IntList update = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
        private final int[] buffer = new int[4096];

        public Section(Dynamic<?> $$0) {
            this.listTag = Lists.newArrayList();
            this.section = $$0;
            this.y = $$0.get("Y").asInt(0);
            this.hasData = $$0.get("Blocks").result().isPresent();
        }

        public Dynamic<?> getBlock(int $$0) {
            if ($$0 < 0 || $$0 > 4095) {
                return MappingConstants.AIR;
            }
            Dynamic<?> $$1 = this.palette.byId(this.buffer[$$0]);
            return $$1 == null ? MappingConstants.AIR : $$1;
        }

        public void setBlock(int $$0, Dynamic<?> $$1) {
            if (this.seen.add($$1)) {
                this.listTag.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName($$1)) ? MappingConstants.AIR : $$1);
            }
            this.buffer[$$0] = ChunkPalettedStorageFix.idFor(this.palette, $$1);
        }

        public int upgrade(int $$02) {
            if (!this.hasData) {
                return $$02;
            }
            ByteBuffer $$1 = (ByteBuffer)this.section.get("Blocks").asByteBufferOpt().result().get();
            DataLayer $$2 = this.section.get("Data").asByteBufferOpt().map($$0 -> new DataLayer(DataFixUtils.toArray((ByteBuffer)$$0))).result().orElseGet(DataLayer::new);
            DataLayer $$3 = this.section.get("Add").asByteBufferOpt().map($$0 -> new DataLayer(DataFixUtils.toArray((ByteBuffer)$$0))).result().orElseGet(DataLayer::new);
            this.seen.add(MappingConstants.AIR);
            ChunkPalettedStorageFix.idFor(this.palette, MappingConstants.AIR);
            this.listTag.add(MappingConstants.AIR);
            for (int $$4 = 0; $$4 < 4096; ++$$4) {
                int $$5 = $$4 & 0xF;
                int $$6 = $$4 >> 8 & 0xF;
                int $$7 = $$4 >> 4 & 0xF;
                int $$8 = $$3.get($$5, $$6, $$7) << 12 | ($$1.get($$4) & 0xFF) << 4 | $$2.get($$5, $$6, $$7);
                if (MappingConstants.FIX.get($$8 >> 4)) {
                    this.addFix($$8 >> 4, $$4);
                }
                if (MappingConstants.VIRTUAL.get($$8 >> 4)) {
                    int $$9 = ChunkPalettedStorageFix.getSideMask($$5 == 0, $$5 == 15, $$7 == 0, $$7 == 15);
                    if ($$9 == 0) {
                        this.update.add($$4);
                    } else {
                        $$02 |= $$9;
                    }
                }
                this.setBlock($$4, BlockStateData.getTag($$8));
            }
            return $$02;
        }

        private void addFix(int $$0, int $$1) {
            IntList $$2 = (IntList)this.toFix.get($$0);
            if ($$2 == null) {
                $$2 = new IntArrayList();
                this.toFix.put($$0, (Object)$$2);
            }
            $$2.add($$1);
        }

        public Dynamic<?> write() {
            Dynamic $$0 = this.section;
            if (!this.hasData) {
                return $$0;
            }
            $$0 = $$0.set("Palette", $$0.createList(this.listTag.stream()));
            int $$1 = Math.max(4, DataFixUtils.ceillog2((int)this.seen.size()));
            PackedBitStorage $$2 = new PackedBitStorage($$1, 4096);
            for (int $$3 = 0; $$3 < this.buffer.length; ++$$3) {
                $$2.set($$3, this.buffer[$$3]);
            }
            $$0 = $$0.set("BlockStates", $$0.createLongList(Arrays.stream($$2.getRaw())));
            $$0 = $$0.remove("Blocks");
            $$0 = $$0.remove("Data");
            $$0 = $$0.remove("Add");
            return $$0;
        }
    }

    static class MappingConstants {
        static final BitSet VIRTUAL = new BitSet(256);
        static final BitSet FIX = new BitSet(256);
        static final Dynamic<?> PUMPKIN = ExtraDataFixUtils.blockState("minecraft:pumpkin");
        static final Dynamic<?> SNOWY_PODZOL = ExtraDataFixUtils.blockState("minecraft:podzol", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_GRASS = ExtraDataFixUtils.blockState("minecraft:grass_block", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_MYCELIUM = ExtraDataFixUtils.blockState("minecraft:mycelium", Map.of("snowy", "true"));
        static final Dynamic<?> UPPER_SUNFLOWER = ExtraDataFixUtils.blockState("minecraft:sunflower", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LILAC = ExtraDataFixUtils.blockState("minecraft:lilac", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_TALL_GRASS = ExtraDataFixUtils.blockState("minecraft:tall_grass", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LARGE_FERN = ExtraDataFixUtils.blockState("minecraft:large_fern", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_ROSE_BUSH = ExtraDataFixUtils.blockState("minecraft:rose_bush", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_PEONY = ExtraDataFixUtils.blockState("minecraft:peony", Map.of("half", "upper"));
        static final Map<String, Dynamic<?>> FLOWER_POT_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            $$0.put("minecraft:air0", ExtraDataFixUtils.blockState("minecraft:flower_pot"));
            $$0.put("minecraft:red_flower0", ExtraDataFixUtils.blockState("minecraft:potted_poppy"));
            $$0.put("minecraft:red_flower1", ExtraDataFixUtils.blockState("minecraft:potted_blue_orchid"));
            $$0.put("minecraft:red_flower2", ExtraDataFixUtils.blockState("minecraft:potted_allium"));
            $$0.put("minecraft:red_flower3", ExtraDataFixUtils.blockState("minecraft:potted_azure_bluet"));
            $$0.put("minecraft:red_flower4", ExtraDataFixUtils.blockState("minecraft:potted_red_tulip"));
            $$0.put("minecraft:red_flower5", ExtraDataFixUtils.blockState("minecraft:potted_orange_tulip"));
            $$0.put("minecraft:red_flower6", ExtraDataFixUtils.blockState("minecraft:potted_white_tulip"));
            $$0.put("minecraft:red_flower7", ExtraDataFixUtils.blockState("minecraft:potted_pink_tulip"));
            $$0.put("minecraft:red_flower8", ExtraDataFixUtils.blockState("minecraft:potted_oxeye_daisy"));
            $$0.put("minecraft:yellow_flower0", ExtraDataFixUtils.blockState("minecraft:potted_dandelion"));
            $$0.put("minecraft:sapling0", ExtraDataFixUtils.blockState("minecraft:potted_oak_sapling"));
            $$0.put("minecraft:sapling1", ExtraDataFixUtils.blockState("minecraft:potted_spruce_sapling"));
            $$0.put("minecraft:sapling2", ExtraDataFixUtils.blockState("minecraft:potted_birch_sapling"));
            $$0.put("minecraft:sapling3", ExtraDataFixUtils.blockState("minecraft:potted_jungle_sapling"));
            $$0.put("minecraft:sapling4", ExtraDataFixUtils.blockState("minecraft:potted_acacia_sapling"));
            $$0.put("minecraft:sapling5", ExtraDataFixUtils.blockState("minecraft:potted_dark_oak_sapling"));
            $$0.put("minecraft:red_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_red_mushroom"));
            $$0.put("minecraft:brown_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_brown_mushroom"));
            $$0.put("minecraft:deadbush0", ExtraDataFixUtils.blockState("minecraft:potted_dead_bush"));
            $$0.put("minecraft:tallgrass2", ExtraDataFixUtils.blockState("minecraft:potted_fern"));
            $$0.put("minecraft:cactus0", ExtraDataFixUtils.blockState("minecraft:potted_cactus"));
        });
        static final Map<String, Dynamic<?>> SKULL_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            MappingConstants.mapSkull($$0, 0, "skeleton", "skull");
            MappingConstants.mapSkull($$0, 1, "wither_skeleton", "skull");
            MappingConstants.mapSkull($$0, 2, "zombie", "head");
            MappingConstants.mapSkull($$0, 3, "player", "head");
            MappingConstants.mapSkull($$0, 4, "creeper", "head");
            MappingConstants.mapSkull($$0, 5, "dragon", "head");
        });
        static final Map<String, Dynamic<?>> DOOR_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            MappingConstants.mapDoor($$0, "oak_door");
            MappingConstants.mapDoor($$0, "iron_door");
            MappingConstants.mapDoor($$0, "spruce_door");
            MappingConstants.mapDoor($$0, "birch_door");
            MappingConstants.mapDoor($$0, "jungle_door");
            MappingConstants.mapDoor($$0, "acacia_door");
            MappingConstants.mapDoor($$0, "dark_oak_door");
        });
        static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            for (int $$1 = 0; $$1 < 26; ++$$1) {
                $$0.put("true" + $$1, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "true", "note", String.valueOf($$1))));
                $$0.put("false" + $$1, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "false", "note", String.valueOf($$1))));
            }
        });
        private static final Int2ObjectMap<String> DYE_COLOR_MAP = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), $$0 -> {
            $$0.put(0, (Object)"white");
            $$0.put(1, (Object)"orange");
            $$0.put(2, (Object)"magenta");
            $$0.put(3, (Object)"light_blue");
            $$0.put(4, (Object)"yellow");
            $$0.put(5, (Object)"lime");
            $$0.put(6, (Object)"pink");
            $$0.put(7, (Object)"gray");
            $$0.put(8, (Object)"light_gray");
            $$0.put(9, (Object)"cyan");
            $$0.put(10, (Object)"purple");
            $$0.put(11, (Object)"blue");
            $$0.put(12, (Object)"brown");
            $$0.put(13, (Object)"green");
            $$0.put(14, (Object)"red");
            $$0.put(15, (Object)"black");
        });
        static final Map<String, Dynamic<?>> BED_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            for (Int2ObjectMap.Entry $$1 : DYE_COLOR_MAP.int2ObjectEntrySet()) {
                if (Objects.equals($$1.getValue(), "red")) continue;
                MappingConstants.addBeds($$0, $$1.getIntKey(), (String)$$1.getValue());
            }
        });
        static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
            for (Int2ObjectMap.Entry $$1 : DYE_COLOR_MAP.int2ObjectEntrySet()) {
                if (Objects.equals($$1.getValue(), "white")) continue;
                MappingConstants.addBanners($$0, 15 - $$1.getIntKey(), (String)$$1.getValue());
            }
        });
        static final Dynamic<?> AIR;

        private MappingConstants() {
        }

        private static void mapSkull(Map<String, Dynamic<?>> $$0, int $$1, String $$2, String $$3) {
            $$0.put($$1 + "north", ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_" + $$3, Map.of("facing", "north")));
            $$0.put($$1 + "east", ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_" + $$3, Map.of("facing", "east")));
            $$0.put($$1 + "south", ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_" + $$3, Map.of("facing", "south")));
            $$0.put($$1 + "west", ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_" + $$3, Map.of("facing", "west")));
            for (int $$4 = 0; $$4 < 16; ++$$4) {
                $$0.put("" + $$1 + $$4, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_" + $$3, Map.of("rotation", String.valueOf($$4))));
            }
        }

        private static void mapDoor(Map<String, Dynamic<?>> $$0, String $$1) {
            String $$2 = "minecraft:" + $$1;
            $$0.put("minecraft:" + $$1 + "eastlowerleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastlowerleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastlowerlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastlowerlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastlowerrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastlowerrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastlowerrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastlowerrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastupperleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastupperleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastupperlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastupperlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastupperrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastupperrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "eastupperrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "eastupperrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northlowerleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northlowerleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northlowerlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northlowerlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northlowerrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northlowerrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northlowerrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northlowerrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northupperleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northupperleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northupperlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northupperlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northupperrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northupperrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "northupperrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "northupperrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southlowerleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southlowerleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southlowerlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southlowerlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southlowerrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southlowerrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southlowerrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southlowerrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southupperleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southupperleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southupperlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southupperlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southupperrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southupperrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "southupperrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "southupperrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westlowerleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westlowerleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westlowerlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westlowerlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westlowerrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westlowerrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westlowerrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westlowerrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westupperleftfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westupperleftfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westupperlefttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westupperlefttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westupperrightfalsefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westupperrightfalsetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            $$0.put("minecraft:" + $$1 + "westupperrighttruefalse", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            $$0.put("minecraft:" + $$1 + "westupperrighttruetrue", ExtraDataFixUtils.blockState($$2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
        }

        private static void addBeds(Map<String, Dynamic<?>> $$0, int $$1, String $$2) {
            $$0.put("southfalsefoot" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "south", "occupied", "false", "part", "foot")));
            $$0.put("westfalsefoot" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "west", "occupied", "false", "part", "foot")));
            $$0.put("northfalsefoot" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "north", "occupied", "false", "part", "foot")));
            $$0.put("eastfalsefoot" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "east", "occupied", "false", "part", "foot")));
            $$0.put("southfalsehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "south", "occupied", "false", "part", "head")));
            $$0.put("westfalsehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "west", "occupied", "false", "part", "head")));
            $$0.put("northfalsehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "north", "occupied", "false", "part", "head")));
            $$0.put("eastfalsehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "east", "occupied", "false", "part", "head")));
            $$0.put("southtruehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "south", "occupied", "true", "part", "head")));
            $$0.put("westtruehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "west", "occupied", "true", "part", "head")));
            $$0.put("northtruehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "north", "occupied", "true", "part", "head")));
            $$0.put("easttruehead" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_bed", Map.of("facing", "east", "occupied", "true", "part", "head")));
        }

        private static void addBanners(Map<String, Dynamic<?>> $$0, int $$1, String $$2) {
            for (int $$3 = 0; $$3 < 16; ++$$3) {
                $$0.put($$3 + "_" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_banner", Map.of("rotation", String.valueOf($$3))));
            }
            $$0.put("north_" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_banner", Map.of("facing", "north")));
            $$0.put("south_" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_banner", Map.of("facing", "south")));
            $$0.put("west_" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_banner", Map.of("facing", "west")));
            $$0.put("east_" + $$1, ExtraDataFixUtils.blockState("minecraft:" + $$2 + "_wall_banner", Map.of("facing", "east")));
        }

        static {
            FIX.set(2);
            FIX.set(3);
            FIX.set(110);
            FIX.set(140);
            FIX.set(144);
            FIX.set(25);
            FIX.set(86);
            FIX.set(26);
            FIX.set(176);
            FIX.set(177);
            FIX.set(175);
            FIX.set(64);
            FIX.set(71);
            FIX.set(193);
            FIX.set(194);
            FIX.set(195);
            FIX.set(196);
            FIX.set(197);
            VIRTUAL.set(54);
            VIRTUAL.set(146);
            VIRTUAL.set(25);
            VIRTUAL.set(26);
            VIRTUAL.set(51);
            VIRTUAL.set(53);
            VIRTUAL.set(67);
            VIRTUAL.set(108);
            VIRTUAL.set(109);
            VIRTUAL.set(114);
            VIRTUAL.set(128);
            VIRTUAL.set(134);
            VIRTUAL.set(135);
            VIRTUAL.set(136);
            VIRTUAL.set(156);
            VIRTUAL.set(163);
            VIRTUAL.set(164);
            VIRTUAL.set(180);
            VIRTUAL.set(203);
            VIRTUAL.set(55);
            VIRTUAL.set(85);
            VIRTUAL.set(113);
            VIRTUAL.set(188);
            VIRTUAL.set(189);
            VIRTUAL.set(190);
            VIRTUAL.set(191);
            VIRTUAL.set(192);
            VIRTUAL.set(93);
            VIRTUAL.set(94);
            VIRTUAL.set(101);
            VIRTUAL.set(102);
            VIRTUAL.set(160);
            VIRTUAL.set(106);
            VIRTUAL.set(107);
            VIRTUAL.set(183);
            VIRTUAL.set(184);
            VIRTUAL.set(185);
            VIRTUAL.set(186);
            VIRTUAL.set(187);
            VIRTUAL.set(132);
            VIRTUAL.set(139);
            VIRTUAL.set(199);
            AIR = ExtraDataFixUtils.blockState("minecraft:air");
        }
    }
}

