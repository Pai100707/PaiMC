package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class BrushableBlock extends BaseEntityBlock implements Fallable {
   public static final MapCodec<BrushableBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into").forGetter(BrushableBlock::getTurnsInto),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound").forGetter(BrushableBlock::getBrushSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_completed_sound").forGetter(BrushableBlock::getBrushCompletedSound),
            propertiesCodec()
         )
         .apply($$0, BrushableBlock::new)
   );
   private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
   public static final int TICK_DELAY = 2;
   private final Block turnsInto;
   private final SoundEvent brushSound;
   private final SoundEvent brushCompletedSound;

   @Override
   public MapCodec<BrushableBlock> codec() {
      return CODEC;
   }

   public BrushableBlock(Block $$0, SoundEvent $$1, SoundEvent $$2, BlockBehaviour.Properties $$3) {
      super($$3);
      this.turnsInto = $$0;
      this.brushSound = $$1;
      this.brushCompletedSound = $$2;
      this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, 0));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(DUSTED);
   }

   @Override
   public void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      $$1.scheduleTick($$2, this, 2);
   }

   @Override
   public BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      $$2.scheduleTick($$3, this, 2);
      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   public void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.getBlockEntity($$2) instanceof BrushableBlockEntity $$4) {
         $$4.checkReset($$1);
      }

      if (FallingBlock.isFree($$1.getBlockState($$2.below())) && $$2.getY() >= $$1.getMinY()) {
         FallingBlockEntity $$5 = FallingBlockEntity.fall($$1, $$2, $$0);
         $$5.disableDrop();
      }
   }

   @Override
   public void onBrokenAfterFall(net.minecraft.world.level.Level $$0, BlockPos $$1, FallingBlockEntity $$2) {
      Vec3 $$3 = $$2.getBoundingBox().getCenter();
      $$0.levelEvent(2001, BlockPos.containing($$3), Block.getId($$2.getBlockState()));
      $$0.gameEvent($$2, GameEvent.BLOCK_DESTROY, $$3);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(16) == 0) {
         BlockPos $$4 = $$2.below();
         if (FallingBlock.isFree($$1.getBlockState($$4))) {
            double $$5 = $$2.getX() + $$3.nextDouble();
            double $$6 = $$2.getY() - 0.05;
            double $$7 = $$2.getZ() + $$3.nextDouble();
            $$1.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, $$0), $$5, $$6, $$7, 0.0, 0.0, 0.0);
         }
      }
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new BrushableBlockEntity($$0, $$1);
   }

   public Block getTurnsInto() {
      return this.turnsInto;
   }

   public SoundEvent getBrushSound() {
      return this.brushSound;
   }

   public SoundEvent getBrushCompletedSound() {
      return this.brushCompletedSound;
   }
}
