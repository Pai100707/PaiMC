package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.LevelBasedValue.Linear;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemRandomChanceWithEnchantedBonusCondition(float unenchantedChance, LevelBasedValue enchantedChance, Holder<Enchantment> enchantment)
   implements LootItemCondition {
   public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("unenchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::unenchantedChance),
            LevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantedChance),
            Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantment)
         )
         .apply($$0, LootItemRandomChanceWithEnchantedBonusCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.ATTACKING_ENTITY);
   }

   public boolean test(LootContext $$0) {
      Entity $$1 = $$0.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
      int $$3 = $$1 instanceof LivingEntity $$2 ? EnchantmentHelper.getEnchantmentLevel(this.enchantment, $$2) : 0;
      float $$4 = $$3 > 0 ? this.enchantedChance.calculate($$3) : this.unenchantedChance;
      return $$0.getRandom().nextFloat() < $$4;
   }

   public static LootItemCondition.Builder randomChanceAndLootingBoost(Provider $$0, float $$1, float $$2) {
      RegistryLookup<Enchantment> $$3 = $$0.lookupOrThrow(Registries.ENCHANTMENT);
      return () -> new LootItemRandomChanceWithEnchantedBonusCondition($$1, new Linear($$1 + $$2, $$2), $$3.getOrThrow(Enchantments.LOOTING));
   }
}
