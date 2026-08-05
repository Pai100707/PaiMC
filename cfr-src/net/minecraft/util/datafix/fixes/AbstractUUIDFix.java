/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractUUIDFix
extends DataFix {
    protected DSL.TypeReference typeReference;

    public AbstractUUIDFix(Schema $$0, DSL.TypeReference $$1) {
        super($$0, false);
        this.typeReference = $$1;
    }

    protected Typed<?> updateNamedChoice(Typed<?> $$0, String $$12, Function<Dynamic<?>, Dynamic<?>> $$2) {
        Type $$3 = this.getInputSchema().getChoiceType(this.typeReference, $$12);
        Type $$4 = this.getOutputSchema().getChoiceType(this.typeReference, $$12);
        return $$0.updateTyped(DSL.namedChoice((String)$$12, (Type)$$3), $$4, $$1 -> $$1.update(DSL.remainderFinder(), $$2));
    }

    protected static Optional<Dynamic<?>> replaceUUIDString(Dynamic<?> $$0, String $$1, String $$2) {
        return AbstractUUIDFix.createUUIDFromString($$0, $$1).map($$3 -> $$0.remove($$1).set($$2, $$3));
    }

    protected static Optional<Dynamic<?>> replaceUUIDMLTag(Dynamic<?> $$0, String $$1, String $$2) {
        return $$0.get($$1).result().flatMap(AbstractUUIDFix::createUUIDFromML).map($$3 -> $$0.remove($$1).set($$2, $$3));
    }

    protected static Optional<Dynamic<?>> replaceUUIDLeastMost(Dynamic<?> $$0, String $$1, String $$2) {
        String $$3 = $$1 + "Most";
        String $$42 = $$1 + "Least";
        return AbstractUUIDFix.createUUIDFromLongs($$0, $$3, $$42).map($$4 -> $$0.remove($$3).remove($$42).set($$2, $$4));
    }

    protected static Optional<Dynamic<?>> createUUIDFromString(Dynamic<?> $$0, String $$12) {
        return $$0.get($$12).result().flatMap($$1 -> {
            String $$2 = $$1.asString(null);
            if ($$2 != null) {
                try {
                    UUID $$3 = UUID.fromString($$2);
                    return AbstractUUIDFix.createUUIDTag($$0, $$3.getMostSignificantBits(), $$3.getLeastSignificantBits());
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            return Optional.empty();
        });
    }

    protected static Optional<Dynamic<?>> createUUIDFromML(Dynamic<?> $$0) {
        return AbstractUUIDFix.createUUIDFromLongs($$0, "M", "L");
    }

    protected static Optional<Dynamic<?>> createUUIDFromLongs(Dynamic<?> $$0, String $$1, String $$2) {
        long $$3 = $$0.get($$1).asLong(0L);
        long $$4 = $$0.get($$2).asLong(0L);
        if ($$3 == 0L || $$4 == 0L) {
            return Optional.empty();
        }
        return AbstractUUIDFix.createUUIDTag($$0, $$3, $$4);
    }

    protected static Optional<Dynamic<?>> createUUIDTag(Dynamic<?> $$0, long $$1, long $$2) {
        return Optional.of($$0.createIntList(Arrays.stream(new int[]{(int)($$1 >> 32), (int)$$1, (int)($$2 >> 32), (int)$$2})));
    }
}

