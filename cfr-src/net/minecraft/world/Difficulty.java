/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum Difficulty implements StringRepresentable
{
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    public static final StringRepresentable.EnumCodec<Difficulty> CODEC;
    private static final IntFunction<Difficulty> BY_ID;
    public static final StreamCodec<ByteBuf, Difficulty> STREAM_CODEC;
    private final int id;
    private final String key;

    private Difficulty(int $$0, String $$1) {
        this.id = $$0;
        this.key = $$1;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return Component.translatable("options.difficulty." + this.key);
    }

    public Component getInfo() {
        return Component.translatable("options.difficulty." + this.key + ".info");
    }

    @Deprecated
    public static Difficulty byId(int $$0) {
        return BY_ID.apply($$0);
    }

    public static @Nullable Difficulty byName(String $$0) {
        return CODEC.byName($$0);
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Difficulty::values);
        BY_ID = ByIdMap.continuous(Difficulty::getId, Difficulty.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Difficulty::getId);
    }
}

