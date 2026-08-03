package net.minecraft.advancements.criterion;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
   private final ProblemReporter reporter;
   private final Provider lootData;

   public CriterionValidator(ProblemReporter $$0, Provider $$1) {
      this.reporter = $$0;
      this.lootData = $$1;
   }

   public void validateEntity(Optional<ContextAwarePredicate> $$0, String $$1) {
      $$0.ifPresent($$1x -> this.validateEntity($$1x, $$1));
   }

   public void validateEntities(List<ContextAwarePredicate> $$0, String $$1) {
      this.validate($$0, LootContextParamSets.ADVANCEMENT_ENTITY, $$1);
   }

   public void validateEntity(ContextAwarePredicate $$0, String $$1) {
      this.validate($$0, LootContextParamSets.ADVANCEMENT_ENTITY, $$1);
   }

   public void validate(ContextAwarePredicate $$0, ContextKeySet $$1, String $$2) {
      $$0.validate(new ValidationContext(this.reporter.forChild(new FieldPathElement($$2)), $$1, this.lootData));
   }

   public void validate(List<ContextAwarePredicate> $$0, ContextKeySet $$1, String $$2) {
      for (int $$3 = 0; $$3 < $$0.size(); $$3++) {
         ContextAwarePredicate $$4 = $$0.get($$3);
         $$4.validate(new ValidationContext(this.reporter.forChild(new IndexedFieldPathElement($$2, $$3)), $$1, this.lootData));
      }
   }
}
