package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class TreeNodePosition {
   private final net.minecraft.advancements.AdvancementNode node;
   @Nullable
   private final net.minecraft.advancements.TreeNodePosition parent;
   @Nullable
   private final net.minecraft.advancements.TreeNodePosition previousSibling;
   private final int childIndex;
   private final List<net.minecraft.advancements.TreeNodePosition> children = Lists.newArrayList();
   private net.minecraft.advancements.TreeNodePosition ancestor;
   @Nullable
   private net.minecraft.advancements.TreeNodePosition thread;
   private int x;
   private float y;
   private float mod;
   private float change;
   private float shift;

   public TreeNodePosition(
      net.minecraft.advancements.AdvancementNode $$0,
      @Nullable net.minecraft.advancements.TreeNodePosition $$1,
      @Nullable net.minecraft.advancements.TreeNodePosition $$2,
      int $$3,
      int $$4
   ) {
      if ($$0.advancement().display().isEmpty()) {
         throw new IllegalArgumentException("Can't position an invisible advancement!");
      } else {
         this.node = $$0;
         this.parent = $$1;
         this.previousSibling = $$2;
         this.childIndex = $$3;
         this.ancestor = this;
         this.x = $$4;
         this.y = -1.0F;
         net.minecraft.advancements.TreeNodePosition $$5 = null;

         for (net.minecraft.advancements.AdvancementNode $$6 : $$0.children()) {
            $$5 = this.addChild($$6, $$5);
         }
      }
   }

   @Nullable
   private net.minecraft.advancements.TreeNodePosition addChild(
      net.minecraft.advancements.AdvancementNode $$0, @Nullable net.minecraft.advancements.TreeNodePosition $$1
   ) {
      if ($$0.advancement().display().isPresent()) {
         $$1 = new net.minecraft.advancements.TreeNodePosition($$0, this, $$1, this.children.size() + 1, this.x + 1);
         this.children.add($$1);
      } else {
         for (net.minecraft.advancements.AdvancementNode $$2 : $$0.children()) {
            $$1 = this.addChild($$2, $$1);
         }
      }

      return $$1;
   }

   private void firstWalk() {
      if (this.children.isEmpty()) {
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
         } else {
            this.y = 0.0F;
         }
      } else {
         net.minecraft.advancements.TreeNodePosition $$0 = null;

         for (net.minecraft.advancements.TreeNodePosition $$1 : this.children) {
            $$1.firstWalk();
            $$0 = $$1.apportion($$0 == null ? $$1 : $$0);
         }

         this.executeShifts();
         float $$2 = (this.children.get(0).y + this.children.get(this.children.size() - 1).y) / 2.0F;
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
            this.mod = this.y - $$2;
         } else {
            this.y = $$2;
         }
      }
   }

   private float secondWalk(float $$0, int $$1, float $$2) {
      this.y += $$0;
      this.x = $$1;
      if (this.y < $$2) {
         $$2 = this.y;
      }

      for (net.minecraft.advancements.TreeNodePosition $$3 : this.children) {
         $$2 = $$3.secondWalk($$0 + this.mod, $$1 + 1, $$2);
      }

      return $$2;
   }

   private void thirdWalk(float $$0) {
      this.y += $$0;

      for (net.minecraft.advancements.TreeNodePosition $$1 : this.children) {
         $$1.thirdWalk($$0);
      }
   }

   private void executeShifts() {
      float $$0 = 0.0F;
      float $$1 = 0.0F;

      for (int $$2 = this.children.size() - 1; $$2 >= 0; $$2--) {
         net.minecraft.advancements.TreeNodePosition $$3 = this.children.get($$2);
         $$3.y += $$0;
         $$3.mod += $$0;
         $$1 += $$3.change;
         $$0 += $$3.shift + $$1;
      }
   }

   @Nullable
   private net.minecraft.advancements.TreeNodePosition previousOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(0) : null;
      }
   }

   @Nullable
   private net.minecraft.advancements.TreeNodePosition nextOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
      }
   }

   private net.minecraft.advancements.TreeNodePosition apportion(net.minecraft.advancements.TreeNodePosition $$0) {
      if (this.previousSibling == null) {
         return $$0;
      } else {
         net.minecraft.advancements.TreeNodePosition $$1 = this;
         net.minecraft.advancements.TreeNodePosition $$2 = this;
         net.minecraft.advancements.TreeNodePosition $$3 = this.previousSibling;
         net.minecraft.advancements.TreeNodePosition $$4 = this.parent.children.get(0);
         float $$5 = this.mod;
         float $$6 = this.mod;
         float $$7 = $$3.mod;

         float $$8;
         for ($$8 = $$4.mod; $$3.nextOrThread() != null && $$1.previousOrThread() != null; $$6 += $$2.mod) {
            $$3 = $$3.nextOrThread();
            $$1 = $$1.previousOrThread();
            $$4 = $$4.previousOrThread();
            $$2 = $$2.nextOrThread();
            $$2.ancestor = this;
            float $$9 = $$3.y + $$7 - ($$1.y + $$5) + 1.0F;
            if ($$9 > 0.0F) {
               $$3.getAncestor(this, $$0).moveSubtree(this, $$9);
               $$5 += $$9;
               $$6 += $$9;
            }

            $$7 += $$3.mod;
            $$5 += $$1.mod;
            $$8 += $$4.mod;
         }

         if ($$3.nextOrThread() != null && $$2.nextOrThread() == null) {
            $$2.thread = $$3.nextOrThread();
            $$2.mod += $$7 - $$6;
         } else {
            if ($$1.previousOrThread() != null && $$4.previousOrThread() == null) {
               $$4.thread = $$1.previousOrThread();
               $$4.mod += $$5 - $$8;
            }

            $$0 = this;
         }

         return $$0;
      }
   }

   private void moveSubtree(net.minecraft.advancements.TreeNodePosition $$0, float $$1) {
      float $$2 = $$0.childIndex - this.childIndex;
      if ($$2 != 0.0F) {
         $$0.change -= $$1 / $$2;
         this.change += $$1 / $$2;
      }

      $$0.shift += $$1;
      $$0.y += $$1;
      $$0.mod += $$1;
   }

   private net.minecraft.advancements.TreeNodePosition getAncestor(
      net.minecraft.advancements.TreeNodePosition $$0, net.minecraft.advancements.TreeNodePosition $$1
   ) {
      return this.ancestor != null && $$0.parent.children.contains(this.ancestor) ? this.ancestor : $$1;
   }

   private void finalizePosition() {
      this.node.advancement().display().ifPresent($$0x -> $$0x.setLocation(this.x, this.y));
      if (!this.children.isEmpty()) {
         for (net.minecraft.advancements.TreeNodePosition $$0 : this.children) {
            $$0.finalizePosition();
         }
      }
   }

   public static void run(net.minecraft.advancements.AdvancementNode $$0) {
      if ($$0.advancement().display().isEmpty()) {
         throw new IllegalArgumentException("Can't position children of an invisible root!");
      } else {
         net.minecraft.advancements.TreeNodePosition $$1 = new net.minecraft.advancements.TreeNodePosition($$0, null, null, 1, 0);
         $$1.firstWalk();
         float $$2 = $$1.secondWalk(0.0F, 0, $$1.y);
         if ($$2 < 0.0F) {
            $$1.thirdWalk(-$$2);
         }

         $$1.finalizePosition();
      }
   }
}
