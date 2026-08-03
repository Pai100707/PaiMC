package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public record ChatTypeDecoration(String translationKey, List<ChatTypeDecoration.Parameter> parameters, Style style) {
   public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey),
            ChatTypeDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters),
            Style.Serializer.CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)
         )
         .apply($$0, ChatTypeDecoration::new)
   );
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ChatTypeDecoration> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      ChatTypeDecoration::translationKey,
      ChatTypeDecoration.Parameter.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ChatTypeDecoration::parameters,
      Style.Serializer.TRUSTED_STREAM_CODEC,
      ChatTypeDecoration::style,
      ChatTypeDecoration::new
   );

   public static ChatTypeDecoration withSender(String $$0) {
      return new ChatTypeDecoration($$0, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
   }

   public static ChatTypeDecoration incomingDirectMessage(String $$0) {
      Style $$1 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration($$0, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), $$1);
   }

   public static ChatTypeDecoration outgoingDirectMessage(String $$0) {
      Style $$1 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration($$0, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.CONTENT), $$1);
   }

   public static ChatTypeDecoration teamMessage(String $$0) {
      return new ChatTypeDecoration(
         $$0, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY
      );
   }

   public Component decorate(Component $$0, ChatType.Bound $$1) {
      Object[] $$2 = this.resolveParameters($$0, $$1);
      return Component.translatable(this.translationKey, $$2).withStyle(this.style);
   }

   private Component[] resolveParameters(Component $$0, ChatType.Bound $$1) {
      Component[] $$2 = new Component[this.parameters.size()];

      for (int $$3 = 0; $$3 < $$2.length; $$3++) {
         ChatTypeDecoration.Parameter $$4 = this.parameters.get($$3);
         $$2[$$3] = $$4.select($$0, $$1);
      }

      return $$2;
   }

   public static enum Parameter implements StringRepresentable {
      SENDER(0, "sender", ($$0, $$1) -> $$1.name()),
      TARGET(1, "target", ($$0, $$1) -> $$1.targetName().orElse(CommonComponents.EMPTY)),
      CONTENT(2, "content", ($$0, $$1) -> $$0);

      private static final IntFunction<ChatTypeDecoration.Parameter> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
      public static final Codec<ChatTypeDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatTypeDecoration.Parameter::values);
      public static final StreamCodec<ByteBuf, ChatTypeDecoration.Parameter> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
      private final int id;
      private final String name;
      private final ChatTypeDecoration.Parameter.Selector selector;

      private Parameter(final int $$0, final String $$1, final ChatTypeDecoration.Parameter.Selector $$2) {
         this.id = $$0;
         this.name = $$1;
         this.selector = $$2;
      }

      public Component select(Component $$0, ChatType.Bound $$1) {
         return this.selector.select($$0, $$1);
      }

      public String getSerializedName() {
         return this.name;
      }

      public interface Selector {
         Component select(Component var1, ChatType.Bound var2);
      }
   }
}
