package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public record PotionsPredicate(net.minecraft.core.HolderSet<Potion> potions) implements SingleComponentItemPredicate<PotionContents> {
   public static final Codec<PotionsPredicate> CODEC = net.minecraft.core.RegistryCodecs.homogeneousList(Registries.POTION)
      .xmap(PotionsPredicate::new, PotionsPredicate::potions);

   public DataComponentType<PotionContents> componentType() {
      return DataComponents.POTION_CONTENTS;
   }

   public boolean matches(PotionContents $$0) {
      Optional<net.minecraft.core.Holder<Potion>> $$1 = $$0.potion();
      return !$$1.isEmpty() && this.potions.contains($$1.get());
   }

   public static DataComponentPredicate potions(net.minecraft.core.HolderSet<Potion> $$0) {
      return new PotionsPredicate($$0);
   }
}
