/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

public record CommonPlayerSpawnInfo(Holder<DimensionType> dimensionType, ResourceKey<Level> dimension, long seed, GameType gameType, @Nullable GameType previousGameType, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown, int seaLevel) {
    public CommonPlayerSpawnInfo(RegistryFriendlyByteBuf $$0) {
        this((Holder)DimensionType.STREAM_CODEC.decode($$0), $$0.readResourceKey(Registries.DIMENSION), $$0.readLong(), GameType.byId($$0.readByte()), GameType.byNullableId($$0.readByte()), $$0.readBoolean(), $$0.readBoolean(), $$0.readOptional(FriendlyByteBuf::readGlobalPos), $$0.readVarInt(), $$0.readVarInt());
    }

    public void write(RegistryFriendlyByteBuf $$0) {
        DimensionType.STREAM_CODEC.encode($$0, this.dimensionType);
        $$0.writeResourceKey(this.dimension);
        $$0.writeLong(this.seed);
        $$0.writeByte(this.gameType.getId());
        $$0.writeByte(GameType.getNullableId(this.previousGameType));
        $$0.writeBoolean(this.isDebug);
        $$0.writeBoolean(this.isFlat);
        $$0.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
        $$0.writeVarInt(this.portalCooldown);
        $$0.writeVarInt(this.seaLevel);
    }
}

