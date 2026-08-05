/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.CompoundList$CompoundListType
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix
extends DataFix {
    public NewVillageFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        CompoundList.CompoundListType $$0 = DSL.compoundList((Type)DSL.string(), (Type)this.getInputSchema().getType(References.STRUCTURE_FEATURE));
        OpticFinder $$1 = $$0.finder();
        return this.cap($$0);
    }

    private <SF> TypeRewriteRule cap(CompoundList.CompoundListType<String, SF> $$02) {
        Type $$1 = this.getInputSchema().getType(References.CHUNK);
        Type $$2 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        OpticFinder $$3 = $$1.findField("Level");
        OpticFinder $$42 = $$3.type().findField("Structures");
        OpticFinder $$5 = $$42.type().findField("Starts");
        OpticFinder $$6 = $$02.finder();
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("NewVillageFix", $$1, $$4 -> $$4.updateTyped($$3, $$3 -> $$3.updateTyped($$42, $$2 -> $$2.updateTyped($$5, $$1 -> $$1.update($$6, $$03 -> $$03.stream().filter($$0 -> !Objects.equals($$0.getFirst(), "Village")).map($$02 -> $$02.mapFirst($$0 -> $$0.equals("New_Village") ? "Village" : $$0)).collect(Collectors.toList()))).update(DSL.remainderFinder(), $$02 -> $$02.update("References", $$0 -> {
            OpticFinder $$6 = $$0.get("New_Village").result();
            return ((Dynamic)DataFixUtils.orElse($$6.map($$1 -> $$0.remove("New_Village").set("Village", $$1)), (Object)$$0)).remove("Village");
        }))))), (TypeRewriteRule)this.fixTypeEverywhereTyped("NewVillageStartFix", $$2, $$0 -> $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("id", $$0 -> Objects.equals(NamespacedSchema.ensureNamespaced($$0.asString("")), "minecraft:new_village") ? $$0.createString("minecraft:village") : $$0))));
    }
}

