/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class FilterMask {
    public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(FilterMask::type, Type::codec);
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), Type.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.filtered")));
    static final MapCodec<FilterMask> PASS_THROUGH_CODEC = MapCodec.unit((Object)PASS_THROUGH);
    static final MapCodec<FilterMask> FULLY_FILTERED_CODEC = MapCodec.unit((Object)FULLY_FILTERED);
    static final MapCodec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask).fieldOf("value");
    private static final char HASH = '#';
    private final BitSet mask;
    private final Type type;

    private FilterMask(BitSet $$0, Type $$1) {
        this.mask = $$0;
        this.type = $$1;
    }

    private FilterMask(BitSet $$0) {
        this.mask = $$0;
        this.type = Type.PARTIALLY_FILTERED;
    }

    public FilterMask(int $$0) {
        this(new BitSet($$0), Type.PARTIALLY_FILTERED);
    }

    private Type type() {
        return this.type;
    }

    private BitSet mask() {
        return this.mask;
    }

    public static FilterMask read(FriendlyByteBuf $$0) {
        Type $$1 = $$0.readEnum(Type.class);
        return switch ($$1.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PASS_THROUGH;
            case 1 -> FULLY_FILTERED;
            case 2 -> new FilterMask($$0.readBitSet(), Type.PARTIALLY_FILTERED);
        };
    }

    public static void write(FriendlyByteBuf $$0, FilterMask $$1) {
        $$0.writeEnum($$1.type);
        if ($$1.type == Type.PARTIALLY_FILTERED) {
            $$0.writeBitSet($$1.mask);
        }
    }

    public void setFiltered(int $$0) {
        this.mask.set($$0);
    }

    public @Nullable String apply(String $$0) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> $$0;
            case 2 -> {
                char[] $$1 = $$0.toCharArray();
                for (int $$2 = 0; $$2 < $$1.length && $$2 < this.mask.length(); ++$$2) {
                    if (!this.mask.get($$2)) continue;
                    $$1[$$2] = 35;
                }
                yield new String($$1);
            }
        };
    }

    public @Nullable Component applyWithFormatting(String $$0) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> Component.literal($$0);
            case 2 -> {
                MutableComponent $$1 = Component.empty();
                int $$2 = 0;
                boolean $$3 = this.mask.get(0);
                while (true) {
                    int $$4 = $$3 ? this.mask.nextClearBit($$2) : this.mask.nextSetBit($$2);
                    int v1 = $$4 = $$4 < 0 ? $$0.length() : $$4;
                    if ($$4 == $$2) break;
                    if ($$3) {
                        $$1.append(Component.literal(StringUtils.repeat((char)'#', (int)($$4 - $$2))).withStyle(FILTERED_STYLE));
                    } else {
                        $$1.append($$0.substring($$2, $$4));
                    }
                    $$3 = !$$3;
                    $$2 = $$4;
                }
                yield $$1;
            }
        };
    }

    public boolean isEmpty() {
        return this.type == Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == Type.FULLY_FILTERED;
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        FilterMask $$1 = (FilterMask)$$0;
        return this.mask.equals($$1.mask) && this.type == $$1.type;
    }

    public int hashCode() {
        int $$0 = this.mask.hashCode();
        $$0 = 31 * $$0 + this.type.hashCode();
        return $$0;
    }

    static enum Type implements StringRepresentable
    {
        PASS_THROUGH("pass_through", () -> PASS_THROUGH_CODEC),
        FULLY_FILTERED("fully_filtered", () -> FULLY_FILTERED_CODEC),
        PARTIALLY_FILTERED("partially_filtered", () -> PARTIALLY_FILTERED_CODEC);

        private final String serializedName;
        private final Supplier<MapCodec<FilterMask>> codec;

        private Type(String $$0, Supplier<MapCodec<FilterMask>> $$1) {
            this.serializedName = $$0;
            this.codec = $$1;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        private MapCodec<FilterMask> codec() {
            return this.codec.get();
        }
    }
}

