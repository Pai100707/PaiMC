/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo
implements ArgumentTypeInfo<IntegerArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
        boolean $$2 = $$0.min != Integer.MIN_VALUE;
        boolean $$3 = $$0.max != Integer.MAX_VALUE;
        $$1.writeByte(ArgumentUtils.createNumberFlags($$2, $$3));
        if ($$2) {
            $$1.writeInt($$0.min);
        }
        if ($$3) {
            $$1.writeInt($$0.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
        byte $$1 = $$0.readByte();
        int $$2 = ArgumentUtils.numberHasMin($$1) ? $$0.readInt() : Integer.MIN_VALUE;
        int $$3 = ArgumentUtils.numberHasMax($$1) ? $$0.readInt() : Integer.MAX_VALUE;
        return new Template($$2, $$3);
    }

    @Override
    public void serializeToJson(Template $$0, JsonObject $$1) {
        if ($$0.min != Integer.MIN_VALUE) {
            $$1.addProperty("min", (Number)$$0.min);
        }
        if ($$0.max != Integer.MAX_VALUE) {
            $$1.addProperty("max", (Number)$$0.max);
        }
    }

    @Override
    public Template unpack(IntegerArgumentType $$0) {
        return new Template($$0.getMinimum(), $$0.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<IntegerArgumentType> {
        final int min;
        final int max;

        Template(int $$1, int $$2) {
            this.min = $$1;
            this.max = $$2;
        }

        @Override
        public IntegerArgumentType instantiate(CommandBuildContext $$0) {
            return IntegerArgumentType.integer((int)this.min, (int)this.max);
        }

        @Override
        public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
            return IntegerArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

