package net.minecraft.world.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.TooltipDisplay;

public class SmithingTemplateItem extends net.minecraft.world.item.Item {
   private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
   private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
   private static final Component INGREDIENTS_TITLE = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.ingredients"))
      )
      .withStyle(TITLE_FORMAT);
   private static final Component APPLIES_TO_TITLE = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.applies_to"))
      )
      .withStyle(TITLE_FORMAT);
   private static final Component SMITHING_TEMPLATE_SUFFIX = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template"))
      )
      .withStyle(TITLE_FORMAT);
   private static final Component ARMOR_TRIM_APPLIES_TO = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.armor_trim.applies_to"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component ARMOR_TRIM_INGREDIENTS = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.armor_trim.ingredients"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component ARMOR_TRIM_BASE_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.armor_trim.base_slot_description"))
   );
   private static final Component ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.armor_trim.additions_slot_description"))
   );
   private static final Component NETHERITE_UPGRADE_APPLIES_TO = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.netherite_upgrade.applies_to"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component NETHERITE_UPGRADE_INGREDIENTS = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.netherite_upgrade.ingredients"))
      )
      .withStyle(DESCRIPTION_FORMAT);
   private static final Component NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.netherite_upgrade.base_slot_description"))
   );
   private static final Component NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
      Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.netherite_upgrade.additions_slot_description"))
   );
   private static final Identifier EMPTY_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
   private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
   private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
   private static final Identifier EMPTY_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
   private static final Identifier EMPTY_SLOT_HOE = Identifier.withDefaultNamespace("container/slot/hoe");
   private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
   private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
   private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.withDefaultNamespace("container/slot/shovel");
   private static final Identifier EMPTY_SLOT_SPEAR = Identifier.withDefaultNamespace("container/slot/spear");
   private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
   private static final Identifier EMPTY_SLOT_INGOT = Identifier.withDefaultNamespace("container/slot/ingot");
   private static final Identifier EMPTY_SLOT_REDSTONE_DUST = Identifier.withDefaultNamespace("container/slot/redstone_dust");
   private static final Identifier EMPTY_SLOT_QUARTZ = Identifier.withDefaultNamespace("container/slot/quartz");
   private static final Identifier EMPTY_SLOT_EMERALD = Identifier.withDefaultNamespace("container/slot/emerald");
   private static final Identifier EMPTY_SLOT_DIAMOND = Identifier.withDefaultNamespace("container/slot/diamond");
   private static final Identifier EMPTY_SLOT_LAPIS_LAZULI = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");
   private static final Identifier EMPTY_SLOT_AMETHYST_SHARD = Identifier.withDefaultNamespace("container/slot/amethyst_shard");
   private static final Identifier EMPTY_SLOT_NAUTILUS_ARMOR = Identifier.withDefaultNamespace("container/slot/nautilus_armor");
   private final Component appliesTo;
   private final Component ingredients;
   private final Component baseSlotDescription;
   private final Component additionsSlotDescription;
   private final List<Identifier> baseSlotEmptyIcons;
   private final List<Identifier> additionalSlotEmptyIcons;

   public SmithingTemplateItem(
      Component $$0, Component $$1, Component $$2, Component $$3, List<Identifier> $$4, List<Identifier> $$5, net.minecraft.world.item.Item.Properties $$6
   ) {
      super($$6);
      this.appliesTo = $$0;
      this.ingredients = $$1;
      this.baseSlotDescription = $$2;
      this.additionsSlotDescription = $$3;
      this.baseSlotEmptyIcons = $$4;
      this.additionalSlotEmptyIcons = $$5;
   }

   public static net.minecraft.world.item.SmithingTemplateItem createArmorTrimTemplate(net.minecraft.world.item.Item.Properties $$0) {
      return new net.minecraft.world.item.SmithingTemplateItem(
         ARMOR_TRIM_APPLIES_TO,
         ARMOR_TRIM_INGREDIENTS,
         ARMOR_TRIM_BASE_SLOT_DESCRIPTION,
         ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION,
         createTrimmableArmorIconList(),
         createTrimmableMaterialIconList(),
         $$0
      );
   }

   public static net.minecraft.world.item.SmithingTemplateItem createNetheriteUpgradeTemplate(net.minecraft.world.item.Item.Properties $$0) {
      return new net.minecraft.world.item.SmithingTemplateItem(
         NETHERITE_UPGRADE_APPLIES_TO,
         NETHERITE_UPGRADE_INGREDIENTS,
         NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION,
         NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
         createNetheriteUpgradeIconList(),
         createNetheriteUpgradeMaterialList(),
         $$0
      );
   }

   private static List<Identifier> createTrimmableArmorIconList() {
      return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS);
   }

   private static List<Identifier> createTrimmableMaterialIconList() {
      return List.of(
         EMPTY_SLOT_INGOT,
         EMPTY_SLOT_REDSTONE_DUST,
         EMPTY_SLOT_LAPIS_LAZULI,
         EMPTY_SLOT_QUARTZ,
         EMPTY_SLOT_DIAMOND,
         EMPTY_SLOT_EMERALD,
         EMPTY_SLOT_AMETHYST_SHARD
      );
   }

   private static List<Identifier> createNetheriteUpgradeIconList() {
      return List.of(
         EMPTY_SLOT_HELMET,
         EMPTY_SLOT_SWORD,
         EMPTY_SLOT_CHESTPLATE,
         EMPTY_SLOT_PICKAXE,
         EMPTY_SLOT_LEGGINGS,
         EMPTY_SLOT_AXE,
         EMPTY_SLOT_BOOTS,
         EMPTY_SLOT_HOE,
         EMPTY_SLOT_SHOVEL,
         EMPTY_SLOT_NAUTILUS_ARMOR,
         EMPTY_SLOT_SPEAR
      );
   }

   private static List<Identifier> createNetheriteUpgradeMaterialList() {
      return List.of(EMPTY_SLOT_INGOT);
   }

   @Override
   public void appendHoverText(
      net.minecraft.world.item.ItemStack $$0,
      net.minecraft.world.item.Item.TooltipContext $$1,
      TooltipDisplay $$2,
      Consumer<Component> $$3,
      net.minecraft.world.item.TooltipFlag $$4
   ) {
      $$3.accept(SMITHING_TEMPLATE_SUFFIX);
      $$3.accept(CommonComponents.EMPTY);
      $$3.accept(APPLIES_TO_TITLE);
      $$3.accept(CommonComponents.space().append(this.appliesTo));
      $$3.accept(INGREDIENTS_TITLE);
      $$3.accept(CommonComponents.space().append(this.ingredients));
   }

   public Component getBaseSlotDescription() {
      return this.baseSlotDescription;
   }

   public Component getAdditionSlotDescription() {
      return this.additionsSlotDescription;
   }

   public List<Identifier> getBaseSlotEmptyIcons() {
      return this.baseSlotEmptyIcons;
   }

   public List<Identifier> getAdditionalSlotEmptyIcons() {
      return this.additionalSlotEmptyIcons;
   }
}
