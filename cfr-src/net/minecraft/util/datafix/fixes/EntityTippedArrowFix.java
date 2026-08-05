/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.SimplestEntityRenameFix;

public class EntityTippedArrowFix
extends SimplestEntityRenameFix {
    public EntityTippedArrowFix(Schema $$0, boolean $$1) {
        super("EntityTippedArrowFix", $$0, $$1);
    }

    @Override
    protected String rename(String $$0) {
        return Objects.equals($$0, "TippedArrow") ? "Arrow" : $$0;
    }
}

