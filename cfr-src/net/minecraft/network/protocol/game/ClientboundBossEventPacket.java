/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBossEventPacket> STREAM_CODEC = Packet.codec(ClientboundBossEventPacket::write, ClientboundBossEventPacket::new);
    private static final int FLAG_DARKEN = 1;
    private static final int FLAG_MUSIC = 2;
    private static final int FLAG_FOG = 4;
    private final UUID id;
    private final Operation operation;
    static final Operation REMOVE_OPERATION = new Operation(){

        @Override
        public OperationType getType() {
            return OperationType.REMOVE;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.remove($$0);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
        }
    };

    private ClientboundBossEventPacket(UUID $$0, Operation $$1) {
        this.id = $$0;
        this.operation = $$1;
    }

    private ClientboundBossEventPacket(RegistryFriendlyByteBuf $$0) {
        this.id = $$0.readUUID();
        OperationType $$1 = $$0.readEnum(OperationType.class);
        this.operation = $$1.reader.decode($$0);
    }

    public static ClientboundBossEventPacket createAddPacket(BossEvent $$0) {
        return new ClientboundBossEventPacket($$0.getId(), new AddOperation($$0));
    }

    public static ClientboundBossEventPacket createRemovePacket(UUID $$0) {
        return new ClientboundBossEventPacket($$0, REMOVE_OPERATION);
    }

    public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent $$0) {
        return new ClientboundBossEventPacket($$0.getId(), new UpdateProgressOperation($$0.getProgress()));
    }

    public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent $$0) {
        return new ClientboundBossEventPacket($$0.getId(), new UpdateNameOperation($$0.getName()));
    }

    public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent $$0) {
        return new ClientboundBossEventPacket($$0.getId(), new UpdateStyleOperation($$0.getColor(), $$0.getOverlay()));
    }

    public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent $$0) {
        return new ClientboundBossEventPacket($$0.getId(), new UpdatePropertiesOperation($$0.shouldDarkenScreen(), $$0.shouldPlayBossMusic(), $$0.shouldCreateWorldFog()));
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeUUID(this.id);
        $$0.writeEnum(this.operation.getType());
        this.operation.write($$0);
    }

    static int encodeProperties(boolean $$0, boolean $$1, boolean $$2) {
        int $$3 = 0;
        if ($$0) {
            $$3 |= 1;
        }
        if ($$1) {
            $$3 |= 2;
        }
        if ($$2) {
            $$3 |= 4;
        }
        return $$3;
    }

    @Override
    public PacketType<ClientboundBossEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BOSS_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleBossUpdate(this);
    }

    public void dispatch(Handler $$0) {
        this.operation.dispatch(this.id, $$0);
    }

    static interface Operation {
        public OperationType getType();

        public void dispatch(UUID var1, Handler var2);

        public void write(RegistryFriendlyByteBuf var1);
    }

    static enum OperationType {
        ADD(AddOperation::new),
        REMOVE($$0 -> REMOVE_OPERATION),
        UPDATE_PROGRESS(UpdateProgressOperation::new),
        UPDATE_NAME(UpdateNameOperation::new),
        UPDATE_STYLE(UpdateStyleOperation::new),
        UPDATE_PROPERTIES(UpdatePropertiesOperation::new);

        final StreamDecoder<RegistryFriendlyByteBuf, Operation> reader;

        private OperationType(StreamDecoder<RegistryFriendlyByteBuf, Operation> $$0) {
            this.reader = $$0;
        }
    }

    static class AddOperation
    implements Operation {
        private final Component name;
        private final float progress;
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        AddOperation(BossEvent $$0) {
            this.name = $$0.getName();
            this.progress = $$0.getProgress();
            this.color = $$0.getColor();
            this.overlay = $$0.getOverlay();
            this.darkenScreen = $$0.shouldDarkenScreen();
            this.playMusic = $$0.shouldPlayBossMusic();
            this.createWorldFog = $$0.shouldCreateWorldFog();
        }

        private AddOperation(RegistryFriendlyByteBuf $$0) {
            this.name = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
            this.progress = $$0.readFloat();
            this.color = $$0.readEnum(BossEvent.BossBarColor.class);
            this.overlay = $$0.readEnum(BossEvent.BossBarOverlay.class);
            short $$1 = $$0.readUnsignedByte();
            this.darkenScreen = ($$1 & 1) > 0;
            this.playMusic = ($$1 & 2) > 0;
            this.createWorldFog = ($$1 & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.ADD;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.add($$0, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.name);
            $$0.writeFloat(this.progress);
            $$0.writeEnum(this.color);
            $$0.writeEnum(this.overlay);
            $$0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    record UpdateProgressOperation(float progress) implements Operation
    {
        private UpdateProgressOperation(RegistryFriendlyByteBuf $$0) {
            this($$0.readFloat());
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROGRESS;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.updateProgress($$0, this.progress);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
            $$0.writeFloat(this.progress);
        }
    }

    record UpdateNameOperation(Component name) implements Operation
    {
        private UpdateNameOperation(RegistryFriendlyByteBuf $$0) {
            this((Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0));
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_NAME;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.updateName($$0, this.name);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.name);
        }
    }

    static class UpdateStyleOperation
    implements Operation {
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;

        UpdateStyleOperation(BossEvent.BossBarColor $$0, BossEvent.BossBarOverlay $$1) {
            this.color = $$0;
            this.overlay = $$1;
        }

        private UpdateStyleOperation(RegistryFriendlyByteBuf $$0) {
            this.color = $$0.readEnum(BossEvent.BossBarColor.class);
            this.overlay = $$0.readEnum(BossEvent.BossBarOverlay.class);
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_STYLE;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.updateStyle($$0, this.color, this.overlay);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
            $$0.writeEnum(this.color);
            $$0.writeEnum(this.overlay);
        }
    }

    static class UpdatePropertiesOperation
    implements Operation {
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        UpdatePropertiesOperation(boolean $$0, boolean $$1, boolean $$2) {
            this.darkenScreen = $$0;
            this.playMusic = $$1;
            this.createWorldFog = $$2;
        }

        private UpdatePropertiesOperation(RegistryFriendlyByteBuf $$0) {
            short $$1 = $$0.readUnsignedByte();
            this.darkenScreen = ($$1 & 1) > 0;
            this.playMusic = ($$1 & 2) > 0;
            this.createWorldFog = ($$1 & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROPERTIES;
        }

        @Override
        public void dispatch(UUID $$0, Handler $$1) {
            $$1.updateProperties($$0, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf $$0) {
            $$0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    public static interface Handler {
        default public void add(UUID $$0, Component $$1, float $$2, BossEvent.BossBarColor $$3, BossEvent.BossBarOverlay $$4, boolean $$5, boolean $$6, boolean $$7) {
        }

        default public void remove(UUID $$0) {
        }

        default public void updateProgress(UUID $$0, float $$1) {
        }

        default public void updateName(UUID $$0, Component $$1) {
        }

        default public void updateStyle(UUID $$0, BossEvent.BossBarColor $$1, BossEvent.BossBarOverlay $$2) {
        }

        default public void updateProperties(UUID $$0, boolean $$1, boolean $$2, boolean $$3) {
        }
    }
}

