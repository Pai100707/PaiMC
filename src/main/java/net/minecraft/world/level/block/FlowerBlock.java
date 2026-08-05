package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends VegetationBlock implements SuspiciousEffectHolder {
   protected static final MapCodec<SuspiciousStewEffects> EFFECTS_FIELD = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
   public static final MapCodec<FlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply($$0, FlowerBlock::new)
   );
   private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 10.0);
   private final SuspiciousStewEffects suspiciousStewEffects;

   @Override
   public MapCodec<? extends FlowerBlock> codec() {
      return CODEC;
   }

   public FlowerBlock(Holder<MobEffect> $$0, float $$1, BlockBehaviour.Properties $$2) {
      this(makeEffectList($$0, $$1), $$2);
   }

   public FlowerBlock(SuspiciousStewEffects $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.suspiciousStewEffects = $$0;
   }

   protected static SuspiciousStewEffects makeEffectList(Holder<MobEffect> $$0, float $$1) {
      return new SuspiciousStewEffects(List.of(new Entry($$0, Mth.floor($$1 * 20.0F))));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE.move($$0.getOffset($$2));
   }

   @Override
   public SuspiciousStewEffects getSuspiciousEffects() {
      return this.suspiciousStewEffects;
   }

   
   public MobEffectInstance getBeeInteractionEffect() {
      return null;
   }
}
