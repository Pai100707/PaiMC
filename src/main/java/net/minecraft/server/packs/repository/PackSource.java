package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource {
   UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
   PackSource DEFAULT = create(NO_DECORATION, true);
   PackSource BUILT_IN = create(decorateWithSource("pack.source.builtin"), true);
   PackSource FEATURE = create(decorateWithSource("pack.source.feature"), false);
   PackSource WORLD = create(decorateWithSource("pack.source.world"), true);
   PackSource SERVER = create(decorateWithSource("pack.source.server"), true);

   Component decorate(Component var1);

   boolean shouldAddAutomatically();

   static PackSource create(final UnaryOperator<Component> $$0, final boolean $$1) {
      return new PackSource() {
         @Override
         public Component decorate(Component $$0x) {
            return $$0.apply($$0);
         }

         @Override
         public boolean shouldAddAutomatically() {
            return $$1;
         }
      };
   }

   private static UnaryOperator<Component> decorateWithSource(String $$0) {
      Component $$1 = Component.translatable($$0);
      return $$1x -> Component.translatable("pack.nameAndSource", new Object[]{$$1x, $$1}).withStyle(ChatFormatting.GRAY);
   }
}
