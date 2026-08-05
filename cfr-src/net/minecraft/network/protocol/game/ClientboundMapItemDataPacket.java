/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;

public record ClientboundMapItemDataPacket(MapId mapId, byte scale, boolean locked, Optional<List<MapDecoration>> decorations, Optional<MapItemSavedData.MapPatch> colorPatch) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMapItemDataPacket> STREAM_CODEC = StreamCodec.composite(MapId.STREAM_CODEC, ClientboundMapItemDataPacket::mapId, ByteBufCodecs.BYTE, ClientboundMapItemDataPacket::scale, ByteBufCodecs.BOOL, ClientboundMapItemDataPacket::locked, MapDecoration.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional), ClientboundMapItemDataPacket::decorations, MapItemSavedData.MapPatch.STREAM_CODEC, ClientboundMapItemDataPacket::colorPatch, ClientboundMapItemDataPacket::new);

    public ClientboundMapItemDataPacket(MapId $$0, byte $$1, boolean $$2, @Nullable Collection<MapDecoration> $$3, @Nullable MapItemSavedData.MapPatch $$4) {
        this($$0, $$1, $$2, $$3 != null ? Optional.of(List.copyOf($$3)) : Optional.empty(), Optional.ofNullable($$4));
    }

    @Override
    public PacketType<ClientboundMapItemDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_MAP_ITEM_DATA;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleMapItemData(this);
    }

    public void applyToMap(MapItemSavedData $$0) {
        this.decorations.ifPresent($$0::addClientSideDecorations);
        this.colorPatch.ifPresent($$1 -> $$1.applyToMap($$0));
    }
}

