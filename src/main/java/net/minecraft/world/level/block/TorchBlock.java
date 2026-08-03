package net.minecraft.world.level.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlock extends BaseTorchBlock {
   protected static final MapCodec<SimpleParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE
      .byNameCodec()
      .comapFlatMap(
         $$0 -> $$0 instanceof SimpleParticleType $$1 ? DataResult.success($$1) : DataResult.error(() -> "Not a SimpleParticleType: " + $$0), $$0 -> $$0
      )
      .fieldOf("particle_options");
   public static final MapCodec<TorchBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(PARTICLE_OPTIONS_FIELD.forGetter($$0x -> $$0x.flameParticle), propertiesCodec()).apply($$0, TorchBlock::new)
   );
   protected final SimpleParticleType flameParticle;

   @Override
   public MapCodec<? extends TorchBlock> codec() {
      return CODEC;
   }

   protected TorchBlock(SimpleParticleType $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.flameParticle = $$0;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      double $$4 = $$2.getX() + 0.5;
      double $$5 = $$2.getY() + 0.7;
      double $$6 = $$2.getZ() + 0.5;
      $$1.addParticle(ParticleTypes.SMOKE, $$4, $$5, $$6, 0.0, 0.0, 0.0);
      $$1.addParticle(this.flameParticle, $$4, $$5, $$6, 0.0, 0.0, 0.0);
   }
}
