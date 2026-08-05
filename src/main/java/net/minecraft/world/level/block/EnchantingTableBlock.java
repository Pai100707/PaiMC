package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantingTableBlock extends BaseEntityBlock {
   public static final MapCodec<EnchantingTableBlock> CODEC = simpleCodec(EnchantingTableBlock::new);
   public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
      .filter($$0 -> Math.abs($$0.getX()) == 2 || Math.abs($$0.getZ()) == 2)
      .<BlockPos>map(BlockPos::immutable)
      .toList();
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 12.0);

   @Override
   public MapCodec<EnchantingTableBlock> codec() {
      return CODEC;
   }

   protected EnchantingTableBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   public static boolean isValidBookShelf(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockPos $$2) {
      return $$0.getBlockState($$1.offset($$2)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
         && $$0.getBlockState($$1.offset($$2.getX() / 2, $$2.getY(), $$2.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      super.animateTick($$0, $$1, $$2, $$3);

      for (BlockPos $$4 : BOOKSHELF_OFFSETS) {
         if ($$3.nextInt(16) == 0 && isValidBookShelf($$1, $$2, $$4)) {
            $$1.addParticle(
               ParticleTypes.ENCHANT,
               $$2.getX() + 0.5,
               $$2.getY() + 2.0,
               $$2.getZ() + 0.5,
               $$4.getX() + $$3.nextFloat() - 0.5,
               $$4.getY() - $$3.nextFloat() - 1.0F,
               $$4.getZ() + $$3.nextFloat() - 0.5
            );
         }
      }
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new EnchantingTableBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0.isClientSide() ? createTickerHelper($$2, BlockEntityType.ENCHANTING_TABLE, EnchantingTableBlockEntity::bookAnimationTick) : null;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         $$3.openMenu($$0.getMenuProvider($$1, $$2));
      }

      return InteractionResult.SUCCESS;
   }

   
   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      if ($$1.getBlockEntity($$2) instanceof EnchantingTableBlockEntity $$4) {
         Component $$5 = $$4.getDisplayName();
         return new SimpleMenuProvider(($$2x, $$3, $$4x) -> new EnchantmentMenu($$2x, $$3, ContainerLevelAccess.create($$1, $$2)), $$5);
      } else {
         return null;
      }
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
