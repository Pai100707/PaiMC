/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public class FilteredBooksFix
extends ItemStackTagFix {
    public FilteredBooksFix(Schema $$02) {
        super($$02, "Remove filtered text from books", $$0 -> $$0.equals("minecraft:writable_book") || $$0.equals("minecraft:written_book"));
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> $$02) {
        return Util.writeAndReadTypedOrThrow($$02, $$02.getType(), $$0 -> $$0.remove("filtered_title").remove("filtered_pages"));
    }
}

