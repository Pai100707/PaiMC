package net.minecraft.world.flag;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public interface FeatureElement {
   Set<ResourceKey<? extends Registry<? extends net.minecraft.world.flag.FeatureElement>>> FILTERED_REGISTRIES = Set.of(
      Registries.ITEM, Registries.BLOCK, Registries.ENTITY_TYPE, Registries.GAME_RULE, Registries.MENU, Registries.POTION, Registries.MOB_EFFECT
   );

   net.minecraft.world.flag.FeatureFlagSet requiredFeatures();

   default boolean isEnabled(net.minecraft.world.flag.FeatureFlagSet $$0) {
      return this.requiredFeatures().isSubsetOf($$0);
   }
}
