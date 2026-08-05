/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.SimpleEntityRenameFix;

public class EntityElderGuardianSplitFix
extends SimpleEntityRenameFix {
    public EntityElderGuardianSplitFix(Schema $$0, boolean $$1) {
        super("EntityElderGuardianSplitFix", $$0, $$1);
    }

    @Override
    protected Pair<String, Dynamic<?>> getNewNameAndTag(String $$0, Dynamic<?> $$1) {
        return Pair.of((Object)(Objects.equals($$0, "Guardian") && $$1.get("Elder").asBoolean(false) ? "ElderGuardian" : $$0), $$1);
    }
}

