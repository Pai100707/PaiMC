package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
   ChunkTrackingView EMPTY = new ChunkTrackingView() {
      @Override
      public boolean contains(int $$0, int $$1, boolean $$2) {
         return false;
      }

      @Override
      public void forEach(Consumer<ChunkPos> $$0) {
      }
   };

   static ChunkTrackingView of(ChunkPos $$0, int $$1) {
      return new ChunkTrackingView.Positioned($$0, $$1);
   }

   static void difference(ChunkTrackingView $$0, ChunkTrackingView $$1, Consumer<ChunkPos> $$2, Consumer<ChunkPos> $$3) {
      if (!$$0.equals($$1)) {
         if ($$0 instanceof ChunkTrackingView.Positioned $$4 && $$1 instanceof ChunkTrackingView.Positioned $$5 && $$4.squareIntersects($$5)) {
            int $$6 = Math.min($$4.minX(), $$5.minX());
            int $$7 = Math.min($$4.minZ(), $$5.minZ());
            int $$8 = Math.max($$4.maxX(), $$5.maxX());
            int $$9 = Math.max($$4.maxZ(), $$5.maxZ());

            for (int $$10 = $$6; $$10 <= $$8; $$10++) {
               for (int $$11 = $$7; $$11 <= $$9; $$11++) {
                  boolean $$12 = $$4.contains($$10, $$11);
                  boolean $$13 = $$5.contains($$10, $$11);
                  if ($$12 != $$13) {
                     if ($$13) {
                        $$2.accept(new ChunkPos($$10, $$11));
                     } else {
                        $$3.accept(new ChunkPos($$10, $$11));
                     }
                  }
               }
            }
         } else {
            $$0.forEach($$3);
            $$1.forEach($$2);
         }
      }
   }

   default boolean contains(ChunkPos $$0) {
      return this.contains($$0.x, $$0.z);
   }

   default boolean contains(int $$0, int $$1) {
      return this.contains($$0, $$1, true);
   }

   boolean contains(int var1, int var2, boolean var3);

   void forEach(Consumer<ChunkPos> var1);

   default boolean isInViewDistance(int $$0, int $$1) {
      return this.contains($$0, $$1, false);
   }

   static boolean isInViewDistance(int $$0, int $$1, int $$2, int $$3, int $$4) {
      return isWithinDistance($$0, $$1, $$2, $$3, $$4, false);
   }

   static boolean isWithinDistance(int $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      int $$6 = $$5 ? 2 : 1;
      long $$7 = Math.max(0, Math.abs($$3 - $$0) - $$6);
      long $$8 = Math.max(0, Math.abs($$4 - $$1) - $$6);
      long $$9 = $$7 * $$7 + $$8 * $$8;
      int $$10 = $$2 * $$2;
      return $$9 < $$10;
   }

   public record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView {
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
      protected boolean squareIntersects(ChunkTrackingView.Positioned $$0) {
         return this.minX() <= $$0.maxX() && this.maxX() >= $$0.minX() && this.minZ() <= $$0.maxZ() && this.maxZ() >= $$0.minZ();
      }

      @Override
      public boolean contains(int $$0, int $$1, boolean $$2) {
         return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, $$0, $$1, $$2);
      }

      @Override
      public void forEach(Consumer<ChunkPos> $$0) {
         for (int $$1 = this.minX(); $$1 <= this.maxX(); $$1++) {
            for (int $$2 = this.minZ(); $$2 <= this.maxZ(); $$2++) {
               if (this.contains($$1, $$2)) {
                  $$0.accept(new ChunkPos($$1, $$2));
               }
            }
         }
      }
   }
}
