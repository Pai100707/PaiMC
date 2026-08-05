package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;

public class AdvancementNode {
   private final net.minecraft.advancements.AdvancementHolder holder;
   
   private final net.minecraft.advancements.AdvancementNode parent;
   private final Set<net.minecraft.advancements.AdvancementNode> children = new ReferenceOpenHashSet();

   @VisibleForTesting
   public AdvancementNode(net.minecraft.advancements.AdvancementHolder $$0, net.minecraft.advancements.AdvancementNode $$1) {
      this.holder = $$0;
      this.parent = $$1;
   }

   public net.minecraft.advancements.Advancement advancement() {
      return this.holder.value();
   }

   public net.minecraft.advancements.AdvancementHolder holder() {
      return this.holder;
   }

   
   public net.minecraft.advancements.AdvancementNode parent() {
      return this.parent;
   }

   public net.minecraft.advancements.AdvancementNode root() {
      return getRoot(this);
   }

   public static net.minecraft.advancements.AdvancementNode getRoot(net.minecraft.advancements.AdvancementNode $$0) {
      net.minecraft.advancements.AdvancementNode $$1 = $$0;

      while (true) {
         net.minecraft.advancements.AdvancementNode $$2 = $$1.parent();
         if ($$2 == null) {
            return $$1;
         }

         $$1 = $$2;
      }
   }

   public Iterable<net.minecraft.advancements.AdvancementNode> children() {
      return this.children;
   }

   @VisibleForTesting
   public void addChild(net.minecraft.advancements.AdvancementNode $$0) {
      this.children.add($$0);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.advancements.AdvancementNode $$1 && this.holder.equals($$1.holder);
   }

   @Override
   public int hashCode() {
      return this.holder.hashCode();
   }

   @Override
   public String toString() {
      return this.holder.id().toString();
   }
}
