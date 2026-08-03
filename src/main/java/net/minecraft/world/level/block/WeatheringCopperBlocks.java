package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.commons.lang3.function.TriFunction;

public record WeatheringCopperBlocks(
   Block unaffected, Block exposed, Block weathered, Block oxidized, Block waxed, Block waxedExposed, Block waxedWeathered, Block waxedOxidized
) {
   public static <WaxedBlock extends Block, WeatheringBlock extends Block & WeatheringCopper> WeatheringCopperBlocks create(
      String $$0,
      TriFunction<String, Function<BlockBehaviour.Properties, Block>, BlockBehaviour.Properties, Block> $$1,
      Function<BlockBehaviour.Properties, WaxedBlock> $$2,
      BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, WeatheringBlock> $$3,
      Function<WeatheringCopper.WeatherState, BlockBehaviour.Properties> $$4
   ) {
      return new WeatheringCopperBlocks(
         (Block)$$1.apply(
            $$0,
            (Function<BlockBehaviour.Properties, Block>)$$1x -> $$3.apply(WeatheringCopper.WeatherState.UNAFFECTED, $$1x),
            $$4.apply(WeatheringCopper.WeatherState.UNAFFECTED)
         ),
         (Block)$$1.apply(
            "exposed_" + $$0,
            (Function<BlockBehaviour.Properties, Block>)$$1x -> $$3.apply(WeatheringCopper.WeatherState.EXPOSED, $$1x),
            $$4.apply(WeatheringCopper.WeatherState.EXPOSED)
         ),
         (Block)$$1.apply(
            "weathered_" + $$0,
            (Function<BlockBehaviour.Properties, Block>)$$1x -> $$3.apply(WeatheringCopper.WeatherState.WEATHERED, $$1x),
            $$4.apply(WeatheringCopper.WeatherState.WEATHERED)
         ),
         (Block)$$1.apply(
            "oxidized_" + $$0,
            (Function<BlockBehaviour.Properties, Block>)$$1x -> $$3.apply(WeatheringCopper.WeatherState.OXIDIZED, $$1x),
            $$4.apply(WeatheringCopper.WeatherState.OXIDIZED)
         ),
         (Block)$$1.apply("waxed_" + $$0, $$2::apply, $$4.apply(WeatheringCopper.WeatherState.UNAFFECTED)),
         (Block)$$1.apply("waxed_exposed_" + $$0, $$2::apply, $$4.apply(WeatheringCopper.WeatherState.EXPOSED)),
         (Block)$$1.apply("waxed_weathered_" + $$0, $$2::apply, $$4.apply(WeatheringCopper.WeatherState.WEATHERED)),
         (Block)$$1.apply("waxed_oxidized_" + $$0, $$2::apply, $$4.apply(WeatheringCopper.WeatherState.OXIDIZED))
      );
   }

   public ImmutableBiMap<Block, Block> weatheringMapping() {
      return ImmutableBiMap.of(this.unaffected, this.exposed, this.exposed, this.weathered, this.weathered, this.oxidized);
   }

   public ImmutableBiMap<Block, Block> waxedMapping() {
      return ImmutableBiMap.of(
         this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized
      );
   }

   public ImmutableList<Block> asList() {
      return ImmutableList.of(
         this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized
      );
   }

   public void forEach(Consumer<Block> $$0) {
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
