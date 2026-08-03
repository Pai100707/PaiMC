package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;

public interface AdvancementSubProvider {
   void generate(Provider var1, Consumer<AdvancementHolder> var2);

   static AdvancementHolder createPlaceholder(String $$0) {
      return Builder.advancement().build(Identifier.parse($$0));
   }
}
