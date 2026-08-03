package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record ToolMaterial(
   TagKey<Block> incorrectBlocksForDrops,
   int durability,
   float speed,
   float attackDamageBonus,
   int enchantmentValue,
   TagKey<net.minecraft.world.item.Item> repairItems
) {
   public static final net.minecraft.world.item.ToolMaterial WOOD = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, ItemTags.WOODEN_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial STONE = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 5, ItemTags.STONE_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial COPPER = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_COPPER_TOOL, 190, 5.0F, 1.0F, 13, ItemTags.COPPER_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial IRON = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, ItemTags.IRON_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial DIAMOND = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial GOLD = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_GOLD_TOOL, 32, 12.0F, 0.0F, 22, ItemTags.GOLD_TOOL_MATERIALS
   );
   public static final net.minecraft.world.item.ToolMaterial NETHERITE = new net.minecraft.world.item.ToolMaterial(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15, ItemTags.NETHERITE_TOOL_MATERIALS
   );

   private net.minecraft.world.item.Item.Properties applyCommonProperties(net.minecraft.world.item.Item.Properties $$0) {
      return $$0.durability(this.durability).repairable(this.repairItems).enchantable(this.enchantmentValue);
   }

   public net.minecraft.world.item.Item.Properties applyToolProperties(
      net.minecraft.world.item.Item.Properties $$0, TagKey<Block> $$1, float $$2, float $$3, float $$4
   ) {
      HolderGetter<Block> $$5 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
      return this.applyCommonProperties($$0)
         .component(
            DataComponents.TOOL,
            new Tool(
               List.of(Tool.Rule.deniesDrops($$5.getOrThrow(this.incorrectBlocksForDrops)), Tool.Rule.minesAndDrops($$5.getOrThrow($$1), this.speed)),
               1.0F,
               1,
               true
            )
         )
         .attributes(this.createToolAttributes($$2, $$3))
         .component(DataComponents.WEAPON, new Weapon(2, $$4));
   }

   private ItemAttributeModifiers createToolAttributes(float $$0, float $$1) {
      return ItemAttributeModifiers.builder()
         .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, $$0 + this.attackDamageBonus, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID, $$1, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .build();
   }

   public net.minecraft.world.item.Item.Properties applySwordProperties(net.minecraft.world.item.Item.Properties $$0, float $$1, float $$2) {
      HolderGetter<Block> $$3 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
      return this.applyCommonProperties($$0)
         .component(
            DataComponents.TOOL,
            new Tool(
               List.of(
                  Tool.Rule.minesAndDrops(HolderSet.direct(new Holder[]{Blocks.COBWEB.builtInRegistryHolder()}), 15.0F),
                  Tool.Rule.overrideSpeed($$3.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                  Tool.Rule.overrideSpeed($$3.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
               ),
               1.0F,
               2,
               false
            )
         )
         .attributes(this.createSwordAttributes($$1, $$2))
         .component(DataComponents.WEAPON, new Weapon(1));
   }

   private ItemAttributeModifiers createSwordAttributes(float $$0, float $$1) {
      return ItemAttributeModifiers.builder()
         .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, $$0 + this.attackDamageBonus, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID, $$1, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .build();
   }
}
