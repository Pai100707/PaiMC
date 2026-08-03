package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
   public static final MapCodec<SetContainerLootTable> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               LootTable.KEY_CODEC.fieldOf("name").forGetter($$0x -> $$0x.name),
               Codec.LONG.optionalFieldOf("seed", 0L).forGetter($$0x -> $$0x.seed),
               BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter($$0x -> $$0x.type)
            )
         )
         .apply($$0, SetContainerLootTable::new)
   );
   private final ResourceKey<LootTable> name;
   private final long seed;
   private final Holder<BlockEntityType<?>> type;

   private SetContainerLootTable(List<LootItemCondition> $$0, ResourceKey<LootTable> $$1, long $$2, Holder<BlockEntityType<?>> $$3) {
      super($$0);
      this.name = $$1;
      this.seed = $$2;
      this.type = $$3;
   }

   @Override
   public LootItemFunctionType<SetContainerLootTable> getType() {
      return LootItemFunctions.SET_LOOT_TABLE;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.isEmpty()) {
         return $$0;
      } else {
         $$0.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.name, this.seed));
         return $$0;
      }
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);
      if (!$$0.allowsReferences()) {
         $$0.reportProblem(new ValidationContext.ReferenceNotAllowedProblem(this.name));
      } else {
         if ($$0.resolver().get(this.name).isEmpty()) {
            $$0.reportProblem(new ValidationContext.MissingReferenceProblem(this.name));
         }
      }
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> $$0, ResourceKey<LootTable> $$1) {
      return simpleBuilder($$2 -> new SetContainerLootTable($$2, $$1, 0L, $$0.builtInRegistryHolder()));
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> $$0, ResourceKey<LootTable> $$1, long $$2) {
      return simpleBuilder($$3 -> new SetContainerLootTable($$3, $$1, $$2, $$0.builtInRegistryHolder()));
   }
}
