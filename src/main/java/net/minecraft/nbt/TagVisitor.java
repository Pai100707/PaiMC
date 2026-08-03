package net.minecraft.nbt;

public interface TagVisitor {
   void visitString(net.minecraft.nbt.StringTag var1);

   void visitByte(net.minecraft.nbt.ByteTag var1);

   void visitShort(net.minecraft.nbt.ShortTag var1);

   void visitInt(net.minecraft.nbt.IntTag var1);

   void visitLong(net.minecraft.nbt.LongTag var1);

   void visitFloat(net.minecraft.nbt.FloatTag var1);

   void visitDouble(net.minecraft.nbt.DoubleTag var1);

   void visitByteArray(net.minecraft.nbt.ByteArrayTag var1);

   void visitIntArray(net.minecraft.nbt.IntArrayTag var1);

   void visitLongArray(net.minecraft.nbt.LongArrayTag var1);

   void visitList(net.minecraft.nbt.ListTag var1);

   void visitCompound(net.minecraft.nbt.CompoundTag var1);

   void visitEnd(net.minecraft.nbt.EndTag var1);
}
