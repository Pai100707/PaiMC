/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public static final Codec<ChatType> DIRECT_CODEC = RecordCodecBuilder.create((T $$0) -> $$0.group((App)ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), (App)ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)).apply((Applicative)$$0, ChatType::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatType> DIRECT_STREAM_CODEC = StreamCodec.composite(ChatTypeDecoration.STREAM_CODEC, ChatType::chat, ChatTypeDecoration.STREAM_CODEC, ChatType::narration, ChatType::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChatType>> STREAM_CODEC = ByteBufCodecs.holder(Registries.CHAT_TYPE, DIRECT_STREAM_CODEC);
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = ChatType.create("chat");
    public static final ResourceKey<ChatType> SAY_COMMAND = ChatType.create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = ChatType.create("msg_command_incoming");
    public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = ChatType.create("msg_command_outgoing");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = ChatType.create("team_msg_command_incoming");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = ChatType.create("team_msg_command_outgoing");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = ChatType.create("emote_command");

    private static ResourceKey<ChatType> create(String $$0) {
        return ResourceKey.create(Registries.CHAT_TYPE, Identifier.withDefaultNamespace($$0));
    }

    public static void bootstrap(BootstrapContext<ChatType> $$0) {
        $$0.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        $$0.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
    }

    public static Bound bind(ResourceKey<ChatType> $$0, Entity $$1) {
        return ChatType.bind($$0, $$1.level().registryAccess(), $$1.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> $$0, CommandSourceStack $$1) {
        return ChatType.bind($$0, $$1.registryAccess(), $$1.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> $$0, RegistryAccess $$1, Component $$2) {
        HolderLookup.RegistryLookup $$3 = $$1.lookupOrThrow(Registries.CHAT_TYPE);
        return new Bound($$3.getOrThrow($$0), $$2);
    }

    public record Bound(Holder<ChatType> chatType, Component name, Optional<Component> targetName) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Bound> STREAM_CODEC = StreamCodec.composite(STREAM_CODEC, Bound::chatType, ComponentSerialization.TRUSTED_STREAM_CODEC, Bound::name, ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, Bound::targetName, Bound::new);

        Bound(Holder<ChatType> $$0, Component $$1) {
            this($$0, $$1, Optional.empty());
        }

        public Component decorate(Component $$0) {
            return this.chatType.value().chat().decorate($$0, this);
        }

        public Component decorateNarration(Component $$0) {
            return this.chatType.value().narration().decorate($$0, this);
        }

        public Bound withTargetName(Component $$0) {
            return new Bound(this.chatType, this.name, Optional.of($$0));
        }
    }
}

