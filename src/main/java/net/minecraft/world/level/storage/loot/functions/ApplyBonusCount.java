package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount extends LootItemConditionalFunction {
   private static final Map<Identifier, ApplyBonusCount.FormulaType> FORMULAS = Stream.of(
         ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.UniformBonusCount.TYPE
      )
      .collect(Collectors.toMap(ApplyBonusCount.FormulaType::id, Function.identity()));
   private static final Codec<ApplyBonusCount.FormulaType> FORMULA_TYPE_CODEC = Identifier.CODEC.comapFlatMap($$0 -> {
      ApplyBonusCount.FormulaType $$1 = FORMULAS.get($$0);
      return $$1 != null ? DataResult.success($$1) : DataResult.error(() -> "No formula type with id: '" + $$0 + "'");
   }, ApplyBonusCount.FormulaType::id);
   private static final MapCodec<ApplyBonusCount.Formula> FORMULA_CODEC = ExtraCodecs.dispatchOptionalValue(
      "formula", "parameters", FORMULA_TYPE_CODEC, ApplyBonusCount.Formula::getType, ApplyBonusCount.FormulaType::codec
   );
   public static final MapCodec<ApplyBonusCount> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and($$0.group(Enchantment.CODEC.fieldOf("enchantment").forGetter($$0x -> $$0x.enchantment), FORMULA_CODEC.forGetter($$0x -> $$0x.formula)))
         .apply($$0, ApplyBonusCount::new)
   );
   private final Holder<Enchantment> enchantment;
   private final ApplyBonusCount.Formula formula;

   private ApplyBonusCount(List<LootItemCondition> $$0, Holder<Enchantment> $$1, ApplyBonusCount.Formula $$2) {
      super($$0);
      this.enchantment = $$1;
      this.formula = $$2;
   }

   @Override
   public LootItemFunctionType<ApplyBonusCount> getType() {
      return LootItemFunctions.APPLY_BONUS;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.TOOL);
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      ItemStack $$2 = $$1.getOptionalParameter(LootContextParams.TOOL);
      if ($$2 != null) {
         int $$3 = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, $$2);
         int $$4 = this.formula.calculateNewCount($$1.getRandom(), $$0.getCount(), $$3);
         $$0.setCount($$4);
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Holder<Enchantment> $$0, float $$1, int $$2) {
      return simpleBuilder($$3 -> new ApplyBonusCount($$3, $$0, new ApplyBonusCount.BinomialWithBonusCount($$2, $$1)));
   }

   public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Holder<Enchantment> $$0) {
      return simpleBuilder($$1 -> new ApplyBonusCount($$1, $$0, ApplyBonusCount.OreDrops.INSTANCE));
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> $$0) {
      return simpleBuilder($$1 -> new ApplyBonusCount($$1, $$0, new ApplyBonusCount.UniformBonusCount(1)));
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> $$0, int $$1) {
      return simpleBuilder($$2 -> new ApplyBonusCount($$2, $$0, new ApplyBonusCount.UniformBonusCount($$1)));
   }

   record BinomialWithBonusCount(int extraRounds, float probability) implements ApplyBonusCount.Formula {
      private static final Codec<ApplyBonusCount.BinomialWithBonusCount> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.INT.fieldOf("extra").forGetter(ApplyBonusCount.BinomialWithBonusCount::extraRounds),
               Codec.FLOAT.fieldOf("probability").forGetter(ApplyBonusCount.BinomialWithBonusCount::probability)
            )
            .apply($$0, ApplyBonusCount.BinomialWithBonusCount::new)
      );
      public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(
         Identifier.withDefaultNamespace("binomial_with_bonus_count"), CODEC
      );

      @Override
      public int calculateNewCount(RandomSource $$0, int $$1, int $$2) {
         for (int $$3 = 0; $$3 < $$2 + this.extraRounds; $$3++) {
            if ($$0.nextFloat() < this.probability) {
               $$1++;
            }
         }

         return $$1;
      }

      @Override
      public ApplyBonusCount.FormulaType getType() {
         return TYPE;
      }
   }

   interface Formula {
      int calculateNewCount(RandomSource var1, int var2, int var3);

      ApplyBonusCount.FormulaType getType();
   }

   record FormulaType(Identifier id, Codec<? extends ApplyBonusCount.Formula> codec) {
   }

   record OreDrops() implements ApplyBonusCount.Formula {
      public static final ApplyBonusCount.OreDrops INSTANCE = new ApplyBonusCount.OreDrops();
      public static final Codec<ApplyBonusCount.OreDrops> CODEC = MapCodec.unitCodec(INSTANCE);
      public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(Identifier.withDefaultNamespace("ore_drops"), CODEC);

      @Override
      public int calculateNewCount(RandomSource $$0, int $$1, int $$2) {
         if ($$2 > 0) {
            int $$3 = $$0.nextInt($$2 + 2) - 1;
            if ($$3 < 0) {
               $$3 = 0;
            }

            return $$1 * ($$3 + 1);
         } else {
            return $$1;
         }
      }

      @Override
      public ApplyBonusCount.FormulaType getType() {
         return TYPE;
      }
   }

   record UniformBonusCount(int bonusMultiplier) implements ApplyBonusCount.Formula {
      public static final Codec<ApplyBonusCount.UniformBonusCount> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(Codec.INT.fieldOf("bonusMultiplier").forGetter(ApplyBonusCount.UniformBonusCount::bonusMultiplier))
            .apply($$0, ApplyBonusCount.UniformBonusCount::new)
      );
      public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(Identifier.withDefaultNamespace("uniform_bonus_count"), CODEC);

      @Override
      public int calculateNewCount(RandomSource $$0, int $$1, int $$2) {
         return $$1 + $$0.nextInt(this.bonusMultiplier * $$2 + 1);
      }

      @Override
      public ApplyBonusCount.FormulaType getType() {
         return TYPE;
      }
   }
}
