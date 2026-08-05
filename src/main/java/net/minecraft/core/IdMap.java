package net.minecraft.core;


public interface IdMap<T> extends Iterable<T> {
   int DEFAULT = -1;

   int getId(T var1);

   
   T byId(int var1);

   default T byIdOrThrow(int $$0) {
      T $$1 = this.byId($$0);
      if ($$1 == null) {
         throw new IllegalArgumentException("No value with id " + $$0);
      } else {
         return $$1;
      }
   }

   default int getIdOrThrow(T $$0) {
      int $$1 = this.getId($$0);
      if ($$1 == -1) {
         throw new IllegalArgumentException("Can't find id for '" + $$0 + "' in map " + this);
      } else {
         return $$1;
      }
   }

   int size();
}
