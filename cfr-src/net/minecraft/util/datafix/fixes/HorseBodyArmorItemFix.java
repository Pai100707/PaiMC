/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class HorseBodyArmorItemFix
extends NamedEntityWriteReadFix {
    private final String previousBodyArmorTag;
    private final boolean clearArmorItems;

    public HorseBodyArmorItemFix(Schema $$0, String $$1, String $$2, boolean $$3) {
        super($$0, true, "Horse armor fix for " + $$1, References.ENTITY, $$1);
        this.previousBodyArmorTag = $$2;
        this.clearArmorItems = $$3;
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
        Optional $$1 = $$0.get(this.previousBodyArmorTag).result();
        if ($$1.isPresent()) {
            Dynamic $$2 = (Dynamic)$$1.get();
            Dynamic $$3 = $$0.remove(this.previousBodyArmorTag);
            if (this.clearArmorItems) {
                $$3 = $$3.update("ArmorItems", $$02 -> $$02.createList(Streams.mapWithIndex((Stream)$$02.asStream(), ($$0, $$1) -> $$1 == 2L ? $$0.emptyMap() : $$0)));
                $$3 = $$3.update("ArmorDropChances", $$02 -> $$02.createList(Streams.mapWithIndex((Stream)$$02.asStream(), ($$0, $$1) -> $$1 == 2L ? $$0.createFloat(0.085f) : $$0)));
            }
            $$3 = $$3.set("body_armor_item", $$2);
            $$3 = $$3.set("body_armor_drop_chance", $$0.createFloat(2.0f));
            return $$3;
        }
        return $$0;
    }
}

