/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class ServerboundSetCommandBlockPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetCommandBlockPacket> STREAM_CODEC = Packet.codec(ServerboundSetCommandBlockPacket::write, ServerboundSetCommandBlockPacket::new);
    private static final int FLAG_TRACK_OUTPUT = 1;
    private static final int FLAG_CONDITIONAL = 2;
    private static final int FLAG_AUTOMATIC = 4;
    private final BlockPos pos;
    private final String command;
    private final boolean trackOutput;
    private final boolean conditional;
    private final boolean automatic;
    private final CommandBlockEntity.Mode mode;

    public ServerboundSetCommandBlockPacket(BlockPos $$0, String $$1, CommandBlockEntity.Mode $$2, boolean $$3, boolean $$4, boolean $$5) {
        this.pos = $$0;
        this.command = $$1;
        this.trackOutput = $$3;
        this.conditional = $$4;
        this.automatic = $$5;
        this.mode = $$2;
    }

    private ServerboundSetCommandBlockPacket(FriendlyByteBuf $$0) {
        this.pos = $$0.readBlockPos();
        this.command = $$0.readUtf();
        this.mode = $$0.readEnum(CommandBlockEntity.Mode.class);
        byte $$1 = $$0.readByte();
        this.trackOutput = ($$1 & 1) != 0;
        this.conditional = ($$1 & 2) != 0;
        this.automatic = ($$1 & 4) != 0;
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBlockPos(this.pos);
        $$0.writeUtf(this.command);
        $$0.writeEnum(this.mode);
        int $$1 = 0;
        if (this.trackOutput) {
            $$1 |= 1;
        }
        if (this.conditional) {
            $$1 |= 2;
        }
        if (this.automatic) {
            $$1 |= 4;
        }
        $$0.writeByte($$1);
    }

    @Override
    public PacketType<ServerboundSetCommandBlockPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_COMMAND_BLOCK;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleSetCommandBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public boolean isConditional() {
        return this.conditional;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public CommandBlockEntity.Mode getMode() {
        return this.mode;
    }
}

