package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips extends LootItemConditionalFunction {
   public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(Codec.unboundedMap(DataComponentType.CODEC, Codec.BOOL).fieldOf("toggles").forGetter($$0x -> $$0x.values))
         .apply($$0, ToggleTooltips::new)
   );
   private final Map<DataComponentType<?>, Boolean> values;

   private ToggleTooltips(List<LootItemCondition> $$0, Map<DataComponentType<?>, Boolean> $$1) {
      super($$0);
      this.values = $$1;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, $$0x -> {
         for (Entry<DataComponentType<?>, Boolean> $$1x : this.values.entrySet()) {
            boolean $$2 = $$1x.getValue();
            $$0x = $$0x.withHidden($$1x.getKey(), !$$2);
         }

         return $$0x;
      });
      return $$0;
   }

   @Override
   public LootItemFunctionType<ToggleTooltips> getType() {
      return LootItemFunctions.TOGGLE_TOOLTIPS;
   }
}
