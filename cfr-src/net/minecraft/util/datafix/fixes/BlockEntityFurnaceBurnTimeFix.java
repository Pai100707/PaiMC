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

public class BlockEntityFurnaceBurnTimeFix
extends NamedEntityFix {
    public BlockEntityFurnaceBurnTimeFix(Schema $$0, String $$1) {
        super($$0, false, "BlockEntityFurnaceBurnTimeFix" + $$1, References.BLOCK_ENTITY, $$1);
    }

    public Dynamic<?> fixBurnTime(Dynamic<?> $$0) {
        $$0 = $$0.renameField("CookTime", "cooking_time_spent");
        $$0 = $$0.renameField("CookTimeTotal", "cooking_total_time");
        $$0 = $$0.renameField("BurnTime", "lit_time_remaining");
        $$0 = $$0.setFieldIfPresent("lit_total_time", $$0.get("lit_time_remaining").result());
        return $$0;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixBurnTime);
    }
}

