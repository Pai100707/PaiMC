/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;

public class LegacyHoverEventFix
extends DataFix {
    public LegacyHoverEventFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.createFixer(this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT), $$0);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C> $$0, Type<H> $$12) {
        Type $$2 = DSL.named((String)References.TEXT_COMPONENT.typeName(), (Type)DSL.or((Type)DSL.or((Type)DSL.string(), (Type)DSL.list($$0)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"extra", (Type)DSL.list($$0))), (Type)DSL.optional((Type)DSL.field((String)"separator", $$0)), (Type)DSL.optional((Type)DSL.field((String)"hoverEvent", $$12)), (Type)DSL.remainderType())));
        if (!$$2.equals((Object)this.getInputSchema().getType(References.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf($$2) + " but got " + String.valueOf(this.getInputSchema().getType(References.TEXT_COMPONENT)));
        }
        return this.fixTypeEverywhere("LegacyHoverEventFix", $$2, $$1 -> $$12 -> $$12.mapSecond($$1 -> $$1.mapRight($$12 -> $$12.mapSecond($$1 -> $$1.mapSecond($$12 -> {
            Dynamic $$2 = (Dynamic)$$12.getSecond();
            Optional $$3 = $$2.get("hoverEvent").result();
            if ($$3.isEmpty()) {
                return $$12;
            }
            Optional $$4 = ((Dynamic)$$3.get()).get("value").result();
            if ($$4.isEmpty()) {
                return $$12;
            }
            String $$5 = ((Either)$$12.getFirst()).left().map(Pair::getFirst).orElse("");
            Pair $$6 = (Pair)this.fixHoverEvent($$12, $$5, (Dynamic)$$3.get());
            return $$12.mapFirst($$1 -> Either.left((Object)$$6));
        })))));
    }

    private <H> H fixHoverEvent(Type<H> $$0, String $$1, Dynamic<?> $$2) {
        if ("show_text".equals($$1)) {
            return LegacyHoverEventFix.fixShowTextHover($$0, $$2);
        }
        return LegacyHoverEventFix.createPlaceholderHover($$0, $$2);
    }

    private static <H> H fixShowTextHover(Type<H> $$0, Dynamic<?> $$1) {
        Dynamic $$2 = $$1.renameField("value", "contents");
        return (H)Util.readTypedOrThrow($$0, $$2).getValue();
    }

    private static <H> H createPlaceholderHover(Type<H> $$0, Dynamic<?> $$1) {
        JsonElement $$2 = (JsonElement)$$1.convert((DynamicOps)JsonOps.INSTANCE).getValue();
        Dynamic $$3 = new Dynamic((DynamicOps)JavaOps.INSTANCE, Map.of("action", "show_text", "contents", Map.of("text", "Legacy hoverEvent: " + GsonHelper.toStableString($$2))));
        return (H)Util.readTypedOrThrow($$0, $$3).getValue();
    }
}

