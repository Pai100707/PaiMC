/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetObjectivePacket> STREAM_CODEC = Packet.codec(ClientboundSetObjectivePacket::write, ClientboundSetObjectivePacket::new);
    public static final int METHOD_ADD = 0;
    public static final int METHOD_REMOVE = 1;
    public static final int METHOD_CHANGE = 2;
    private final String objectiveName;
    private final Component displayName;
    private final ObjectiveCriteria.RenderType renderType;
    private final Optional<NumberFormat> numberFormat;
    private final int method;

    public ClientboundSetObjectivePacket(Objective $$0, int $$1) {
        this.objectiveName = $$0.getName();
        this.displayName = $$0.getDisplayName();
        this.renderType = $$0.getRenderType();
        this.numberFormat = Optional.ofNullable($$0.numberFormat());
        this.method = $$1;
    }

    private ClientboundSetObjectivePacket(RegistryFriendlyByteBuf $$0) {
        this.objectiveName = $$0.readUtf();
        this.method = $$0.readByte();
        if (this.method == 0 || this.method == 2) {
            this.displayName = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
            this.renderType = $$0.readEnum(ObjectiveCriteria.RenderType.class);
            this.numberFormat = (Optional)NumberFormatTypes.OPTIONAL_STREAM_CODEC.decode($$0);
        } else {
            this.displayName = CommonComponents.EMPTY;
            this.renderType = ObjectiveCriteria.RenderType.INTEGER;
            this.numberFormat = Optional.empty();
        }
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeUtf(this.objectiveName);
        $$0.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.displayName);
            $$0.writeEnum(this.renderType);
            NumberFormatTypes.OPTIONAL_STREAM_CODEC.encode($$0, this.numberFormat);
        }
    }

    @Override
    public PacketType<ClientboundSetObjectivePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleAddObjective(this);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public int getMethod() {
        return this.method;
    }

    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }

    public Optional<NumberFormat> getNumberFormat() {
        return this.numberFormat;
    }
}

