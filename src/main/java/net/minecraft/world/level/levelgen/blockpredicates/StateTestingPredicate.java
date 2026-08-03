package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StateTestingPredicate implements BlockPredicate {
   protected final Vec3i offset;

   protected static <P extends StateTestingPredicate> P1<Mu<P>, Vec3i> stateTestingCodec(Instance<P> $$0) {
      return $$0.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter($$0x -> $$0x.offset));
   }

   protected StateTestingPredicate(Vec3i $$0) {
      this.offset = $$0;
   }

   public final boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return this.test($$0.getBlockState($$1.offset(this.offset)));
   }

   protected abstract boolean test(BlockState var1);
}
