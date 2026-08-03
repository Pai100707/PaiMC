package net.minecraft.world.item.crafting.display;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextKeySet.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;

public class SlotDisplayContext {
   public static final ContextKey<FuelValues> FUEL_VALUES = ContextKey.vanilla("fuel_values");
   public static final ContextKey<Provider> REGISTRIES = ContextKey.vanilla("registries");
   public static final ContextKeySet CONTEXT = new Builder().optional(FUEL_VALUES).optional(REGISTRIES).build();

   public static ContextMap fromLevel(Level $$0) {
      return new net.minecraft.util.context.ContextMap.Builder()
         .withParameter(FUEL_VALUES, $$0.fuelValues())
         .withParameter(REGISTRIES, $$0.registryAccess())
         .create(CONTEXT);
   }
}
