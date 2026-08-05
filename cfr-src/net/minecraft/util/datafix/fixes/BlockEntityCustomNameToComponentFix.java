/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockEntityCustomNameToComponentFix
extends DataFix {
    private static final Set<String> NAMEABLE_BLOCK_ENTITIES = Set.of("minecraft:beacon", "minecraft:banner", "minecraft:brewing_stand", "minecraft:chest", "minecraft:trapped_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:enchanting_table", "minecraft:furnace", "minecraft:hopper", "minecraft:shulker_box");

    public BlockEntityCustomNameToComponentFix(Schema $$0) {
        super($$0, true);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder $$0 = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        Type $$1 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type $$2 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        Type<?> $$32 = ExtraDataFixUtils.patchSubType($$1, $$1, $$2);
        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", $$1, $$2, $$3 -> {
            Optional $$4 = $$3.getOptional($$0);
            if ($$4.isPresent() && !NAMEABLE_BLOCK_ENTITIES.contains($$4.get())) {
                return ExtraDataFixUtils.cast($$2, $$3);
            }
            return Util.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast($$32, $$3), $$2, BlockEntityCustomNameToComponentFix::fixTagCustomName);
        });
    }

    public static <T> Dynamic<T> fixTagCustomName(Dynamic<T> $$0) {
        String $$1 = $$0.get("CustomName").asString("");
        if ($$1.isEmpty()) {
            return $$0.remove("CustomName");
        }
        return $$0.set("CustomName", LegacyComponentDataFixUtils.createPlainTextComponent($$0.getOps(), $$1));
    }
}

