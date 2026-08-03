package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class SequenceFunction implements LootItemFunction {
   public static final MapCodec<SequenceFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LootItemFunctions.TYPED_CODEC.listOf().fieldOf("functions").forGetter($$0x -> $$0x.functions)).apply($$0, SequenceFunction::new)
   );
   public static final Codec<SequenceFunction> INLINE_CODEC = LootItemFunctions.TYPED_CODEC.listOf().xmap(SequenceFunction::new, $$0 -> $$0.functions);
   private final List<LootItemFunction> functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

   private SequenceFunction(List<LootItemFunction> $$0) {
      this.functions = $$0;
      this.compositeFunction = LootItemFunctions.compose($$0);
   }

   public static SequenceFunction of(List<LootItemFunction> $$0) {
      return new SequenceFunction(List.copyOf($$0));
   }

   public ItemStack apply(ItemStack $$0, LootContext $$1) {
      return this.compositeFunction.apply($$0, $$1);
   }

   @Override
   public void validate(ValidationContext $$0) {
      LootItemFunction.super.validate($$0);

      for (int $$1 = 0; $$1 < this.functions.size(); $$1++) {
         this.functions.get($$1).validate($$0.forChild(new IndexedFieldPathElement("functions", $$1)));
      }
   }

   @Override
   public LootItemFunctionType<SequenceFunction> getType() {
      return LootItemFunctions.SEQUENCE;
   }
}
