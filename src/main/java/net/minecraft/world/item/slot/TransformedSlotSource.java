package net.minecraft.world.item.slot;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class TransformedSlotSource implements SlotSource {
   protected final SlotSource slotSource;

   protected TransformedSlotSource(SlotSource $$0) {
      this.slotSource = $$0;
   }

   @Override
   public abstract MapCodec<? extends TransformedSlotSource> codec();

   protected static <T extends TransformedSlotSource> P1<Mu<T>, SlotSource> commonFields(Instance<T> $$0) {
      return $$0.group(SlotSources.CODEC.fieldOf("slot_source").forGetter($$0x -> $$0x.slotSource));
   }

   protected abstract SlotCollection transform(SlotCollection var1);

   @Override
   public final SlotCollection provide(LootContext $$0) {
      return this.transform(this.slotSource.provide($$0));
   }

   public void validate(ValidationContext $$0) {
      SlotSource.super.validate($$0);
      this.slotSource.validate($$0.forChild(new FieldPathElement("slot_source")));
   }
}
