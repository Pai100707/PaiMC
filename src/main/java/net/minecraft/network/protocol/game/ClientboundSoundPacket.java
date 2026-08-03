package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundSoundPacket> STREAM_CODEC = Packet.codec(
      ClientboundSoundPacket::write, ClientboundSoundPacket::new
   );
   public static final float LOCATION_ACCURACY = 8.0F;
   private final Holder<SoundEvent> sound;
   private final SoundSource source;
   private final int x;
   private final int y;
   private final int z;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundPacket(Holder<SoundEvent> $$0, SoundSource $$1, double $$2, double $$3, double $$4, float $$5, float $$6, long $$7) {
      this.sound = $$0;
      this.source = $$1;
      this.x = (int)($$2 * 8.0);
      this.y = (int)($$3 * 8.0);
      this.z = (int)($$4 * 8.0);
      this.volume = $$5;
      this.pitch = $$6;
      this.seed = $$7;
   }

   private ClientboundSoundPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.sound = (Holder<SoundEvent>)SoundEvent.STREAM_CODEC.decode($$0);
      this.source = $$0.readEnum(SoundSource.class);
      this.x = $$0.readInt();
      this.y = $$0.readInt();
      this.z = $$0.readInt();
      this.volume = $$0.readFloat();
      this.pitch = $$0.readFloat();
      this.seed = $$0.readLong();
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      SoundEvent.STREAM_CODEC.encode($$0, this.sound);
      $$0.writeEnum(this.source);
      $$0.writeInt(this.x);
      $$0.writeInt(this.y);
      $$0.writeInt(this.z);
      $$0.writeFloat(this.volume);
      $$0.writeFloat(this.pitch);
      $$0.writeLong(this.seed);
   }

   @Override
   public PacketType<ClientboundSoundPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SOUND;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSoundEvent(this);
   }

   public Holder<SoundEvent> getSound() {
      return this.sound;
   }

   public SoundSource getSource() {
      return this.source;
   }

   public double getX() {
      return this.x / 8.0F;
   }

   public double getY() {
      return this.y / 8.0F;
   }

   public double getZ() {
      return this.z / 8.0F;
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public long getSeed() {
      return this.seed;
   }
}
