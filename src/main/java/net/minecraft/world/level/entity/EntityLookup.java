package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.AbortableIterationConsumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap();
   private final Map<UUID, T> byUuid = Maps.newHashMap();

   public <U extends T> void getEntities(EntityTypeTest<T, U> $$0, AbortableIterationConsumer<U> $$1) {
      ObjectIterator var3 = this.byId.values().iterator();

      while (var3.hasNext()) {
         T $$2 = (T)var3.next();
         U $$3 = (U)$$0.tryCast($$2);
         if ($$3 != null && $$1.accept($$3).shouldAbort()) {
            return;
         }
      }
   }

   public Iterable<T> getAllEntities() {
      return Iterables.unmodifiableIterable(this.byId.values());
   }

   public void add(T $$0) {
      UUID $$1 = $$0.getUUID();
      if (this.byUuid.containsKey($$1)) {
         LOGGER.warn("Duplicate entity UUID {}: {}", $$1, $$0);
      } else {
         this.byUuid.put($$1, $$0);
         this.byId.put($$0.getId(), $$0);
      }
   }

   public void remove(T $$0) {
      this.byUuid.remove($$0.getUUID());
      this.byId.remove($$0.getId());
   }

   @Nullable
   public T getEntity(int $$0) {
      return (T)this.byId.get($$0);
   }

   @Nullable
   public T getEntity(UUID $$0) {
      return this.byUuid.get($$0);
   }

   public int count() {
      return this.byUuid.size();
   }
}
