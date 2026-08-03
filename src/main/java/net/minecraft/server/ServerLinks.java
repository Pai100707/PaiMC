package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public record ServerLinks(List<net.minecraft.server.ServerLinks.Entry> entries) {
   public static final net.minecraft.server.ServerLinks EMPTY = new net.minecraft.server.ServerLinks(List.of());
   public static final StreamCodec<ByteBuf, Either<net.minecraft.server.ServerLinks.KnownLinkType, Component>> TYPE_STREAM_CODEC = ByteBufCodecs.either(
      net.minecraft.server.ServerLinks.KnownLinkType.STREAM_CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC
   );
   public static final StreamCodec<ByteBuf, List<net.minecraft.server.ServerLinks.UntrustedEntry>> UNTRUSTED_LINKS_STREAM_CODEC = net.minecraft.server.ServerLinks.UntrustedEntry.STREAM_CODEC
      .apply(ByteBufCodecs.list());

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public Optional<net.minecraft.server.ServerLinks.Entry> findKnownType(net.minecraft.server.ServerLinks.KnownLinkType $$0) {
      return this.entries.stream().filter($$1 -> (Boolean)$$1.type.map($$1x -> $$1x == $$0, $$0xx -> false)).findFirst();
   }

   public List<net.minecraft.server.ServerLinks.UntrustedEntry> untrust() {
      return this.entries.stream().map($$0 -> new net.minecraft.server.ServerLinks.UntrustedEntry($$0.type, $$0.link.toString())).toList();
   }

   public record Entry(Either<net.minecraft.server.ServerLinks.KnownLinkType, Component> type, URI link) {

      public static net.minecraft.server.ServerLinks.Entry knownType(net.minecraft.server.ServerLinks.KnownLinkType $$0, URI $$1) {
         return new net.minecraft.server.ServerLinks.Entry(Either.left($$0), $$1);
      }

      public static net.minecraft.server.ServerLinks.Entry custom(Component $$0, URI $$1) {
         return new net.minecraft.server.ServerLinks.Entry(Either.right($$0), $$1);
      }

      public Component displayName() {
         return (Component)this.type.map(net.minecraft.server.ServerLinks.KnownLinkType::displayName, $$0 -> $$0);
      }
   }

   public static enum KnownLinkType {
      BUG_REPORT(0, "report_bug"),
      COMMUNITY_GUIDELINES(1, "community_guidelines"),
      SUPPORT(2, "support"),
      STATUS(3, "status"),
      FEEDBACK(4, "feedback"),
      COMMUNITY(5, "community"),
      WEBSITE(6, "website"),
      FORUMS(7, "forums"),
      NEWS(8, "news"),
      ANNOUNCEMENTS(9, "announcements");

      private static final IntFunction<net.minecraft.server.ServerLinks.KnownLinkType> BY_ID = ByIdMap.continuous(
         $$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO
      );
      public static final StreamCodec<ByteBuf, net.minecraft.server.ServerLinks.KnownLinkType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
      private final int id;
      private final String name;

      private KnownLinkType(final int $$0, final String $$1) {
         this.id = $$0;
         this.name = $$1;
      }

      private Component displayName() {
         return Component.translatable("known_server_link." + this.name);
      }

      public net.minecraft.server.ServerLinks.Entry create(URI $$0) {
         return net.minecraft.server.ServerLinks.Entry.knownType(this, $$0);
      }
   }

   public record UntrustedEntry(Either<net.minecraft.server.ServerLinks.KnownLinkType, Component> type, String link) {
      public static final StreamCodec<ByteBuf, net.minecraft.server.ServerLinks.UntrustedEntry> STREAM_CODEC = StreamCodec.composite(
         net.minecraft.server.ServerLinks.TYPE_STREAM_CODEC,
         net.minecraft.server.ServerLinks.UntrustedEntry::type,
         ByteBufCodecs.STRING_UTF8,
         net.minecraft.server.ServerLinks.UntrustedEntry::link,
         net.minecraft.server.ServerLinks.UntrustedEntry::new
      );
   }
}
