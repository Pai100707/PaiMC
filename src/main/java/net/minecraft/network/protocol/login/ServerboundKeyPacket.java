package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundKeyPacket> STREAM_CODEC = Packet.codec(
      ServerboundKeyPacket::write, ServerboundKeyPacket::new
   );
   private final byte[] keybytes;
   private final byte[] encryptedChallenge;

   public ServerboundKeyPacket(SecretKey $$0, PublicKey $$1, byte[] $$2) throws CryptException {
      this.keybytes = Crypt.encryptUsingKey($$1, $$0.getEncoded());
      this.encryptedChallenge = Crypt.encryptUsingKey($$1, $$2);
   }

   private ServerboundKeyPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.keybytes = $$0.readByteArray();
      this.encryptedChallenge = $$0.readByteArray();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeByteArray(this.keybytes);
      $$0.writeByteArray(this.encryptedChallenge);
   }

   @Override
   public PacketType<ServerboundKeyPacket> type() {
      return LoginPacketTypes.SERVERBOUND_KEY;
   }

   public void handle(ServerLoginPacketListener $$0) {
      $$0.handleKey(this);
   }

   public SecretKey getSecretKey(PrivateKey $$0) throws CryptException {
      return Crypt.decryptByteToSecretKey($$0, this.keybytes);
   }

   public boolean isChallengeValid(byte[] $$0, PrivateKey $$1) {
      try {
         return Arrays.equals($$0, Crypt.decryptUsingKey($$1, this.encryptedChallenge));
      } catch (CryptException var4) {
         return false;
      }
   }
}
