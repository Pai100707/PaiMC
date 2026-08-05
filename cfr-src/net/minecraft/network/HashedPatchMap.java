/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HashedPatchMap(Map<DataComponentType<?>, Integer> addedComponents, Set<DataComponentType<?>> removedComponents) {
    public static final StreamCodec<RegistryFriendlyByteBuf, HashedPatchMap> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), ByteBufCodecs.INT, 256), HashedPatchMap::addedComponents, ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), 256), HashedPatchMap::removedComponents, HashedPatchMap::new);

    public static HashedPatchMap create(DataComponentPatch $$0, HashGenerator $$1) {
        DataComponentPatch.SplitResult $$22 = $$0.split();
        IdentityHashMap $$3 = new IdentityHashMap($$22.added().size());
        $$22.added().forEach($$2 -> $$3.put($$2.type(), (Integer)$$1.apply($$2)));
        return new HashedPatchMap($$3, $$22.removed());
    }

    public boolean matches(DataComponentPatch $$0, HashGenerator $$1) {
        DataComponentPatch.SplitResult $$2 = $$0.split();
        if (!$$2.removed().equals(this.removedComponents)) {
            return false;
        }
        if (this.addedComponents.size() != $$2.added().size()) {
            return false;
        }
        for (TypedDataComponent<?> $$3 : $$2.added()) {
            Integer $$4 = this.addedComponents.get($$3.type());
            if ($$4 == null) {
                return false;
            }
            Integer $$5 = (Integer)$$1.apply($$3);
            if ($$5.equals($$4)) continue;
            return false;
        }
        return true;
    }

    @FunctionalInterface
    public static interface HashGenerator
    extends Function<TypedDataComponent<?>, Integer> {
    }
}

