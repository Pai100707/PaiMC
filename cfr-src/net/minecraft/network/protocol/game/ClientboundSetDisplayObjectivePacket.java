/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.jspecify.annotations.Nullable;

public class ClientboundSetDisplayObjectivePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetDisplayObjectivePacket> STREAM_CODEC = Packet.codec(ClientboundSetDisplayObjectivePacket::write, ClientboundSetDisplayObjectivePacket::new);
    private final DisplaySlot slot;
    private final String objectiveName;

    public ClientboundSetDisplayObjectivePacket(DisplaySlot $$0, @Nullable Objective $$1) {
        this.slot = $$0;
        this.objectiveName = $$1 == null ? "" : $$1.getName();
    }

    private ClientboundSetDisplayObjectivePacket(FriendlyByteBuf $$0) {
        this.slot = $$0.readById(DisplaySlot.BY_ID);
        this.objectiveName = $$0.readUtf();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeById(DisplaySlot::id, this.slot);
        $$0.writeUtf(this.objectiveName);
    }

    @Override
    public PacketType<ClientboundSetDisplayObjectivePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetDisplayObjective(this);
    }

    public DisplaySlot getSlot() {
        return this.slot;
    }

    public @Nullable String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}

