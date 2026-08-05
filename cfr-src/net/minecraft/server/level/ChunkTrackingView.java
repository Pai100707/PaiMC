/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
    public static final ChunkTrackingView EMPTY = new ChunkTrackingView(){

        @Override
        public boolean contains(int $$0, int $$1, boolean $$2) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> $$0) {
        }
    };

    public static ChunkTrackingView of(ChunkPos $$0, int $$1) {
        return new Positioned($$0, $$1);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void difference(ChunkTrackingView $$0, ChunkTrackingView $$1, Consumer<ChunkPos> $$2, Consumer<ChunkPos> $$3) {
        Positioned $$5;
        Positioned $$4;
        block8: {
            block7: {
                if ($$0.equals($$1)) {
                    return;
                }
                if (!($$0 instanceof Positioned)) break block7;
                $$4 = (Positioned)$$0;
                if ($$1 instanceof Positioned && $$4.squareIntersects($$5 = (Positioned)$$1)) break block8;
            }
            $$0.forEach($$3);
            $$1.forEach($$2);
            return;
        }
        int $$6 = Math.min($$4.minX(), $$5.minX());
        int $$7 = Math.min($$4.minZ(), $$5.minZ());
        int $$8 = Math.max($$4.maxX(), $$5.maxX());
        int $$9 = Math.max($$4.maxZ(), $$5.maxZ());
        int $$10 = $$6;
        while ($$10 <= $$8) {
            for (int $$11 = $$7; $$11 <= $$9; ++$$11) {
                boolean $$13;
                boolean $$12 = $$4.contains($$10, $$11);
                if ($$12 == ($$13 = $$5.contains($$10, $$11))) continue;
                if ($$13) {
                    $$2.accept(new ChunkPos($$10, $$11));
                    continue;
                }
                $$3.accept(new ChunkPos($$10, $$11));
            }
            ++$$10;
        }
        return;
    }

    default public boolean contains(ChunkPos $$0) {
        return this.contains($$0.x, $$0.z);
    }

    default public boolean contains(int $$0, int $$1) {
        return this.contains($$0, $$1, true);
    }

    public boolean contains(int var1, int var2, boolean var3);

    public void forEach(Consumer<ChunkPos> var1);

    default public boolean isInViewDistance(int $$0, int $$1) {
        return this.contains($$0, $$1, false);
    }

    public static boolean isInViewDistance(int $$0, int $$1, int $$2, int $$3, int $$4) {
        return ChunkTrackingView.isWithinDistance($$0, $$1, $$2, $$3, $$4, false);
    }

    public static boolean isWithinDistance(int $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
        int $$6 = $$5 ? 2 : 1;
        long $$7 = Math.max(0, Math.abs($$3 - $$0) - $$6);
        long $$8 = Math.max(0, Math.abs($$4 - $$1) - $$6);
        long $$9 = $$7 * $$7 + $$8 * $$8;
        int $$10 = $$2 * $$2;
        return $$9 < (long)$$10;
    }

    public record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView
    {
        int minX() {
            return this.center.x - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.z - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.x + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean squareIntersects(Positioned $$0) {
            return this.minX() <= $$0.maxX() && this.maxX() >= $$0.minX() && this.minZ() <= $$0.maxZ() && this.maxZ() >= $$0.minZ();
        }

        @Override
        public boolean contains(int $$0, int $$1, boolean $$2) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, $$0, $$1, $$2);
        }

        @Override
        public void forEach(Consumer<ChunkPos> $$0) {
            for (int $$1 = this.minX(); $$1 <= this.maxX(); ++$$1) {
                for (int $$2 = this.minZ(); $$2 <= this.maxZ(); ++$$2) {
                    if (!this.contains($$1, $$2)) continue;
                    $$0.accept(new ChunkPos($$1, $$2));
                }
            }
        }
    }
}

