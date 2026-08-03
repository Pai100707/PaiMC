package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction extends LootItemConditionalFunction {
   private static final Codec<NumberProvider> COLOR_PROVIDER_CODEC = Codec.withAlternative(
      NumberProviders.CODEC, ExtraCodecs.RGB_COLOR_CODEC, ConstantValue::new
   );
   public static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ListOperation.StandAlone.codec(NumberProviders.CODEC, Integer.MAX_VALUE).optionalFieldOf("floats").forGetter($$0x -> $$0x.floats),
               ListOperation.StandAlone.codec(Codec.BOOL, Integer.MAX_VALUE).optionalFieldOf("flags").forGetter($$0x -> $$0x.flags),
               ListOperation.StandAlone.codec(Codec.STRING, Integer.MAX_VALUE).optionalFieldOf("strings").forGetter($$0x -> $$0x.strings),
               ListOperation.StandAlone.codec(COLOR_PROVIDER_CODEC, Integer.MAX_VALUE).optionalFieldOf("colors").forGetter($$0x -> $$0x.colors)
            )
         )
         .apply($$0, SetCustomModelDataFunction::new)
   );
   private final Optional<ListOperation.StandAlone<NumberProvider>> floats;
   private final Optional<ListOperation.StandAlone<Boolean>> flags;
   private final Optional<ListOperation.StandAlone<String>> strings;
   private final Optional<ListOperation.StandAlone<NumberProvider>> colors;

   public SetCustomModelDataFunction(
      List<LootItemCondition> $$0,
      Optional<ListOperation.StandAlone<NumberProvider>> $$1,
      Optional<ListOperation.StandAlone<Boolean>> $$2,
      Optional<ListOperation.StandAlone<String>> $$3,
      Optional<ListOperation.StandAlone<NumberProvider>> $$4
   ) {
      super($$0);
      this.floats = $$1;
      this.flags = $$2;
      this.strings = $$3;
      this.colors = $$4;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Stream.concat(this.floats.stream(), this.colors.stream())
         .flatMap($$0 -> $$0.value().stream())
         .flatMap($$0 -> $$0.getReferencedContextParams().stream())
         .collect(Collectors.toSet());
   }

   @Override
   public LootItemFunctionType<SetCustomModelDataFunction> getType() {
      return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
   }

   private static <T> List<T> apply(Optional<ListOperation.StandAlone<T>> $$0, List<T> $$1) {
      return $$0.<List<T>>map($$1x -> $$1x.apply($$1)).orElse($$1);
   }

   private static <T, E> List<E> apply(Optional<ListOperation.StandAlone<T>> $$0, List<E> $$1, Function<T, E> $$2) {
      return $$0.<List<E>>map($$2x -> {
         List<E> $$3 = $$2x.value().stream().map($$2).toList();
         return $$2x.operation().apply($$1, $$3);
      }).orElse($$1);
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      CustomModelData $$2 = (CustomModelData)$$0.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
      $$0.set(
         DataComponents.CUSTOM_MODEL_DATA,
         new CustomModelData(
            apply(this.floats, $$2.floats(), $$1x -> $$1x.getFloat($$1)),
            apply(this.flags, $$2.flags()),
            apply(this.strings, $$2.strings()),
            apply(this.colors, $$2.colors(), $$1x -> $$1x.getInt($$1))
         )
      );
      return $$0;
   }
}
