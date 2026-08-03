package net.minecraft.server.level.progress;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class LoggingLevelLoadListener implements LevelLoadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final boolean includePlayerChunks;
   private final LevelLoadProgressTracker progressTracker;
   private boolean closed;
   private long startTime = Long.MAX_VALUE;
   private long nextLogTime = Long.MAX_VALUE;

   public LoggingLevelLoadListener(boolean $$0) {
      this.includePlayerChunks = $$0;
      this.progressTracker = new LevelLoadProgressTracker($$0);
   }

   public static LoggingLevelLoadListener forDedicatedServer() {
      return new LoggingLevelLoadListener(false);
   }

   public static LoggingLevelLoadListener forSingleplayer() {
      return new LoggingLevelLoadListener(true);
   }

   @Override
   public void start(LevelLoadListener.Stage $$0, int $$1) {
      if (!this.closed) {
         if (this.startTime == Long.MAX_VALUE) {
            long $$2 = Util.getMillis();
            this.startTime = $$2;
            this.nextLogTime = $$2;
         }

         this.progressTracker.start($$0, $$1);
         switch ($$0) {
            case PREPARE_GLOBAL_SPAWN:
               LOGGER.info("Selecting global world spawn...");
               break;
            case LOAD_INITIAL_CHUNKS:
               LOGGER.info("Loading {} persistent chunks...", $$1);
               break;
            case LOAD_PLAYER_CHUNKS:
               LOGGER.info("Loading {} chunks for player spawn...", $$1);
         }
      }
   }

   @Override
   public void update(LevelLoadListener.Stage $$0, int $$1, int $$2) {
      if (!this.closed) {
         this.progressTracker.update($$0, $$1, $$2);
         if (Util.getMillis() > this.nextLogTime) {
            this.nextLogTime += 500L;
            int $$3 = Mth.floor(this.progressTracker.get() * 100.0F);
            LOGGER.info(Component.translatable("menu.preparingSpawn", new Object[]{$$3}).getString());
         }
      }
   }

   @Override
   public void finish(LevelLoadListener.Stage $$0) {
      if (!this.closed) {
         this.progressTracker.finish($$0);
         LevelLoadListener.Stage $$1 = this.includePlayerChunks ? LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS : LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS;
         if ($$0 == $$1) {
            LOGGER.info("Time elapsed: {} ms", Util.getMillis() - this.startTime);
            this.nextLogTime = Long.MAX_VALUE;
            this.closed = true;
         }
      }
   }

   @Override
   public void updateFocus(ResourceKey<Level> $$0, ChunkPos $$1) {
   }
}
