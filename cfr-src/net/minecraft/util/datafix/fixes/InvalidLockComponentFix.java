/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.datafix.fixes.DataComponentRemainderFix;
import org.jspecify.annotations.Nullable;

public class InvalidLockComponentFix
extends DataComponentRemainderFix {
    private static final Optional<String> INVALID_LOCK_CUSTOM_NAME = Optional.of("\"\"");

    public InvalidLockComponentFix(Schema $$0) {
        super($$0, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> $$0) {
        return InvalidLockComponentFix.fixLock($$0);
    }

    public static <T> @Nullable Dynamic<T> fixLock(Dynamic<T> $$0) {
        return InvalidLockComponentFix.isBrokenLock($$0) ? null : $$0;
    }

    private static <T> boolean isBrokenLock(Dynamic<T> $$0) {
        return InvalidLockComponentFix.isMapWithOneField($$0, "components", $$02 -> InvalidLockComponentFix.isMapWithOneField($$02, "minecraft:custom_name", $$0 -> $$0.asString().result().equals(INVALID_LOCK_CUSTOM_NAME)));
    }

    private static <T> boolean isMapWithOneField(Dynamic<T> $$0, String $$1, Predicate<Dynamic<T>> $$2) {
        Optional $$3 = $$0.getMapValues().result();
        if ($$3.isEmpty() || ((Map)$$3.get()).size() != 1) {
            return false;
        }
        return $$0.get($$1).result().filter($$2).isPresent();
    }
}

