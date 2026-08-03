package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock extends VegetationBlock implements BonemealableBlock {
   public static final MapCodec<SweetBerryBushBlock> CODEC = simpleCodec(SweetBerryBushBlock::new);
   private static final float HURT_SPEED_THRESHOLD = 0.003F;
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final VoxelShape SHAPE_SAPLING = Block.column(10.0, 0.0, 8.0);
   private static final VoxelShape SHAPE_GROWING = Block.column(14.0, 0.0, 16.0);

   @Override
   public MapCodec<SweetBerryBushBlock> codec() {
      return CODEC;
   }

   public SweetBerryBushBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(Items.SWEET_BERRIES);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return switch ($$0.getValue(AGE)) {
         case 0 -> SHAPE_SAPLING;
         case 3 -> Shapes.block();
         default -> SHAPE_GROWING;
      };
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(AGE) < 3;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = $$0.getValue(AGE);
      if ($$4 < 3 && $$3.nextInt(5) == 0 && $$1.getRawBrightness($$2.above(), 0) >= 9) {
         BlockState $$5 = $$0.setValue(AGE, $$4 + 1);
         $$1.setBlock($$2, $$5, 2);
         $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$5));
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$3 instanceof LivingEntity && $$3.getType() != EntityType.FOX && $$3.getType() != EntityType.BEE) {
         $$3.makeStuckInBlock($$0, new Vec3(0.8F, 0.75, 0.8F));
         if ($$1 instanceof ServerLevel $$6 && $$0.getValue(AGE) != 0) {
            Vec3 $$8 = $$3.isClientAuthoritative() ? $$3.getKnownMovement() : $$3.oldPosition().subtract($$3.position());
            if ($$8.horizontalDistanceSqr() > 0.0) {
               double $$9 = Math.abs($$8.x());
               double $$10 = Math.abs($$8.z());
               if ($$9 >= 0.003F || $$10 >= 0.003F) {
                  $$3.hurtServer($$6, $$1.damageSources().sweetBerryBush(), 1.0F);
               }
            }
         }
      }
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      int $$7 = $$1.getValue(AGE);
      boolean $$8 = $$7 == 3;
      return (InteractionResult)(!$$8 && $$0.is(Items.BONE_MEAL) ? InteractionResult.PASS : super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6));
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$0.getValue(AGE) > 1) {
         if ($$1 instanceof ServerLevel $$5) {
            Block.dropFromBlockInteractLootTable(
               $$5, BuiltInLootTables.HARVEST_SWEET_BERRY_BUSH, $$0, $$1.getBlockEntity($$2), null, $$3, ($$1x, $$2x) -> Block.popResource($$1x, $$2, $$2x)
            );
            $$5.playSound(null, $$2, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + $$5.random.nextFloat() * 0.4F);
            BlockState $$6 = $$0.setValue(AGE, 1);
            $$5.setBlock($$2, $$6, 2);
            $$5.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$3, $$6));
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.useWithoutItem($$0, $$1, $$2, $$3, $$4);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$2.getValue(AGE) < 3;
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      int $$4 = Math.min(3, $$3.getValue(AGE) + 1);
      $$0.setBlock($$2, $$3.setValue(AGE, $$4), 2);
   }
}
