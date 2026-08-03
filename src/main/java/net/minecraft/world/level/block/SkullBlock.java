package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkullBlock extends AbstractSkullBlock {
   public static final MapCodec<SkullBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(SkullBlock.Type.CODEC.fieldOf("kind").forGetter(AbstractSkullBlock::getType), propertiesCodec()).apply($$0, SkullBlock::new)
   );
   public static final int MAX = RotationSegment.getMaxSegmentIndex();
   private static final int ROTATIONS = MAX + 1;
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 8.0);
   private static final VoxelShape SHAPE_PIGLIN = Block.column(10.0, 0.0, 8.0);

   @Override
   public MapCodec<? extends SkullBlock> codec() {
      return CODEC;
   }

   protected SkullBlock(SkullBlock.Type $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getType() == SkullBlock.Types.PIGLIN ? SHAPE_PIGLIN : SHAPE;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return super.getStateForPlacement($$0).setValue(ROTATION, RotationSegment.convertToSegment($$0.getRotation()));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(ROTATION, $$1.rotate($$0.getValue(ROTATION), ROTATIONS));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(ROTATION, $$1.mirror($$0.getValue(ROTATION), ROTATIONS));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      super.createBlockStateDefinition($$0);
      $$0.add(ROTATION);
   }

   public interface Type extends StringRepresentable {
      Map<String, SkullBlock.Type> TYPES = new Object2ObjectArrayMap();
      Codec<SkullBlock.Type> CODEC = Codec.stringResolver(StringRepresentable::getSerializedName, TYPES::get);
   }

   public static enum Types implements SkullBlock.Type {
      SKELETON("skeleton"),
      WITHER_SKELETON("wither_skeleton"),
      PLAYER("player"),
      ZOMBIE("zombie"),
      CREEPER("creeper"),
      PIGLIN("piglin"),
      DRAGON("dragon");

      private final String name;

      private Types(final String $$0) {
         this.name = $$0;
         TYPES.put($$0, this);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
