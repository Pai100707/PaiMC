/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt($$0 -> $$0.getIntOr(1, 0)).thenComparingInt($$0 -> $$0.getIntOr(0, 0)).thenComparingInt($$0 -> $$0.getIntOr(2, 0));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble($$0 -> $$0.getDoubleOr(1, 0.0)).thenComparingDouble($$0 -> $$0.getDoubleOr(0, 0.0)).thenComparingDouble($$0 -> $$0.getDoubleOr(2, 0.0));
    private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on((String)",");
    private static final Splitter COLON_SPLITTER = Splitter.on((char)':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag $$0, @Nullable Tag $$1, boolean $$2) {
        if ($$0 == $$1) {
            return true;
        }
        if ($$0 == null) {
            return true;
        }
        if ($$1 == null) {
            return false;
        }
        if (!$$0.getClass().equals($$1.getClass())) {
            return false;
        }
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$3 = (CompoundTag)$$0;
            CompoundTag $$4 = (CompoundTag)$$1;
            if ($$4.size() < $$3.size()) {
                return false;
            }
            for (Map.Entry<String, Tag> $$5 : $$3.entrySet()) {
                Tag $$6 = $$5.getValue();
                if (NbtUtils.compareNbt($$6, $$4.get($$5.getKey()), $$2)) continue;
                return false;
            }
            return true;
        }
        if ($$0 instanceof ListTag) {
            ListTag $$7 = (ListTag)$$0;
            if ($$2) {
                ListTag $$8 = (ListTag)$$1;
                if ($$7.isEmpty()) {
                    return $$8.isEmpty();
                }
                if ($$8.size() < $$7.size()) {
                    return false;
                }
                for (Tag $$9 : $$7) {
                    boolean $$10 = false;
                    for (Tag $$11 : $$8) {
                        if (!NbtUtils.compareNbt($$9, $$11, $$2)) continue;
                        $$10 = true;
                        break;
                    }
                    if ($$10) continue;
                    return false;
                }
                return true;
            }
        }
        return $$0.equals($$1);
    }

    public static BlockState readBlockState(HolderGetter<Block> $$0, CompoundTag $$1) {
        Optional $$2 = $$1.read("Name", BLOCK_NAME_CODEC).flatMap($$0::get);
        if ($$2.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block $$3 = (Block)((Holder)$$2.get()).value();
        BlockState $$4 = $$3.defaultBlockState();
        Optional<CompoundTag> $$5 = $$1.getCompound("Properties");
        if ($$5.isPresent()) {
            StateDefinition<Block, BlockState> $$6 = $$3.getStateDefinition();
            for (String $$7 : $$5.get().keySet()) {
                Property<?> $$8 = $$6.getProperty($$7);
                if ($$8 == null) continue;
                $$4 = NbtUtils.setValueHelper($$4, $$8, $$7, $$5.get(), $$1);
            }
        }
        return $$4;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S $$0, Property<T> $$1, String $$2, CompoundTag $$3, CompoundTag $$4) {
        Optional $$5 = $$3.getString($$2).flatMap($$1::getValue);
        if ($$5.isPresent()) {
            return (S)((StateHolder)$$0.setValue($$1, (Comparable)((Comparable)$$5.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{$$2, $$3.get($$2), $$4});
        return $$0;
    }

    public static CompoundTag writeBlockState(BlockState $$0) {
        CompoundTag $$1 = new CompoundTag();
        $$1.putString("Name", BuiltInRegistries.BLOCK.getKey($$0.getBlock()).toString());
        Map<Property<?>, Comparable<?>> $$2 = $$0.getValues();
        if (!$$2.isEmpty()) {
            CompoundTag $$3 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> $$4 : $$2.entrySet()) {
                Property<?> $$5 = $$4.getKey();
                $$3.putString($$5.getName(), NbtUtils.getName($$5, $$4.getValue()));
            }
            $$1.put("Properties", $$3);
        }
        return $$1;
    }

    public static CompoundTag writeFluidState(FluidState $$0) {
        CompoundTag $$1 = new CompoundTag();
        $$1.putString("Name", BuiltInRegistries.FLUID.getKey($$0.getType()).toString());
        Map<Property<?>, Comparable<?>> $$2 = $$0.getValues();
        if (!$$2.isEmpty()) {
            CompoundTag $$3 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> $$4 : $$2.entrySet()) {
                Property<?> $$5 = $$4.getKey();
                $$3.putString($$5.getName(), NbtUtils.getName($$5, $$4.getValue()));
            }
            $$1.put("Properties", $$3);
        }
        return $$1;
    }

    private static <T extends Comparable<T>> String getName(Property<T> $$0, Comparable<?> $$1) {
        return $$0.getName($$1);
    }

    public static String prettyPrint(Tag $$0) {
        return NbtUtils.prettyPrint($$0, false);
    }

    public static String prettyPrint(Tag $$0, boolean $$1) {
        return NbtUtils.prettyPrint(new StringBuilder(), $$0, 0, $$1).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder $$0, Tag $$1, int $$2, boolean $$3) {
        Tag tag = $$1;
        Objects.requireNonNull(tag);
        Tag tag2 = tag;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PrimitiveTag.class, EndTag.class, ByteArrayTag.class, ListTag.class, IntArrayTag.class, CompoundTag.class, LongArrayTag.class}, (Object)tag2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                PrimitiveTag $$4 = (PrimitiveTag)tag2;
                yield $$0.append($$4);
            }
            case 1 -> {
                EndTag $$5 = (EndTag)tag2;
                yield $$0;
            }
            case 2 -> {
                ByteArrayTag $$6 = (ByteArrayTag)tag2;
                byte[] $$7 = $$6.getAsByteArray();
                int $$8 = $$7.length;
                NbtUtils.indent($$2, $$0).append("byte[").append($$8).append("] {\n");
                if ($$3) {
                    NbtUtils.indent($$2 + 1, $$0);
                    for (int $$9 = 0; $$9 < $$7.length; ++$$9) {
                        if ($$9 != 0) {
                            $$0.append(',');
                        }
                        if ($$9 % 16 == 0 && $$9 / 16 > 0) {
                            $$0.append('\n');
                            if ($$9 < $$7.length) {
                                NbtUtils.indent($$2 + 1, $$0);
                            }
                        } else if ($$9 != 0) {
                            $$0.append(' ');
                        }
                        $$0.append(String.format(Locale.ROOT, "0x%02X", $$7[$$9] & 0xFF));
                    }
                } else {
                    NbtUtils.indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
                }
                $$0.append('\n');
                NbtUtils.indent($$2, $$0).append('}');
                yield $$0;
            }
            case 3 -> {
                ListTag $$10 = (ListTag)tag2;
                int $$11 = $$10.size();
                NbtUtils.indent($$2, $$0).append("list").append("[").append($$11).append("] [");
                if ($$11 != 0) {
                    $$0.append('\n');
                }
                for (int $$12 = 0; $$12 < $$11; ++$$12) {
                    if ($$12 != 0) {
                        $$0.append(",\n");
                    }
                    NbtUtils.indent($$2 + 1, $$0);
                    NbtUtils.prettyPrint($$0, $$10.get($$12), $$2 + 1, $$3);
                }
                if ($$11 != 0) {
                    $$0.append('\n');
                }
                NbtUtils.indent($$2, $$0).append(']');
                yield $$0;
            }
            case 4 -> {
                IntArrayTag $$13 = (IntArrayTag)tag2;
                int[] $$14 = $$13.getAsIntArray();
                int $$15 = 0;
                for (int $$16 : $$14) {
                    $$15 = Math.max($$15, String.format(Locale.ROOT, "%X", $$16).length());
                }
                int $$17 = $$14.length;
                NbtUtils.indent($$2, $$0).append("int[").append($$17).append("] {\n");
                if ($$3) {
                    NbtUtils.indent($$2 + 1, $$0);
                    for (int $$18 = 0; $$18 < $$14.length; ++$$18) {
                        if ($$18 != 0) {
                            $$0.append(',');
                        }
                        if ($$18 % 16 == 0 && $$18 / 16 > 0) {
                            $$0.append('\n');
                            if ($$18 < $$14.length) {
                                NbtUtils.indent($$2 + 1, $$0);
                            }
                        } else if ($$18 != 0) {
                            $$0.append(' ');
                        }
                        $$0.append(String.format(Locale.ROOT, "0x%0" + $$15 + "X", $$14[$$18]));
                    }
                } else {
                    NbtUtils.indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
                }
                $$0.append('\n');
                NbtUtils.indent($$2, $$0).append('}');
                yield $$0;
            }
            case 5 -> {
                CompoundTag $$19 = (CompoundTag)tag2;
                ArrayList $$20 = Lists.newArrayList($$19.keySet());
                Collections.sort($$20);
                NbtUtils.indent($$2, $$0).append('{');
                if ($$0.length() - $$0.lastIndexOf("\n") > 2 * ($$2 + 1)) {
                    $$0.append('\n');
                    NbtUtils.indent($$2 + 1, $$0);
                }
                int $$21 = $$20.stream().mapToInt(String::length).max().orElse(0);
                String $$22 = Strings.repeat((String)" ", (int)$$21);
                for (int $$23 = 0; $$23 < $$20.size(); ++$$23) {
                    if ($$23 != 0) {
                        $$0.append(",\n");
                    }
                    String $$24 = (String)$$20.get($$23);
                    NbtUtils.indent($$2 + 1, $$0).append('\"').append($$24).append('\"').append($$22, 0, $$22.length() - $$24.length()).append(": ");
                    NbtUtils.prettyPrint($$0, $$19.get($$24), $$2 + 1, $$3);
                }
                if (!$$20.isEmpty()) {
                    $$0.append('\n');
                }
                NbtUtils.indent($$2, $$0).append('}');
                yield $$0;
            }
            case 6 -> {
                LongArrayTag $$25 = (LongArrayTag)tag2;
                long[] $$26 = $$25.getAsLongArray();
                long $$27 = 0L;
                for (long $$28 : $$26) {
                    $$27 = Math.max($$27, (long)String.format(Locale.ROOT, "%X", $$28).length());
                }
                long $$29 = $$26.length;
                NbtUtils.indent($$2, $$0).append("long[").append($$29).append("] {\n");
                if ($$3) {
                    NbtUtils.indent($$2 + 1, $$0);
                    for (int $$30 = 0; $$30 < $$26.length; ++$$30) {
                        if ($$30 != 0) {
                            $$0.append(',');
                        }
                        if ($$30 % 16 == 0 && $$30 / 16 > 0) {
                            $$0.append('\n');
                            if ($$30 < $$26.length) {
                                NbtUtils.indent($$2 + 1, $$0);
                            }
                        } else if ($$30 != 0) {
                            $$0.append(' ');
                        }
                        $$0.append(String.format(Locale.ROOT, "0x%0" + $$27 + "X", $$26[$$30]));
                    }
                } else {
                    NbtUtils.indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
                }
                $$0.append('\n');
                NbtUtils.indent($$2, $$0).append('}');
                yield $$0;
            }
        };
    }

    private static StringBuilder indent(int $$0, StringBuilder $$1) {
        int $$2 = $$1.lastIndexOf("\n") + 1;
        int $$3 = $$1.length() - $$2;
        for (int $$4 = 0; $$4 < 2 * $$0 - $$3; ++$$4) {
            $$1.append(' ');
        }
        return $$1;
    }

    public static Component toPrettyComponent(Tag $$0) {
        return new TextComponentTagVisitor("").visit($$0);
    }

    public static String structureToSnbt(CompoundTag $$0) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate($$0));
    }

    public static CompoundTag snbtToStructure(String $$0) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseCompoundFully($$0));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag $$02) {
        Optional<ListTag> $$6;
        ListTag $$3;
        Optional<ListTag> $$12 = $$02.getList("palettes");
        if ($$12.isPresent()) {
            ListTag $$22 = $$12.get().getListOrEmpty(0);
        } else {
            $$3 = $$02.getListOrEmpty("palette");
        }
        ListTag $$4 = $$3.compoundStream().map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        $$02.put("palette", $$4);
        if ($$12.isPresent()) {
            ListTag $$5 = new ListTag();
            $$12.get().stream().flatMap($$0 -> $$0.asList().stream()).forEach($$2 -> {
                CompoundTag $$3 = new CompoundTag();
                for (int $$4 = 0; $$4 < $$2.size(); ++$$4) {
                    $$3.putString($$4.getString($$4).orElseThrow(), NbtUtils.packBlockState($$2.getCompound($$4).orElseThrow()));
                }
                $$5.add($$3);
            });
            $$02.put("palettes", $$5);
        }
        if (($$6 = $$02.getList("entities")).isPresent()) {
            ListTag $$7 = $$6.get().compoundStream().sorted(Comparator.comparing($$0 -> $$0.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_DOUBLE_COMPARATOR))).collect(Collectors.toCollection(ListTag::new));
            $$02.put("entities", $$7);
        }
        ListTag $$8 = $$02.getList("blocks").stream().flatMap(ListTag::compoundStream).sorted(Comparator.comparing($$0 -> $$0.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_INT_COMPARATOR))).peek($$1 -> $$1.putString("state", $$4.getString($$1.getIntOr("state", 0)).orElseThrow())).collect(Collectors.toCollection(ListTag::new));
        $$02.put(SNBT_DATA_TAG, $$8);
        $$02.remove("blocks");
        return $$02;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag $$02) {
        ListTag $$1 = $$02.getListOrEmpty("palette");
        Map $$2 = (Map)$$1.stream().flatMap($$0 -> $$0.asString().stream()).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        Optional<ListTag> $$3 = $$02.getList("palettes");
        if ($$3.isPresent()) {
            $$02.put("palettes", $$3.get().compoundStream().map($$12 -> $$2.keySet().stream().map($$1 -> $$12.getString((String)$$1).orElseThrow()).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            $$02.remove("palette");
        } else {
            $$02.put("palette", $$2.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        Optional<ListTag> $$4 = $$02.getList(SNBT_DATA_TAG);
        if ($$4.isPresent()) {
            Object2IntOpenHashMap $$5 = new Object2IntOpenHashMap();
            $$5.defaultReturnValue(-1);
            for (int $$6 = 0; $$6 < $$1.size(); ++$$6) {
                $$5.put((Object)$$1.getString($$6).orElseThrow(), $$6);
            }
            ListTag $$7 = $$4.get();
            for (int $$8 = 0; $$8 < $$7.size(); ++$$8) {
                CompoundTag $$9 = $$7.getCompound($$8).orElseThrow();
                String $$10 = $$9.getString("state").orElseThrow();
                int $$11 = $$5.getInt((Object)$$10);
                if ($$11 == -1) {
                    throw new IllegalStateException("Entry " + $$10 + " missing from palette");
                }
                $$9.putInt("state", $$11);
            }
            $$02.put("blocks", $$7);
            $$02.remove(SNBT_DATA_TAG);
        }
        return $$02;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag $$0) {
        StringBuilder $$12 = new StringBuilder($$0.getString("Name").orElseThrow());
        $$0.getCompound("Properties").ifPresent($$1 -> {
            String $$2 = $$1.entrySet().stream().sorted(Map.Entry.comparingByKey()).map($$0 -> (String)$$0.getKey() + ":" + ((Tag)$$0.getValue()).asString().orElseThrow()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            $$12.append('{').append($$2).append('}');
        });
        return $$12.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String $$0) {
        String $$6;
        CompoundTag $$1 = new CompoundTag();
        int $$22 = $$0.indexOf(123);
        if ($$22 >= 0) {
            String $$3 = $$0.substring(0, $$22);
            CompoundTag $$4 = new CompoundTag();
            if ($$22 + 2 <= $$0.length()) {
                String $$5 = $$0.substring($$22 + 1, $$0.indexOf(125, $$22));
                COMMA_SPLITTER.split((CharSequence)$$5).forEach($$2 -> {
                    List $$3 = COLON_SPLITTER.splitToList((CharSequence)$$2);
                    if ($$3.size() == 2) {
                        $$4.putString((String)$$3.get(0), (String)$$3.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)$$0);
                    }
                });
                $$1.put("Properties", $$4);
            }
        } else {
            $$6 = $$0;
        }
        $$1.putString("Name", $$6);
        return $$1;
    }

    public static CompoundTag addCurrentDataVersion(CompoundTag $$0) {
        int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion($$0, $$1);
    }

    public static CompoundTag addDataVersion(CompoundTag $$0, int $$1) {
        $$0.putInt("DataVersion", $$1);
        return $$0;
    }

    public static Dynamic<Tag> addCurrentDataVersion(Dynamic<Tag> $$0) {
        int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion($$0, $$1);
    }

    public static Dynamic<Tag> addDataVersion(Dynamic<Tag> $$0, int $$1) {
        return $$0.set("DataVersion", $$0.createInt($$1));
    }

    public static void addCurrentDataVersion(ValueOutput $$0) {
        int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
        NbtUtils.addDataVersion($$0, $$1);
    }

    public static void addDataVersion(ValueOutput $$0, int $$1) {
        $$0.putInt("DataVersion", $$1);
    }

    public static int getDataVersion(CompoundTag $$0) {
        return NbtUtils.getDataVersion($$0, -1);
    }

    public static int getDataVersion(CompoundTag $$0, int $$1) {
        return $$0.getIntOr("DataVersion", $$1);
    }

    public static int getDataVersion(Dynamic<?> $$0, int $$1) {
        return $$0.get("DataVersion").asInt($$1);
    }
}

