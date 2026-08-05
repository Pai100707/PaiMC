/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class JukeboxTicksSinceSongStartedFix
extends NamedEntityFix {
    public JukeboxTicksSinceSongStartedFix(Schema $$0) {
        super($$0, false, "JukeboxTicksSinceSongStartedFix", References.BLOCK_ENTITY, "minecraft:jukebox");
    }

    public Dynamic<?> fixTag(Dynamic<?> $$0) {
        long $$1 = $$0.get("TickCount").asLong(0L) - $$0.get("RecordStartTick").asLong(0L);
        Dynamic $$2 = $$0.remove("IsPlaying").remove("TickCount").remove("RecordStartTick");
        if ($$1 > 0L) {
            return $$2.set("ticks_since_song_started", $$0.createLong($$1));
        }
        return $$2;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixTag);
    }
}

