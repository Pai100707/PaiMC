package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetInstrumentFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter($$0x -> $$0x.options))
         .apply($$0, SetInstrumentFunction::new)
   );
   private final TagKey<Instrument> options;

   private SetInstrumentFunction(List<LootItemCondition> $$0, TagKey<Instrument> $$1) {
      super($$0);
      this.options = $$1;
   }

   @Override
   public LootItemFunctionType<SetInstrumentFunction> getType() {
      return LootItemFunctions.SET_INSTRUMENT;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      Registry<Instrument> $$2 = $$1.getLevel().registryAccess().lookupOrThrow(Registries.INSTRUMENT);
      Optional<Holder<Instrument>> $$3 = $$2.getRandomElementOf(this.options, $$1.getRandom());
      if ($$3.isPresent()) {
         $$0.set(DataComponents.INSTRUMENT, new InstrumentComponent($$3.get()));
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> $$0) {
      return simpleBuilder($$1 -> new SetInstrumentFunction($$1, $$0));
   }
}
