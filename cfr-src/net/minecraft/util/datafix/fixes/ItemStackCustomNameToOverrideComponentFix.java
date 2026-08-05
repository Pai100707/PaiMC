/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.OptionalDynamic;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackCustomNameToOverrideComponentFix
extends DataFix {
    private static final Set<String> MAP_NAMES = Set.of("filled_map.buried_treasure", "filled_map.explorer_jungle", "filled_map.explorer_swamp", "filled_map.mansion", "filled_map.monument", "filled_map.trial_chambers", "filled_map.village_desert", "filled_map.village_plains", "filled_map.village_savanna", "filled_map.village_snowy", "filled_map.village_taiga");

    public ItemStackCustomNameToOverrideComponentFix(Schema $$0) {
        super($$0, false);
    }

    public final TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$1 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$22 = $$0.findField("components");
        return this.fixTypeEverywhereTyped("ItemStack custom_name to item_name component fix", $$0, $$2 -> {
            Optional $$3 = $$2.getOptional($$1);
            Optional<String> $$4 = $$3.map(Pair::getSecond);
            if ($$4.filter($$0 -> $$0.equals("minecraft:white_banner")).isPresent()) {
                return $$2.updateTyped($$22, ItemStackCustomNameToOverrideComponentFix::fixBanner);
            }
            if ($$4.filter($$0 -> $$0.equals("minecraft:filled_map")).isPresent()) {
                return $$2.updateTyped($$22, ItemStackCustomNameToOverrideComponentFix::fixMap);
            }
            return $$2;
        });
    }

    private static <T> Typed<T> fixMap(Typed<T> $$0) {
        return ItemStackCustomNameToOverrideComponentFix.fixCustomName($$0, MAP_NAMES::contains);
    }

    private static <T> Typed<T> fixBanner(Typed<T> $$02) {
        return ItemStackCustomNameToOverrideComponentFix.fixCustomName($$02, $$0 -> $$0.equals("block.minecraft.ominous_banner"));
    }

    private static <T> Typed<T> fixCustomName(Typed<T> $$0, Predicate<String> $$12) {
        return Util.writeAndReadTypedOrThrow($$0, $$0.getType(), $$1 -> {
            OptionalDynamic $$2 = $$1.get("minecraft:custom_name");
            Optional $$3 = $$2.asString().result().flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter($$12);
            if ($$3.isPresent()) {
                return $$1.renameField("minecraft:custom_name", "minecraft:item_name");
            }
            return $$1;
        });
    }
}

