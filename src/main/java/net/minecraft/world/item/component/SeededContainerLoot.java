package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record SeededContainerLoot(ResourceKey<LootTable> lootTable, long seed) implements TooltipProvider {
   private static final Component UNKNOWN_CONTENTS = Component.translatable("item.container.loot_table.unknown");
   public static final Codec<SeededContainerLoot> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(SeededContainerLoot::lootTable),
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(SeededContainerLoot::seed)
         )
         .apply($$0, SeededContainerLoot::new)
   );

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      $$1.accept(UNKNOWN_CONTENTS);
   }
}
