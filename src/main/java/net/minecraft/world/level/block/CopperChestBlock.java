package net.minecraft.world.level.block;

import com.google.common.collect.BiMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestBlock extends ChestBlock {
   public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound),
            propertiesCodec()
         )
         .apply($$0, CopperChestBlock::new)
   );
   private static final Map<Block, Supplier<Block>> COPPER_TO_COPPER_CHEST_MAPPING = Map.of(
      Blocks.COPPER_BLOCK,
      () -> Blocks.COPPER_CHEST,
      Blocks.EXPOSED_COPPER,
      () -> Blocks.EXPOSED_COPPER_CHEST,
      Blocks.WEATHERED_COPPER,
      () -> Blocks.WEATHERED_COPPER_CHEST,
      Blocks.OXIDIZED_COPPER,
      () -> Blocks.OXIDIZED_COPPER_CHEST,
      Blocks.WAXED_COPPER_BLOCK,
      () -> Blocks.COPPER_CHEST,
      Blocks.WAXED_EXPOSED_COPPER,
      () -> Blocks.EXPOSED_COPPER_CHEST,
      Blocks.WAXED_WEATHERED_COPPER,
      () -> Blocks.WEATHERED_COPPER_CHEST,
      Blocks.WAXED_OXIDIZED_COPPER,
      () -> Blocks.OXIDIZED_COPPER_CHEST
   );
   private final WeatheringCopper.WeatherState weatherState;

   @Override
   public MapCodec<? extends CopperChestBlock> codec() {
      return CODEC;
   }

   public CopperChestBlock(WeatheringCopper.WeatherState $$0, SoundEvent $$1, SoundEvent $$2, BlockBehaviour.Properties $$3) {
      super(() -> BlockEntityType.CHEST, $$1, $$2, $$3);
      this.weatherState = $$0;
   }

   @Override
   public boolean chestCanConnectTo(BlockState $$0) {
      return $$0.is(BlockTags.COPPER_CHESTS) && $$0.hasProperty(ChestBlock.TYPE);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = super.getStateForPlacement($$0);
      return getLeastOxidizedChestOfConnectedBlocks($$1, $$0.getLevel(), $$0.getClickedPos());
   }

   private static BlockState getLeastOxidizedChestOfConnectedBlocks(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.relative(getConnectedDirection($$0)));
      if (!$$0.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)
         && $$0.getBlock() instanceof CopperChestBlock $$4
         && $$3.getBlock() instanceof CopperChestBlock $$5) {
         BlockState $$6 = $$0;
         BlockState $$7 = $$3;
         if ($$4.isWaxed() != $$5.isWaxed()) {
            $$6 = unwaxBlock($$4, $$0).orElse($$0);
            $$7 = unwaxBlock($$5, $$3).orElse($$3);
         }

         Block $$8 = $$4.weatherState.ordinal() <= $$5.weatherState.ordinal() ? $$6.getBlock() : $$7.getBlock();
         return $$8.withPropertiesOf($$6);
      } else {
         return $$0;
      }
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      BlockState $$8 = super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      if (this.chestCanConnectTo($$6)) {
         ChestType $$9 = $$8.getValue(ChestBlock.TYPE);
         if (!$$9.equals(ChestType.SINGLE) && getConnectedDirection($$8) == $$4) {
            return $$6.getBlock().withPropertiesOf($$8);
         }
      }

      return $$8;
   }

   private static Optional<BlockState> unwaxBlock(CopperChestBlock $$0, BlockState $$1) {
      return !$$0.isWaxed()
         ? Optional.of($$1)
         : Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get($$1.getBlock())).map($$1x -> $$1x.withPropertiesOf($$1));
   }

   public WeatheringCopper.WeatherState getState() {
      return this.weatherState;
   }

   public static BlockState getFromCopperBlock(Block $$0, Direction $$1, net.minecraft.world.level.Level $$2, BlockPos $$3) {
      CopperChestBlock $$4 = (CopperChestBlock)COPPER_TO_COPPER_CHEST_MAPPING.getOrDefault($$0, Blocks.COPPER_CHEST::asBlock).get();
      ChestType $$5 = $$4.getChestType($$2, $$3, $$1);
      BlockState $$6 = $$4.defaultBlockState().setValue(FACING, $$1).setValue(TYPE, $$5);
      return getLeastOxidizedChestOfConnectedBlocks($$6, $$2, $$3);
   }

   public boolean isWaxed() {
      return true;
   }

   @Override
   public boolean shouldChangedStateKeepBlockEntity(BlockState $$0) {
      return $$0.is(BlockTags.COPPER_CHESTS);
   }
}
