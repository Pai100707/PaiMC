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
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BoatSplitFix
extends DataFix {
    public BoatSplitFix(Schema $$0) {
        super($$0, true);
    }

    private static boolean isNormalBoat(String $$0) {
        return $$0.equals("minecraft:boat");
    }

    private static boolean isChestBoat(String $$0) {
        return $$0.equals("minecraft:chest_boat");
    }

    private static boolean isAnyBoat(String $$0) {
        return BoatSplitFix.isNormalBoat($$0) || BoatSplitFix.isChestBoat($$0);
    }

    private static String mapVariantToNormalBoat(String $$0) {
        return switch ($$0) {
            default -> "minecraft:oak_boat";
            case "spruce" -> "minecraft:spruce_boat";
            case "birch" -> "minecraft:birch_boat";
            case "jungle" -> "minecraft:jungle_boat";
            case "acacia" -> "minecraft:acacia_boat";
            case "cherry" -> "minecraft:cherry_boat";
            case "dark_oak" -> "minecraft:dark_oak_boat";
            case "mangrove" -> "minecraft:mangrove_boat";
            case "bamboo" -> "minecraft:bamboo_raft";
        };
    }

    private static String mapVariantToChestBoat(String $$0) {
        return switch ($$0) {
            default -> "minecraft:oak_chest_boat";
            case "spruce" -> "minecraft:spruce_chest_boat";
            case "birch" -> "minecraft:birch_chest_boat";
            case "jungle" -> "minecraft:jungle_chest_boat";
            case "acacia" -> "minecraft:acacia_chest_boat";
            case "cherry" -> "minecraft:cherry_chest_boat";
            case "dark_oak" -> "minecraft:dark_oak_chest_boat";
            case "mangrove" -> "minecraft:mangrove_chest_boat";
            case "bamboo" -> "minecraft:bamboo_chest_raft";
        };
    }

    public TypeRewriteRule makeRule() {
        OpticFinder $$0 = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        Type $$1 = this.getInputSchema().getType(References.ENTITY);
        Type $$22 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("BoatSplitFix", $$1, $$22, $$2 -> {
            Optional $$3 = $$2.getOptional($$0);
            if ($$3.isPresent() && BoatSplitFix.isAnyBoat((String)$$3.get())) {
                String $$7;
                Dynamic $$4 = (Dynamic)$$2.getOrCreate(DSL.remainderFinder());
                Optional $$5 = $$4.get("Type").asString().result();
                if (BoatSplitFix.isChestBoat((String)$$3.get())) {
                    String $$6 = $$5.map(BoatSplitFix::mapVariantToChestBoat).orElse("minecraft:oak_chest_boat");
                } else {
                    $$7 = $$5.map(BoatSplitFix::mapVariantToNormalBoat).orElse("minecraft:oak_boat");
                }
                return ExtraDataFixUtils.cast($$22, $$2).update(DSL.remainderFinder(), $$0 -> $$0.remove("Type")).set($$0, (Object)$$7);
            }
            return ExtraDataFixUtils.cast($$22, $$2);
        });
    }
}

