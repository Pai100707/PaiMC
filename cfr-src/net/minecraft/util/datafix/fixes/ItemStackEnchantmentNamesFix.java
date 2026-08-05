/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class ItemStackEnchantmentNamesFix
extends DataFix {
    private static final Int2ObjectMap<String> MAP = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), $$0 -> {
        $$0.put(0, (Object)"minecraft:protection");
        $$0.put(1, (Object)"minecraft:fire_protection");
        $$0.put(2, (Object)"minecraft:feather_falling");
        $$0.put(3, (Object)"minecraft:blast_protection");
        $$0.put(4, (Object)"minecraft:projectile_protection");
        $$0.put(5, (Object)"minecraft:respiration");
        $$0.put(6, (Object)"minecraft:aqua_affinity");
        $$0.put(7, (Object)"minecraft:thorns");
        $$0.put(8, (Object)"minecraft:depth_strider");
        $$0.put(9, (Object)"minecraft:frost_walker");
        $$0.put(10, (Object)"minecraft:binding_curse");
        $$0.put(16, (Object)"minecraft:sharpness");
        $$0.put(17, (Object)"minecraft:smite");
        $$0.put(18, (Object)"minecraft:bane_of_arthropods");
        $$0.put(19, (Object)"minecraft:knockback");
        $$0.put(20, (Object)"minecraft:fire_aspect");
        $$0.put(21, (Object)"minecraft:looting");
        $$0.put(22, (Object)"minecraft:sweeping");
        $$0.put(32, (Object)"minecraft:efficiency");
        $$0.put(33, (Object)"minecraft:silk_touch");
        $$0.put(34, (Object)"minecraft:unbreaking");
        $$0.put(35, (Object)"minecraft:fortune");
        $$0.put(48, (Object)"minecraft:power");
        $$0.put(49, (Object)"minecraft:punch");
        $$0.put(50, (Object)"minecraft:flame");
        $$0.put(51, (Object)"minecraft:infinity");
        $$0.put(61, (Object)"minecraft:luck_of_the_sea");
        $$0.put(62, (Object)"minecraft:lure");
        $$0.put(65, (Object)"minecraft:loyalty");
        $$0.put(66, (Object)"minecraft:impaling");
        $$0.put(67, (Object)"minecraft:riptide");
        $$0.put(68, (Object)"minecraft:channeling");
        $$0.put(70, (Object)"minecraft:mending");
        $$0.put(71, (Object)"minecraft:vanishing_curse");
    });

    public ItemStackEnchantmentNamesFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$12 = $$0.findField("tag");
        return this.fixTypeEverywhereTyped("ItemStackEnchantmentFix", $$0, $$1 -> $$1.updateTyped($$12, $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag)));
    }

    private Dynamic<?> fixTag(Dynamic<?> $$03) {
        Optional $$1 = $$03.get("ench").asStreamOpt().map($$02 -> $$02.map($$0 -> $$0.set("id", $$0.createString((String)MAP.getOrDefault($$0.get("id").asInt(0), (Object)"null"))))).map(arg_0 -> $$03.createList(arg_0)).result();
        if ($$1.isPresent()) {
            $$03 = $$03.remove("ench").set("Enchantments", (Dynamic)$$1.get());
        }
        return $$03.update("StoredEnchantments", $$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asStreamOpt().map($$02 -> $$02.map($$0 -> $$0.set("id", $$0.createString((String)MAP.getOrDefault($$0.get("id").asInt(0), (Object)"null"))))).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)).result(), (Object)$$0));
    }
}

