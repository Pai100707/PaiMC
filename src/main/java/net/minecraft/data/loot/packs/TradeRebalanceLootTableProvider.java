package net.minecraft.data.loot.packs;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class TradeRebalanceLootTableProvider {
   public static LootTableProvider create(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      return new LootTableProvider(
         $$0, Set.of(), List.of(new LootTableProvider.SubProviderEntry(TradeRebalanceChestLoot::new, LootContextParamSets.CHEST)), $$1
      );
   }
}
