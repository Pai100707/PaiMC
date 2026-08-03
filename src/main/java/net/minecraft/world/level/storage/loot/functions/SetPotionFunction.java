package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetPotionFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(Potion.CODEC.fieldOf("id").forGetter($$0x -> $$0x.potion)).apply($$0, SetPotionFunction::new)
   );
   private final Holder<Potion> potion;

   private SetPotionFunction(List<LootItemCondition> $$0, Holder<Potion> $$1) {
      super($$0);
      this.potion = $$1;
   }

   @Override
   public LootItemFunctionType<SetPotionFunction> getType() {
      return LootItemFunctions.SET_POTION;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, this.potion, PotionContents::withPotion);
      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> setPotion(Holder<Potion> $$0) {
      return simpleBuilder($$1 -> new SetPotionFunction($$1, $$0));
   }
}
