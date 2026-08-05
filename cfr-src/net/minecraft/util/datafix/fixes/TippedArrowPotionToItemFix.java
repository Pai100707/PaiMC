/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class TippedArrowPotionToItemFix
extends NamedEntityWriteReadFix {
    public TippedArrowPotionToItemFix(Schema $$0) {
        super($$0, false, "TippedArrowPotionToItemFix", References.ENTITY, "minecraft:arrow");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
        Optional $$1 = $$0.get("Potion").result();
        Optional $$2 = $$0.get("custom_potion_effects").result();
        Optional $$32 = $$0.get("Color").result();
        if ($$1.isEmpty() && $$2.isEmpty() && $$32.isEmpty()) {
            return $$0;
        }
        return $$0.remove("Potion").remove("custom_potion_effects").remove("Color").update("item", $$3 -> {
            Dynamic $$4 = $$3.get("tag").orElseEmptyMap();
            if ($$1.isPresent()) {
                $$4 = $$4.set("Potion", (Dynamic)$$1.get());
            }
            if ($$2.isPresent()) {
                $$4 = $$4.set("custom_potion_effects", (Dynamic)$$2.get());
            }
            if ($$32.isPresent()) {
                $$4 = $$4.set("CustomPotionColor", (Dynamic)$$32.get());
            }
            return $$3.set("tag", $$4);
        });
    }
}

