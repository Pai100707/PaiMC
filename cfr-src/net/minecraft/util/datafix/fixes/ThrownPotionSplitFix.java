/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ThrownPotionSplitFix
extends EntityRenameFix {
    private final Supplier<ItemIdFinder> itemIdFinder = Suppliers.memoize(() -> {
        Type $$0 = this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:potion");
        Type<?> $$1 = ExtraDataFixUtils.patchSubType($$0, this.getInputSchema().getType(References.ENTITY), this.getOutputSchema().getType(References.ENTITY));
        OpticFinder $$2 = $$1.findField("Item");
        OpticFinder $$3 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        return new ItemIdFinder($$2, (OpticFinder<Pair<String, String>>)$$3);
    });

    public ThrownPotionSplitFix(Schema $$0) {
        super("ThrownPotionSplitFix", $$0, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String $$0, Typed<?> $$1) {
        if (!$$0.equals("minecraft:potion")) {
            return Pair.of((Object)$$0, $$1);
        }
        String $$2 = this.itemIdFinder.get().getItemId($$1);
        if ("minecraft:lingering_potion".equals($$2)) {
            return Pair.of((Object)"minecraft:lingering_potion", $$1);
        }
        return Pair.of((Object)"minecraft:splash_potion", $$1);
    }

    record ItemIdFinder(OpticFinder<?> itemFinder, OpticFinder<Pair<String, String>> itemIdFinder) {
        public String getItemId(Typed<?> $$02) {
            return $$02.getOptionalTyped(this.itemFinder).flatMap($$0 -> $$0.getOptional(this.itemIdFinder)).map(Pair::getSecond).map(NamespacedSchema::ensureNamespaced).orElse("");
        }
    }
}

