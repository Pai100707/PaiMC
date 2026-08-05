/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType$StringType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer
implements ArgumentTypeInfo<StringArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
        $$1.writeEnum((Enum<?>)$$0.type);
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
        StringArgumentType.StringType $$1 = $$0.readEnum(StringArgumentType.StringType.class);
        return new Template($$1);
    }

    @Override
    public void serializeToJson(Template $$0, JsonObject $$1) {
        $$1.addProperty("type", switch ($$0.type) {
            default -> throw new MatchException(null, null);
            case StringArgumentType.StringType.SINGLE_WORD -> "word";
            case StringArgumentType.StringType.QUOTABLE_PHRASE -> "phrase";
            case StringArgumentType.StringType.GREEDY_PHRASE -> "greedy";
        });
    }

    @Override
    public Template unpack(StringArgumentType $$0) {
        return new Template($$0.getType());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<StringArgumentType> {
        final StringArgumentType.StringType type;

        public Template(StringArgumentType.StringType $$1) {
            this.type = $$1;
        }

        @Override
        public StringArgumentType instantiate(CommandBuildContext $$0) {
            return switch (this.type) {
                default -> throw new MatchException(null, null);
                case StringArgumentType.StringType.SINGLE_WORD -> StringArgumentType.word();
                case StringArgumentType.StringType.QUOTABLE_PHRASE -> StringArgumentType.string();
                case StringArgumentType.StringType.GREEDY_PHRASE -> StringArgumentType.greedyString();
            };
        }

        @Override
        public ArgumentTypeInfo<StringArgumentType, ?> type() {
            return StringArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

