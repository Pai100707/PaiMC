/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.SimplestEntityRenameFix;

public class EntityPufferfishRenameFix
extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.builder().put((Object)"minecraft:puffer_fish_spawn_egg", (Object)"minecraft:pufferfish_spawn_egg").build();

    public EntityPufferfishRenameFix(Schema $$0, boolean $$1) {
        super("EntityPufferfishRenameFix", $$0, $$1);
    }

    @Override
    protected String rename(String $$0) {
        return Objects.equals("minecraft:puffer_fish", $$0) ? "minecraft:pufferfish" : $$0;
    }
}

