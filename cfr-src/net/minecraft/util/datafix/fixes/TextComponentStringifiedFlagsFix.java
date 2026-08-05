/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class TextComponentStringifiedFlagsFix
extends DataFix {
    public TextComponentStringifiedFlagsFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$02 = this.getInputSchema().getType(References.TEXT_COMPONENT);
        return this.fixTypeEverywhere("TextComponentStringyFlagsFix", $$02, $$0 -> $$02 -> $$02.mapSecond($$0 -> $$0.mapRight($$02 -> $$02.mapSecond($$0 -> $$0.mapSecond($$02 -> $$02.mapSecond($$0 -> $$0.update("bold", TextComponentStringifiedFlagsFix::stringToBool).update("italic", TextComponentStringifiedFlagsFix::stringToBool).update("underlined", TextComponentStringifiedFlagsFix::stringToBool).update("strikethrough", TextComponentStringifiedFlagsFix::stringToBool).update("obfuscated", TextComponentStringifiedFlagsFix::stringToBool)))))));
    }

    private static <T> Dynamic<T> stringToBool(Dynamic<T> $$0) {
        Optional $$1 = $$0.asString().result();
        if ($$1.isPresent()) {
            return $$0.createBoolean(Boolean.parseBoolean((String)$$1.get()));
        }
        return $$0;
    }
}

