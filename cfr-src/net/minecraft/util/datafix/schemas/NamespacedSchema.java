/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Const$PrimitiveType
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.PrimitiveCodec
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.Identifier;

public class NamespacedSchema
extends Schema {
    public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>(){

        public <T> DataResult<String> read(DynamicOps<T> $$0, T $$1) {
            return $$0.getStringValue($$1).map(NamespacedSchema::ensureNamespaced);
        }

        public <T> T write(DynamicOps<T> $$0, String $$1) {
            return (T)$$0.createString($$1);
        }

        public String toString() {
            return "NamespacedString";
        }

        public /* synthetic */ Object write(DynamicOps dynamicOps, Object object) {
            return this.write(dynamicOps, (String)object);
        }
    };
    private static final Type<String> NAMESPACED_STRING = new Const.PrimitiveType(NAMESPACED_STRING_CODEC);

    public NamespacedSchema(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public static String ensureNamespaced(String $$0) {
        Identifier $$1 = Identifier.tryParse($$0);
        if ($$1 != null) {
            return $$1.toString();
        }
        return $$0;
    }

    public static Type<String> namespacedString() {
        return NAMESPACED_STRING;
    }

    public Type<?> getChoiceType(DSL.TypeReference $$0, String $$1) {
        return super.getChoiceType($$0, NamespacedSchema.ensureNamespaced($$1));
    }
}

