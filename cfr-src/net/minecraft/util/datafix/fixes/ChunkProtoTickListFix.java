/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
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
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public class ChunkProtoTickListFix
extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of((Object)"minecraft:bubble_column", (Object)"minecraft:kelp", (Object)"minecraft:kelp_plant", (Object)"minecraft:seagrass", (Object)"minecraft:tall_seagrass");

    public ChunkProtoTickListFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder $$1 = $$0.findField("Level");
        OpticFinder $$2 = $$1.type().findField("Sections");
        OpticFinder $$3 = ((List.ListType)$$2.type()).getElement().finder();
        OpticFinder $$4 = $$3.type().findField("block_states");
        OpticFinder $$5 = $$3.type().findField("biomes");
        OpticFinder $$6 = $$4.type().findField("palette");
        OpticFinder $$72 = $$1.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped("ChunkProtoTickListFix", $$0, $$7 -> $$7.updateTyped($$1, $$6 -> {
            $$6 = $$6.update(DSL.remainderFinder(), $$0 -> (Dynamic)DataFixUtils.orElse($$0.get("LiquidTicks").result().map($$1 -> $$0.set("fluid_ticks", $$1).remove("LiquidTicks")), (Object)$$0));
            Object $$7 = (Dynamic)$$6.get(DSL.remainderFinder());
            MutableInt $$8 = new MutableInt();
            Int2ObjectArrayMap $$9 = new Int2ObjectArrayMap();
            $$6.getOptionalTyped($$2).ifPresent(arg_0 -> ChunkProtoTickListFix.lambda$makeRule$7($$3, $$5, $$8, $$4, (Int2ObjectMap)$$9, $$6, arg_0));
            byte $$10 = $$8.byteValue();
            $$6 = $$6.update(DSL.remainderFinder(), $$12 -> $$12.update("yPos", $$1 -> $$1.createByte($$10)));
            if ($$6.getOptionalTyped($$72).isPresent() || $$7.get("fluid_ticks").result().isPresent()) {
                return $$6;
            }
            int $$11 = $$7.get("xPos").asInt(0);
            int $$122 = $$7.get("zPos").asInt(0);
            Dynamic<?> $$132 = this.makeTickList((Dynamic<?>)$$7, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)$$9, $$10, $$11, $$122, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
            Dynamic<?> $$14 = this.makeTickList((Dynamic<?>)$$7, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)$$9, $$10, $$11, $$122, "ToBeTicked", ChunkProtoTickListFix::getBlock);
            Optional $$15 = $$72.type().readTyped($$14).result();
            if ($$15.isPresent()) {
                $$6 = $$6.set($$72, (Typed)((Pair)$$15.get()).getFirst());
            }
            return $$6.update(DSL.remainderFinder(), $$1 -> $$1.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", $$132));
        }));
    }

    private Dynamic<?> makeTickList(Dynamic<?> $$02, Int2ObjectMap<Supplier<PoorMansPalettedContainer>> $$1, byte $$2, int $$3, int $$4, String $$5, Function<Dynamic<?>, String> $$6) {
        Stream<Object> $$7 = Stream.empty();
        List $$8 = $$02.get($$5).asList(Function.identity());
        for (int $$9 = 0; $$9 < $$8.size(); ++$$9) {
            int $$10 = $$9 + $$2;
            Supplier $$11 = (Supplier)$$1.get($$10);
            Stream<Dynamic> $$12 = ((Dynamic)$$8.get($$9)).asStream().mapToInt($$0 -> $$0.asShort((short)-1)).filter($$0 -> $$0 > 0).mapToObj(arg_0 -> this.lambda$makeTickList$15($$02, (Supplier)$$11, $$3, $$10, $$4, $$6, arg_0));
            $$7 = Stream.concat($$7, $$12);
        }
        return $$02.createList($$7);
    }

    private static String getBlock(@Nullable Dynamic<?> $$0) {
        return $$0 != null ? $$0.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> $$0) {
        if ($$0 == null) {
            return "minecraft:empty";
        }
        String $$1 = $$0.get("Name").asString("");
        if ("minecraft:water".equals($$1)) {
            return $$0.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
        }
        if ("minecraft:lava".equals($$1)) {
            return $$0.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
        }
        if (ALWAYS_WATERLOGGED.contains((Object)$$1) || $$0.get("Properties").get("waterlogged").asBoolean(false)) {
            return "minecraft:water";
        }
        return "minecraft:empty";
    }

    private Dynamic<?> createTick(Dynamic<?> $$0, @Nullable Supplier<PoorMansPalettedContainer> $$1, int $$2, int $$3, int $$4, int $$5, Function<Dynamic<?>, String> $$6) {
        int $$7 = $$5 & 0xF;
        int $$8 = $$5 >>> 4 & 0xF;
        int $$9 = $$5 >>> 8 & 0xF;
        String $$10 = $$6.apply($$1 != null ? $$1.get().get($$7, $$8, $$9) : null);
        return $$0.createMap((Map)ImmutableMap.builder().put((Object)$$0.createString("i"), (Object)$$0.createString($$10)).put((Object)$$0.createString("x"), (Object)$$0.createInt($$2 * 16 + $$7)).put((Object)$$0.createString("y"), (Object)$$0.createInt($$3 * 16 + $$8)).put((Object)$$0.createString("z"), (Object)$$0.createInt($$4 * 16 + $$9)).put((Object)$$0.createString("t"), (Object)$$0.createInt(0)).put((Object)$$0.createString("p"), (Object)$$0.createInt(0)).build());
    }

    private /* synthetic */ Dynamic lambda$makeTickList$15(Dynamic $$0, Supplier $$1, int $$2, int $$3, int $$4, Function $$5, int $$6) {
        return this.createTick($$0, $$1, $$2, $$3, $$4, $$6, $$5);
    }

    private static /* synthetic */ void lambda$makeRule$7(OpticFinder $$0, OpticFinder $$1, MutableInt $$2, OpticFinder $$3, Int2ObjectMap $$4, OpticFinder $$52, Typed $$6) {
        $$6.getAllTyped($$0).forEach($$5 -> {
            Dynamic $$6 = (Dynamic)$$5.get(DSL.remainderFinder());
            int $$7 = $$6.get("Y").asInt(Integer.MAX_VALUE);
            if ($$7 == Integer.MAX_VALUE) {
                return;
            }
            if ($$5.getOptionalTyped($$1).isPresent()) {
                $$2.setValue(Math.min($$7, $$2.intValue()));
            }
            $$5.getOptionalTyped($$3).ifPresent($$3 -> $$4.put($$7, (Object)Suppliers.memoize(() -> {
                OpticFinder $$52 = $$3.getOptionalTyped($$52).map($$02 -> $$02.write().result().map($$0 -> $$0.asList(Function.identity())).orElse(Collections.emptyList())).orElse(Collections.emptyList());
                Object $$3 = ((Dynamic)$$3.get(DSL.remainderFinder())).get("data").asLongStream().toArray();
                return new PoorMansPalettedContainer((List<? extends Dynamic<?>>)$$52, (long[])$$3);
            })));
        });
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> $$0, long[] $$1) {
            this.palette = $$0;
            this.data = $$1;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2($$0.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        public @Nullable Dynamic<?> get(int $$0, int $$1, int $$2) {
            int $$3 = this.palette.size();
            if ($$3 < 1) {
                return null;
            }
            if ($$3 == 1) {
                return this.palette.getFirst();
            }
            int $$4 = this.getIndex($$0, $$1, $$2);
            int $$5 = $$4 / this.valuesPerLong;
            if ($$5 < 0 || $$5 >= this.data.length) {
                return null;
            }
            long $$6 = this.data[$$5];
            int $$7 = ($$4 - $$5 * this.valuesPerLong) * this.bits;
            int $$8 = (int)($$6 >> $$7 & this.mask);
            if ($$8 < 0 || $$8 >= $$3) {
                return null;
            }
            return this.palette.get($$8);
        }

        private int getIndex(int $$0, int $$1, int $$2) {
            return ($$1 << 4 | $$2) << 4 | $$0;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}

