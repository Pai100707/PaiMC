package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
   public NetherWorldCarver(Codec<CaveCarverConfiguration> $$0) {
      super($$0);
      this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
   }

   @Override
   protected int getCaveBound() {
      return 10;
   }

   @Override
   protected float getThickness(RandomSource $$0) {
      return ($$0.nextFloat() * 2.0F + $$0.nextFloat()) * 2.0F;
   }

   @Override
   protected double getYScale() {
      return 5.0;
   }

   protected boolean carveBlock(
      CarvingContext $$0,
      CaveCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      CarvingMask $$4,
      MutableBlockPos $$5,
      MutableBlockPos $$6,
      Aquifer $$7,
      MutableBoolean $$8
   ) {
      if (this.canReplaceBlock($$1, $$2.getBlockState($$5))) {
         BlockState $$9;
         if ($$5.getY() <= $$0.getMinGenY() + 31) {
            $$9 = LAVA.createLegacyBlock();
         } else {
            $$9 = CAVE_AIR;
         }

         $$2.setBlockState($$5, $$9);
         return true;
      } else {
         return false;
      }
   }
}
