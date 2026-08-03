package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class EyeblossomBlock extends FlowerBlock {
   public static final MapCodec<EyeblossomBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.BOOL.fieldOf("open").forGetter($$0x -> $$0x.type.open), propertiesCodec()).apply($$0, EyeblossomBlock::new)
   );
   private static final int EYEBLOSSOM_XZ_RANGE = 3;
   private static final int EYEBLOSSOM_Y_RANGE = 2;
   private final EyeblossomBlock.Type type;

   @Override
   public MapCodec<? extends EyeblossomBlock> codec() {
      return CODEC;
   }

   public EyeblossomBlock(EyeblossomBlock.Type $$0, BlockBehaviour.Properties $$1) {
      super($$0.effect, $$0.effectDuration, $$1);
      this.type = $$0;
   }

   public EyeblossomBlock(boolean $$0, BlockBehaviour.Properties $$1) {
      super(EyeblossomBlock.Type.fromBoolean($$0).effect, EyeblossomBlock.Type.fromBoolean($$0).effectDuration, $$1);
      this.type = EyeblossomBlock.Type.fromBoolean($$0);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if (this.type.emitSounds() && $$3.nextInt(700) == 0) {
         BlockState $$4 = $$1.getBlockState($$2.below());
         if ($$4.is(Blocks.PALE_MOSS_BLOCK)) {
            $$1.playLocalSound($$2.getX(), $$2.getY(), $$2.getZ(), SoundEvents.EYEBLOSSOM_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false);
         }
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (this.tryChangingState($$0, $$1, $$2, $$3)) {
         $$1.playSound(null, $$2, this.type.transform().longSwitchSound, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.randomTick($$0, $$1, $$2, $$3);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (this.tryChangingState($$0, $$1, $$2, $$3)) {
         $$1.playSound(null, $$2, this.type.transform().shortSwitchSound, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.tick($$0, $$1, $$2, $$3);
   }

   private boolean tryChangingState(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      boolean $$4 = ((TriState)$$1.environmentAttributes().getValue(EnvironmentAttributes.EYEBLOSSOM_OPEN, $$2)).toBoolean(this.type.open);
      if ($$4 == this.type.open) {
         return false;
      } else {
         EyeblossomBlock.Type $$5 = this.type.transform();
         $$1.setBlock($$2, $$5.state(), 3);
         $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$0));
         $$5.spawnTransformParticle($$1, $$2, $$3);
         BlockPos.betweenClosed($$2.offset(-3, -2, -3), $$2.offset(3, 2, 3)).forEach($$4x -> {
            BlockState $$5x = $$1.getBlockState($$4x);
            if ($$5x == $$0) {
               double $$6 = Math.sqrt($$2.distSqr($$4x));
               int $$7 = $$3.nextIntBetweenInclusive((int)($$6 * 5.0), (int)($$6 * 10.0));
               $$1.scheduleTick($$4x, $$0.getBlock(), $$7);
            }
         });
         return true;
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (!$$1.isClientSide()
         && $$1.getDifficulty() != Difficulty.PEACEFUL
         && $$3 instanceof Bee $$6
         && Bee.attractsBees($$0)
         && !$$6.hasEffect(MobEffects.POISON)) {
         $$6.addEffect(this.getBeeInteractionEffect());
      }
   }

   @Override
   public MobEffectInstance getBeeInteractionEffect() {
      return new MobEffectInstance(MobEffects.POISON, 25);
   }

   public static enum Type {
      OPEN(true, MobEffects.BLINDNESS, 11.0F, SoundEvents.EYEBLOSSOM_OPEN_LONG, SoundEvents.EYEBLOSSOM_OPEN, 16545810),
      CLOSED(false, MobEffects.NAUSEA, 7.0F, SoundEvents.EYEBLOSSOM_CLOSE_LONG, SoundEvents.EYEBLOSSOM_CLOSE, 6250335);

      final boolean open;
      final Holder<MobEffect> effect;
      final float effectDuration;
      final SoundEvent longSwitchSound;
      final SoundEvent shortSwitchSound;
      private final int particleColor;

      private Type(final boolean $$0, final Holder<MobEffect> $$1, final float $$2, final SoundEvent $$3, final SoundEvent $$4, final int $$5) {
         this.open = $$0;
         this.effect = $$1;
         this.effectDuration = $$2;
         this.longSwitchSound = $$3;
         this.shortSwitchSound = $$4;
         this.particleColor = $$5;
      }

      public Block block() {
         return this.open ? Blocks.OPEN_EYEBLOSSOM : Blocks.CLOSED_EYEBLOSSOM;
      }

      public BlockState state() {
         return this.block().defaultBlockState();
      }

      public EyeblossomBlock.Type transform() {
         return fromBoolean(!this.open);
      }

      public boolean emitSounds() {
         return this.open;
      }

      public static EyeblossomBlock.Type fromBoolean(boolean $$0) {
         return $$0 ? OPEN : CLOSED;
      }

      public void spawnTransformParticle(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
         Vec3 $$3 = $$1.getCenter();
         double $$4 = 0.5 + $$2.nextDouble();
         Vec3 $$5 = new Vec3($$2.nextDouble() - 0.5, $$2.nextDouble() + 1.0, $$2.nextDouble() - 0.5);
         Vec3 $$6 = $$3.add($$5.scale($$4));
         TrailParticleOption $$7 = new TrailParticleOption($$6, this.particleColor, (int)(20.0 * $$4));
         $$0.sendParticles($$7, $$3.x, $$3.y, $$3.z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      public SoundEvent longSwitchSound() {
         return this.longSwitchSound;
      }
   }
}
