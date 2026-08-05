package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
   public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
   private static final int MAX_ARGUMENT_COUNT = 8;
   private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

   public ArgumentSignatures(FriendlyByteBuf $$0) {
      this((List<ArgumentSignatures.Entry>)$$0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
   }

   public void write(FriendlyByteBuf $$0) {
      $$0.writeCollection(this.entries, ($$0x, $$1) -> $$1.write($$0x));
   }

   public static ArgumentSignatures signCommand(SignableCommand<?> $$0, ArgumentSignatures.Signer $$1) {
      List<ArgumentSignatures.Entry> $$2 = $$0.arguments().stream().map($$1x -> {
         MessageSignature $$2x = $$1.sign($$1x.value());
         return $$2x != null ? new ArgumentSignatures.Entry($$1x.name(), $$2x) : null;
      }).filter(Objects::nonNull).toList();
      return new ArgumentSignatures($$2);
   }

   public record Entry(String name, MessageSignature signature) {
      public Entry(FriendlyByteBuf $$0) {
         this($$0.readUtf(16), MessageSignature.read($$0));
      }

      public void write(FriendlyByteBuf $$0) {
         $$0.writeUtf(this.name, 16);
         MessageSignature.write($$0, this.signature);
      }
   }

   @FunctionalInterface
   public interface Signer {
      
      MessageSignature sign(String var1);
   }
}
