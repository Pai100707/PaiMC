package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.cow.MushroomCow.Variant;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public record VanillaShearingLoot(Provider registries) implements LootTableSubProvider {
   @Override
   public void generate(BiConsumer<ResourceKey<LootTable>, Builder> $$0) {
      $$0.accept(
         BuiltInLootTables.BOGGED_SHEAR,
         LootTable.lootTable()
            .withPool(
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(2.0F))
                  .add(LootItem.lootTableItem(Items.BROWN_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                  .add(LootItem.lootTableItem(Items.RED_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
            )
      );
      LootData.WOOL_ITEM_BY_DYE
         .forEach(
            ($$1, $$2) -> $$0.accept(
               (ResourceKey)BuiltInLootTables.SHEAR_SHEEP_BY_DYE.get($$1),
               LootTable.lootTable().withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F)).add(LootItem.lootTableItem($$2)))
            )
         );
      $$0.accept(
         BuiltInLootTables.SHEAR_SHEEP, LootTable.lootTable().withPool(EntityLootSubProvider.createSheepDispatchPool(BuiltInLootTables.SHEAR_SHEEP_BY_DYE))
      );
      $$0.accept(
         BuiltInLootTables.SHEAR_MOOSHROOM,
         LootTable.lootTable()
            .withPool(
               LootPool.lootPool()
                  .add(
                     AlternativesEntry.alternatives(
                        new net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder[]{
                           NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_RED_MOOSHROOM)
                              .when(
                                 LootItemEntityPropertyCondition.hasProperties(
                                    EntityTarget.THIS,
                                    net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                                       .components(
                                          net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                                             .exact(DataComponentExactPredicate.expect(DataComponents.MOOSHROOM_VARIANT, Variant.RED))
                                             .build()
                                       )
                                 )
                              ),
                           NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_BROWN_MOOSHROOM)
                              .when(
                                 LootItemEntityPropertyCondition.hasProperties(
                                    EntityTarget.THIS,
                                    net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                                       .components(
                                          net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                                             .exact(DataComponentExactPredicate.expect(DataComponents.MOOSHROOM_VARIANT, Variant.BROWN))
                                             .build()
                                       )
                                 )
                              )
                        }
                     )
                  )
            )
      );
      $$0.accept(
         BuiltInLootTables.SHEAR_RED_MOOSHROOM,
         LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0F)).add(LootItem.lootTableItem(Items.RED_MUSHROOM)))
      );
      $$0.accept(
         BuiltInLootTables.SHEAR_BROWN_MOOSHROOM,
         LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0F)).add(LootItem.lootTableItem(Items.BROWN_MUSHROOM)))
      );
      $$0.accept(
         BuiltInLootTables.SHEAR_SNOW_GOLEM,
         LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.CARVED_PUMPKIN)))
      );
   }
}
