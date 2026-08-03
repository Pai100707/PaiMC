package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;

public class BlockEntityIdFix extends DataFix {
   public static final Map<String, String> ID_MAP = (Map<String, String>)DataFixUtils.make(Maps.newHashMap(), $$0 -> {
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
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      Type<?> $$1 = this.getOutputSchema().getType(References.ITEM_STACK);
      TaggedChoiceType<String> $$2 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
      TaggedChoiceType<String> $$3 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
      return TypeRewriteRule.seq(
         this.convertUnchecked("item stack block entity name hook converter", $$0, $$1),
         this.fixTypeEverywhere("BlockEntityIdFix", $$2, $$3, $$0x -> $$0xx -> $$0xx.mapFirst($$0xxx -> ID_MAP.getOrDefault($$0xxx, $$0xxx)))
      );
   }
}
