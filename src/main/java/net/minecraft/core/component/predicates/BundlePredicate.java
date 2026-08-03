package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public record BundlePredicate(Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements SingleComponentItemPredicate<BundleContents> {
   public static final Codec<BundlePredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(CollectionPredicate.codec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(BundlePredicate::items))
         .apply($$0, BundlePredicate::new)
   );

   public DataComponentType<BundleContents> componentType() {
      return DataComponents.BUNDLE_CONTENTS;
   }

   public boolean matches(BundleContents $$0) {
      return !this.items.isPresent() || this.items.get().test($$0.items());
   }
}
