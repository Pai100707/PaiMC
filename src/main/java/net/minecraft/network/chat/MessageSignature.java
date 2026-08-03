package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import org.jspecify.annotations.Nullable;

public record MessageSignature(byte[] bytes) {
   public static final Codec<MessageSignature> CODEC = ExtraCodecs.BASE64_STRING.xmap(MessageSignature::new, MessageSignature::bytes);
   public static final int BYTES = 256;

   public MessageSignature(byte[] bytes) {
      Preconditions.checkState(bytes.length == 256, "Invalid message signature size");
      this.bytes = bytes;
   }

   public static MessageSignature read(net.minecraft.network.FriendlyByteBuf $$0) {
      byte[] $$1 = new byte[256];
      $$0.readBytes($$1);
      return new MessageSignature($$1);
   }

   public static void write(net.minecraft.network.FriendlyByteBuf $$0, MessageSignature $$1) {
      $$0.writeBytes($$1.bytes);
   }

   public boolean verify(SignatureValidator $$0, SignatureUpdater $$1) {
      return $$0.validate($$1, this.bytes);
   }

   public ByteBuffer asByteBuffer() {
      return ByteBuffer.wrap(this.bytes);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 || $$0 instanceof MessageSignature $$1 && Arrays.equals(this.bytes, $$1.bytes);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   @Override
   public String toString() {
      return Base64.getEncoder().encodeToString(this.bytes);
   }

   public static String describe(@Nullable MessageSignature $$0) {
      return $$0 == null ? "<no signature>" : $$0.toString();
   }

   public MessageSignature.Packed pack(MessageSignatureCache $$0) {
      int $$1 = $$0.pack(this);
      return $$1 != -1 ? new MessageSignature.Packed($$1) : new MessageSignature.Packed(this);
   }

   public int checksum() {
      return Arrays.hashCode(this.bytes);
   }

   public record Packed(int id, @Nullable MessageSignature fullSignature) {
      public static final int FULL_SIGNATURE = -1;

      public Packed(MessageSignature $$0) {
         this(-1, $$0);
      }

      public Packed(int $$0) {
         this($$0, null);
      }

      public static MessageSignature.Packed read(net.minecraft.network.FriendlyByteBuf $$0) {
         int $$1 = $$0.readVarInt() - 1;
         return $$1 == -1 ? new MessageSignature.Packed(MessageSignature.read($$0)) : new MessageSignature.Packed($$1);
      }

      public static void write(net.minecraft.network.FriendlyByteBuf $$0, MessageSignature.Packed $$1) {
         $$0.writeVarInt($$1.id() + 1);
         if ($$1.fullSignature() != null) {
            MessageSignature.write($$0, $$1.fullSignature());
         }
      }

      public Optional<MessageSignature> unpack(MessageSignatureCache $$0) {
         return this.fullSignature != null ? Optional.of(this.fullSignature) : Optional.ofNullable($$0.unpack(this.id));
      }
   }
}
