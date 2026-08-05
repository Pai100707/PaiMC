/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<Parameter> parameters, Style style) {
    public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey), (App)Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters), (App)Style.Serializer.CODEC.optionalFieldOf("style", (Object)Style.EMPTY).forGetter(ChatTypeDecoration::style)).apply((Applicative)$$0, ChatTypeDecoration::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatTypeDecoration> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ChatTypeDecoration::translationKey, Parameter.STREAM_CODEC.apply(ByteBufCodecs.list()), ChatTypeDecoration::parameters, Style.Serializer.TRUSTED_STREAM_CODEC, ChatTypeDecoration::style, ChatTypeDecoration::new);

    public static ChatTypeDecoration withSender(String $$0) {
        return new ChatTypeDecoration($$0, List.of(Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public static ChatTypeDecoration incomingDirectMessage(String $$0) {
        Style $$1 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration($$0, List.of(Parameter.SENDER, Parameter.CONTENT), $$1);
    }

    public static ChatTypeDecoration outgoingDirectMessage(String $$0) {
        Style $$1 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration($$0, List.of(Parameter.TARGET, Parameter.CONTENT), $$1);
    }

    public static ChatTypeDecoration teamMessage(String $$0) {
        return new ChatTypeDecoration($$0, List.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public Component decorate(Component $$0, ChatType.Bound $$1) {
        Object[] $$2 = this.resolveParameters($$0, $$1);
        return Component.translatable(this.translationKey, $$2).withStyle(this.style);
    }

    private Component[] resolveParameters(Component $$0, ChatType.Bound $$1) {
        Component[] $$2 = new Component[this.parameters.size()];
        for (int $$3 = 0; $$3 < $$2.length; ++$$3) {
            Parameter $$4 = this.parameters.get($$3);
            $$2[$$3] = $$4.select($$0, $$1);
        }
        return $$2;
    }

    public static enum Parameter implements StringRepresentable
    {
        SENDER(0, "sender", ($$0, $$1) -> $$1.name()),
        TARGET(1, "target", ($$0, $$1) -> $$1.targetName().orElse(CommonComponents.EMPTY)),
        CONTENT(2, "content", ($$0, $$1) -> $$0);

        private static final IntFunction<Parameter> BY_ID;
        public static final Codec<Parameter> CODEC;
        public static final StreamCodec<ByteBuf, Parameter> STREAM_CODEC;
        private final int id;
        private final String name;
        private final Selector selector;

        private Parameter(int $$0, String $$1, Selector $$2) {
            this.id = $$0;
            this.name = $$1;
            this.selector = $$2;
        }

        public Component select(Component $$0, ChatType.Bound $$1) {
            return this.selector.select($$0, $$1);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            BY_ID = ByIdMap.continuous($$0 -> $$0.id, Parameter.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            CODEC = StringRepresentable.fromEnum(Parameter::values);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
        }

        public static interface Selector {
            public Component select(Component var1, ChatType.Bound var2);
        }
    }
}

