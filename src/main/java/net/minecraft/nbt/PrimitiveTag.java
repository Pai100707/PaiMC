package net.minecraft.nbt;

public sealed interface PrimitiveTag extends net.minecraft.nbt.Tag permits net.minecraft.nbt.NumericTag, net.minecraft.nbt.StringTag {
   @Override
   default net.minecraft.nbt.Tag copy() {
      return this;
   }
}
