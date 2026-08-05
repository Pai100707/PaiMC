package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
   public static final MapCodec<TntBlock> CODEC = simpleCodec(TntBlock::new);
   public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

   @Override
   public MapCodec<TntBlock> codec() {
      return CODEC;
   }

   public TntBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, false));
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         if ($$1.hasNeighborSignal($$2) && prime($$1, $$2)) {
            $$1.removeBlock($$2, false);
         }
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if ($$1.hasNeighborSignal($$2) && prime($$1, $$2)) {
         $$1.removeBlock($$2, false);
      }
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$0.isClientSide() && !$$3.getAbilities().instabuild && $$2.getValue(UNSTABLE)) {
         prime($$0, $$1);
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   @Override
   public void wasExploded(ServerLevel $$0, BlockPos $$1, net.minecraft.world.level.Explosion $$2) {
      if ($$0.getGameRules().get(GameRules.TNT_EXPLODES)) {
         PrimedTnt $$3 = new PrimedTnt($$0, $$1.getX() + 0.5, $$1.getY(), $$1.getZ() + 0.5, $$2.getIndirectSourceEntity());
         int $$4 = $$3.getFuse();
         $$3.setFuse((short)($$0.random.nextInt($$4 / 4) + $$4 / 8));
         $$0.addFreshEntity($$3);
      }
   }

   public static boolean prime(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return prime($$0, $$1, null);
   }

   private static boolean prime(net.minecraft.world.level.Level $$0, BlockPos $$1, LivingEntity $$2) {
      if ($$0 instanceof ServerLevel $$3 && $$3.getGameRules().get(GameRules.TNT_EXPLODES)) {
         PrimedTnt $$5 = new PrimedTnt($$0, $$1.getX() + 0.5, $$1.getY(), $$1.getZ() + 0.5, $$2);
         $$0.addFreshEntity($$5);
         $$0.playSound(null, $$5.getX(), $$5.getY(), $$5.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$0.gameEvent($$2, GameEvent.PRIME_FUSE, $$1);
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if (!$$0.is(Items.FLINT_AND_STEEL) && !$$0.is(Items.FIRE_CHARGE)) {
         return super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6);
      } else {
         if (prime($$2, $$3, $$4)) {
            $$2.setBlock($$3, Blocks.AIR.defaultBlockState(), 11);
            Item $$7 = $$0.getItem();
            if ($$0.is(Items.FLINT_AND_STEEL)) {
               $$0.hurtAndBreak(1, $$4, $$5.asEquipmentSlot());
            } else {
               $$0.consume(1, $$4);
            }

            $$4.awardStat(Stats.ITEM_USED.get($$7));
         } else if ($$2 instanceof ServerLevel $$8 && !$$8.getGameRules().get(GameRules.TNT_EXPLODES)) {
            $$4.displayClientMessage(Component.translatable("block.minecraft.tnt.disabled"), true);
            return InteractionResult.PASS;
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      if ($$0 instanceof ServerLevel $$4) {
         BlockPos $$5 = $$2.getBlockPos();
         Entity $$6 = $$3.getOwner();
         if ($$3.isOnFire() && $$3.mayInteract($$4, $$5) && prime($$0, $$5, $$6 instanceof LivingEntity ? (LivingEntity)$$6 : null)) {
            $$0.removeBlock($$5, false);
         }
      }
   }

   @Override
   public boolean dropFromExplosion(net.minecraft.world.level.Explosion $$0) {
      return false;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(UNSTABLE);
   }
}
