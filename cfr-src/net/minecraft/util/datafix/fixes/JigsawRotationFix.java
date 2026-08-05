/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.util.datafix.fixes.AbstractBlockPropertyFix;

public class JigsawRotationFix
extends AbstractBlockPropertyFix {
    private static final Map<String, String> RENAMES = ImmutableMap.builder().put((Object)"down", (Object)"down_south").put((Object)"up", (Object)"up_north").put((Object)"north", (Object)"north_up").put((Object)"south", (Object)"south_up").put((Object)"west", (Object)"west_up").put((Object)"east", (Object)"east_up").build();

    public JigsawRotationFix(Schema $$0) {
        super($$0, "jigsaw_rotation_fix");
    }

    @Override
    protected boolean shouldFix(String $$0) {
        return $$0.equals("minecraft:jigsaw");
    }

    @Override
    protected <T> Dynamic<T> fixProperties(String $$0, Dynamic<T> $$1) {
        String $$2 = $$1.get("facing").asString("north");
        return $$1.remove("facing").set("orientation", $$1.createString(RENAMES.getOrDefault($$2, $$2)));
    }
}

