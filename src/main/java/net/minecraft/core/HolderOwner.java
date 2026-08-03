package net.minecraft.core;

public interface HolderOwner<T> {
   default boolean canSerializeIn(net.minecraft.core.HolderOwner<T> $$0) {
      return $$0 == this;
   }
}
