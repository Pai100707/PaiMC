package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import org.jspecify.annotations.Nullable;

public class WitherSkullBlock extends SkullBlock {
   public static final MapCodec<WitherSkullBlock> CODEC = simpleCodec(WitherSkullBlock::new);
   @Nullable
   private static BlockPattern witherPatternFull;
   @Nullable
   private static BlockPattern witherPatternBase;

   @Override
   public MapCodec<WitherSkullBlock> codec() {
      return CODEC;
   }

   protected WitherSkullBlock(BlockBehaviour.Properties $$0) {
      super(SkullBlock.Types.WITHER_SKELETON, $$0);
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, @Nullable LivingEntity $$3, ItemStack $$4) {
      checkSpawn($$0, $$1);
   }

   public static void checkSpawn(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      if ($$0.getBlockEntity($$1) instanceof SkullBlockEntity $$2) {
         checkSpawn($$0, $$1, $$2);
      }
   }

   public static void checkSpawn(net.minecraft.world.level.Level $$0, BlockPos $$1, SkullBlockEntity $$2) {
      if (!$$0.isClientSide()) {
         BlockState $$3 = $$2.getBlockState();
         boolean $$4 = $$3.is(Blocks.WITHER_SKELETON_SKULL) || $$3.is(Blocks.WITHER_SKELETON_WALL_SKULL);
         if ($$4 && $$1.getY() >= $$0.getMinY() && $$0.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPattern.BlockPatternMatch $$5 = getOrCreateWitherFull().find($$0, $$1);
            if ($$5 != null) {
               WitherBoss $$6 = (WitherBoss)EntityType.WITHER.create($$0, EntitySpawnReason.TRIGGERED);
               if ($$6 != null) {
                  CarvedPumpkinBlock.clearPatternBlocks($$0, $$5);
                  BlockPos $$7 = $$5.getBlock(1, 2, 0).getPos();
                  $$6.snapTo($$7.getX() + 0.5, $$7.getY() + 0.55, $$7.getZ() + 0.5, $$5.getForwards().getAxis() == Axis.X ? 0.0F : 90.0F, 0.0F);
                  $$6.yBodyRot = $$5.getForwards().getAxis() == Axis.X ? 0.0F : 90.0F;
                  $$6.makeInvulnerable();

                  for (ServerPlayer $$8 : $$0.getEntitiesOfClass(ServerPlayer.class, $$6.getBoundingBox().inflate(50.0))) {
                     CriteriaTriggers.SUMMONED_ENTITY.trigger($$8, $$6);
                  }

                  $$0.addFreshEntity($$6);
                  CarvedPumpkinBlock.updatePatternBlocks($$0, $$5);
               }
            }
         }
      }
   }

   public static boolean canSpawnMob(net.minecraft.world.level.Level $$0, BlockPos $$1, ItemStack $$2) {
      return $$2.is(Items.WITHER_SKELETON_SKULL) && $$1.getY() >= $$0.getMinY() + 2 && $$0.getDifficulty() != Difficulty.PEACEFUL && !$$0.isClientSide()
         ? getOrCreateWitherBase().find($$0, $$1) != null
         : false;
   }

   private static BlockPattern getOrCreateWitherFull() {
      if (witherPatternFull == null) {
         witherPatternFull = BlockPatternBuilder.start()
            .aisle("^^^", "###", "~#~")
            .where('#', $$0 -> $$0.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where(
               '^',
               BlockInWorld.hasState(
                  BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))
               )
            )
            .where('~', $$0 -> $$0.getState().isAir())
            .build();
      }

      return witherPatternFull;
   }

   private static BlockPattern getOrCreateWitherBase() {
      if (witherPatternBase == null) {
         witherPatternBase = BlockPatternBuilder.start()
            .aisle("   ", "###", "~#~")
            .where('#', $$0 -> $$0.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where('~', $$0 -> $$0.getState().isAir())
            .build();
      }

      return witherPatternBase;
   }
}
