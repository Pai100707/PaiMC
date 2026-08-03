package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Util;
import net.minecraft.util.SignatureUpdater.Output;
import org.jspecify.annotations.Nullable;

public record PlayerChatMessage(
   SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask
) {
   public static final MapCodec<PlayerChatMessage> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            SignedMessageLink.CODEC.fieldOf("link").forGetter(PlayerChatMessage::link),
            MessageSignature.CODEC.optionalFieldOf("signature").forGetter($$0x -> Optional.ofNullable($$0x.signature)),
            SignedMessageBody.MAP_CODEC.forGetter(PlayerChatMessage::signedBody),
            ComponentSerialization.CODEC.optionalFieldOf("unsigned_content").forGetter($$0x -> Optional.ofNullable($$0x.unsignedContent)),
            FilterMask.CODEC.optionalFieldOf("filter_mask", FilterMask.PASS_THROUGH).forGetter(PlayerChatMessage::filterMask)
         )
         .apply($$0, ($$0x, $$1, $$2, $$3, $$4) -> new PlayerChatMessage($$0x, (MessageSignature)$$1.orElse(null), $$2, (Component)$$3.orElse(null), $$4))
   );
   private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
   public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
   public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

   public static PlayerChatMessage system(String $$0) {
      return unsigned(SYSTEM_SENDER, $$0);
   }

   public static PlayerChatMessage unsigned(UUID $$0, String $$1) {
      SignedMessageBody $$2 = SignedMessageBody.unsigned($$1);
      SignedMessageLink $$3 = SignedMessageLink.unsigned($$0);
      return new PlayerChatMessage($$3, null, $$2, null, FilterMask.PASS_THROUGH);
   }

   public PlayerChatMessage withUnsignedContent(Component $$0) {
      Component $$1 = !$$0.equals(Component.literal(this.signedContent())) ? $$0 : null;
      return new PlayerChatMessage(this.link, this.signature, this.signedBody, $$1, this.filterMask);
   }

   public PlayerChatMessage removeUnsignedContent() {
      return this.unsignedContent != null ? new PlayerChatMessage(this.link, this.signature, this.signedBody, null, this.filterMask) : this;
   }

   public PlayerChatMessage filter(FilterMask $$0) {
      return this.filterMask.equals($$0) ? this : new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, $$0);
   }

   public PlayerChatMessage filter(boolean $$0) {
      return this.filter($$0 ? this.filterMask : FilterMask.PASS_THROUGH);
   }

   public PlayerChatMessage removeSignature() {
      SignedMessageBody $$0 = SignedMessageBody.unsigned(this.signedContent());
      SignedMessageLink $$1 = SignedMessageLink.unsigned(this.sender());
      return new PlayerChatMessage($$1, null, $$0, this.unsignedContent, this.filterMask);
   }

   public static void updateSignature(Output $$0, SignedMessageLink $$1, SignedMessageBody $$2) throws SignatureException {
      $$0.update(Ints.toByteArray(1));
      $$1.updateSignature($$0);
      $$2.updateSignature($$0);
   }

   public boolean verify(SignatureValidator $$0) {
      return this.signature != null && this.signature.verify($$0, $$0x -> updateSignature($$0x, this.link, this.signedBody));
   }

   public String signedContent() {
      return this.signedBody.content();
   }

   public Component decoratedContent() {
      return Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
   }

   public Instant timeStamp() {
      return this.signedBody.timeStamp();
   }

   public long salt() {
      return this.signedBody.salt();
   }

   public boolean hasExpiredServer(Instant $$0) {
      return $$0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
   }

   public boolean hasExpiredClient(Instant $$0) {
      return $$0.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
   }

   public UUID sender() {
      return this.link.sender();
   }

   public boolean isSystem() {
      return this.sender().equals(SYSTEM_SENDER);
   }

   public boolean hasSignature() {
      return this.signature != null;
   }

   public boolean hasSignatureFrom(UUID $$0) {
      return this.hasSignature() && this.link.sender().equals($$0);
   }

   public boolean isFullyFiltered() {
      return this.filterMask.isFullyFiltered();
   }

   public static String describeSigned(PlayerChatMessage $$0) {
      return "'"
         + $$0.signedBody.content()
         + "' @ "
         + $$0.signedBody.timeStamp()
         + "\n - From: "
         + $$0.link.sender()
         + "/"
         + $$0.link.sessionId()
         + ", message #"
         + $$0.link.index()
         + "\n - Salt: "
         + $$0.signedBody.salt()
         + "\n - Signature: "
         + MessageSignature.describe($$0.signature)
         + "\n - Last Seen: [\n"
         + $$0.signedBody.lastSeen().entries().stream().map($$0x -> "     " + MessageSignature.describe($$0x) + "\n").collect(Collectors.joining())
         + " ]\n";
   }
}
