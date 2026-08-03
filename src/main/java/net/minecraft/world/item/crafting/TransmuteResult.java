package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

public record TransmuteResult(Holder<net.minecraft.world.item.Item> item, int count, DataComponentPatch components) {
   private static final Codec<TransmuteResult> FULL_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.world.item.Item.CODEC.fieldOf("id").forGetter(TransmuteResult::item),
            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(TransmuteResult::count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(TransmuteResult::components)
         )
         .apply($$0, TransmuteResult::new)
   );
   public static final Codec<TransmuteResult> CODEC = Codec.withAlternative(
         FULL_CODEC, net.minecraft.world.item.Item.CODEC, $$0 -> new TransmuteResult((net.minecraft.world.item.Item)$$0.value())
      )
      .validate(TransmuteResult::validate);
   public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteResult> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.item.Item.STREAM_CODEC,
      TransmuteResult::item,
      ByteBufCodecs.VAR_INT,
      TransmuteResult::count,
      DataComponentPatch.STREAM_CODEC,
      TransmuteResult::components,
      TransmuteResult::new
   );

   public TransmuteResult(net.minecraft.world.item.Item $$0) {
      this($$0.builtInRegistryHolder(), 1, DataComponentPatch.EMPTY);
   }

   private static DataResult<TransmuteResult> validate(TransmuteResult $$0) {
      return net.minecraft.world.item.ItemStack.validateStrict(new net.minecraft.world.item.ItemStack($$0.item, $$0.count, $$0.components)).map($$1 -> $$0);
   }

   public net.minecraft.world.item.ItemStack apply(net.minecraft.world.item.ItemStack $$0) {
      net.minecraft.world.item.ItemStack $$1 = $$0.transmuteCopy((ItemLike)this.item.value(), this.count);
      $$1.applyComponents(this.components);
      return $$1;
   }

   public boolean isResultUnchanged(net.minecraft.world.item.ItemStack $$0) {
      net.minecraft.world.item.ItemStack $$1 = this.apply($$0);
      return $$1.getCount() == 1 && net.minecraft.world.item.ItemStack.isSameItemSameComponents($$0, $$1);
   }

   public SlotDisplay display() {
      return new SlotDisplay.ItemStackSlotDisplay(new net.minecraft.world.item.ItemStack(this.item, this.count, this.components));
   }
}
