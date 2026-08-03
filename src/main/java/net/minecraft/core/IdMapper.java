package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public class IdMapper<T> implements net.minecraft.core.IdMap<T> {
   private int nextId;
   private final Reference2IntMap<T> tToId;
   private final List<T> idToT;

   public IdMapper() {
      this(512);
   }

   public IdMapper(int $$0) {
      this.idToT = Lists.newArrayListWithExpectedSize($$0);
      this.tToId = new Reference2IntOpenHashMap($$0);
      this.tToId.defaultReturnValue(-1);
   }

   public void addMapping(T $$0, int $$1) {
      this.tToId.put($$0, $$1);

      while (this.idToT.size() <= $$1) {
         this.idToT.add(null);
      }

      this.idToT.set($$1, $$0);
      if (this.nextId <= $$1) {
         this.nextId = $$1 + 1;
      }
   }

   public void add(T $$0) {
      this.addMapping($$0, this.nextId);
   }

   @Override
   public int getId(T $$0) {
      return this.tToId.getInt($$0);
   }

   @Nullable
   @Override
   public final T byId(int $$0) {
      return $$0 >= 0 && $$0 < this.idToT.size() ? this.idToT.get($$0) : null;
   }

   @Override
   public Iterator<T> iterator() {
      return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
   }

   public boolean contains(int $$0) {
      return this.byId($$0) != null;
   }

   @Override
   public int size() {
      return this.tToId.size();
   }
}
