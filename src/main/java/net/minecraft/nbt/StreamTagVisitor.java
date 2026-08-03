package net.minecraft.nbt;

public interface StreamTagVisitor {
   net.minecraft.nbt.StreamTagVisitor.ValueResult visitEnd();

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(String var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(short var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(float var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(double var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte[] var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int[] var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long[] var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visitList(net.minecraft.nbt.TagType<?> var1, int var2);

   net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> var1);

   net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> var1, String var2);

   net.minecraft.nbt.StreamTagVisitor.EntryResult visitElement(net.minecraft.nbt.TagType<?> var1, int var2);

   net.minecraft.nbt.StreamTagVisitor.ValueResult visitContainerEnd();

   net.minecraft.nbt.StreamTagVisitor.ValueResult visitRootEntry(net.minecraft.nbt.TagType<?> var1);

   public static enum EntryResult {
      ENTER,
      SKIP,
      BREAK,
      HALT;
   }

   public static enum ValueResult {
      CONTINUE,
      BREAK,
      HALT;
   }
}
