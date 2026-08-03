package net.minecraft.world.level.block;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AttachedStemBlock extends VegetationBlock {
   public static final MapCodec<AttachedStemBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter($$0x -> $$0x.fruit),
            ResourceKey.codec(Registries.BLOCK).fieldOf("stem").forGetter($$0x -> $$0x.stem),
            ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter($$0x -> $$0x.seed),
            propertiesCodec()
         )
         .apply($$0, AttachedStemBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(4.0, 0.0, 10.0, 0.0, 10.0));
   private final ResourceKey<Block> fruit;
   private final ResourceKey<Block> stem;
   private final ResourceKey<Item> seed;

   @Override
   public MapCodec<AttachedStemBlock> codec() {
      return CODEC;
   }

   protected AttachedStemBlock(ResourceKey<Block> $$0, ResourceKey<Block> $$1, ResourceKey<Item> $$2, BlockBehaviour.Properties $$3) {
      super($$3);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
      this.stem = $$0;
      this.fruit = $$1;
      this.seed = $$2;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
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
      if (!$$6.is(this.fruit) && $$4 == $$0.getValue(FACING)) {
         Optional<Block> $$8 = $$1.registryAccess().lookupOrThrow(Registries.BLOCK).getOptional(this.stem);
         if ($$8.isPresent()) {
            return $$8.get().defaultBlockState().trySetValue(StemBlock.AGE, 7);
         }
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(Blocks.FARMLAND);
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(
         (net.minecraft.world.level.ItemLike)DataFixUtils.orElse($$0.registryAccess().lookupOrThrow(Registries.ITEM).getOptional(this.seed), this)
      );
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }
}
