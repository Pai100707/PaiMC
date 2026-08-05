/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;

public class TooltipDisplayComponentFix
extends DataFix {
    private static final List<String> CONVERTED_ADDITIONAL_TOOLTIP_TYPES = List.of("minecraft:banner_patterns", "minecraft:bees", "minecraft:block_entity_data", "minecraft:block_state", "minecraft:bundle_contents", "minecraft:charged_projectiles", "minecraft:container", "minecraft:container_loot", "minecraft:firework_explosion", "minecraft:fireworks", "minecraft:instrument", "minecraft:map_id", "minecraft:painting/variant", "minecraft:pot_decorations", "minecraft:potion_contents", "minecraft:tropical_fish/pattern", "minecraft:written_book_content");

    public TooltipDisplayComponentFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
        Type $$1 = this.getOutputSchema().getType(References.DATA_COMPONENTS);
        OpticFinder $$2 = $$0.findField("minecraft:can_place_on");
        OpticFinder $$3 = $$0.findField("minecraft:can_break");
        Type $$42 = $$1.findFieldType("minecraft:can_place_on");
        Type $$5 = $$1.findFieldType("minecraft:can_break");
        return this.fixTypeEverywhereTyped("TooltipDisplayComponentFix", $$0, $$1, $$4 -> TooltipDisplayComponentFix.fix($$4, $$2, $$3, $$42, $$5));
    }

    private static Typed<?> fix(Typed<?> $$0, OpticFinder<?> $$12, OpticFinder<?> $$2, Type<?> $$3, Type<?> $$4) {
        HashSet<String> $$5 = new HashSet<String>();
        $$0 = TooltipDisplayComponentFix.fixAdventureModePredicate($$0, $$12, $$3, "minecraft:can_place_on", $$5);
        $$0 = TooltipDisplayComponentFix.fixAdventureModePredicate($$0, $$2, $$4, "minecraft:can_break", $$5);
        return $$0.update(DSL.remainderFinder(), $$1 -> {
            $$1 = TooltipDisplayComponentFix.fixSimpleComponent($$1, "minecraft:trim", $$5);
            $$1 = TooltipDisplayComponentFix.fixSimpleComponent($$1, "minecraft:unbreakable", $$5);
            $$1 = TooltipDisplayComponentFix.fixComponentAndUnwrap($$1, "minecraft:dyed_color", "rgb", $$5);
            $$1 = TooltipDisplayComponentFix.fixComponentAndUnwrap($$1, "minecraft:attribute_modifiers", "modifiers", $$5);
            $$1 = TooltipDisplayComponentFix.fixComponentAndUnwrap($$1, "minecraft:enchantments", "levels", $$5);
            $$1 = TooltipDisplayComponentFix.fixComponentAndUnwrap($$1, "minecraft:stored_enchantments", "levels", $$5);
            $$1 = TooltipDisplayComponentFix.fixComponentAndUnwrap($$1, "minecraft:jukebox_playable", "song", $$5);
            boolean $$2 = $$1.get("minecraft:hide_tooltip").result().isPresent();
            $$1 = $$1.remove("minecraft:hide_tooltip");
            boolean $$3 = $$1.get("minecraft:hide_additional_tooltip").result().isPresent();
            $$1 = $$1.remove("minecraft:hide_additional_tooltip");
            if ($$3) {
                for (String $$4 : CONVERTED_ADDITIONAL_TOOLTIP_TYPES) {
                    if (!$$1.get($$4).result().isPresent()) continue;
                    $$5.add($$4);
                }
            }
            if ($$5.isEmpty() && !$$2) {
                return $$1;
            }
            return $$1.set("minecraft:tooltip_display", $$1.createMap(Map.of($$1.createString("hide_tooltip"), $$1.createBoolean($$2), $$1.createString("hidden_components"), $$1.createList($$5.stream().map(arg_0 -> ((Dynamic)$$1).createString(arg_0))))));
        });
    }

    private static Dynamic<?> fixSimpleComponent(Dynamic<?> $$0, String $$1, Set<String> $$2) {
        return TooltipDisplayComponentFix.fixRemainderComponent($$0, $$1, $$2, UnaryOperator.identity());
    }

    private static Dynamic<?> fixComponentAndUnwrap(Dynamic<?> $$0, String $$12, String $$2, Set<String> $$3) {
        return TooltipDisplayComponentFix.fixRemainderComponent($$0, $$12, $$3, $$1 -> (Dynamic)DataFixUtils.orElse((Optional)$$1.get($$2).result(), (Object)$$1));
    }

    private static Dynamic<?> fixRemainderComponent(Dynamic<?> $$0, String $$1, Set<String> $$2, UnaryOperator<Dynamic<?>> $$32) {
        return $$0.update($$1, $$3 -> {
            boolean $$4 = $$3.get("show_in_tooltip").asBoolean(true);
            if (!$$4) {
                $$2.add($$1);
            }
            return (Dynamic)$$32.apply($$3.remove("show_in_tooltip"));
        });
    }

    private static Typed<?> fixAdventureModePredicate(Typed<?> $$0, OpticFinder<?> $$1, Type<?> $$2, String $$32, Set<String> $$4) {
        return $$0.updateTyped($$1, $$2, $$3 -> Util.writeAndReadTypedOrThrow($$3, $$2, $$2 -> {
            Object $$3 = $$2.get("predicates");
            if ($$3.result().isEmpty()) {
                return $$2;
            }
            boolean $$4 = $$2.get("show_in_tooltip").asBoolean(true);
            if (!$$4) {
                $$4.add($$32);
            }
            return (Dynamic)$$3.result().get();
        }));
    }
}

