package net.minecraft.world.item.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeSlotSource implements SlotSource {
   protected final List<SlotSource> terms;
   private final Function<LootContext, SlotCollection> compositeSlotSource;

   protected CompositeSlotSource(List<SlotSource> $$0) {
      this.terms = $$0;
      this.compositeSlotSource = SlotSources.group($$0);
   }

   protected static <T extends CompositeSlotSource> MapCodec<T> createCodec(Function<List<SlotSource>, T> $$0) {
      return RecordCodecBuilder.mapCodec($$1 -> $$1.group(SlotSources.CODEC.listOf().fieldOf("terms").forGetter($$0xx -> $$0xx.terms)).apply($$1, $$0));
   }

   protected static <T extends CompositeSlotSource> Codec<T> createInlineCodec(Function<List<SlotSource>, T> $$0) {
      return SlotSources.CODEC.listOf().xmap($$0, $$0x -> $$0x.terms);
   }

   @Override
   public abstract MapCodec<? extends CompositeSlotSource> codec();

   @Override
   public SlotCollection provide(LootContext $$0) {
      return this.compositeSlotSource.apply($$0);
   }

   public void validate(ValidationContext $$0) {
      SlotSource.super.validate($$0);

      for (int $$1 = 0; $$1 < this.terms.size(); $$1++) {
         this.terms.get($$1).validate($$0.forChild(new IndexedFieldPathElement("terms", $$1)));
      }
   }
}
