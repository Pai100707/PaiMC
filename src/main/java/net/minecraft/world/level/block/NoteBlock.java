package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block {
   public static final MapCodec<NoteBlock> CODEC = simpleCodec(NoteBlock::new);
   public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
   public static final int NOTE_VOLUME = 3;

   @Override
   public MapCodec<NoteBlock> codec() {
      return CODEC;
   }

   public NoteBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, 0).setValue(POWERED, false));
   }

   private BlockState setInstrument(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      NoteBlockInstrument $$3 = $$0.getBlockState($$1.above()).instrument();
      if ($$3.worksAboveNoteBlock()) {
         return $$2.setValue(INSTRUMENT, $$3);
      } else {
         NoteBlockInstrument $$4 = $$0.getBlockState($$1.below()).instrument();
         NoteBlockInstrument $$5 = $$4.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : $$4;
         return $$2.setValue(INSTRUMENT, $$5);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.setInstrument($$0.getLevel(), $$0.getClickedPos(), this.defaultBlockState());
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      boolean $$8 = $$4.getAxis() == Axis.Y;
      return $$8 ? this.setInstrument($$1, $$3, $$0) : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      boolean $$6 = $$1.hasNeighborSignal($$2);
      if ($$6 != $$0.getValue(POWERED)) {
         if ($$6) {
            this.playNote(null, $$0, $$1, $$2);
         }

         $$1.setBlock($$2, $$0.setValue(POWERED, $$6), 3);
      }
   }

   private void playNote(Entity $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3) {
      if ($$1.getValue(INSTRUMENT).worksAboveNoteBlock() || $$2.getBlockState($$3.above()).isAir()) {
         $$2.blockEvent($$3, this, 0, 0);
         $$2.gameEvent($$0, GameEvent.NOTE_BLOCK_PLAY, $$3);
      }
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      return (InteractionResult)($$0.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && $$6.getDirection() == Direction.UP
         ? InteractionResult.PASS
         : super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6));
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         $$0 = $$0.cycle(NOTE);
         $$1.setBlock($$2, $$0, 3);
         this.playNote($$3, $$0, $$1, $$2);
         $$3.awardStat(Stats.TUNE_NOTEBLOCK);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected void attack(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3) {
      if (!$$1.isClientSide()) {
         this.playNote($$3, $$0, $$1, $$2);
         $$3.awardStat(Stats.PLAY_NOTEBLOCK);
      }
   }

   public static float getPitchFromNote(int $$0) {
      return (float)Math.pow(2.0, ($$0 - 12) / 12.0);
   }

   @Override
   protected boolean triggerEvent(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, int $$3, int $$4) {
      NoteBlockInstrument $$5 = $$0.getValue(INSTRUMENT);
      float $$7;
      if ($$5.isTunable()) {
         int $$6 = $$0.getValue(NOTE);
         $$7 = getPitchFromNote($$6);
         $$1.addParticle(ParticleTypes.NOTE, $$2.getX() + 0.5, $$2.getY() + 1.2, $$2.getZ() + 0.5, $$6 / 24.0, 0.0, 0.0);
      } else {
         $$7 = 1.0F;
      }

      Holder<SoundEvent> $$10;
      if ($$5.hasCustomSound()) {
         Identifier $$9 = this.getCustomSoundId($$1, $$2);
         if ($$9 == null) {
            return false;
         }

         $$10 = Holder.direct(SoundEvent.createVariableRangeEvent($$9));
      } else {
         $$10 = $$5.getSoundEvent();
      }

      $$1.playSeededSound(null, $$2.getX() + 0.5, $$2.getY() + 0.5, $$2.getZ() + 0.5, $$10, SoundSource.RECORDS, 3.0F, $$7, $$1.random.nextLong());
      return true;
   }

   
   private Identifier getCustomSoundId(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return $$0.getBlockEntity($$1.above()) instanceof SkullBlockEntity $$2 ? $$2.getNoteBlockSound() : null;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(INSTRUMENT, POWERED, NOTE);
   }
}
