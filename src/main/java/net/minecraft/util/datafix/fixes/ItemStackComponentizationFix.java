package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.jspecify.annotations.Nullable;

public class ItemStackComponentizationFix extends DataFix {
   private static final int HIDE_ENCHANTMENTS = 1;
   private static final int HIDE_MODIFIERS = 2;
   private static final int HIDE_UNBREAKABLE = 4;
   private static final int HIDE_CAN_DESTROY = 8;
   private static final int HIDE_CAN_PLACE = 16;
   private static final int HIDE_ADDITIONAL = 32;
   private static final int HIDE_DYE = 64;
   private static final int HIDE_UPGRADES = 128;
   private static final Set<String> POTION_HOLDER_IDS = Set.of(
      "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow"
   );
   private static final Set<String> BUCKETED_MOB_IDS = Set.of(
      "minecraft:pufferfish_bucket",
      "minecraft:salmon_bucket",
      "minecraft:cod_bucket",
      "minecraft:tropical_fish_bucket",
      "minecraft:axolotl_bucket",
      "minecraft:tadpole_bucket"
   );
   private static final List<String> BUCKETED_MOB_TAGS = List.of(
      "NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag"
   );
   private static final Set<String> BOOLEAN_BLOCK_STATE_PROPERTIES = Set.of(
      "attached",
      "bottom",
      "conditional",
      "disarmed",
      "drag",
      "enabled",
      "extended",
      "eye",
      "falling",
      "hanging",
      "has_bottle_0",
      "has_bottle_1",
      "has_bottle_2",
      "has_record",
      "has_book",
      "inverted",
      "in_wall",
      "lit",
      "locked",
      "occupied",
      "open",
      "persistent",
      "powered",
      "short",
      "signal_fire",
      "snowy",
      "triggered",
      "unstable",
      "waterlogged",
      "berries",
      "bloom",
      "shrieking",
      "can_summon",
      "up",
      "down",
      "north",
      "east",
      "south",
      "west",
      "slot_0_occupied",
      "slot_1_occupied",
      "slot_2_occupied",
      "slot_3_occupied",
      "slot_4_occupied",
      "slot_5_occupied",
      "cracked",
      "crafting"
   );
   private static final Splitter PROPERTY_SPLITTER = Splitter.on(',');

   public ItemStackComponentizationFix(Schema $$0) {
      super($$0, true);
   }

   private static void fixItemStack(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      int $$2 = $$0.removeTag("HideFlags").asInt(0);
      $$0.moveTagToComponent("Damage", "minecraft:damage", $$1.createInt(0));
      $$0.moveTagToComponent("RepairCost", "minecraft:repair_cost", $$1.createInt(0));
      $$0.moveTagToComponent("CustomModelData", "minecraft:custom_model_data");
      $$0.removeTag("BlockStateTag").result().ifPresent($$1x -> $$0.setComponent("minecraft:block_state", fixBlockStateTag($$1x)));
      $$0.moveTagToComponent("EntityTag", "minecraft:entity_data");
      $$0.fixSubTag("BlockEntityTag", false, $$1x -> {
         String $$2x = NamespacedSchema.ensureNamespaced($$1x.get("id").asString(""));
         $$1x = fixBlockEntityTag($$0, $$1x, $$2x);
         Dynamic<?> $$3 = $$1x.remove("id");
         return $$3.equals($$1x.emptyMap()) ? $$3 : $$1x;
      });
      $$0.moveTagToComponent("BlockEntityTag", "minecraft:block_entity_data");
      if ($$0.removeTag("Unbreakable").asBoolean(false)) {
         Dynamic<?> $$3 = $$1.emptyMap();
         if (($$2 & 4) != 0) {
            $$3 = $$3.set("show_in_tooltip", $$1.createBoolean(false));
         }

         $$0.setComponent("minecraft:unbreakable", $$3);
      }

      fixEnchantments($$0, $$1, "Enchantments", "minecraft:enchantments", ($$2 & 1) != 0);
      if ($$0.is("minecraft:enchanted_book")) {
         fixEnchantments($$0, $$1, "StoredEnchantments", "minecraft:stored_enchantments", ($$2 & 32) != 0);
      }

      $$0.fixSubTag("display", false, $$2x -> fixDisplay($$0, $$2x, $$2));
      fixAdventureModeChecks($$0, $$1, $$2);
      fixAttributeModifiers($$0, $$1, $$2);
      Optional<? extends Dynamic<?>> $$4 = $$0.removeTag("Trim").result();
      if ($$4.isPresent()) {
         Dynamic<?> $$5 = (Dynamic<?>)$$4.get();
         if (($$2 & 128) != 0) {
            $$5 = $$5.set("show_in_tooltip", $$5.createBoolean(false));
         }

         $$0.setComponent("minecraft:trim", $$5);
      }

      if (($$2 & 32) != 0) {
         $$0.setComponent("minecraft:hide_additional_tooltip", $$1.emptyMap());
      }

      if ($$0.is("minecraft:crossbow")) {
         $$0.removeTag("Charged");
         $$0.moveTagToComponent("ChargedProjectiles", "minecraft:charged_projectiles", $$1.createList(Stream.empty()));
      }

      if ($$0.is("minecraft:bundle")) {
         $$0.moveTagToComponent("Items", "minecraft:bundle_contents", $$1.createList(Stream.empty()));
      }

      if ($$0.is("minecraft:filled_map")) {
         $$0.moveTagToComponent("map", "minecraft:map_id");
         Map<? extends Dynamic<?>, ? extends Dynamic<?>> $$6 = $$0.removeTag("Decorations")
            .asStream()
            .map(ItemStackComponentizationFix::fixMapDecoration)
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, ($$0x, $$1x) -> $$0x));
         if (!$$6.isEmpty()) {
            $$0.setComponent("minecraft:map_decorations", $$1.createMap($$6));
         }
      }

      if ($$0.is(POTION_HOLDER_IDS)) {
         fixPotionContents($$0, $$1);
      }

      if ($$0.is("minecraft:writable_book")) {
         fixWritableBook($$0, $$1);
      }

      if ($$0.is("minecraft:written_book")) {
         fixWrittenBook($$0, $$1);
      }

      if ($$0.is("minecraft:suspicious_stew")) {
         $$0.moveTagToComponent("effects", "minecraft:suspicious_stew_effects");
      }

      if ($$0.is("minecraft:debug_stick")) {
         $$0.moveTagToComponent("DebugProperty", "minecraft:debug_stick_state");
      }

      if ($$0.is(BUCKETED_MOB_IDS)) {
         fixBucketedMobData($$0, $$1);
      }

      if ($$0.is("minecraft:goat_horn")) {
         $$0.moveTagToComponent("instrument", "minecraft:instrument");
      }

      if ($$0.is("minecraft:knowledge_book")) {
         $$0.moveTagToComponent("Recipes", "minecraft:recipes");
      }

      if ($$0.is("minecraft:compass")) {
         fixLodestoneTracker($$0, $$1);
      }

      if ($$0.is("minecraft:firework_rocket")) {
         fixFireworkRocket($$0);
      }

      if ($$0.is("minecraft:firework_star")) {
         fixFireworkStar($$0);
      }

      if ($$0.is("minecraft:player_head")) {
         $$0.removeTag("SkullOwner").result().ifPresent($$1x -> $$0.setComponent("minecraft:profile", fixProfile($$1x)));
      }
   }

   private static Dynamic<?> fixBlockStateTag(Dynamic<?> $$0) {
      return (Dynamic<?>)DataFixUtils.orElse($$0.asMapOpt().result().map($$0x -> $$0x.collect(Collectors.toMap(Pair::getFirst, $$0xx -> {
         String $$1 = ((Dynamic)$$0xx.getFirst()).asString("");
         Dynamic<?> $$2 = (Dynamic<?>)$$0xx.getSecond();
         if (BOOLEAN_BLOCK_STATE_PROPERTIES.contains($$1)) {
            Optional<Boolean> $$3 = $$2.asBoolean().result();
            if ($$3.isPresent()) {
               return $$2.createString(String.valueOf($$3.get()));
            }
         }

         Optional<Number> $$4 = $$2.asNumber().result();
         return $$4.isPresent() ? $$2.createString($$4.get().toString()) : $$2;
      }))).map($$0::createMap), $$0);
   }

   private static Dynamic<?> fixDisplay(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1, int $$2) {
      $$1.get("Name").result().filter(LegacyComponentDataFixUtils::isStrictlyValidJson).ifPresent($$1x -> $$0.setComponent("minecraft:custom_name", $$1x));
      OptionalDynamic<?> $$3 = $$1.get("Lore");
      if ($$3.result().isPresent()) {
         $$0.setComponent("minecraft:lore", $$1.createList($$1.get("Lore").asStream().filter(LegacyComponentDataFixUtils::isStrictlyValidJson)));
      }

      Optional<Integer> $$4 = $$1.get("color").asNumber().result().map(Number::intValue);
      boolean $$5 = ($$2 & 64) != 0;
      if ($$4.isPresent() || $$5) {
         Dynamic<?> $$6 = $$1.emptyMap().set("rgb", $$1.createInt($$4.orElse(10511680)));
         if ($$5) {
            $$6 = $$6.set("show_in_tooltip", $$1.createBoolean(false));
         }

         $$0.setComponent("minecraft:dyed_color", $$6);
      }

      Optional<String> $$7 = $$1.get("LocName").asString().result();
      if ($$7.isPresent()) {
         $$0.setComponent("minecraft:item_name", LegacyComponentDataFixUtils.createTranslatableComponent($$1.getOps(), $$7.get()));
      }

      if ($$0.is("minecraft:filled_map")) {
         $$0.setComponent("minecraft:map_color", $$1.get("MapColor"));
         $$1 = $$1.remove("MapColor");
      }

      return $$1.remove("Name").remove("Lore").remove("color").remove("LocName");
   }

   private static <T> Dynamic<T> fixBlockEntityTag(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<T> $$1, String $$2) {
      $$0.setComponent("minecraft:lock", $$1.get("Lock"));
      $$1 = $$1.remove("Lock");
      Optional<Dynamic<T>> $$3 = $$1.get("LootTable").result();
      if ($$3.isPresent()) {
         Dynamic<T> $$4 = $$1.emptyMap().set("loot_table", $$3.get());
         long $$5 = $$1.get("LootTableSeed").asLong(0L);
         if ($$5 != 0L) {
            $$4 = $$4.set("seed", $$1.createLong($$5));
         }

         $$0.setComponent("minecraft:container_loot", $$4);
         $$1 = $$1.remove("LootTable").remove("LootTableSeed");
      }
      return switch ($$2) {
         case "minecraft:skull" -> {
            $$0.setComponent("minecraft:note_block_sound", $$1.get("note_block_sound"));
            yield $$1.remove("note_block_sound");
         }
         case "minecraft:decorated_pot" -> {
            $$0.setComponent("minecraft:pot_decorations", $$1.get("sherds"));
            Optional<Dynamic<T>> $$6 = $$1.get("item").result();
            if ($$6.isPresent()) {
               $$0.setComponent("minecraft:container", $$1.createList(Stream.of($$1.emptyMap().set("slot", $$1.createInt(0)).set("item", $$6.get()))));
            }

            yield $$1.remove("sherds").remove("item");
         }
         case "minecraft:banner" -> {
            $$0.setComponent("minecraft:banner_patterns", $$1.get("patterns"));
            Optional<Number> $$7 = $$1.get("Base").asNumber().result();
            if ($$7.isPresent()) {
               $$0.setComponent("minecraft:base_color", $$1.createString(ExtraDataFixUtils.dyeColorIdToName($$7.get().intValue())));
            }

            yield $$1.remove("patterns").remove("Base");
         }
         case "minecraft:shulker_box", "minecraft:chest", "minecraft:trapped_chest", "minecraft:furnace", "minecraft:ender_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:brewing_stand", "minecraft:hopper", "minecraft:barrel", "minecraft:smoker", "minecraft:blast_furnace", "minecraft:campfire", "minecraft:chiseled_bookshelf", "minecraft:crafter" -> {
            List<Dynamic<T>> $$8 = $$1.get("Items")
               .asList($$0x -> $$0x.emptyMap().set("slot", $$0x.createInt($$0x.get("Slot").asByte((byte)0) & 255)).set("item", $$0x.remove("Slot")));
            if (!$$8.isEmpty()) {
               $$0.setComponent("minecraft:container", $$1.createList($$8.stream()));
            }

            yield $$1.remove("Items");
         }
         case "minecraft:beehive" -> {
            $$0.setComponent("minecraft:bees", $$1.get("bees"));
            yield $$1.remove("bees");
         }
         default -> $$1;
      };
   }

   private static void fixEnchantments(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1, String $$2, String $$3, boolean $$4) {
      OptionalDynamic<?> $$5 = $$0.removeTag($$2);
      List<Pair<String, Integer>> $$6 = $$5.asList(Function.identity())
         .stream()
         .flatMap($$0x -> parseEnchantment($$0x).stream())
         .filter($$0x -> (Integer)$$0x.getSecond() > 0)
         .toList();
      if (!$$6.isEmpty() || $$4) {
         Dynamic<?> $$7 = $$1.emptyMap();
         Dynamic<?> $$8 = $$1.emptyMap();

         for (Pair<String, Integer> $$9 : $$6) {
            $$8 = $$8.set((String)$$9.getFirst(), $$1.createInt((Integer)$$9.getSecond()));
         }

         $$7 = $$7.set("levels", $$8);
         if ($$4) {
            $$7 = $$7.set("show_in_tooltip", $$1.createBoolean(false));
         }

         $$0.setComponent($$3, $$7);
      }

      if ($$5.result().isPresent() && $$6.isEmpty()) {
         $$0.setComponent("minecraft:enchantment_glint_override", $$1.createBoolean(true));
      }
   }

   private static Optional<Pair<String, Integer>> parseEnchantment(Dynamic<?> $$0) {
      return $$0.get("id")
         .asString()
         .apply2stable(($$0x, $$1) -> Pair.of($$0x, net.minecraft.util.Mth.clamp($$1.intValue(), 0, 255)), $$0.get("lvl").asNumber())
         .result();
   }

   private static void fixAdventureModeChecks(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1, int $$2) {
      fixBlockStatePredicates($$0, $$1, "CanDestroy", "minecraft:can_break", ($$2 & 8) != 0);
      fixBlockStatePredicates($$0, $$1, "CanPlaceOn", "minecraft:can_place_on", ($$2 & 16) != 0);
   }

   private static void fixBlockStatePredicates(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1, String $$2, String $$3, boolean $$4) {
      Optional<? extends Dynamic<?>> $$5 = $$0.removeTag($$2).result();
      if (!$$5.isEmpty()) {
         Dynamic<?> $$6 = $$1.emptyMap()
            .set(
               "predicates",
               $$1.createList(
                  $$5.get()
                     .asStream()
                     .map($$0x -> (Dynamic)DataFixUtils.orElse($$0x.asString().map($$1x -> fixBlockStatePredicate($$0x, $$1x)).result(), $$0x))
               )
            );
         if ($$4) {
            $$6 = $$6.set("show_in_tooltip", $$1.createBoolean(false));
         }

         $$0.setComponent($$3, $$6);
      }
   }

   private static Dynamic<?> fixBlockStatePredicate(Dynamic<?> $$0, String $$1) {
      int $$2 = $$1.indexOf(91);
      int $$3 = $$1.indexOf(123);
      int $$4 = $$1.length();
      if ($$2 != -1) {
         $$4 = $$2;
      }

      if ($$3 != -1) {
         $$4 = Math.min($$4, $$3);
      }

      String $$5 = $$1.substring(0, $$4);
      Dynamic<?> $$6 = $$0.emptyMap().set("blocks", $$0.createString($$5.trim()));
      int $$7 = $$1.indexOf(93);
      if ($$2 != -1 && $$7 != -1) {
         Dynamic<?> $$8 = $$0.emptyMap();

         for (String $$10 : PROPERTY_SPLITTER.split($$1.substring($$2 + 1, $$7))) {
            int $$11 = $$10.indexOf(61);
            if ($$11 != -1) {
               String $$12 = $$10.substring(0, $$11).trim();
               String $$13 = $$10.substring($$11 + 1).trim();
               $$8 = $$8.set($$12, $$0.createString($$13));
            }
         }

         $$6 = $$6.set("state", $$8);
      }

      int $$14 = $$1.indexOf(125);
      if ($$3 != -1 && $$14 != -1) {
         $$6 = $$6.set("nbt", $$0.createString($$1.substring($$3, $$14 + 1)));
      }

      return $$6;
   }

   private static void fixAttributeModifiers(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1, int $$2) {
      OptionalDynamic<?> $$3 = $$0.removeTag("AttributeModifiers");
      if (!$$3.result().isEmpty()) {
         boolean $$4 = ($$2 & 2) != 0;
         List<? extends Dynamic<?>> $$5 = $$3.asList(ItemStackComponentizationFix::fixAttributeModifier);
         Dynamic<?> $$6 = $$1.emptyMap().set("modifiers", $$1.createList($$5.stream()));
         if ($$4) {
            $$6 = $$6.set("show_in_tooltip", $$1.createBoolean(false));
         }

         $$0.setComponent("minecraft:attribute_modifiers", $$6);
      }
   }

   private static Dynamic<?> fixAttributeModifier(Dynamic<?> $$0) {
      Dynamic<?> $$1 = $$0.emptyMap().set("name", $$0.createString("")).set("amount", $$0.createDouble(0.0)).set("operation", $$0.createString("add_value"));
      $$1 = Dynamic.copyField($$0, "AttributeName", $$1, "type");
      $$1 = Dynamic.copyField($$0, "Slot", $$1, "slot");
      $$1 = Dynamic.copyField($$0, "UUID", $$1, "uuid");
      $$1 = Dynamic.copyField($$0, "Name", $$1, "name");
      $$1 = Dynamic.copyField($$0, "Amount", $$1, "amount");
      return Dynamic.copyAndFixField($$0, "Operation", $$1, "operation", $$0x -> {
         return $$0x.createString(switch ($$0x.asInt(0)) {
            case 1 -> "add_multiplied_base";
            case 2 -> "add_multiplied_total";
            default -> "add_value";
         });
      });
   }

   private static Pair<Dynamic<?>, Dynamic<?>> fixMapDecoration(Dynamic<?> $$0) {
      Dynamic<?> $$1 = (Dynamic<?>)DataFixUtils.orElseGet($$0.get("id").result(), () -> $$0.createString(""));
      Dynamic<?> $$2 = $$0.emptyMap()
         .set("type", $$0.createString(fixMapDecorationType($$0.get("type").asInt(0))))
         .set("x", $$0.createDouble($$0.get("x").asDouble(0.0)))
         .set("z", $$0.createDouble($$0.get("z").asDouble(0.0)))
         .set("rotation", $$0.createFloat((float)$$0.get("rot").asDouble(0.0)));
      return Pair.of($$1, $$2);
   }

   private static String fixMapDecorationType(int $$0) {
      return switch ($$0) {
         case 1 -> "frame";
         case 2 -> "red_marker";
         case 3 -> "blue_marker";
         case 4 -> "target_x";
         case 5 -> "target_point";
         case 6 -> "player_off_map";
         case 7 -> "player_off_limits";
         case 8 -> "mansion";
         case 9 -> "monument";
         case 10 -> "banner_white";
         case 11 -> "banner_orange";
         case 12 -> "banner_magenta";
         case 13 -> "banner_light_blue";
         case 14 -> "banner_yellow";
         case 15 -> "banner_lime";
         case 16 -> "banner_pink";
         case 17 -> "banner_gray";
         case 18 -> "banner_light_gray";
         case 19 -> "banner_cyan";
         case 20 -> "banner_purple";
         case 21 -> "banner_blue";
         case 22 -> "banner_brown";
         case 23 -> "banner_green";
         case 24 -> "banner_red";
         case 25 -> "banner_black";
         case 26 -> "red_x";
         case 27 -> "village_desert";
         case 28 -> "village_plains";
         case 29 -> "village_savanna";
         case 30 -> "village_snowy";
         case 31 -> "village_taiga";
         case 32 -> "jungle_temple";
         case 33 -> "swamp_hut";
         default -> "player";
      };
   }

   private static void fixPotionContents(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      Dynamic<?> $$2 = $$1.emptyMap();
      Optional<String> $$3 = $$0.removeTag("Potion").asString().result().filter($$0x -> !$$0x.equals("minecraft:empty"));
      if ($$3.isPresent()) {
         $$2 = $$2.set("potion", $$1.createString($$3.get()));
      }

      $$2 = $$0.moveTagInto("CustomPotionColor", $$2, "custom_color");
      $$2 = $$0.moveTagInto("custom_potion_effects", $$2, "custom_effects");
      if (!$$2.equals($$1.emptyMap())) {
         $$0.setComponent("minecraft:potion_contents", $$2);
      }
   }

   private static void fixWritableBook(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      Dynamic<?> $$2 = fixBookPages($$0, $$1);
      if ($$2 != null) {
         $$0.setComponent("minecraft:writable_book_content", $$1.emptyMap().set("pages", $$2));
      }
   }

   private static void fixWrittenBook(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      Dynamic<?> $$2 = fixBookPages($$0, $$1);
      String $$3 = $$0.removeTag("title").asString("");
      Optional<String> $$4 = $$0.removeTag("filtered_title").asString().result();
      Dynamic<?> $$5 = $$1.emptyMap();
      $$5 = $$5.set("title", createFilteredText($$1, $$3, $$4));
      $$5 = $$0.moveTagInto("author", $$5, "author");
      $$5 = $$0.moveTagInto("resolved", $$5, "resolved");
      $$5 = $$0.moveTagInto("generation", $$5, "generation");
      if ($$2 != null) {
         $$5 = $$5.set("pages", $$2);
      }

      $$0.setComponent("minecraft:written_book_content", $$5);
   }

   @Nullable
   private static Dynamic<?> fixBookPages(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      List<String> $$2 = $$0.removeTag("pages").asList($$0x -> $$0x.asString(""));
      Map<String, String> $$3 = $$0.removeTag("filtered_pages").asMap($$0x -> $$0x.asString("0"), $$0x -> $$0x.asString(""));
      if ($$2.isEmpty()) {
         return null;
      } else {
         List<Dynamic<?>> $$4 = new ArrayList<>($$2.size());

         for (int $$5 = 0; $$5 < $$2.size(); $$5++) {
            String $$6 = $$2.get($$5);
            String $$7 = $$3.get(String.valueOf($$5));
            $$4.add(createFilteredText($$1, $$6, Optional.ofNullable($$7)));
         }

         return $$1.createList($$4.stream());
      }
   }

   private static Dynamic<?> createFilteredText(Dynamic<?> $$0, String $$1, Optional<String> $$2) {
      Dynamic<?> $$3 = $$0.emptyMap().set("raw", $$0.createString($$1));
      if ($$2.isPresent()) {
         $$3 = $$3.set("filtered", $$0.createString($$2.get()));
      }

      return $$3;
   }

   private static void fixBucketedMobData(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      Dynamic<?> $$2 = $$1.emptyMap();

      for (String $$3 : BUCKETED_MOB_TAGS) {
         $$2 = $$0.moveTagInto($$3, $$2, $$3);
      }

      if (!$$2.equals($$1.emptyMap())) {
         $$0.setComponent("minecraft:bucket_entity_data", $$2);
      }
   }

   private static void fixLodestoneTracker(ItemStackComponentizationFix.ItemStackData $$0, Dynamic<?> $$1) {
      Optional<? extends Dynamic<?>> $$2 = $$0.removeTag("LodestonePos").result();
      Optional<? extends Dynamic<?>> $$3 = $$0.removeTag("LodestoneDimension").result();
      if (!$$2.isEmpty() || !$$3.isEmpty()) {
         boolean $$4 = $$0.removeTag("LodestoneTracked").asBoolean(true);
         Dynamic<?> $$5 = $$1.emptyMap();
         if ($$2.isPresent() && $$3.isPresent()) {
            $$5 = $$5.set("target", $$1.emptyMap().set("pos", $$2.get()).set("dimension", $$3.get()));
         }

         if (!$$4) {
            $$5 = $$5.set("tracked", $$1.createBoolean(false));
         }

         $$0.setComponent("minecraft:lodestone_tracker", $$5);
      }
   }

   private static void fixFireworkStar(ItemStackComponentizationFix.ItemStackData $$0) {
      $$0.fixSubTag("Explosion", true, $$1 -> {
         $$0.setComponent("minecraft:firework_explosion", fixFireworkExplosion($$1));
         return $$1.remove("Type").remove("Colors").remove("FadeColors").remove("Trail").remove("Flicker");
      });
   }

   private static void fixFireworkRocket(ItemStackComponentizationFix.ItemStackData $$0) {
      $$0.fixSubTag("Fireworks", true, $$1 -> {
         Stream<? extends Dynamic<?>> $$2 = $$1.get("Explosions").asStream().map(ItemStackComponentizationFix::fixFireworkExplosion);
         int $$3 = $$1.get("Flight").asInt(0);
         $$0.setComponent("minecraft:fireworks", $$1.emptyMap().set("explosions", $$1.createList($$2)).set("flight_duration", $$1.createByte((byte)$$3)));
         return $$1.remove("Explosions").remove("Flight");
      });
   }

   private static Dynamic<?> fixFireworkExplosion(Dynamic<?> $$0) {
      $$0 = $$0.set("shape", $$0.createString(switch ($$0.get("Type").asInt(0)) {
         case 1 -> "large_ball";
         case 2 -> "star";
         case 3 -> "creeper";
         case 4 -> "burst";
         default -> "small_ball";
      })).remove("Type");
      $$0 = $$0.renameField("Colors", "colors");
      $$0 = $$0.renameField("FadeColors", "fade_colors");
      $$0 = $$0.renameField("Trail", "has_trail");
      return $$0.renameField("Flicker", "has_twinkle");
   }

   public static Dynamic<?> fixProfile(Dynamic<?> $$0) {
      Optional<String> $$1 = $$0.asString().result();
      if ($$1.isPresent()) {
         return isValidPlayerName($$1.get()) ? $$0.emptyMap().set("name", $$0.createString($$1.get())) : $$0.emptyMap();
      } else {
         String $$2 = $$0.get("Name").asString("");
         Optional<? extends Dynamic<?>> $$3 = $$0.get("Id").result();
         Dynamic<?> $$4 = fixProfileProperties($$0.get("Properties"));
         Dynamic<?> $$5 = $$0.emptyMap();
         if (isValidPlayerName($$2)) {
            $$5 = $$5.set("name", $$0.createString($$2));
         }

         if ($$3.isPresent()) {
            $$5 = $$5.set("id", $$3.get());
         }

         if ($$4 != null) {
            $$5 = $$5.set("properties", $$4);
         }

         return $$5;
      }
   }

   private static boolean isValidPlayerName(String $$0) {
      return $$0.length() > 16 ? false : $$0.chars().filter($$0x -> $$0x <= 32 || $$0x >= 127).findAny().isEmpty();
   }

   @Nullable
   private static Dynamic<?> fixProfileProperties(OptionalDynamic<?> $$0) {
      Map<String, List<Pair<String, Optional<String>>>> $$1 = $$0.asMap($$0x -> $$0x.asString(""), $$0x -> $$0x.asList($$0xx -> {
         String $$1x = $$0xx.get("Value").asString("");
         Optional<String> $$2 = $$0xx.get("Signature").asString().result();
         return Pair.of($$1x, $$2);
      }));
      return $$1.isEmpty() ? null : $$0.createList($$1.entrySet().stream().flatMap($$1x -> ((List)$$1x.getValue()).stream().map($$2 -> {
         Dynamic<?> $$3 = $$0.emptyMap().set("name", $$0.createString((String)$$1x.getKey())).set("value", $$0.createString((String)$$2.getFirst()));
         Optional<String> $$4 = (Optional<String>)$$2.getSecond();
         return $$4.isPresent() ? $$3.set("signature", $$0.createString($$4.get())) : $$3;
      })));
   }

   protected TypeRewriteRule makeRule() {
      return this.writeFixAndRead(
         "ItemStack componentization", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), $$0 -> {
            Optional<? extends Dynamic<?>> $$1 = ItemStackComponentizationFix.ItemStackData.read($$0).map($$0x -> {
               fixItemStack($$0x, $$0x.tag);
               return $$0x.write();
            });
            return (Dynamic)DataFixUtils.orElse($$1, $$0);
         }
      );
   }

   static class ItemStackData {
      private final String item;
      private final int count;
      private Dynamic<?> components;
      private final Dynamic<?> remainder;
      Dynamic<?> tag;

      private ItemStackData(String $$0, int $$1, Dynamic<?> $$2) {
         this.item = NamespacedSchema.ensureNamespaced($$0);
         this.count = $$1;
         this.components = $$2.emptyMap();
         this.tag = $$2.get("tag").orElseEmptyMap();
         this.remainder = $$2.remove("tag");
      }

      public static Optional<ItemStackComponentizationFix.ItemStackData> read(Dynamic<?> $$0) {
         return $$0.get("id")
            .asString()
            .apply2stable(
               ($$1, $$2) -> new ItemStackComponentizationFix.ItemStackData($$1, $$2.intValue(), $$0.remove("id").remove("Count")), $$0.get("Count").asNumber()
            )
            .result();
      }

      public OptionalDynamic<?> removeTag(String $$0) {
         OptionalDynamic<?> $$1 = this.tag.get($$0);
         this.tag = this.tag.remove($$0);
         return $$1;
      }

      public void setComponent(String $$0, Dynamic<?> $$1) {
         this.components = this.components.set($$0, $$1);
      }

      public void setComponent(String $$0, OptionalDynamic<?> $$1) {
         $$1.result().ifPresent($$1x -> this.components = this.components.set($$0, $$1x));
      }

      public Dynamic<?> moveTagInto(String $$0, Dynamic<?> $$1, String $$2) {
         Optional<? extends Dynamic<?>> $$3 = this.removeTag($$0).result();
         return $$3.isPresent() ? $$1.set($$2, $$3.get()) : $$1;
      }

      public void moveTagToComponent(String $$0, String $$1, Dynamic<?> $$2) {
         Optional<? extends Dynamic<?>> $$3 = this.removeTag($$0).result();
         if ($$3.isPresent() && !$$3.get().equals($$2)) {
            this.setComponent($$1, (Dynamic<?>)$$3.get());
         }
      }

      public void moveTagToComponent(String $$0, String $$1) {
         this.removeTag($$0).result().ifPresent($$1x -> this.setComponent($$1, $$1x));
      }

      public void fixSubTag(String $$0, boolean $$1, UnaryOperator<Dynamic<?>> $$2) {
         OptionalDynamic<?> $$3 = this.tag.get($$0);
         if (!$$1 || !$$3.result().isEmpty()) {
            Dynamic<?> $$4 = $$3.orElseEmptyMap();
            $$4 = $$2.apply($$4);
            if ($$4.equals($$4.emptyMap())) {
               this.tag = this.tag.remove($$0);
            } else {
               this.tag = this.tag.set($$0, $$4);
            }
         }
      }

      public Dynamic<?> write() {
         Dynamic<?> $$0 = this.tag.emptyMap().set("id", this.tag.createString(this.item)).set("count", this.tag.createInt(this.count));
         if (!this.tag.equals(this.tag.emptyMap())) {
            this.components = this.components.set("minecraft:custom_data", this.tag);
         }

         if (!this.components.equals(this.tag.emptyMap())) {
            $$0 = $$0.set("components", this.components);
         }

         return mergeRemainder($$0, this.remainder);
      }

      private static <T> Dynamic<T> mergeRemainder(Dynamic<T> $$0, Dynamic<?> $$1) {
         DynamicOps<T> $$2 = $$0.getOps();
         return $$2.getMap($$0.getValue())
            .flatMap($$2x -> $$2.mergeToMap($$1.convert($$2).getValue(), $$2x))
            .map($$1x -> new Dynamic($$2, $$1x))
            .result()
            .orElse($$0);
      }

      public boolean is(String $$0) {
         return this.item.equals($$0);
      }

      public boolean is(Set<String> $$0) {
         return $$0.contains(this.item);
      }

      public boolean hasComponent(String $$0) {
         return this.components.get($$0).result().isPresent();
      }
   }
}
