package net.minecraft.world.entity.variant;

import com.mojang.serialization.Codec;
import java.util.List;

public record SpawnPrioritySelectors(List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors) {
   public static final SpawnPrioritySelectors EMPTY = new SpawnPrioritySelectors(List.of());
   public static final Codec<SpawnPrioritySelectors> CODEC = PriorityProvider.Selector.codec(SpawnCondition.CODEC)
      .listOf()
      .xmap(SpawnPrioritySelectors::new, SpawnPrioritySelectors::selectors);

   public static SpawnPrioritySelectors single(SpawnCondition $$0, int $$1) {
      return new SpawnPrioritySelectors(PriorityProvider.single($$0, $$1));
   }

   public static SpawnPrioritySelectors fallback(int $$0) {
      return new SpawnPrioritySelectors(PriorityProvider.alwaysTrue($$0));
   }
}
