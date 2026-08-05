/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.server.dialog;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum DialogAction implements StringRepresentable
{
    CLOSE(0, "close"),
    NONE(1, "none"),
    WAIT_FOR_RESPONSE(2, "wait_for_response");

    public static final IntFunction<DialogAction> BY_ID;
    public static final StringRepresentable.EnumCodec<DialogAction> CODEC;
    public static final StreamCodec<ByteBuf, DialogAction> STREAM_CODEC;
    private final int id;
    private final String name;

    private DialogAction(int $$0, String $$1) {
        this.id = $$0;
        this.name = $$1;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean willUnpause() {
        return this == CLOSE || this == WAIT_FOR_RESPONSE;
    }

    static {
        BY_ID = ByIdMap.continuous($$0 -> $$0.id, DialogAction.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        CODEC = StringRepresentable.fromEnum(DialogAction::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
    }
}

