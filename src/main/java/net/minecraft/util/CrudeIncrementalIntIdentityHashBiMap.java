package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.core.IdMap;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
   private static final int NOT_FOUND = -1;
   private static final Object EMPTY_SLOT = null;
   private static final float LOADFACTOR = 0.8F;
   private K[] keys;
   private int[] values;
   private K[] byId;
   private int nextId;
   private int size;

   private CrudeIncrementalIntIdentityHashBiMap(int $$0) {
      this.keys = (K[])(new Object[$$0]);
      this.values = new int[$$0];
      this.byId = (K[])(new Object[$$0]);
   }

   private CrudeIncrementalIntIdentityHashBiMap(K[] $$0, int[] $$1, K[] $$2, int $$3, int $$4) {
      this.keys = $$0;
      this.values = $$1;
      this.byId = $$2;
      this.nextId = $$3;
      this.size = $$4;
   }

   public static <A> net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap<A> create(int $$0) {
      return new net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap((int)($$0 / 0.8F));
   }

   public int getId(K $$0) {
      return this.getValue(this.indexOf($$0, this.hash($$0)));
   }

   
   public K byId(int $$0) {
      return $$0 >= 0 && $$0 < this.byId.length ? this.byId[$$0] : null;
   }

   private int getValue(int $$0) {
      return $$0 == -1 ? -1 : this.values[$$0];
   }

   public boolean contains(K $$0) {
      return this.getId($$0) != -1;
   }

   public boolean contains(int $$0) {
      return this.byId($$0) != null;
   }

   public int add(K $$0) {
      int $$1 = this.nextId();
      this.addMapping($$0, $$1);
      return $$1;
   }

   private int nextId() {
      while (this.nextId < this.byId.length && this.byId[this.nextId] != null) {
         this.nextId++;
      }

      return this.nextId;
   }

   private void grow(int $$0) {
      K[] $$1 = this.keys;
      int[] $$2 = this.values;
      net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap<K> $$3 = new net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap<>($$0);

      for (int $$4 = 0; $$4 < $$1.length; $$4++) {
         if ($$1[$$4] != null) {
            $$3.addMapping($$1[$$4], $$2[$$4]);
         }
      }

      this.keys = $$3.keys;
      this.values = $$3.values;
      this.byId = $$3.byId;
      this.nextId = $$3.nextId;
      this.size = $$3.size;
   }

   public void addMapping(K $$0, int $$1) {
      int $$2 = Math.max($$1, this.size + 1);
      if ($$2 >= this.keys.length * 0.8F) {
         int $$3 = this.keys.length << 1;

         while ($$3 < $$1) {
            $$3 <<= 1;
         }

         this.grow($$3);
      }

      int $$4 = this.findEmpty(this.hash($$0));
      this.keys[$$4] = $$0;
      this.values[$$4] = $$1;
      this.byId[$$1] = $$0;
      this.size++;
      if ($$1 == this.nextId) {
         this.nextId++;
      }
   }

   private int hash(K $$0) {
      return (net.minecraft.util.Mth.murmurHash3Mixer(System.identityHashCode($$0)) & 2147483647) % this.keys.length;
   }

   private int indexOf(K $$0, int $$1) {
      for (int $$2 = $$1; $$2 < this.keys.length; $$2++) {
         if (this.keys[$$2] == $$0) {
            return $$2;
         }

         if (this.keys[$$2] == EMPTY_SLOT) {
            return -1;
         }
      }

      for (int $$3 = 0; $$3 < $$1; $$3++) {
         if (this.keys[$$3] == $$0) {
            return $$3;
         }

         if (this.keys[$$3] == EMPTY_SLOT) {
            return -1;
         }
      }

      return -1;
   }

   private int findEmpty(int $$0) {
      for (int $$1 = $$0; $$1 < this.keys.length; $$1++) {
         if (this.keys[$$1] == EMPTY_SLOT) {
            return $$1;
         }
      }

      for (int $$2 = 0; $$2 < $$0; $$2++) {
         if (this.keys[$$2] == EMPTY_SLOT) {
            return $$2;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator<K> iterator() {
      return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.keys, null);
      Arrays.fill(this.byId, null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }

   public net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap<K> copy() {
      return new net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap<>(
         (K[])((Object[])this.keys.clone()), (int[])this.values.clone(), (K[])((Object[])this.byId.clone()), this.nextId, this.size
      );
   }
}
