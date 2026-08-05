/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SaddleEquipmentSlotFix
extends DataFix {
    private static final Set<String> ENTITIES_WITH_SADDLE_ITEM = Set.of("minecraft:horse", "minecraft:skeleton_horse", "minecraft:zombie_horse", "minecraft:donkey", "minecraft:mule", "minecraft:camel", "minecraft:llama", "minecraft:trader_llama");
    private static final Set<String> ENTITIES_WITH_SADDLE_FLAG = Set.of("minecraft:pig", "minecraft:strider");
    private static final String SADDLE_FLAG = "Saddle";
    private static final String NEW_SADDLE = "saddle";

    public SaddleEquipmentSlotFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType $$0 = this.getInputSchema().findChoiceType(References.ENTITY);
        OpticFinder $$1 = DSL.typeFinder((Type)$$0);
        Type $$2 = this.getInputSchema().getType(References.ENTITY);
        Type $$32 = this.getOutputSchema().getType(References.ENTITY);
        Type<?> $$4 = ExtraDataFixUtils.patchSubType($$2, $$2, $$32);
        return this.fixTypeEverywhereTyped("SaddleEquipmentSlotFix", $$2, $$32, $$3 -> {
            String $$4 = $$3.getOptional($$1).map(Pair::getFirst).map(NamespacedSchema::ensureNamespaced).orElse("");
            Typed $$5 = ExtraDataFixUtils.cast($$4, $$3);
            if (ENTITIES_WITH_SADDLE_ITEM.contains($$4)) {
                return Util.writeAndReadTypedOrThrow($$5, $$32, SaddleEquipmentSlotFix::fixEntityWithSaddleItem);
            }
            if (ENTITIES_WITH_SADDLE_FLAG.contains($$4)) {
                return Util.writeAndReadTypedOrThrow($$5, $$32, SaddleEquipmentSlotFix::fixEntityWithSaddleFlag);
            }
            return ExtraDataFixUtils.cast($$32, $$3);
        });
    }

    private static Dynamic<?> fixEntityWithSaddleItem(Dynamic<?> $$0) {
        if ($$0.get("SaddleItem").result().isEmpty()) {
            return $$0;
        }
        return SaddleEquipmentSlotFix.fixDropChances($$0.renameField("SaddleItem", NEW_SADDLE));
    }

    private static Dynamic<?> fixEntityWithSaddleFlag(Dynamic<?> $$0) {
        boolean $$1 = $$0.get(SADDLE_FLAG).asBoolean(false);
        $$0 = $$0.remove(SADDLE_FLAG);
        if (!$$1) {
            return $$0;
        }
        Dynamic $$2 = $$0.emptyMap().set("id", $$0.createString("minecraft:saddle")).set("count", $$0.createInt(1));
        return SaddleEquipmentSlotFix.fixDropChances($$0.set(NEW_SADDLE, $$2));
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> $$0) {
        Dynamic $$1 = $$0.get("drop_chances").orElseEmptyMap().set(NEW_SADDLE, $$0.createFloat(2.0f));
        return $$0.set("drop_chances", $$1);
    }
}

