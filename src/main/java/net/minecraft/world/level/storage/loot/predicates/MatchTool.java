package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record MatchTool(Optional<ItemPredicate> predicate) implements LootItemCondition {
   public static final MapCodec<MatchTool> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchTool::predicate)).apply($$0, MatchTool::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.MATCH_TOOL;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext $$0) {
      ItemStack $$1 = $$0.getOptionalParameter(LootContextParams.TOOL);
      return $$1 != null && (this.predicate.isEmpty() || this.predicate.get().test($$1));
   }

   public static LootItemCondition.Builder toolMatches(net.minecraft.advancements.criterion.ItemPredicate.Builder $$0) {
      return () -> new MatchTool(Optional.of($$0.build()));
   }
}
