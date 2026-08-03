package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronBlock extends AbstractCauldronBlock {
   public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);
   private static final float RAIN_FILL_CHANCE = 0.05F;
   private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

   @Override
   public MapCodec<CauldronBlock> codec() {
      return CODEC;
   }

   public CauldronBlock(BlockBehaviour.Properties $$0) {
      super($$0, CauldronInteraction.EMPTY);
   }

   @Override
   public boolean isFull(BlockState $$0) {
      return false;
   }

   protected static boolean shouldHandlePrecipitation(net.minecraft.world.level.Level $$0, Biome.Precipitation $$1) {
      if ($$1 == Biome.Precipitation.RAIN) {
         return $$0.getRandom().nextFloat() < 0.05F;
      } else {
         return $$1 == Biome.Precipitation.SNOW ? $$0.getRandom().nextFloat() < 0.1F : false;
      }
   }

   @Override
   public void handlePrecipitation(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Biome.Precipitation $$3) {
      if (shouldHandlePrecipitation($$1, $$3)) {
         if ($$3 == Biome.Precipitation.RAIN) {
            $$1.setBlockAndUpdate($$2, Blocks.WATER_CAULDRON.defaultBlockState());
            $$1.gameEvent(null, GameEvent.BLOCK_CHANGE, $$2);
         } else if ($$3 == Biome.Precipitation.SNOW) {
            $$1.setBlockAndUpdate($$2, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            $$1.gameEvent(null, GameEvent.BLOCK_CHANGE, $$2);
         }
      }
   }

   @Override
   protected boolean canReceiveStalactiteDrip(Fluid $$0) {
      return true;
   }

   @Override
   protected void receiveStalactiteDrip(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Fluid $$3) {
      if ($$3 == Fluids.WATER) {
         BlockState $$4 = Blocks.WATER_CAULDRON.defaultBlockState();
         $$1.setBlockAndUpdate($$2, $$4);
         $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$4));
         $$1.levelEvent(1047, $$2, 0);
      } else if ($$3 == Fluids.LAVA) {
         BlockState $$5 = Blocks.LAVA_CAULDRON.defaultBlockState();
         $$1.setBlockAndUpdate($$2, $$5);
         $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$5));
         $$1.levelEvent(1046, $$2, 0);
      }
   }
}
