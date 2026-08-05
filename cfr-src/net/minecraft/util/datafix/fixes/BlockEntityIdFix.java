/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityIdFix
extends DataFix {
    public static final Map<String, String> ID_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
        $$0.put("Airportal", "minecraft:end_portal");
        $$0.put("Banner", "minecraft:banner");
        $$0.put("Beacon", "minecraft:beacon");
        $$0.put("Cauldron", "minecraft:brewing_stand");
        $$0.put("Chest", "minecraft:chest");
        $$0.put("Comparator", "minecraft:comparator");
        $$0.put("Control", "minecraft:command_block");
        $$0.put("DLDetector", "minecraft:daylight_detector");
        $$0.put("Dropper", "minecraft:dropper");
        $$0.put("EnchantTable", "minecraft:enchanting_table");
        $$0.put("EndGateway", "minecraft:end_gateway");
        $$0.put("EnderChest", "minecraft:ender_chest");
        $$0.put("FlowerPot", "minecraft:flower_pot");
        $$0.put("Furnace", "minecraft:furnace");
        $$0.put("Hopper", "minecraft:hopper");
        $$0.put("MobSpawner", "minecraft:mob_spawner");
        $$0.put("Music", "minecraft:noteblock");
        $$0.put("Piston", "minecraft:piston");
        $$0.put("RecordPlayer", "minecraft:jukebox");
        $$0.put("Sign", "minecraft:sign");
        $$0.put("Skull", "minecraft:skull");
        $$0.put("Structure", "minecraft:structure_block");
        $$0.put("Trap", "minecraft:dispenser");
    });

    public BlockEntityIdFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        Type $$02 = this.getInputSchema().getType(References.ITEM_STACK);
        Type $$1 = this.getOutputSchema().getType(References.ITEM_STACK);
        TaggedChoice.TaggedChoiceType $$2 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType $$3 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
        return TypeRewriteRule.seq((TypeRewriteRule)this.convertUnchecked("item stack block entity name hook converter", $$02, $$1), (TypeRewriteRule)this.fixTypeEverywhere("BlockEntityIdFix", (Type)$$2, (Type)$$3, $$0 -> $$02 -> $$02.mapFirst($$0 -> ID_MAP.getOrDefault($$0, (String)$$0))));
    }
}

