package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class CustomData {
   public static final CustomData EMPTY = new CustomData(new CompoundTag());
   public static final Codec<CompoundTag> COMPOUND_TAG_CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.FLATTENED_CODEC);
   public static final Codec<CustomData> CODEC = COMPOUND_TAG_CODEC.xmap(CustomData::new, $$0 -> $$0.tag);
   @Deprecated
   public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, $$0 -> $$0.tag);
   private final CompoundTag tag;

   private CustomData(CompoundTag $$0) {
      this.tag = $$0;
   }

   public static CustomData of(CompoundTag $$0) {
      return new CustomData($$0.copy());
   }

   public boolean matchedBy(CompoundTag $$0) {
      return NbtUtils.compareNbt($$0, this.tag, true);
   }

   public static void update(DataComponentType<CustomData> $$0, net.minecraft.world.item.ItemStack $$1, Consumer<CompoundTag> $$2) {
      CustomData $$3 = ((CustomData)$$1.getOrDefault($$0, EMPTY)).update($$2);
      if ($$3.tag.isEmpty()) {
         $$1.remove($$0);
      } else {
         $$1.set($$0, $$3);
      }
   }

   public static void set(DataComponentType<CustomData> $$0, net.minecraft.world.item.ItemStack $$1, CompoundTag $$2) {
      if (!$$2.isEmpty()) {
         $$1.set($$0, of($$2));
      } else {
         $$1.remove($$0);
      }
   }

   public CustomData update(Consumer<CompoundTag> $$0) {
      CompoundTag $$1 = this.tag.copy();
      $$0.accept($$1);
      return new CustomData($$1);
   }

   public boolean isEmpty() {
      return this.tag.isEmpty();
   }

   public CompoundTag copyTag() {
      return this.tag.copy();
   }

   @Override
   public boolean equals(Object $$0) {
      if ($$0 == this) {
         return true;
      } else {
         return $$0 instanceof CustomData $$1 ? this.tag.equals($$1.tag) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.tag.hashCode();
   }

   @Override
   public String toString() {
      return this.tag.toString();
   }
}
