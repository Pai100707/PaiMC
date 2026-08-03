package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.SignatureUpdater.Output;

public record LastSeenMessages(List<MessageSignature> entries) {
   public static final Codec<LastSeenMessages> CODEC = MessageSignature.CODEC.listOf().xmap(LastSeenMessages::new, LastSeenMessages::entries);
   public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
   public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

   public void updateSignature(Output $$0) throws SignatureException {
      $$0.update(Ints.toByteArray(this.entries.size()));

      for (MessageSignature $$1 : this.entries) {
         $$0.update($$1.bytes());
      }
   }

   public LastSeenMessages.Packed pack(MessageSignatureCache $$0) {
      return new LastSeenMessages.Packed(this.entries.stream().map($$1 -> $$1.pack($$0)).toList());
   }

   public byte computeChecksum() {
      int $$0 = 1;

      for (MessageSignature $$1 : this.entries) {
         $$0 = 31 * $$0 + $$1.checksum();
      }

      byte $$2 = (byte)$$0;
      return $$2 == 0 ? 1 : $$2;
   }

   public record Packed(List<MessageSignature.Packed> entries) {
      public static final LastSeenMessages.Packed EMPTY = new LastSeenMessages.Packed(List.of());

      public Packed(net.minecraft.network.FriendlyByteBuf $$0) {
         this($$0.readCollection(net.minecraft.network.FriendlyByteBuf.limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
      }

      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeCollection(this.entries, MessageSignature.Packed::write);
      }

      public Optional<LastSeenMessages> unpack(MessageSignatureCache $$0) {
         List<MessageSignature> $$1 = new ArrayList<>(this.entries.size());

         for (MessageSignature.Packed $$2 : this.entries) {
            Optional<MessageSignature> $$3 = $$2.unpack($$0);
            if ($$3.isEmpty()) {
               return Optional.empty();
            }

            $$1.add($$3.get());
         }

         return Optional.of(new LastSeenMessages($$1));
      }
   }

   public record Update(int offset, BitSet acknowledged, byte checksum) {
      public static final byte IGNORE_CHECKSUM = 0;

      public Update(net.minecraft.network.FriendlyByteBuf $$0) {
         this($$0.readVarInt(), $$0.readFixedBitSet(20), $$0.readByte());
      }

      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeVarInt(this.offset);
         $$0.writeFixedBitSet(this.acknowledged, 20);
         $$0.writeByte(this.checksum);
      }

      public boolean verifyChecksum(LastSeenMessages $$0) {
         return this.checksum == 0 || this.checksum == $$0.computeChecksum();
      }
   }
}
