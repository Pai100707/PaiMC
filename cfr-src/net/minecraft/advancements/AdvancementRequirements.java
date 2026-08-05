/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(FriendlyByteBuf $$02) {
        this($$02.readList($$0 -> $$0.readList(FriendlyByteBuf::readUtf)));
    }

    public void write(FriendlyByteBuf $$02) {
        $$02.writeCollection(this.requirements, ($$0, $$1) -> $$0.writeCollection($$1, FriendlyByteBuf::writeUtf));
    }

    public static AdvancementRequirements allOf(Collection<String> $$0) {
        return new AdvancementRequirements($$0.stream().map(List::of).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> $$0) {
        return new AdvancementRequirements(List.of(List.copyOf($$0)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> $$0) {
        if (this.requirements.isEmpty()) {
            return false;
        }
        for (List<String> $$1 : this.requirements) {
            if (AdvancementRequirements.anyMatch($$1, $$0)) continue;
            return false;
        }
        return true;
    }

    public int count(Predicate<String> $$0) {
        int $$1 = 0;
        for (List<String> $$2 : this.requirements) {
            if (!AdvancementRequirements.anyMatch($$2, $$0)) continue;
            ++$$1;
        }
        return $$1;
    }

    private static boolean anyMatch(List<String> $$0, Predicate<String> $$1) {
        for (String $$2 : $$0) {
            if (!$$1.test($$2)) continue;
            return true;
        }
        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> $$0) {
        ObjectOpenHashSet $$1 = new ObjectOpenHashSet();
        for (List<String> $$2 : this.requirements) {
            if ($$2.isEmpty() && $$0.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }
            $$1.addAll($$2);
        }
        if (!$$0.equals($$1)) {
            Sets.SetView $$3 = Sets.difference($$0, (Set)$$1);
            Sets.SetView $$4 = Sets.difference((Set)$$1, $$0);
            return DataResult.error(() -> AdvancementRequirements.lambda$validate$3((Set)$$3, (Set)$$4));
        }
        return DataResult.success((Object)this);
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        ObjectOpenHashSet $$0 = new ObjectOpenHashSet();
        for (List<String> $$1 : this.requirements) {
            $$0.addAll($$1);
        }
        return $$0;
    }

    private static /* synthetic */ String lambda$validate$3(Set $$0, Set $$1) {
        return "Advancement completion requirements did not exactly match specified criteria. Missing: " + String.valueOf($$0) + ". Unknown: " + String.valueOf($$1);
    }

    public static interface Strategy {
        public static final Strategy AND = AdvancementRequirements::allOf;
        public static final Strategy OR = AdvancementRequirements::anyOf;

        public AdvancementRequirements create(Collection<String> var1);
    }
}

