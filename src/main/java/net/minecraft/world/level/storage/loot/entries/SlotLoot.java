package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SlotLoot extends LootPoolSingletonContainer {
   public static final MapCodec<SlotLoot> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(SlotSources.CODEC.fieldOf("slot_source").forGetter($$0x -> $$0x.slotSource)).and(singletonFields($$0)).apply($$0, SlotLoot::new)
   );
   private final SlotSource slotSource;

   private SlotLoot(SlotSource $$0, int $$1, int $$2, List<LootItemCondition> $$3, List<LootItemFunction> $$4) {
      super($$1, $$2, $$3, $$4);
      this.slotSource = $$0;
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.SLOTS;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
      this.slotSource.provide($$1).itemCopies().filter($$0x -> !$$0x.isEmpty()).forEach($$0);
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);
      this.slotSource.validate($$0.forChild(new FieldPathElement("slot_source")));
   }
}
