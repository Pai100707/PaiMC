package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import org.jspecify.annotations.Nullable;

public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
   public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<ReplaceSphereConfiguration> $$0) {
      ReplaceSphereConfiguration $$1 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      RandomSource $$3 = $$0.random();
      Block $$4 = $$1.targetState.getBlock();
      BlockPos $$5 = findTarget($$2, $$0.origin().mutable().clamp(Axis.Y, $$2.getMinY() + 1, $$2.getMaxY()), $$4);
      if ($$5 == null) {
         return false;
      } else {
         int $$6 = $$1.radius().sample($$3);
         int $$7 = $$1.radius().sample($$3);
         int $$8 = $$1.radius().sample($$3);
         int $$9 = Math.max($$6, Math.max($$7, $$8));
         boolean $$10 = false;

         for (BlockPos $$11 : BlockPos.withinManhattan($$5, $$6, $$7, $$8)) {
            if ($$11.distManhattan($$5) > $$9) {
               break;
            }

            BlockState $$12 = $$2.getBlockState($$11);
            if ($$12.is($$4)) {
               this.setBlock($$2, $$11, $$1.replaceState);
               $$10 = true;
            }
         }

         return $$10;
      }
   }

   @Nullable
   private static BlockPos findTarget(net.minecraft.world.level.LevelAccessor $$0, MutableBlockPos $$1, Block $$2) {
      while ($$1.getY() > $$0.getMinY() + 1) {
         BlockState $$3 = $$0.getBlockState($$1);
         if ($$3.is($$2)) {
            return $$1;
         }

         $$1.move(Direction.DOWN);
      }

      return null;
   }
}
