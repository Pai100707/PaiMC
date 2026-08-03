package net.minecraft.network.syncher;

public record EntityDataAccessor<T>(int id, EntityDataSerializer<T> serializer) {
   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         EntityDataAccessor<?> $$1 = (EntityDataAccessor<?>)$$0;
         return this.id == $$1.id;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.id;
   }

   @Override
   public String toString() {
      return "<entity data: " + this.id + ">";
   }
}
