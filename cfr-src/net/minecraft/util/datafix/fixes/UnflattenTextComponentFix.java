/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class UnflattenTextComponentFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public UnflattenTextComponentFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.TEXT_COMPONENT);
        Type $$1 = this.getOutputSchema().getType(References.TEXT_COMPONENT);
        return this.createFixer((Type<Pair<String, String>>)$$0, $$1);
    }

    private <T> TypeRewriteRule createFixer(Type<Pair<String, String>> $$0, Type<T> $$12) {
        return this.fixTypeEverywhere("UnflattenTextComponentFix", $$0, $$12, $$1 -> $$2 -> Util.readTypedOrThrow($$12, UnflattenTextComponentFix.unflattenJson($$1, (String)$$2.getSecond()), true).getValue());
    }

    private static <T> Dynamic<T> unflattenJson(DynamicOps<T> $$0, String $$1) {
        try {
            JsonElement $$2 = LenientJsonParser.parse($$1);
            if (!$$2.isJsonNull()) {
                return new Dynamic($$0, JsonOps.INSTANCE.convertTo($$0, $$2));
            }
        }
        catch (Exception $$3) {
            LOGGER.error("Failed to unflatten text component json: {}", (Object)$$1, (Object)$$3);
        }
        return new Dynamic($$0, $$0.createString($$1));
    }
}

