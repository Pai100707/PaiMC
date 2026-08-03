package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
   Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(
      () -> ImmutableBiMap.builder()
         .put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)
         .put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)
         .put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)
         .put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)
         .put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)
         .put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)
         .put(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER)
         .put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER)
         .put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER)
         .put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)
         .put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)
         .put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)
         .put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS)
         .put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS)
         .put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)
         .put(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR)
         .put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR)
         .put(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR)
         .put(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR)
         .put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR)
         .put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR)
         .putAll(Blocks.COPPER_BARS.weatheringMapping())
         .put(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE)
         .put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE)
         .put(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE)
         .put(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB)
         .put(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB)
         .put(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB)
         .putAll(Blocks.COPPER_LANTERN.weatheringMapping())
         .put(Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST)
         .put(Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER_CHEST)
         .put(Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST)
         .put(Blocks.COPPER_GOLEM_STATUE, Blocks.EXPOSED_COPPER_GOLEM_STATUE)
         .put(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WEATHERED_COPPER_GOLEM_STATUE)
         .put(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.OXIDIZED_COPPER_GOLEM_STATUE)
         .put(Blocks.LIGHTNING_ROD, Blocks.EXPOSED_LIGHTNING_ROD)
         .put(Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WEATHERED_LIGHTNING_ROD)
         .put(Blocks.WEATHERED_LIGHTNING_ROD, Blocks.OXIDIZED_LIGHTNING_ROD)
         .putAll(Blocks.COPPER_CHAIN.weatheringMapping())
         .build()
   );
   Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

   static Optional<Block> getPrevious(Block $$0) {
      return Optional.ofNullable((Block)PREVIOUS_BY_BLOCK.get().get($$0));
   }

   static Block getFirst(Block $$0) {
      Block $$1 = $$0;

      for (Block $$2 = (Block)PREVIOUS_BY_BLOCK.get().get($$0); $$2 != null; $$2 = (Block)PREVIOUS_BY_BLOCK.get().get($$2)) {
         $$1 = $$2;
      }

      return $$1;
   }

   static Optional<BlockState> getPrevious(BlockState $$0) {
      return getPrevious($$0.getBlock()).map($$1 -> $$1.withPropertiesOf($$0));
   }

   static Optional<Block> getNext(Block $$0) {
      return Optional.ofNullable((Block)NEXT_BY_BLOCK.get().get($$0));
   }

   static BlockState getFirst(BlockState $$0) {
      return getFirst($$0.getBlock()).withPropertiesOf($$0);
   }

   @Override
   default Optional<BlockState> getNext(BlockState $$0) {
      return getNext($$0.getBlock()).map($$1 -> $$1.withPropertiesOf($$0));
   }

   @Override
   default float getChanceModifier() {
      return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
   }

   public static enum WeatherState implements StringRepresentable {
      UNAFFECTED("unaffected"),
      EXPOSED("exposed"),
      WEATHERED("weathered"),
      OXIDIZED("oxidized");

      public static final IntFunction<WeatheringCopper.WeatherState> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.CLAMP);
      public static final Codec<WeatheringCopper.WeatherState> CODEC = StringRepresentable.fromEnum(WeatheringCopper.WeatherState::values);
      public static final StreamCodec<ByteBuf, WeatheringCopper.WeatherState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
      private final String name;

      private WeatherState(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }

      public WeatheringCopper.WeatherState next() {
         return BY_ID.apply(this.ordinal() + 1);
      }

      public WeatheringCopper.WeatherState previous() {
         return BY_ID.apply(this.ordinal() - 1);
      }
   }
}
