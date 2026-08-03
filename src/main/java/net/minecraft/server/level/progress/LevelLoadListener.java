package net.minecraft.server.level.progress;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface LevelLoadListener {
   static LevelLoadListener compose(final LevelLoadListener $$0, final LevelLoadListener $$1) {
      return new LevelLoadListener() {
         @Override
         public void start(LevelLoadListener.Stage $$0x, int $$1x) {
            $$0.start($$0, $$1);
            $$1.start($$0, $$1);
         }

         @Override
         public void update(LevelLoadListener.Stage $$0x, int $$1x, int $$2) {
            $$0.update($$0, $$1, $$2);
            $$1.update($$0, $$1, $$2);
         }

         @Override
         public void finish(LevelLoadListener.Stage $$0x) {
            $$0.finish($$0);
            $$1.finish($$0);
         }

         @Override
         public void updateFocus(ResourceKey<Level> $$0x, ChunkPos $$1x) {
            $$0.updateFocus($$0, $$1);
            $$1.updateFocus($$0, $$1);
         }
      };
   }

   void start(LevelLoadListener.Stage var1, int var2);

   void update(LevelLoadListener.Stage var1, int var2, int var3);

   void finish(LevelLoadListener.Stage var1);

   void updateFocus(ResourceKey<Level> var1, ChunkPos var2);

   public static enum Stage {
      START_SERVER,
      PREPARE_GLOBAL_SPAWN,
      LOAD_INITIAL_CHUNKS,
      LOAD_PLAYER_CHUNKS;
   }
}
