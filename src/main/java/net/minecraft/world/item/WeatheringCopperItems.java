package net.minecraft.world.item;

import com.google.common.collect.ImmutableBiMap;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopperBlocks;

public record WeatheringCopperItems(
   net.minecraft.world.item.Item unaffected,
   net.minecraft.world.item.Item exposed,
   net.minecraft.world.item.Item weathered,
   net.minecraft.world.item.Item oxidized,
   net.minecraft.world.item.Item waxed,
   net.minecraft.world.item.Item waxedExposed,
   net.minecraft.world.item.Item waxedWeathered,
   net.minecraft.world.item.Item waxedOxidized
) {
   public static net.minecraft.world.item.WeatheringCopperItems create(WeatheringCopperBlocks $$0, Function<Block, net.minecraft.world.item.Item> $$1) {
      return new net.minecraft.world.item.WeatheringCopperItems(
         $$1.apply($$0.unaffected()),
         $$1.apply($$0.exposed()),
         $$1.apply($$0.weathered()),
         $$1.apply($$0.oxidized()),
         $$1.apply($$0.waxed()),
         $$1.apply($$0.waxedExposed()),
         $$1.apply($$0.waxedWeathered()),
         $$1.apply($$0.waxedOxidized())
      );
   }

   public ImmutableBiMap<net.minecraft.world.item.Item, net.minecraft.world.item.Item> waxedMapping() {
      return ImmutableBiMap.of(
         this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized
      );
   }

   public void forEach(Consumer<net.minecraft.world.item.Item> $$0) {
      $$0.accept(this.unaffected);
      $$0.accept(this.exposed);
      $$0.accept(this.weathered);
      $$0.accept(this.oxidized);
      $$0.accept(this.waxed);
      $$0.accept(this.waxedExposed);
      $$0.accept(this.waxedWeathered);
      $$0.accept(this.waxedOxidized);
   }
}
