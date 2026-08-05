/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jspecify.annotations.Nullable;

public class AttributeMap {
    private final Map<Holder<Attribute>, AttributeInstance> attributes = new Object2ObjectOpenHashMap();
    private final Set<AttributeInstance> attributesToSync = new ObjectOpenHashSet();
    private final Set<AttributeInstance> attributesToUpdate = new ObjectOpenHashSet();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier $$0) {
        this.supplier = $$0;
    }

    private void onAttributeModified(AttributeInstance $$0) {
        this.attributesToUpdate.add($$0);
        if ($$0.getAttribute().value().isClientSyncable()) {
            this.attributesToSync.add($$0);
        }
    }

    public Set<AttributeInstance> getAttributesToSync() {
        return this.attributesToSync;
    }

    public Set<AttributeInstance> getAttributesToUpdate() {
        return this.attributesToUpdate;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return this.attributes.values().stream().filter($$0 -> $$0.getAttribute().value().isClientSyncable()).collect(Collectors.toList());
    }

    public @Nullable AttributeInstance getInstance(Holder<Attribute> $$02) {
        return this.attributes.computeIfAbsent($$02, $$0 -> this.supplier.createInstance(this::onAttributeModified, (Holder<Attribute>)$$0));
    }

    public boolean hasAttribute(Holder<Attribute> $$0) {
        return this.attributes.get($$0) != null || this.supplier.hasAttribute($$0);
    }

    public boolean hasModifier(Holder<Attribute> $$0, Identifier $$1) {
        AttributeInstance $$2 = this.attributes.get($$0);
        return $$2 != null ? $$2.getModifier($$1) != null : this.supplier.hasModifier($$0, $$1);
    }

    public double getValue(Holder<Attribute> $$0) {
        AttributeInstance $$1 = this.attributes.get($$0);
        return $$1 != null ? $$1.getValue() : this.supplier.getValue($$0);
    }

    public double getBaseValue(Holder<Attribute> $$0) {
        AttributeInstance $$1 = this.attributes.get($$0);
        return $$1 != null ? $$1.getBaseValue() : this.supplier.getBaseValue($$0);
    }

    public double getModifierValue(Holder<Attribute> $$0, Identifier $$1) {
        AttributeInstance $$2 = this.attributes.get($$0);
        return $$2 != null ? $$2.getModifier($$1).amount() : this.supplier.getModifierValue($$0, $$1);
    }

    public void addTransientAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> $$02) {
        $$02.forEach(($$0, $$1) -> {
            AttributeInstance $$2 = this.getInstance((Holder<Attribute>)$$0);
            if ($$2 != null) {
                $$2.removeModifier($$1.id());
                $$2.addTransientModifier((AttributeModifier)$$1);
            }
        });
    }

    public void removeAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> $$02) {
        $$02.asMap().forEach(($$0, $$12) -> {
            AttributeInstance $$2 = this.attributes.get($$0);
            if ($$2 != null) {
                $$12.forEach($$1 -> $$2.removeModifier($$1.id()));
            }
        });
    }

    public void assignAllValues(AttributeMap $$02) {
        $$02.attributes.values().forEach($$0 -> {
            AttributeInstance $$1 = this.getInstance($$0.getAttribute());
            if ($$1 != null) {
                $$1.replaceFrom((AttributeInstance)$$0);
            }
        });
    }

    public void assignBaseValues(AttributeMap $$02) {
        $$02.attributes.values().forEach($$0 -> {
            AttributeInstance $$1 = this.getInstance($$0.getAttribute());
            if ($$1 != null) {
                $$1.setBaseValue($$0.getBaseValue());
            }
        });
    }

    public void assignPermanentModifiers(AttributeMap $$02) {
        $$02.attributes.values().forEach($$0 -> {
            AttributeInstance $$1 = this.getInstance($$0.getAttribute());
            if ($$1 != null) {
                $$1.addPermanentModifiers($$0.getPermanentModifiers());
            }
        });
    }

    public boolean resetBaseValue(Holder<Attribute> $$0) {
        if (!this.supplier.hasAttribute($$0)) {
            return false;
        }
        AttributeInstance $$1 = this.attributes.get($$0);
        if ($$1 != null) {
            $$1.setBaseValue(this.supplier.getBaseValue($$0));
        }
        return true;
    }

    public List<AttributeInstance.Packed> pack() {
        ArrayList<AttributeInstance.Packed> $$0 = new ArrayList<AttributeInstance.Packed>(this.attributes.values().size());
        for (AttributeInstance $$1 : this.attributes.values()) {
            $$0.add($$1.pack());
        }
        return $$0;
    }

    public void apply(List<AttributeInstance.Packed> $$0) {
        for (AttributeInstance.Packed $$1 : $$0) {
            AttributeInstance $$2 = this.getInstance($$1.attribute());
            if ($$2 == null) continue;
            $$2.apply($$1);
        }
    }
}

