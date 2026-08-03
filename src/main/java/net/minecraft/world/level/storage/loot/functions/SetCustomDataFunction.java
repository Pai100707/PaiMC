package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetCustomDataFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(TagParser.LENIENT_CODEC.fieldOf("tag").forGetter($$0x -> $$0x.tag)).apply($$0, SetCustomDataFunction::new)
   );
   private final CompoundTag tag;

   private SetCustomDataFunction(List<LootItemCondition> $$0, CompoundTag $$1) {
      super($$0);
      this.tag = $$1;
   }

   @Override
   public LootItemFunctionType<SetCustomDataFunction> getType() {
      return LootItemFunctions.SET_CUSTOM_DATA;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      CustomData.update(DataComponents.CUSTOM_DATA, $$0, $$0x -> $$0x.merge(this.tag));
      return $$0;
   }

   @Deprecated
   public static LootItemConditionalFunction.Builder<?> setCustomData(CompoundTag $$0) {
      return simpleBuilder($$1 -> new SetCustomDataFunction($$1, $$0));
   }
}
