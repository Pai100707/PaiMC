/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ContextAwarePredicate {
    public static final Codec<ContextAwarePredicate> CODEC = LootItemCondition.DIRECT_CODEC.listOf().xmap(ContextAwarePredicate::new, $$0 -> $$0.conditions);
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> $$0) {
        this.conditions = $$0;
        this.compositePredicates = Util.allOf($$0);
    }

    public static ContextAwarePredicate create(LootItemCondition ... $$0) {
        return new ContextAwarePredicate(List.of($$0));
    }

    public boolean matches(LootContext $$0) {
        return this.compositePredicates.test($$0);
    }

    public void validate(ValidationContext $$0) {
        for (int $$1 = 0; $$1 < this.conditions.size(); ++$$1) {
            LootItemCondition $$2 = this.conditions.get($$1);
            $$2.validate($$0.forChild(new ProblemReporter.IndexedPathElement($$1)));
        }
    }
}

