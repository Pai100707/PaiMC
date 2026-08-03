package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater.Output;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
   public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content),
            ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp),
            Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt),
            LastSeenMessages.CODEC.optionalFieldOf("last_seen", LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)
         )
         .apply($$0, SignedMessageBody::new)
   );

   public static SignedMessageBody unsigned(String $$0) {
      return new SignedMessageBody($$0, Instant.now(), 0L, LastSeenMessages.EMPTY);
   }

   public void updateSignature(Output $$0) throws SignatureException {
      $$0.update(Longs.toByteArray(this.salt));
      $$0.update(Longs.toByteArray(this.timeStamp.getEpochSecond()));
      byte[] $$1 = this.content.getBytes(StandardCharsets.UTF_8);
      $$0.update(Ints.toByteArray($$1.length));
      $$0.update($$1);
      this.lastSeen.updateSignature($$0);
   }

   public SignedMessageBody.Packed pack(MessageSignatureCache $$0) {
      return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack($$0));
   }

   public record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
      public Packed(net.minecraft.network.FriendlyByteBuf $$0) {
         this($$0.readUtf(256), $$0.readInstant(), $$0.readLong(), new LastSeenMessages.Packed($$0));
      }

      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeUtf(this.content, 256);
         $$0.writeInstant(this.timeStamp);
         $$0.writeLong(this.salt);
         this.lastSeen.write($$0);
      }

      public Optional<SignedMessageBody> unpack(MessageSignatureCache $$0) {
         return this.lastSeen.unpack($$0).map($$0x -> new SignedMessageBody(this.content, this.timeStamp, this.salt, $$0x));
      }
   }
}
