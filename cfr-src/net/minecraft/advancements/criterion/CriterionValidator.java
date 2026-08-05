/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
    private final ProblemReporter reporter;
    private final HolderGetter.Provider lootData;

    public CriterionValidator(ProblemReporter $$0, HolderGetter.Provider $$1) {
        this.reporter = $$0;
        this.lootData = $$1;
    }

    public void validateEntity(Optional<ContextAwarePredicate> $$0, String $$12) {
        $$0.ifPresent($$1 -> this.validateEntity((ContextAwarePredicate)$$1, $$12));
    }

    public void validateEntities(List<ContextAwarePredicate> $$0, String $$1) {
        this.validate($$0, LootContextParamSets.ADVANCEMENT_ENTITY, $$1);
    }

    public void validateEntity(ContextAwarePredicate $$0, String $$1) {
        this.validate($$0, LootContextParamSets.ADVANCEMENT_ENTITY, $$1);
    }

    public void validate(ContextAwarePredicate $$0, ContextKeySet $$1, String $$2) {
        $$0.validate(new ValidationContext(this.reporter.forChild(new ProblemReporter.FieldPathElement($$2)), $$1, this.lootData));
    }

    public void validate(List<ContextAwarePredicate> $$0, ContextKeySet $$1, String $$2) {
        for (int $$3 = 0; $$3 < $$0.size(); ++$$3) {
            ContextAwarePredicate $$4 = $$0.get($$3);
            $$4.validate(new ValidationContext(this.reporter.forChild(new ProblemReporter.IndexedFieldPathElement($$2, $$3)), $$1, this.lootData));
        }
    }
}

