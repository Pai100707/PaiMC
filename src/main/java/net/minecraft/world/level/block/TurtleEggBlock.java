package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TurtleEggBlock extends Block {
   public static final MapCodec<TurtleEggBlock> CODEC = simpleCodec(TurtleEggBlock::new);
   public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
   public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
   public static final int MAX_HATCH_LEVEL = 2;
   public static final int MIN_EGGS = 1;
   public static final int MAX_EGGS = 4;
   private static final VoxelShape SHAPE_SINGLE = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
   private static final VoxelShape SHAPE_MULTIPLE = Block.column(14.0, 0.0, 7.0);

   @Override
   public MapCodec<TurtleEggBlock> codec() {
      return CODEC;
   }

   public TurtleEggBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
   }

   @Override
   public void stepOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3) {
      if (!$$3.isSteppingCarefully()) {
         this.destroyEgg($$0, $$2, $$1, $$3, 100);
      }

      super.stepOn($$0, $$1, $$2, $$3);
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      if (!($$3 instanceof Zombie)) {
         this.destroyEgg($$0, $$1, $$2, $$3, 3);
      }

      super.fallOn($$0, $$1, $$2, $$3, $$4);
   }

   private void destroyEgg(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, int $$4) {
      if ($$1.is(Blocks.TURTLE_EGG) && $$0 instanceof ServerLevel $$5 && this.canDestroyEgg($$5, $$3) && $$0.random.nextInt($$4) == 0) {
         this.decreaseEggs($$5, $$2, $$1);
      }
   }

   private void decreaseEggs(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      $$0.playSound(null, $$1, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + $$0.random.nextFloat() * 0.2F);
      int $$3 = $$2.getValue(EGGS);
      if ($$3 <= 1) {
         $$0.destroyBlock($$1, false);
      } else {
         $$0.setBlock($$1, $$2.setValue(EGGS, $$3 - 1), 2);
         $$0.gameEvent(GameEvent.BLOCK_DESTROY, $$1, GameEvent.Context.of($$2));
         $$0.levelEvent(2001, $$1, Block.getId($$2));
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (this.shouldUpdateHatchLevel($$1, $$2) && onSand($$1, $$2)) {
         int $$4 = $$0.getValue(HATCH);
         if ($$4 < 2) {
            $$1.playSound(null, $$2, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + $$3.nextFloat() * 0.2F);
            $$1.setBlock($$2, $$0.setValue(HATCH, $$4 + 1), 2);
            $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$0));
         } else {
            $$1.playSound(null, $$2, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + $$3.nextFloat() * 0.2F);
            $$1.removeBlock($$2, false);
            $$1.gameEvent(GameEvent.BLOCK_DESTROY, $$2, GameEvent.Context.of($$0));

            for (int $$5 = 0; $$5 < $$0.getValue(EGGS); $$5++) {
               $$1.levelEvent(2001, $$2, Block.getId($$0));
               Turtle $$6 = (Turtle)EntityType.TURTLE.create($$1, EntitySpawnReason.BREEDING);
               if ($$6 != null) {
                  $$6.setAge(-24000);
                  $$6.setHomePos($$2);
                  $$6.snapTo($$2.getX() + 0.3 + $$5 * 0.2, $$2.getY(), $$2.getZ() + 0.3, 0.0F, 0.0F);
                  $$1.addFreshEntity($$6);
               }
            }
         }
      }
   }

   public static boolean onSand(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return isSand($$0, $$1.below());
   }

   public static boolean isSand(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return $$0.getBlockState($$1).is(BlockTags.SAND);
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (onSand($$1, $$2) && !$$1.isClientSide()) {
         $$1.levelEvent(2012, $$2, 15);
      }
   }

   private boolean shouldUpdateHatchLevel(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      float $$2 = (Float)$$0.environmentAttributes().getValue(EnvironmentAttributes.TURTLE_EGG_HATCH_CHANCE, $$1);
      return $$2 > 0.0F && $$0.random.nextFloat() < $$2;
   }

   @Override
   public void playerDestroy(net.minecraft.world.level.Level $$0, Player $$1, BlockPos $$2, BlockState $$3, @Nullable BlockEntity $$4, ItemStack $$5) {
      super.playerDestroy($$0, $$1, $$2, $$3, $$4, $$5);
      this.decreaseEggs($$0, $$2, $$3);
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      return !$$1.isSecondaryUseActive() && $$1.getItemInHand().is(this.asItem()) && $$0.getValue(EGGS) < 4 ? true : super.canBeReplaced($$0, $$1);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos());
      return $$1.is(this) ? $$1.setValue(EGGS, Math.min(4, $$1.getValue(EGGS) + 1)) : super.getStateForPlacement($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$0.getValue(EGGS) == 1 ? SHAPE_SINGLE : SHAPE_MULTIPLE;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(HATCH, EGGS);
   }

   private boolean canDestroyEgg(ServerLevel $$0, Entity $$1) {
      if ($$1 instanceof Turtle || $$1 instanceof Bat) {
         return false;
      } else {
         return !($$1 instanceof LivingEntity) ? false : $$1 instanceof Player || $$0.getGameRules().get(GameRules.MOB_GRIEFING);
      }
   }
}
