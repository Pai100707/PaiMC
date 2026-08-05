/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public final class PatchedDataComponentMap
implements DataComponentMap {
    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap $$0) {
        this($$0, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap $$0, Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1, boolean $$2) {
        this.prototype = $$0;
        this.patch = $$1;
        this.copyOnWrite = $$2;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap $$0, DataComponentPatch $$1) {
        if (PatchedDataComponentMap.isPatchSanitized($$0, $$1.map)) {
            return new PatchedDataComponentMap($$0, $$1.map, true);
        }
        PatchedDataComponentMap $$2 = new PatchedDataComponentMap($$0);
        $$2.applyPatch($$1);
        return $$2;
    }

    private static boolean isPatchSanitized(DataComponentMap $$0, Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1) {
        for (Map.Entry $$2 : Reference2ObjectMaps.fastIterable($$1)) {
            Object $$3 = $$0.get((DataComponentType)$$2.getKey());
            Optional $$4 = (Optional)$$2.getValue();
            if ($$4.isPresent() && $$4.get().equals($$3)) {
                return false;
            }
            if (!$$4.isEmpty() || $$3 != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        Optional $$1 = (Optional)this.patch.get($$0);
        if ($$1 != null) {
            return $$1.orElse(null);
        }
        return this.prototype.get($$0);
    }

    public boolean hasNonDefault(DataComponentType<?> $$0) {
        return this.patch.containsKey($$0);
    }

    public <T> @Nullable T set(DataComponentType<T> $$0, @Nullable T $$1) {
        Optional $$4;
        this.ensureMapOwnership();
        T $$2 = this.prototype.get($$0);
        if (Objects.equals($$1, $$2)) {
            Optional $$3 = (Optional)this.patch.remove($$0);
        } else {
            $$4 = (Optional)this.patch.put($$0, Optional.ofNullable($$1));
        }
        if ($$4 != null) {
            return $$4.orElse($$2);
        }
        return $$2;
    }

    public <T> @Nullable T set(TypedDataComponent<T> $$0) {
        return this.set($$0.type(), $$0.value());
    }

    public <T> @Nullable T remove(DataComponentType<? extends T> $$0) {
        Optional $$3;
        this.ensureMapOwnership();
        T $$1 = this.prototype.get($$0);
        if ($$1 != null) {
            Optional $$2 = (Optional)this.patch.put($$0, Optional.empty());
        } else {
            $$3 = (Optional)this.patch.remove($$0);
        }
        if ($$3 != null) {
            return $$3.orElse(null);
        }
        return $$1;
    }

    public void applyPatch(DataComponentPatch $$0) {
        this.ensureMapOwnership();
        for (Map.Entry $$1 : Reference2ObjectMaps.fastIterable($$0.map)) {
            this.applyPatch((DataComponentType)$$1.getKey(), (Optional)$$1.getValue());
        }
    }

    private void applyPatch(DataComponentType<?> $$0, Optional<?> $$1) {
        Object $$2 = this.prototype.get($$0);
        if ($$1.isPresent()) {
            if ($$1.get().equals($$2)) {
                this.patch.remove($$0);
            } else {
                this.patch.put($$0, $$1);
            }
        } else if ($$2 != null) {
            this.patch.put($$0, Optional.empty());
        } else {
            this.patch.remove($$0);
        }
    }

    public void restorePatch(DataComponentPatch $$0) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll($$0.map);
    }

    public void clearPatch() {
        this.ensureMapOwnership();
        this.patch.clear();
    }

    public void setAll(DataComponentMap $$0) {
        for (TypedDataComponent<?> $$1 : $$0) {
            $$1.applyTo(this);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        }
        ReferenceArraySet $$0 = new ReferenceArraySet(this.prototype.keySet());
        for (Reference2ObjectMap.Entry $$1 : Reference2ObjectMaps.fastIterable(this.patch)) {
            Optional $$2 = (Optional)$$1.getValue();
            if ($$2.isPresent()) {
                $$0.add((DataComponentType)$$1.getKey());
                continue;
            }
            $$0.remove($$1.getKey());
        }
        return $$0;
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        }
        ArrayList<TypedDataComponent> $$0 = new ArrayList<TypedDataComponent>(this.patch.size() + this.prototype.size());
        for (Reference2ObjectMap.Entry $$1 : Reference2ObjectMaps.fastIterable(this.patch)) {
            if (!((Optional)$$1.getValue()).isPresent()) continue;
            $$0.add(TypedDataComponent.createUnchecked((DataComponentType)$$1.getKey(), ((Optional)$$1.getValue()).get()));
        }
        for (TypedDataComponent $$2 : this.prototype) {
            if (this.patch.containsKey($$2.type())) continue;
            $$0.add($$2);
        }
        return $$0.iterator();
    }

    @Override
    public int size() {
        int $$0 = this.prototype.size();
        for (Reference2ObjectMap.Entry $$1 : Reference2ObjectMaps.fastIterable(this.patch)) {
            boolean $$3;
            boolean $$2 = ((Optional)$$1.getValue()).isPresent();
            if ($$2 == ($$3 = this.prototype.has((DataComponentType)$$1.getKey()))) continue;
            $$0 += $$2 ? 1 : -1;
        }
        return $$0;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        }
        this.copyOnWrite = true;
        return new DataComponentPatch(this.patch);
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    public DataComponentMap toImmutableMap() {
        if (this.patch.isEmpty()) {
            return this.prototype;
        }
        return this.copy();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof PatchedDataComponentMap)) return false;
        PatchedDataComponentMap $$1 = (PatchedDataComponentMap)$$0;
        if (!this.prototype.equals($$1.prototype)) return false;
        if (!this.patch.equals($$1.patch)) return false;
        return true;
    }

    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
    }
}

