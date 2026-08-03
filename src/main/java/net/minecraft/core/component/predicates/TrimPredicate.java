package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public record TrimPredicate(Optional<net.minecraft.core.HolderSet<TrimMaterial>> material, Optional<net.minecraft.core.HolderSet<TrimPattern>> pattern)
   implements SingleComponentItemPredicate<ArmorTrim> {
   public static final Codec<TrimPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.core.RegistryCodecs.homogeneousList(Registries.TRIM_MATERIAL).optionalFieldOf("material").forGetter(TrimPredicate::material),
            net.minecraft.core.RegistryCodecs.homogeneousList(Registries.TRIM_PATTERN).optionalFieldOf("pattern").forGetter(TrimPredicate::pattern)
         )
         .apply($$0, TrimPredicate::new)
   );

   public DataComponentType<ArmorTrim> componentType() {
      return DataComponents.TRIM;
   }

   public boolean matches(ArmorTrim $$0) {
      return this.material.isPresent() && !this.material.get().contains($$0.material())
         ? false
         : !this.pattern.isPresent() || this.pattern.get().contains($$0.pattern());
   }
}
