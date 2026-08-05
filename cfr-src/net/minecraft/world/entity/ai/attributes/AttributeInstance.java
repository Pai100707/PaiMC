/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jspecify.annotations.Nullable;

public class AttributeInstance {
    private final Holder<Attribute> attribute;
    private final Map<AttributeModifier.Operation, Map<Identifier, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<Identifier, AttributeModifier> modifierById = new Object2ObjectArrayMap();
    private final Map<Identifier, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;

    public AttributeInstance(Holder<Attribute> $$0, Consumer<AttributeInstance> $$1) {
        this.attribute = $$0;
        this.onDirty = $$1;
        this.baseValue = $$0.value().getDefaultValue();
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double $$0) {
        if ($$0 == this.baseValue) {
            return;
        }
        this.baseValue = $$0;
        this.setDirty();
    }

    @VisibleForTesting
    Map<Identifier, AttributeModifier> getModifiers(AttributeModifier.Operation $$02) {
        return this.modifiersByOperation.computeIfAbsent($$02, $$0 -> new Object2ObjectOpenHashMap());
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    public Set<AttributeModifier> getPermanentModifiers() {
        return ImmutableSet.copyOf(this.permanentModifiers.values());
    }

    public @Nullable AttributeModifier getModifier(Identifier $$0) {
        return this.modifierById.get($$0);
    }

    public boolean hasModifier(Identifier $$0) {
        return this.modifierById.get($$0) != null;
    }

    private void addModifier(AttributeModifier $$0) {
        AttributeModifier $$1 = this.modifierById.putIfAbsent($$0.id(), $$0);
        if ($$1 != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers($$0.operation()).put($$0.id(), $$0);
        this.setDirty();
    }

    public void addOrUpdateTransientModifier(AttributeModifier $$0) {
        AttributeModifier $$1 = this.modifierById.put($$0.id(), $$0);
        if ($$0 == $$1) {
            return;
        }
        this.getModifiers($$0.operation()).put($$0.id(), $$0);
        this.setDirty();
    }

    public void addTransientModifier(AttributeModifier $$0) {
        this.addModifier($$0);
    }

    public void addOrReplacePermanentModifier(AttributeModifier $$0) {
        this.removeModifier($$0.id());
        this.addModifier($$0);
        this.permanentModifiers.put($$0.id(), $$0);
    }

    public void addPermanentModifier(AttributeModifier $$0) {
        this.addModifier($$0);
        this.permanentModifiers.put($$0.id(), $$0);
    }

    public void addPermanentModifiers(Collection<AttributeModifier> $$0) {
        for (AttributeModifier $$1 : $$0) {
            this.addPermanentModifier($$1);
        }
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier $$0) {
        this.removeModifier($$0.id());
    }

    public boolean removeModifier(Identifier $$0) {
        AttributeModifier $$1 = this.modifierById.remove($$0);
        if ($$1 == null) {
            return false;
        }
        this.getModifiers($$1.operation()).remove($$0);
        this.permanentModifiers.remove($$0);
        this.setDirty();
        return true;
    }

    public void removeModifiers() {
        for (AttributeModifier $$0 : this.getModifiers()) {
            this.removeModifier($$0);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    private double calculateValue() {
        double $$0 = this.getBaseValue();
        for (AttributeModifier $$1 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
            $$0 += $$1.amount();
        }
        double $$2 = $$0;
        for (AttributeModifier $$3 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            $$2 += $$0 * $$3.amount();
        }
        for (AttributeModifier $$4 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            $$2 *= 1.0 + $$4.amount();
        }
        return this.attribute.value().sanitizeValue($$2);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation $$0) {
        return this.modifiersByOperation.getOrDefault($$0, Map.of()).values();
    }

    public void replaceFrom(AttributeInstance $$02) {
        this.baseValue = $$02.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll($$02.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.putAll($$02.permanentModifiers);
        this.modifiersByOperation.clear();
        $$02.modifiersByOperation.forEach(($$0, $$1) -> this.getModifiers((AttributeModifier.Operation)$$0).putAll((Map<Identifier, AttributeModifier>)$$1));
        this.setDirty();
    }

    public Packed pack() {
        return new Packed(this.attribute, this.baseValue, List.copyOf(this.permanentModifiers.values()));
    }

    public void apply(Packed $$0) {
        this.baseValue = $$0.baseValue;
        for (AttributeModifier $$1 : $$0.modifiers) {
            this.modifierById.put($$1.id(), $$1);
            this.getModifiers($$1.operation()).put($$1.id(), $$1);
            this.permanentModifiers.put($$1.id(), $$1);
        }
        this.setDirty();
    }

    public static final class Packed
    extends Record {
        private final Holder<Attribute> attribute;
        final double baseValue;
        final List<AttributeModifier> modifiers;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("id").forGetter(Packed::attribute), (App)Codec.DOUBLE.fieldOf("base").orElse((Object)0.0).forGetter(Packed::baseValue), (App)AttributeModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(Packed::modifiers)).apply((Applicative)$$0, Packed::new));
        public static final Codec<List<Packed>> LIST_CODEC = CODEC.listOf();

        public Packed(Holder<Attribute> $$0, double $$1, List<AttributeModifier> $$2) {
            this.attribute = $$0;
            this.baseValue = $$1;
            this.modifiers = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this, $$0);
        }

        public Holder<Attribute> attribute() {
            return this.attribute;
        }

        public double baseValue() {
            return this.baseValue;
        }

        public List<AttributeModifier> modifiers() {
            return this.modifiers;
        }
    }
}

