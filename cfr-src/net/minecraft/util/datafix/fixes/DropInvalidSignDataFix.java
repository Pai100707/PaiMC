/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.BlockEntitySignDoubleSidedEditableTextFix;
import net.minecraft.util.datafix.fixes.References;

public class DropInvalidSignDataFix
extends DataFix {
    private final String entityName;

    public DropInvalidSignDataFix(Schema $$0, String $$1) {
        super($$0, false);
        this.entityName = $$1;
    }

    private <T> Dynamic<T> fix(Dynamic<T> $$0) {
        $$0 = $$0.update("front_text", DropInvalidSignDataFix::fixText);
        $$0 = $$0.update("back_text", DropInvalidSignDataFix::fixText);
        for (String $$1 : BlockEntitySignDoubleSidedEditableTextFix.FIELDS_TO_DROP) {
            $$0 = $$0.remove($$1);
        }
        return $$0;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> $$0) {
        Optional $$1 = $$0.get("filtered_messages").asStreamOpt().result();
        if ($$1.isEmpty()) {
            return $$0;
        }
        Dynamic $$22 = LegacyComponentDataFixUtils.createEmptyComponent($$0.getOps());
        List<Dynamic> $$32 = $$0.get("messages").asStreamOpt().result().orElse(Stream.of(new Dynamic[0])).toList();
        List $$4 = Streams.mapWithIndex((Stream)((Stream)$$1.get()), ($$2, $$3) -> {
            Dynamic $$4 = $$3 < (long)$$32.size() ? (Dynamic)$$32.get((int)$$3) : $$22;
            return $$2.equals((Object)$$22) ? $$4 : $$2;
        }).toList();
        if ($$4.equals($$32)) {
            return $$0.remove("filtered_messages");
        }
        return $$0.set("filtered_messages", $$0.createList($$4.stream()));
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type $$1 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, this.entityName);
        OpticFinder $$22 = DSL.namedChoice((String)this.entityName, (Type)$$1);
        return this.fixTypeEverywhereTyped("DropInvalidSignDataFix for " + this.entityName, $$0, $$2 -> $$2.updateTyped($$22, $$1, $$1 -> {
            Object $$2 = ((Dynamic)$$1.get(DSL.remainderFinder())).get("_filtered_correct").asBoolean(false);
            if ($$2) {
                return $$1.update(DSL.remainderFinder(), $$0 -> $$0.remove("_filtered_correct"));
            }
            return Util.writeAndReadTypedOrThrow($$1, $$1, this::fix);
        }));
    }
}

