package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.util.Util;

public class AnyOfCondition extends CompositeLootItemCondition {
   public static final MapCodec<AnyOfCondition> CODEC = createCodec(AnyOfCondition::new);

   AnyOfCondition(List<LootItemCondition> $$0) {
      super($$0, Util.anyOf($$0));
   }

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.ANY_OF;
   }

   public static AnyOfCondition.Builder anyOf(LootItemCondition.Builder... $$0) {
      return new AnyOfCondition.Builder($$0);
   }

   public static class Builder extends CompositeLootItemCondition.Builder {
      public Builder(LootItemCondition.Builder... $$0) {
         super($$0);
      }

      @Override
      public AnyOfCondition.Builder or(LootItemCondition.Builder $$0) {
         this.addTerm($$0);
         return this;
      }

      @Override
      protected LootItemCondition create(List<LootItemCondition> $$0) {
         return new AnyOfCondition($$0);
      }
   }
}
