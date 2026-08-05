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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class TrialSpawnerConfigFix
extends NamedEntityWriteReadFix {
    public TrialSpawnerConfigFix(Schema $$0) {
        super($$0, true, "Trial Spawner config tag fixer", References.BLOCK_ENTITY, "minecraft:trial_spawner");
    }

    private static <T> Dynamic<T> moveToConfigTag(Dynamic<T> $$0) {
        List<String> $$1 = List.of("spawn_range", "total_mobs", "simultaneous_mobs", "total_mobs_added_per_player", "simultaneous_mobs_added_per_player", "ticks_between_spawn", "spawn_potentials", "loot_tables_to_eject", "items_to_drop_when_ominous");
        HashMap<Dynamic, Dynamic> $$2 = new HashMap<Dynamic, Dynamic>($$1.size());
        for (String $$3 : $$1) {
            Optional $$4 = $$0.get($$3).get().result();
            if (!$$4.isPresent()) continue;
            $$2.put($$0.createString($$3), (Dynamic)$$4.get());
            $$0 = $$0.remove($$3);
        }
        return $$2.isEmpty() ? $$0 : $$0.set("normal_config", $$0.createMap($$2));
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
        return TrialSpawnerConfigFix.moveToConfigTag($$0);
    }
}

