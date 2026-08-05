package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.Vec3;

public class JukeboxSongPlayer {
   public static final int PLAY_EVENT_INTERVAL_TICKS = 20;
   private long ticksSinceSongStarted;
   
   private Holder<net.minecraft.world.item.JukeboxSong> song;
   private final BlockPos blockPos;
   private final net.minecraft.world.item.JukeboxSongPlayer.OnSongChanged onSongChanged;

   public JukeboxSongPlayer(net.minecraft.world.item.JukeboxSongPlayer.OnSongChanged $$0, BlockPos $$1) {
      this.onSongChanged = $$0;
      this.blockPos = $$1;
   }

   public boolean isPlaying() {
      return this.song != null;
   }

   
   public net.minecraft.world.item.JukeboxSong getSong() {
      return this.song == null ? null : (net.minecraft.world.item.JukeboxSong)this.song.value();
   }

   public long getTicksSinceSongStarted() {
      return this.ticksSinceSongStarted;
   }

   public void setSongWithoutPlaying(Holder<net.minecraft.world.item.JukeboxSong> $$0, long $$1) {
      if (!((net.minecraft.world.item.JukeboxSong)$$0.value()).hasFinished($$1)) {
         this.song = $$0;
         this.ticksSinceSongStarted = $$1;
      }
   }

   public void play(LevelAccessor $$0, Holder<net.minecraft.world.item.JukeboxSong> $$1) {
      this.song = $$1;
      this.ticksSinceSongStarted = 0L;
      int $$2 = $$0.registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).getId((net.minecraft.world.item.JukeboxSong)this.song.value());
      $$0.levelEvent(null, 1010, this.blockPos, $$2);
      this.onSongChanged.notifyChange();
   }

   public void stop(LevelAccessor $$0, BlockState $$1) {
      if (this.song != null) {
         this.song = null;
         this.ticksSinceSongStarted = 0L;
         $$0.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.blockPos, Context.of($$1));
         $$0.levelEvent(1011, this.blockPos, 0);
         this.onSongChanged.notifyChange();
      }
   }

   public void tick(LevelAccessor $$0, BlockState $$1) {
      if (this.song != null) {
         if (((net.minecraft.world.item.JukeboxSong)this.song.value()).hasFinished(this.ticksSinceSongStarted)) {
            this.stop($$0, $$1);
         } else {
            if (this.shouldEmitJukeboxPlayingEvent()) {
               $$0.gameEvent(GameEvent.JUKEBOX_PLAY, this.blockPos, Context.of($$1));
               spawnMusicParticles($$0, this.blockPos);
            }

            this.ticksSinceSongStarted++;
         }
      }
   }

   private boolean shouldEmitJukeboxPlayingEvent() {
      return this.ticksSinceSongStarted % 20L == 0L;
   }

   private static void spawnMusicParticles(LevelAccessor $$0, BlockPos $$1) {
      if ($$0 instanceof ServerLevel $$2) {
         Vec3 $$3 = Vec3.atBottomCenterOf($$1).add(0.0, 1.2F, 0.0);
         float $$4 = $$0.getRandom().nextInt(4) / 24.0F;
         $$2.sendParticles(ParticleTypes.NOTE, $$3.x(), $$3.y(), $$3.z(), 0, $$4, 0.0, 0.0, 1.0);
      }
   }

   @FunctionalInterface
   public interface OnSongChanged {
      void notifyChange();
   }
}
