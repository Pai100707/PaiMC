package net.minecraft.advancements;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementTree {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<Identifier, net.minecraft.advancements.AdvancementNode> nodes = new Object2ObjectOpenHashMap();
   private final Set<net.minecraft.advancements.AdvancementNode> roots = new ObjectLinkedOpenHashSet();
   private final Set<net.minecraft.advancements.AdvancementNode> tasks = new ObjectLinkedOpenHashSet();
   @Nullable
   private net.minecraft.advancements.AdvancementTree.Listener listener;

   private void remove(net.minecraft.advancements.AdvancementNode $$0) {
      for (net.minecraft.advancements.AdvancementNode $$1 : $$0.children()) {
         this.remove($$1);
      }

      LOGGER.info("Forgot about advancement {}", $$0.holder());
      this.nodes.remove($$0.holder().id());
      if ($$0.parent() == null) {
         this.roots.remove($$0);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementRoot($$0);
         }
      } else {
         this.tasks.remove($$0);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementTask($$0);
         }
      }
   }

   public void remove(Set<Identifier> $$0) {
      for (Identifier $$1 : $$0) {
         net.minecraft.advancements.AdvancementNode $$2 = this.nodes.get($$1);
         if ($$2 == null) {
            LOGGER.warn("Told to remove advancement {} but I don't know what that is", $$1);
         } else {
            this.remove($$2);
         }
      }
   }

   public void addAll(Collection<net.minecraft.advancements.AdvancementHolder> $$0) {
      List<net.minecraft.advancements.AdvancementHolder> $$1 = new ArrayList<>($$0);

      while (!$$1.isEmpty()) {
         if (!$$1.removeIf(this::tryInsert)) {
            LOGGER.error("Couldn't load advancements: {}", $$1);
            break;
         }
      }

      LOGGER.info("Loaded {} advancements", this.nodes.size());
   }

   private boolean tryInsert(net.minecraft.advancements.AdvancementHolder $$0) {
      Optional<Identifier> $$1 = $$0.value().parent();
      net.minecraft.advancements.AdvancementNode $$2 = $$1.map(this.nodes::get).orElse(null);
      if ($$2 == null && $$1.isPresent()) {
         return false;
      } else {
         net.minecraft.advancements.AdvancementNode $$3 = new net.minecraft.advancements.AdvancementNode($$0, $$2);
         if ($$2 != null) {
            $$2.addChild($$3);
         }

         this.nodes.put($$0.id(), $$3);
         if ($$2 == null) {
            this.roots.add($$3);
            if (this.listener != null) {
               this.listener.onAddAdvancementRoot($$3);
            }
         } else {
            this.tasks.add($$3);
            if (this.listener != null) {
               this.listener.onAddAdvancementTask($$3);
            }
         }

         return true;
      }
   }

   public void clear() {
      this.nodes.clear();
      this.roots.clear();
      this.tasks.clear();
      if (this.listener != null) {
         this.listener.onAdvancementsCleared();
      }
   }

   public Iterable<net.minecraft.advancements.AdvancementNode> roots() {
      return this.roots;
   }

   public Collection<net.minecraft.advancements.AdvancementNode> nodes() {
      return this.nodes.values();
   }

   @Nullable
   public net.minecraft.advancements.AdvancementNode get(Identifier $$0) {
      return this.nodes.get($$0);
   }

   @Nullable
   public net.minecraft.advancements.AdvancementNode get(net.minecraft.advancements.AdvancementHolder $$0) {
      return this.nodes.get($$0.id());
   }

   public void setListener(@Nullable net.minecraft.advancements.AdvancementTree.Listener $$0) {
      this.listener = $$0;
      if ($$0 != null) {
         for (net.minecraft.advancements.AdvancementNode $$1 : this.roots) {
            $$0.onAddAdvancementRoot($$1);
         }

         for (net.minecraft.advancements.AdvancementNode $$2 : this.tasks) {
            $$0.onAddAdvancementTask($$2);
         }
      }
   }

   public interface Listener {
      void onAddAdvancementRoot(net.minecraft.advancements.AdvancementNode var1);

      void onRemoveAdvancementRoot(net.minecraft.advancements.AdvancementNode var1);

      void onAddAdvancementTask(net.minecraft.advancements.AdvancementNode var1);

      void onRemoveAdvancementTask(net.minecraft.advancements.AdvancementNode var1);

      void onAdvancementsCleared();
   }
}
