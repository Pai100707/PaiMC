/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket
implements Packet<ClientLoginPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundHelloPacket> STREAM_CODEC = Packet.codec(ClientboundHelloPacket::write, ClientboundHelloPacket::new);
    private final String serverId;
    private final byte[] publicKey;
    private final byte[] challenge;
    private final boolean shouldAuthenticate;

    public ClientboundHelloPacket(String $$0, byte[] $$1, byte[] $$2, boolean $$3) {
        this.serverId = $$0;
        this.publicKey = $$1;
        this.challenge = $$2;
        this.shouldAuthenticate = $$3;
    }

    private ClientboundHelloPacket(FriendlyByteBuf $$0) {
        this.serverId = $$0.readUtf(20);
        this.publicKey = $$0.readByteArray();
        this.challenge = $$0.readByteArray();
        this.shouldAuthenticate = $$0.readBoolean();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUtf(this.serverId);
        $$0.writeByteArray(this.publicKey);
        $$0.writeByteArray(this.challenge);
        $$0.writeBoolean(this.shouldAuthenticate);
    }

    @Override
    public PacketType<ClientboundHelloPacket> type() {
        return LoginPacketTypes.CLIENTBOUND_HELLO;
    }

    @Override
    public void handle(ClientLoginPacketListener $$0) {
        $$0.handleHello(this);
    }

    public String getServerId() {
        return this.serverId;
    }

    public PublicKey getPublicKey() throws CryptException {
        return Crypt.byteToPublicKey(this.publicKey);
    }

    public byte[] getChallenge() {
        return this.challenge;
    }

    public boolean shouldAuthenticate() {
        return this.shouldAuthenticate;
    }
}

