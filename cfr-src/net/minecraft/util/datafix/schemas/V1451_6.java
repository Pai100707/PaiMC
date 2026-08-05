/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1451_6
extends NamespacedSchema {
    public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
    protected static final Hook.HookFunction UNPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> $$02, T $$12) {
            Dynamic $$2 = new Dynamic($$02, $$12);
            return (T)((Dynamic)DataFixUtils.orElse($$2.get("CriteriaName").asString().result().map($$0 -> {
                int $$1 = $$0.indexOf(58);
                if ($$1 < 0) {
                    return Pair.of((Object)V1451_6.SPECIAL_OBJECTIVE_MARKER, (Object)$$0);
                }
                try {
                    Identifier $$2 = Identifier.bySeparator($$0.substring(0, $$1), '.');
                    Identifier $$3 = Identifier.bySeparator($$0.substring($$1 + 1), '.');
                    return Pair.of((Object)$$2.toString(), (Object)$$3.toString());
                }
                catch (Exception $$4) {
                    return Pair.of((Object)V1451_6.SPECIAL_OBJECTIVE_MARKER, (Object)$$0);
                }
            }).map($$1 -> $$2.set("CriteriaType", $$2.createMap((Map)ImmutableMap.of((Object)$$2.createString("type"), (Object)$$2.createString((String)$$1.getFirst()), (Object)$$2.createString("id"), (Object)$$2.createString((String)$$1.getSecond()))))), (Object)$$2)).getValue();
        }
    };
    protected static final Hook.HookFunction REPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> $$0, T $$12) {
            Dynamic $$2 = new Dynamic($$0, $$12);
            Optional<Dynamic> $$3 = $$2.get("CriteriaType").get().result().flatMap($$1 -> {
                Optional $$2 = $$1.get("type").asString().result();
                Optional $$3 = $$1.get("id").asString().result();
                if ($$2.isPresent() && $$3.isPresent()) {
                    String $$4 = (String)$$2.get();
                    if ($$4.equals(V1451_6.SPECIAL_OBJECTIVE_MARKER)) {
                        return Optional.of($$2.createString((String)$$3.get()));
                    }
                    return Optional.of($$1.createString(V1451_6.packNamespacedWithDot($$4) + ":" + V1451_6.packNamespacedWithDot((String)$$3.get())));
                }
                return Optional.empty();
            });
            return (T)((Dynamic)DataFixUtils.orElse($$3.map($$1 -> $$2.set("CriteriaName", $$1).remove("CriteriaType")), (Object)$$2)).getValue();
        }
    };

    public V1451_6(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        Supplier<TypeTemplate> $$3 = () -> DSL.compoundList((TypeTemplate)References.ITEM_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()));
        $$0.registerType(false, References.STATS, () -> DSL.optionalFields((String)"stats", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"minecraft:mined", (Object)DSL.compoundList((TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:crafted", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:used", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:broken", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:picked_up", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:dropped", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:killed", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:killed_by", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:custom", (Object)DSL.compoundList((TypeTemplate)DSL.constType(V1451_6.namespacedString()), (TypeTemplate)DSL.constType((Type)DSL.intType())))})));
        Map<String, Supplier<TypeTemplate>> $$4 = V1451_6.createCriterionTypes($$0);
        $$0.registerType(false, References.OBJECTIVE, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"CriteriaType", (TypeTemplate)DSL.taggedChoiceLazy((String)"type", (Type)DSL.string(), (Map)$$4), (String)"DisplayName", (TypeTemplate)References.TEXT_COMPONENT.in($$0)), (Hook.HookFunction)UNPACK_OBJECTIVE_ID, (Hook.HookFunction)REPACK_OBJECTIVE_ID));
    }

    protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema $$0) {
        Supplier<TypeTemplate> $$1 = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in($$0));
        Supplier<TypeTemplate> $$2 = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.BLOCK_NAME.in($$0));
        Supplier<TypeTemplate> $$3 = () -> DSL.optionalFields((String)"id", (TypeTemplate)References.ENTITY_NAME.in($$0));
        HashMap $$4 = Maps.newHashMap();
        $$4.put("minecraft:mined", $$2);
        $$4.put("minecraft:crafted", $$1);
        $$4.put("minecraft:used", $$1);
        $$4.put("minecraft:broken", $$1);
        $$4.put("minecraft:picked_up", $$1);
        $$4.put("minecraft:dropped", $$1);
        $$4.put("minecraft:killed", $$3);
        $$4.put("minecraft:killed_by", $$3);
        $$4.put("minecraft:custom", () -> DSL.optionalFields((String)"id", (TypeTemplate)DSL.constType(V1451_6.namespacedString())));
        $$4.put(SPECIAL_OBJECTIVE_MARKER, () -> DSL.optionalFields((String)"id", (TypeTemplate)DSL.constType((Type)DSL.string())));
        return $$4;
    }

    public static String packNamespacedWithDot(String $$0) {
        Identifier $$1 = Identifier.tryParse($$0);
        return $$1 != null ? $$1.getNamespace() + "." + $$1.getPath() : $$0;
    }
}

