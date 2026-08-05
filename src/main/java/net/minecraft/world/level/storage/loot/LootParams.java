package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;

public class LootParams {
   private final ServerLevel level;
   private final ContextMap params;
   private final Map<Identifier, LootParams.DynamicDrop> dynamicDrops;
   private final float luck;

   public LootParams(ServerLevel $$0, ContextMap $$1, Map<Identifier, LootParams.DynamicDrop> $$2, float $$3) {
      this.level = $$0;
      this.params = $$1;
      this.dynamicDrops = $$2;
      this.luck = $$3;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public ContextMap contextMap() {
      return this.params;
   }

   public void addDynamicDrops(Identifier $$0, Consumer<ItemStack> $$1) {
      LootParams.DynamicDrop $$2 = this.dynamicDrops.get($$0);
      if ($$2 != null) {
         $$2.add($$1);
      }
   }

   public float getLuck() {
      return this.luck;
   }

   public static class Builder {
      private final ServerLevel level;
      private final net.minecraft.util.context.ContextMap.Builder params = new net.minecraft.util.context.ContextMap.Builder();
      private final Map<Identifier, LootParams.DynamicDrop> dynamicDrops = Maps.newHashMap();
      private float luck;

      public Builder(ServerLevel $$0) {
         this.level = $$0;
      }

      public ServerLevel getLevel() {
         return this.level;
      }

      public <T> LootParams.Builder withParameter(ContextKey<T> $$0, T $$1) {
         this.params.withParameter($$0, $$1);
         return this;
      }

      public <T> LootParams.Builder withOptionalParameter(ContextKey<T> $$0, T $$1) {
         this.params.withOptionalParameter($$0, $$1);
         return this;
      }

      public <T> T getParameter(ContextKey<T> $$0) {
         return (T)this.params.getParameter($$0);
      }

      
      public <T> T getOptionalParameter(ContextKey<T> $$0) {
         return (T)this.params.getOptionalParameter($$0);
      }

      public LootParams.Builder withDynamicDrop(Identifier $$0, LootParams.DynamicDrop $$1) {
         LootParams.DynamicDrop $$2 = this.dynamicDrops.put($$0, $$1);
         if ($$2 != null) {
            throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
         } else {
            return this;
         }
      }

      public LootParams.Builder withLuck(float $$0) {
         this.luck = $$0;
         return this;
      }

      public LootParams create(ContextKeySet $$0) {
         ContextMap $$1 = this.params.create($$0);
         return new LootParams(this.level, $$1, this.dynamicDrops, this.luck);
      }
   }

   @FunctionalInterface
   public interface DynamicDrop {
      void add(Consumer<ItemStack> var1);
   }
}
