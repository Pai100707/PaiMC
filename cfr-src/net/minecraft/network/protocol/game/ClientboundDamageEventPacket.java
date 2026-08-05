/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, Holder<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDamageEventPacket> STREAM_CODEC = Packet.codec(ClientboundDamageEventPacket::write, ClientboundDamageEventPacket::new);

    public ClientboundDamageEventPacket(Entity $$0, DamageSource $$1) {
        this($$0.getId(), $$1.typeHolder(), $$1.getEntity() != null ? $$1.getEntity().getId() : -1, $$1.getDirectEntity() != null ? $$1.getDirectEntity().getId() : -1, Optional.ofNullable($$1.sourcePositionRaw()));
    }

    private ClientboundDamageEventPacket(RegistryFriendlyByteBuf $$02) {
        this($$02.readVarInt(), (Holder)DamageType.STREAM_CODEC.decode($$02), ClientboundDamageEventPacket.readOptionalEntityId($$02), ClientboundDamageEventPacket.readOptionalEntityId($$02), $$02.readOptional($$0 -> new Vec3($$0.readDouble(), $$0.readDouble(), $$0.readDouble())));
    }

    private static void writeOptionalEntityId(FriendlyByteBuf $$0, int $$1) {
        $$0.writeVarInt($$1 + 1);
    }

    private static int readOptionalEntityId(FriendlyByteBuf $$0) {
        return $$0.readVarInt() - 1;
    }

    private void write(RegistryFriendlyByteBuf $$02) {
        $$02.writeVarInt(this.entityId);
        DamageType.STREAM_CODEC.encode($$02, this.sourceType);
        ClientboundDamageEventPacket.writeOptionalEntityId($$02, this.sourceCauseId);
        ClientboundDamageEventPacket.writeOptionalEntityId($$02, this.sourceDirectId);
        $$02.writeOptional(this.sourcePosition, ($$0, $$1) -> {
            $$0.writeDouble($$1.x());
            $$0.writeDouble($$1.y());
            $$0.writeDouble($$1.z());
        });
    }

    @Override
    public PacketType<ClientboundDamageEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleDamageEvent(this);
    }

    public DamageSource getSource(Level $$0) {
        if (this.sourcePosition.isPresent()) {
            return new DamageSource(this.sourceType, this.sourcePosition.get());
        }
        Entity $$1 = $$0.getEntity(this.sourceCauseId);
        Entity $$2 = $$0.getEntity(this.sourceDirectId);
        return new DamageSource(this.sourceType, $$2, $$1);
    }
}

