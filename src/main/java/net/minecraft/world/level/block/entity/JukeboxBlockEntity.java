package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem.BlockContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements BlockContainerSingleItem {
   public static final String SONG_ITEM_TAG_ID = "RecordItem";
   public static final String TICKS_SINCE_SONG_STARTED_TAG_ID = "ticks_since_song_started";
   private ItemStack item = ItemStack.EMPTY;
   private final JukeboxSongPlayer jukeboxSongPlayer = new JukeboxSongPlayer(this::onSongChanged, this.getBlockPos());

   public JukeboxBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.JUKEBOX, $$0, $$1);
   }

   public JukeboxSongPlayer getSongPlayer() {
      return this.jukeboxSongPlayer;
   }

   public void onSongChanged() {
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.setChanged();
   }

   private void notifyItemChangedInJukebox(boolean $$0) {
      if (this.level != null && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
         this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, $$0), 2);
         this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
      }
   }

   public void popOutTheItem() {
      if (this.level != null && !this.level.isClientSide()) {
         BlockPos $$0 = this.getBlockPos();
         ItemStack $$1 = this.getTheItem();
         if (!$$1.isEmpty()) {
            this.removeTheItem();
            Vec3 $$2 = Vec3.atLowerCornerWithOffset($$0, 0.5, 1.01, 0.5).offsetRandomXZ(this.level.random, 0.7F);
            ItemStack $$3 = $$1.copy();
            ItemEntity $$4 = new ItemEntity(this.level, $$2.x(), $$2.y(), $$2.z(), $$3);
            $$4.setDefaultPickUpDelay();
            this.level.addFreshEntity($$4);
            this.onSongChanged();
         }
      }
   }

   public static void tick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, JukeboxBlockEntity $$3) {
      $$3.jukeboxSongPlayer.tick($$0, $$2);
   }

   public int getComparatorOutput() {
      return JukeboxSong.fromStack(this.level.registryAccess(), this.item).map(Holder::value).<Integer>map(JukeboxSong::comparatorOutput).orElse(0);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      ItemStack $$1 = $$0.<ItemStack>read("RecordItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
      if (!this.item.isEmpty() && !ItemStack.isSameItemSameComponents($$1, this.item)) {
         this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
      }

      this.item = $$1;
      $$0.getLong("ticks_since_song_started")
         .ifPresent($$1x -> JukeboxSong.fromStack($$0.lookup(), this.item).ifPresent($$1xx -> this.jukeboxSongPlayer.setSongWithoutPlaying($$1xx, $$1x)));
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.getTheItem().isEmpty()) {
         $$0.store("RecordItem", ItemStack.CODEC, this.getTheItem());
      }

      if (this.jukeboxSongPlayer.getSong() != null) {
         $$0.putLong("ticks_since_song_started", this.jukeboxSongPlayer.getTicksSinceSongStarted());
      }
   }

   public ItemStack getTheItem() {
      return this.item;
   }

   public ItemStack splitTheItem(int $$0) {
      ItemStack $$1 = this.item;
      this.setTheItem(ItemStack.EMPTY);
      return $$1;
   }

   public void setTheItem(ItemStack $$0) {
      this.item = $$0;
      boolean $$1 = !this.item.isEmpty();
      Optional<Holder<JukeboxSong>> $$2 = JukeboxSong.fromStack(this.level.registryAccess(), this.item);
      this.notifyItemChangedInJukebox($$1);
      if ($$1 && $$2.isPresent()) {
         this.jukeboxSongPlayer.play(this.level, $$2.get());
      } else {
         this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
      }
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
      this.level.levelEvent(1011, this.getBlockPos(), 0);
   }

   public int getMaxStackSize() {
      return 1;
   }

   public BlockEntity getContainerBlockEntity() {
      return this;
   }

   public boolean canPlaceItem(int $$0, ItemStack $$1) {
      return $$1.has(DataComponents.JUKEBOX_PLAYABLE) && this.getItem($$0).isEmpty();
   }

   public boolean canTakeItem(Container $$0, int $$1, ItemStack $$2) {
      return $$0.hasAnyMatching(ItemStack::isEmpty);
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      this.popOutTheItem();
   }

   @VisibleForTesting
   public void setSongItemWithoutPlaying(ItemStack $$0) {
      this.item = $$0;
      JukeboxSong.fromStack(this.level.registryAccess(), $$0).ifPresent($$0x -> this.jukeboxSongPlayer.setSongWithoutPlaying($$0x, 0L));
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.setChanged();
   }

   @VisibleForTesting
   public void tryForcePlaySong() {
      JukeboxSong.fromStack(this.level.registryAccess(), this.getTheItem()).ifPresent($$0 -> this.jukeboxSongPlayer.play(this.level, $$0));
   }
}
