package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentTypeInfo<StringArgumentType, StringArgumentSerializer.Template> {
   public void serializeToNetwork(StringArgumentSerializer.Template $$0, FriendlyByteBuf $$1) {
      $$1.writeEnum($$0.type);
   }

   public StringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      StringType $$1 = (StringType)$$0.readEnum(StringType.class);
      return new StringArgumentSerializer.Template($$1);
   }

   public void serializeToJson(StringArgumentSerializer.Template $$0, JsonObject $$1) {
      $$1.addProperty("type", switch ($$0.type) {
         case SINGLE_WORD -> "word";
         case QUOTABLE_PHRASE -> "phrase";
         case GREEDY_PHRASE -> "greedy";
         default -> throw new MatchException(null, null);
      });
   }

   public StringArgumentSerializer.Template unpack(StringArgumentType $$0) {
      return new StringArgumentSerializer.Template($$0.getType());
   }

   public final class Template implements ArgumentTypeInfo.Template<StringArgumentType> {
      final StringType type;

      public Template(final StringType $$1) {
         this.type = $$1;
      }

      public StringArgumentType instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return switch (this.type) {
            case SINGLE_WORD -> StringArgumentType.word();
            case QUOTABLE_PHRASE -> StringArgumentType.string();
            case GREEDY_PHRASE -> StringArgumentType.greedyString();
            default -> throw new MatchException(null, null);
         };
      }

      @Override
      public ArgumentTypeInfo<StringArgumentType, ?> type() {
         return StringArgumentSerializer.this;
      }
   }
}
