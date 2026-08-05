/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 */
package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class SingletonArgumentInfo<A extends ArgumentType<?>>
implements ArgumentTypeInfo<A, Template> {
    private final Template template;

    private SingletonArgumentInfo(Function<CommandBuildContext, A> $$0) {
        this.template = new Template($$0);
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextFree(Supplier<T> $$0) {
        return new SingletonArgumentInfo<ArgumentType>($$1 -> (ArgumentType)$$0.get());
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextAware(Function<CommandBuildContext, T> $$0) {
        return new SingletonArgumentInfo<T>($$0);
    }

    @Override
    public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
    }

    @Override
    public void serializeToJson(Template $$0, JsonObject $$1) {
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
        return this.template;
    }

    @Override
    public Template unpack(A $$0) {
        return this.template;
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template unpack(ArgumentType argumentType) {
        return this.unpack(argumentType);
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<A> {
        private final Function<CommandBuildContext, A> constructor;

        public Template(Function<CommandBuildContext, A> $$1) {
            this.constructor = $$1;
        }

        @Override
        public A instantiate(CommandBuildContext $$0) {
            return (ArgumentType)this.constructor.apply($$0);
        }

        @Override
        public ArgumentTypeInfo<A, ?> type() {
            return SingletonArgumentInfo.this;
        }
    }
}

