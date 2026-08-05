/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level.progress;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface LevelLoadListener {
    public static LevelLoadListener compose(final LevelLoadListener $$0, final LevelLoadListener $$1) {
        return new LevelLoadListener(){

            @Override
            public void start(Stage $$02, int $$12) {
                $$0.start($$02, $$12);
                $$1.start($$02, $$12);
            }

            @Override
            public void update(Stage $$02, int $$12, int $$2) {
                $$0.update($$02, $$12, $$2);
                $$1.update($$02, $$12, $$2);
            }

            @Override
            public void finish(Stage $$02) {
                $$0.finish($$02);
                $$1.finish($$02);
            }

            @Override
            public void updateFocus(ResourceKey<Level> $$02, ChunkPos $$12) {
                $$0.updateFocus($$02, $$12);
                $$1.updateFocus($$02, $$12);
            }
        };
    }

    public void start(Stage var1, int var2);

    public void update(Stage var1, int var2, int var3);

    public void finish(Stage var1);

    public void updateFocus(ResourceKey<Level> var1, ChunkPos var2);

    public static enum Stage {
        START_SERVER,
        PREPARE_GLOBAL_SPAWN,
        LOAD_INITIAL_CHUNKS,
        LOAD_PLAYER_CHUNKS;

    }
}

