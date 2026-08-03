package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record BonusLevelTableCondition(Holder<Enchantment> enchantment, List<Float> values) implements LootItemCondition {
   public static final MapCodec<BonusLevelTableCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(BonusLevelTableCondition::enchantment),
            ExtraCodecs.nonEmptyList(Codec.FLOAT.listOf()).fieldOf("chances").forGetter(BonusLevelTableCondition::values)
         )
         .apply($$0, BonusLevelTableCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.TABLE_BONUS;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext $$0) {
      ItemStack $$1 = $$0.getOptionalParameter(LootContextParams.TOOL);
      int $$2 = $$1 != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, $$1) : 0;
      float $$3 = this.values.get(Math.min($$2, this.values.size() - 1));
      return $$0.getRandom().nextFloat() < $$3;
   }

   public static LootItemCondition.Builder bonusLevelFlatChance(Holder<Enchantment> $$0, float... $$1) {
      List<Float> $$2 = new ArrayList<>($$1.length);

      for (float $$3 : $$1) {
         $$2.add($$3);
      }

      return () -> new BonusLevelTableCondition($$0, $$2);
   }
}
