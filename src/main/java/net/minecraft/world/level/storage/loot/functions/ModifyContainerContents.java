package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModifyContainerContents extends LootItemConditionalFunction {
   public static final MapCodec<ModifyContainerContents> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ContainerComponentManipulators.CODEC.fieldOf("component").forGetter($$0x -> $$0x.component),
               LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter($$0x -> $$0x.modifier)
            )
         )
         .apply($$0, ModifyContainerContents::new)
   );
   private final ContainerComponentManipulator<?> component;
   private final LootItemFunction modifier;

   private ModifyContainerContents(List<LootItemCondition> $$0, ContainerComponentManipulator<?> $$1, LootItemFunction $$2) {
      super($$0);
      this.component = $$1;
      this.modifier = $$2;
   }

   @Override
   public LootItemFunctionType<ModifyContainerContents> getType() {
      return LootItemFunctions.MODIFY_CONTENTS;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.isEmpty()) {
         return $$0;
      } else {
         this.component.modifyItems($$0, $$1x -> this.modifier.apply($$1x, $$1));
         return $$0;
      }
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);
      this.modifier.validate($$0.forChild(new FieldPathElement("modifier")));
   }
}
