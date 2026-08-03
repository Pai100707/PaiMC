package net.minecraft.nbt;

public class TagTypes {
   private static final net.minecraft.nbt.TagType<?>[] TYPES = new net.minecraft.nbt.TagType[]{
      net.minecraft.nbt.EndTag.TYPE,
      net.minecraft.nbt.ByteTag.TYPE,
      net.minecraft.nbt.ShortTag.TYPE,
      net.minecraft.nbt.IntTag.TYPE,
      net.minecraft.nbt.LongTag.TYPE,
      net.minecraft.nbt.FloatTag.TYPE,
      net.minecraft.nbt.DoubleTag.TYPE,
      net.minecraft.nbt.ByteArrayTag.TYPE,
      net.minecraft.nbt.StringTag.TYPE,
      net.minecraft.nbt.ListTag.TYPE,
      net.minecraft.nbt.CompoundTag.TYPE,
      net.minecraft.nbt.IntArrayTag.TYPE,
      net.minecraft.nbt.LongArrayTag.TYPE
   };

   public static net.minecraft.nbt.TagType<?> getType(int $$0) {
      return $$0 >= 0 && $$0 < TYPES.length ? TYPES[$$0] : net.minecraft.nbt.TagType.createInvalid($$0);
   }
}
