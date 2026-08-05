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
import net.minecraft.util.datafix.fixes.ItemStackTagRemainderFix;

public class GoatHornIdFix
extends ItemStackTagRemainderFix {
    private static final String[] INSTRUMENTS = new String[]{"minecraft:ponder_goat_horn", "minecraft:sing_goat_horn", "minecraft:seek_goat_horn", "minecraft:feel_goat_horn", "minecraft:admire_goat_horn", "minecraft:call_goat_horn", "minecraft:yearn_goat_horn", "minecraft:dream_goat_horn"};

    public GoatHornIdFix(Schema $$02) {
        super($$02, "GoatHornIdFix", $$0 -> $$0.equals("minecraft:goat_horn"));
    }

    @Override
    protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> $$0) {
        int $$1 = $$0.get("SoundVariant").asInt(0);
        String $$2 = INSTRUMENTS[$$1 >= 0 && $$1 < INSTRUMENTS.length ? $$1 : 0];
        return $$0.remove("SoundVariant").set("instrument", $$0.createString($$2));
    }
}

