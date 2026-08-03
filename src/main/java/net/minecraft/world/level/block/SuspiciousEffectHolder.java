package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.jspecify.annotations.Nullable;

public interface SuspiciousEffectHolder {
   SuspiciousStewEffects getSuspiciousEffects();

   static List<SuspiciousEffectHolder> getAllEffectHolders() {
      return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
   }

   @Nullable
   static SuspiciousEffectHolder tryGet(net.minecraft.world.level.ItemLike $$0) {
      if ($$0.asItem() instanceof BlockItem $$1 && $$1.getBlock() instanceof SuspiciousEffectHolder $$2) {
         return $$2;
      } else {
         return $$0.asItem() instanceof SuspiciousEffectHolder $$3 ? $$3 : null;
      }
   }
}
