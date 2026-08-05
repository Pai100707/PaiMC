/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public abstract class AbstractPoiSectionFix
extends DataFix {
    private final String name;

    public AbstractPoiSectionFix(Schema $$0, String $$1) {
        super($$0, false);
        this.name = $$1;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = DSL.named((String)References.POI_CHUNK.typeName(), (Type)DSL.remainderType());
        if (!Objects.equals($$0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, $$0, $$02 -> $$0 -> $$0.mapSecond(this::cap));
    }

    private <T> Dynamic<T> cap(Dynamic<T> $$0) {
        return $$0.update("Sections", $$02 -> $$02.updateMapValues($$0 -> $$0.mapSecond(this::processSection)));
    }

    private Dynamic<?> processSection(Dynamic<?> $$0) {
        return $$0.update("Records", this::processSectionRecords);
    }

    private <T> Dynamic<T> processSectionRecords(Dynamic<T> $$0) {
        return (Dynamic)DataFixUtils.orElse($$0.asStreamOpt().result().map($$1 -> $$0.createList(this.processRecords((Stream)$$1))), $$0);
    }

    protected abstract <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> var1);
}

