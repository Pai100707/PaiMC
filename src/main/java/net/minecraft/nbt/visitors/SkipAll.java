package net.minecraft.nbt.visitors;

public interface SkipAll extends net.minecraft.nbt.StreamTagVisitor {
   SkipAll INSTANCE = new SkipAll() {};

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visitEnd() {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(String $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(short $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(float $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(double $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte[] $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int[] $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long[] $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visitList(net.minecraft.nbt.TagType<?> $$0, int $$1) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.EntryResult visitElement(net.minecraft.nbt.TagType<?> $$0, int $$1) {
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0) {
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visitContainerEnd() {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   default net.minecraft.nbt.StreamTagVisitor.ValueResult visitRootEntry(net.minecraft.nbt.TagType<?> $$0) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }
}
