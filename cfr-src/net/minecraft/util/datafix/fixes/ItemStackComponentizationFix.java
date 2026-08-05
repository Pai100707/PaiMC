/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.OptionalDynamic
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.jspecify.annotations.Nullable;

public class ItemStackComponentizationFix
extends DataFix {
    private static final int HIDE_ENCHANTMENTS = 1;
    private static final int HIDE_MODIFIERS = 2;
    private static final int HIDE_UNBREAKABLE = 4;
    private static final int HIDE_CAN_DESTROY = 8;
    private static final int HIDE_CAN_PLACE = 16;
    private static final int HIDE_ADDITIONAL = 32;
    private static final int HIDE_DYE = 64;
    private static final int HIDE_UPGRADES = 128;
    private static final Set<String> POTION_HOLDER_IDS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");
    private static final Set<String> BUCKETED_MOB_IDS = Set.of("minecraft:pufferfish_bucket", "minecraft:salmon_bucket", "minecraft:cod_bucket", "minecraft:tropical_fish_bucket", "minecraft:axolotl_bucket", "minecraft:tadpole_bucket");
    private static final List<String> BUCKETED_MOB_TAGS = List.of("NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag");
    private static final Set<String> BOOLEAN_BLOCK_STATE_PROPERTIES = Set.of("attached", "bottom", "conditional", "disarmed", "drag", "enabled", "extended", "eye", "falling", "hanging", "has_bottle_0", "has_bottle_1", "has_bottle_2", "has_record", "has_book", "inverted", "in_wall", "lit", "locked", "occupied", "open", "persistent", "powered", "short", "signal_fire", "snowy", "triggered", "unstable", "waterlogged", "berries", "bloom", "shrieking", "can_summon", "up", "down", "north", "east", "south", "west", "slot_0_occupied", "slot_1_occupied", "slot_2_occupied", "slot_3_occupied", "slot_4_occupied", "slot_5_occupied", "cracked", "crafting");
    private static final Splitter PROPERTY_SPLITTER = Splitter.on((char)',');

    public ItemStackComponentizationFix(Schema $$0) {
        super($$0, true);
    }

    private static void fixItemStack(ItemStackData $$02, Dynamic<?> $$12) {
        int $$22 = $$02.removeTag("HideFlags").asInt(0);
        $$02.moveTagToComponent("Damage", "minecraft:damage", $$12.createInt(0));
        $$02.moveTagToComponent("RepairCost", "minecraft:repair_cost", $$12.createInt(0));
        $$02.moveTagToComponent("CustomModelData", "minecraft:custom_model_data");
        $$02.removeTag("BlockStateTag").result().ifPresent($$1 -> $$02.setComponent("minecraft:block_state", ItemStackComponentizationFix.fixBlockStateTag($$1)));
        $$02.moveTagToComponent("EntityTag", "minecraft:entity_data");
        $$02.fixSubTag("BlockEntityTag", false, $$1 -> {
            String $$2 = NamespacedSchema.ensureNamespaced($$1.get("id").asString(""));
            Dynamic $$3 = ($$1 = ItemStackComponentizationFix.fixBlockEntityTag($$02, $$1, $$2)).remove("id");
            if ($$3.equals((Object)$$1.emptyMap())) {
                return $$3;
            }
            return $$1;
        });
        $$02.moveTagToComponent("BlockEntityTag", "minecraft:block_entity_data");
        if ($$02.removeTag("Unbreakable").asBoolean(false)) {
            Dynamic $$3 = $$12.emptyMap();
            if (($$22 & 4) != 0) {
                $$3 = $$3.set("show_in_tooltip", $$12.createBoolean(false));
            }
            $$02.setComponent("minecraft:unbreakable", $$3);
        }
        ItemStackComponentizationFix.fixEnchantments($$02, $$12, "Enchantments", "minecraft:enchantments", ($$22 & 1) != 0);
        if ($$02.is("minecraft:enchanted_book")) {
            ItemStackComponentizationFix.fixEnchantments($$02, $$12, "StoredEnchantments", "minecraft:stored_enchantments", ($$22 & 0x20) != 0);
        }
        $$02.fixSubTag("display", false, $$2 -> ItemStackComponentizationFix.fixDisplay($$02, $$2, $$22));
        ItemStackComponentizationFix.fixAdventureModeChecks($$02, $$12, $$22);
        ItemStackComponentizationFix.fixAttributeModifiers($$02, $$12, $$22);
        Optional $$4 = $$02.removeTag("Trim").result();
        if ($$4.isPresent()) {
            Dynamic $$5 = (Dynamic)$$4.get();
            if (($$22 & 0x80) != 0) {
                $$5 = $$5.set("show_in_tooltip", $$5.createBoolean(false));
            }
            $$02.setComponent("minecraft:trim", $$5);
        }
        if (($$22 & 0x20) != 0) {
            $$02.setComponent("minecraft:hide_additional_tooltip", $$12.emptyMap());
        }
        if ($$02.is("minecraft:crossbow")) {
            $$02.removeTag("Charged");
            $$02.moveTagToComponent("ChargedProjectiles", "minecraft:charged_projectiles", $$12.createList(Stream.empty()));
        }
        if ($$02.is("minecraft:bundle")) {
            $$02.moveTagToComponent("Items", "minecraft:bundle_contents", $$12.createList(Stream.empty()));
        }
        if ($$02.is("minecraft:filled_map")) {
            $$02.moveTagToComponent("map", "minecraft:map_id");
            Map<Dynamic, Dynamic> $$6 = $$02.removeTag("Decorations").asStream().map(ItemStackComponentizationFix::fixMapDecoration).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, ($$0, $$1) -> $$0));
            if (!$$6.isEmpty()) {
                $$02.setComponent("minecraft:map_decorations", $$12.createMap($$6));
            }
        }
        if ($$02.is(POTION_HOLDER_IDS)) {
            ItemStackComponentizationFix.fixPotionContents($$02, $$12);
        }
        if ($$02.is("minecraft:writable_book")) {
            ItemStackComponentizationFix.fixWritableBook($$02, $$12);
        }
        if ($$02.is("minecraft:written_book")) {
            ItemStackComponentizationFix.fixWrittenBook($$02, $$12);
        }
        if ($$02.is("minecraft:suspicious_stew")) {
            $$02.moveTagToComponent("effects", "minecraft:suspicious_stew_effects");
        }
        if ($$02.is("minecraft:debug_stick")) {
            $$02.moveTagToComponent("DebugProperty", "minecraft:debug_stick_state");
        }
        if ($$02.is(BUCKETED_MOB_IDS)) {
            ItemStackComponentizationFix.fixBucketedMobData($$02, $$12);
        }
        if ($$02.is("minecraft:goat_horn")) {
            $$02.moveTagToComponent("instrument", "minecraft:instrument");
        }
        if ($$02.is("minecraft:knowledge_book")) {
            $$02.moveTagToComponent("Recipes", "minecraft:recipes");
        }
        if ($$02.is("minecraft:compass")) {
            ItemStackComponentizationFix.fixLodestoneTracker($$02, $$12);
        }
        if ($$02.is("minecraft:firework_rocket")) {
            ItemStackComponentizationFix.fixFireworkRocket($$02);
        }
        if ($$02.is("minecraft:firework_star")) {
            ItemStackComponentizationFix.fixFireworkStar($$02);
        }
        if ($$02.is("minecraft:player_head")) {
            $$02.removeTag("SkullOwner").result().ifPresent($$1 -> $$02.setComponent("minecraft:profile", ItemStackComponentizationFix.fixProfile($$1)));
        }
    }

    private static Dynamic<?> fixBlockStateTag(Dynamic<?> $$0) {
        return (Dynamic)DataFixUtils.orElse($$0.asMapOpt().result().map($$02 -> $$02.collect(Collectors.toMap(Pair::getFirst, $$0 -> {
            Optional $$3;
            String $$1 = ((Dynamic)$$0.getFirst()).asString("");
            Dynamic $$2 = (Dynamic)$$0.getSecond();
            if (BOOLEAN_BLOCK_STATE_PROPERTIES.contains($$1) && ($$3 = $$2.asBoolean().result()).isPresent()) {
                return $$2.createString(String.valueOf($$3.get()));
            }
            Optional $$4 = $$2.asNumber().result();
            if ($$4.isPresent()) {
                return $$2.createString(((Number)$$4.get()).toString());
            }
            return $$2;
        }))).map(arg_0 -> $$0.createMap(arg_0)), $$0);
    }

    private static Dynamic<?> fixDisplay(ItemStackData $$0, Dynamic<?> $$12, int $$2) {
        Optional $$7;
        boolean $$5;
        $$12.get("Name").result().filter(LegacyComponentDataFixUtils::isStrictlyValidJson).ifPresent($$1 -> $$0.setComponent("minecraft:custom_name", (Dynamic<?>)$$1));
        OptionalDynamic $$3 = $$12.get("Lore");
        if ($$3.result().isPresent()) {
            $$0.setComponent("minecraft:lore", $$12.createList($$12.get("Lore").asStream().filter(LegacyComponentDataFixUtils::isStrictlyValidJson)));
        }
        Optional<Integer> $$4 = $$12.get("color").asNumber().result().map(Number::intValue);
        boolean bl = $$5 = ($$2 & 0x40) != 0;
        if ($$4.isPresent() || $$5) {
            Dynamic $$6 = $$12.emptyMap().set("rgb", $$12.createInt($$4.orElse(10511680).intValue()));
            if ($$5) {
                $$6 = $$6.set("show_in_tooltip", $$12.createBoolean(false));
            }
            $$0.setComponent("minecraft:dyed_color", $$6);
        }
        if (($$7 = $$12.get("LocName").asString().result()).isPresent()) {
            $$0.setComponent("minecraft:item_name", LegacyComponentDataFixUtils.createTranslatableComponent($$12.getOps(), (String)$$7.get()));
        }
        if ($$0.is("minecraft:filled_map")) {
            $$0.setComponent("minecraft:map_color", $$12.get("MapColor"));
            $$12 = $$12.remove("MapColor");
        }
        return $$12.remove("Name").remove("Lore").remove("color").remove("LocName");
    }

    private static <T> Dynamic<T> fixBlockEntityTag(ItemStackData $$02, Dynamic<T> $$1, String $$2) {
        $$02.setComponent("minecraft:lock", $$1.get("Lock"));
        $$1 = $$1.remove("Lock");
        Optional $$3 = $$1.get("LootTable").result();
        if ($$3.isPresent()) {
            Dynamic $$4 = $$1.emptyMap().set("loot_table", (Dynamic)$$3.get());
            long $$5 = $$1.get("LootTableSeed").asLong(0L);
            if ($$5 != 0L) {
                $$4 = $$4.set("seed", $$1.createLong($$5));
            }
            $$02.setComponent("minecraft:container_loot", $$4);
            $$1 = $$1.remove("LootTable").remove("LootTableSeed");
        }
        return switch ($$2) {
            case "minecraft:skull" -> {
                $$02.setComponent("minecraft:note_block_sound", $$1.get("note_block_sound"));
                yield $$1.remove("note_block_sound");
            }
            case "minecraft:decorated_pot" -> {
                $$02.setComponent("minecraft:pot_decorations", $$1.get("sherds"));
                Optional $$6 = $$1.get("item").result();
                if ($$6.isPresent()) {
                    $$02.setComponent("minecraft:container", $$1.createList(Stream.of($$1.emptyMap().set("slot", $$1.createInt(0)).set("item", (Dynamic)$$6.get()))));
                }
                yield $$1.remove("sherds").remove("item");
            }
            case "minecraft:banner" -> {
                $$02.setComponent("minecraft:banner_patterns", $$1.get("patterns"));
                Optional $$7 = $$1.get("Base").asNumber().result();
                if ($$7.isPresent()) {
                    $$02.setComponent("minecraft:base_color", $$1.createString(ExtraDataFixUtils.dyeColorIdToName(((Number)$$7.get()).intValue())));
                }
                yield $$1.remove("patterns").remove("Base");
            }
            case "minecraft:shulker_box", "minecraft:chest", "minecraft:trapped_chest", "minecraft:furnace", "minecraft:ender_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:brewing_stand", "minecraft:hopper", "minecraft:barrel", "minecraft:smoker", "minecraft:blast_furnace", "minecraft:campfire", "minecraft:chiseled_bookshelf", "minecraft:crafter" -> {
                List $$8 = $$1.get("Items").asList($$0 -> $$0.emptyMap().set("slot", $$0.createInt($$0.get("Slot").asByte((byte)0) & 0xFF)).set("item", $$0.remove("Slot")));
                if (!$$8.isEmpty()) {
                    $$02.setComponent("minecraft:container", $$1.createList($$8.stream()));
                }
                yield $$1.remove("Items");
            }
            case "minecraft:beehive" -> {
                $$02.setComponent("minecraft:bees", $$1.get("bees"));
                yield $$1.remove("bees");
            }
            default -> $$1;
        };
    }

    private static void fixEnchantments(ItemStackData $$02, Dynamic<?> $$1, String $$2, String $$3, boolean $$4) {
        OptionalDynamic<?> $$5 = $$02.removeTag($$2);
        List<Pair> $$6 = $$5.asList(Function.identity()).stream().flatMap($$0 -> ItemStackComponentizationFix.parseEnchantment($$0).stream()).filter($$0 -> (Integer)$$0.getSecond() > 0).toList();
        if (!$$6.isEmpty() || $$4) {
            Dynamic $$7 = $$1.emptyMap();
            Dynamic $$8 = $$1.emptyMap();
            for (Pair $$9 : $$6) {
                $$8 = $$8.set((String)$$9.getFirst(), $$1.createInt(((Integer)$$9.getSecond()).intValue()));
            }
            $$7 = $$7.set("levels", $$8);
            if ($$4) {
                $$7 = $$7.set("show_in_tooltip", $$1.createBoolean(false));
            }
            $$02.setComponent($$3, $$7);
        }
        if ($$5.result().isPresent() && $$6.isEmpty()) {
            $$02.setComponent("minecraft:enchantment_glint_override", $$1.createBoolean(true));
        }
    }

    private static Optional<Pair<String, Integer>> parseEnchantment(Dynamic<?> $$02) {
        return $$02.get("id").asString().apply2stable(($$0, $$1) -> Pair.of((Object)$$0, (Object)Mth.clamp($$1.intValue(), 0, 255)), $$02.get("lvl").asNumber()).result();
    }

    private static void fixAdventureModeChecks(ItemStackData $$0, Dynamic<?> $$1, int $$2) {
        ItemStackComponentizationFix.fixBlockStatePredicates($$0, $$1, "CanDestroy", "minecraft:can_break", ($$2 & 8) != 0);
        ItemStackComponentizationFix.fixBlockStatePredicates($$0, $$1, "CanPlaceOn", "minecraft:can_place_on", ($$2 & 0x10) != 0);
    }

    private static void fixBlockStatePredicates(ItemStackData $$02, Dynamic<?> $$1, String $$2, String $$3, boolean $$4) {
        Optional $$5 = $$02.removeTag($$2).result();
        if ($$5.isEmpty()) {
            return;
        }
        Dynamic $$6 = $$1.emptyMap().set("predicates", $$1.createList(((Dynamic)$$5.get()).asStream().map($$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asString().map($$1 -> ItemStackComponentizationFix.fixBlockStatePredicate($$0, $$1)).result(), (Object)$$0))));
        if ($$4) {
            $$6 = $$6.set("show_in_tooltip", $$1.createBoolean(false));
        }
        $$02.setComponent($$3, $$6);
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
        Dynamic $$6 = $$0.emptyMap().set("blocks", $$0.createString($$5.trim()));
        int $$7 = $$1.indexOf(93);
        if ($$2 != -1 && $$7 != -1) {
            Dynamic $$8 = $$0.emptyMap();
            Iterable $$9 = PROPERTY_SPLITTER.split((CharSequence)$$1.substring($$2 + 1, $$7));
            for (String $$10 : $$9) {
                int $$11 = $$10.indexOf(61);
                if ($$11 == -1) continue;
                String $$12 = $$10.substring(0, $$11).trim();
                String $$13 = $$10.substring($$11 + 1).trim();
                $$8 = $$8.set($$12, $$0.createString($$13));
            }
            $$6 = $$6.set("state", $$8);
        }
        int $$14 = $$1.indexOf(125);
        if ($$3 != -1 && $$14 != -1) {
            $$6 = $$6.set("nbt", $$0.createString($$1.substring($$3, $$14 + 1)));
        }
        return $$6;
    }

    private static void fixAttributeModifiers(ItemStackData $$0, Dynamic<?> $$1, int $$2) {
        OptionalDynamic<?> $$3 = $$0.removeTag("AttributeModifiers");
        if ($$3.result().isEmpty()) {
            return;
        }
        boolean $$4 = ($$2 & 2) != 0;
        List $$5 = $$3.asList(ItemStackComponentizationFix::fixAttributeModifier);
        Dynamic $$6 = $$1.emptyMap().set("modifiers", $$1.createList($$5.stream()));
        if ($$4) {
            $$6 = $$6.set("show_in_tooltip", $$1.createBoolean(false));
        }
        $$0.setComponent("minecraft:attribute_modifiers", $$6);
    }

    private static Dynamic<?> fixAttributeModifier(Dynamic<?> $$02) {
        Dynamic $$1 = $$02.emptyMap().set("name", $$02.createString("")).set("amount", $$02.createDouble(0.0)).set("operation", $$02.createString("add_value"));
        $$1 = Dynamic.copyField($$02, (String)"AttributeName", (Dynamic)$$1, (String)"type");
        $$1 = Dynamic.copyField($$02, (String)"Slot", (Dynamic)$$1, (String)"slot");
        $$1 = Dynamic.copyField($$02, (String)"UUID", (Dynamic)$$1, (String)"uuid");
        $$1 = Dynamic.copyField($$02, (String)"Name", (Dynamic)$$1, (String)"name");
        $$1 = Dynamic.copyField($$02, (String)"Amount", (Dynamic)$$1, (String)"amount");
        $$1 = Dynamic.copyAndFixField($$02, (String)"Operation", (Dynamic)$$1, (String)"operation", $$0 -> $$0.createString(switch ($$0.asInt(0)) {
            default -> "add_value";
            case 1 -> "add_multiplied_base";
            case 2 -> "add_multiplied_total";
        }));
        return $$1;
    }

    private static Pair<Dynamic<?>, Dynamic<?>> fixMapDecoration(Dynamic<?> $$0) {
        Dynamic $$1 = (Dynamic)DataFixUtils.orElseGet((Optional)$$0.get("id").result(), () -> $$0.createString(""));
        Dynamic $$2 = $$0.emptyMap().set("type", $$0.createString(ItemStackComponentizationFix.fixMapDecorationType($$0.get("type").asInt(0)))).set("x", $$0.createDouble($$0.get("x").asDouble(0.0))).set("z", $$0.createDouble($$0.get("z").asDouble(0.0))).set("rotation", $$0.createFloat((float)$$0.get("rot").asDouble(0.0)));
        return Pair.of((Object)$$1, (Object)$$2);
    }

    private static String fixMapDecorationType(int $$0) {
        return switch ($$0) {
            default -> "player";
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
        };
    }

    private static void fixPotionContents(ItemStackData $$02, Dynamic<?> $$1) {
        Dynamic<?> $$2 = $$1.emptyMap();
        Optional<String> $$3 = $$02.removeTag("Potion").asString().result().filter($$0 -> !$$0.equals("minecraft:empty"));
        if ($$3.isPresent()) {
            $$2 = $$2.set("potion", $$1.createString($$3.get()));
        }
        $$2 = $$02.moveTagInto("CustomPotionColor", $$2, "custom_color");
        if (!($$2 = $$02.moveTagInto("custom_potion_effects", $$2, "custom_effects")).equals((Object)$$1.emptyMap())) {
            $$02.setComponent("minecraft:potion_contents", $$2);
        }
    }

    private static void fixWritableBook(ItemStackData $$0, Dynamic<?> $$1) {
        Dynamic<?> $$2 = ItemStackComponentizationFix.fixBookPages($$0, $$1);
        if ($$2 != null) {
            $$0.setComponent("minecraft:writable_book_content", $$1.emptyMap().set("pages", $$2));
        }
    }

    private static void fixWrittenBook(ItemStackData $$0, Dynamic<?> $$1) {
        Dynamic<?> $$2 = ItemStackComponentizationFix.fixBookPages($$0, $$1);
        String $$3 = $$0.removeTag("title").asString("");
        Optional $$4 = $$0.removeTag("filtered_title").asString().result();
        Dynamic $$5 = $$1.emptyMap();
        $$5 = $$5.set("title", ItemStackComponentizationFix.createFilteredText($$1, $$3, $$4));
        $$5 = $$0.moveTagInto("author", $$5, "author");
        $$5 = $$0.moveTagInto("resolved", $$5, "resolved");
        $$5 = $$0.moveTagInto("generation", $$5, "generation");
        if ($$2 != null) {
            $$5 = $$5.set("pages", $$2);
        }
        $$0.setComponent("minecraft:written_book_content", $$5);
    }

    private static @Nullable Dynamic<?> fixBookPages(ItemStackData $$02, Dynamic<?> $$1) {
        List $$2 = $$02.removeTag("pages").asList($$0 -> $$0.asString(""));
        Map $$3 = $$02.removeTag("filtered_pages").asMap($$0 -> $$0.asString("0"), $$0 -> $$0.asString(""));
        if ($$2.isEmpty()) {
            return null;
        }
        ArrayList $$4 = new ArrayList($$2.size());
        for (int $$5 = 0; $$5 < $$2.size(); ++$$5) {
            String $$6 = (String)$$2.get($$5);
            String $$7 = (String)$$3.get(String.valueOf($$5));
            $$4.add(ItemStackComponentizationFix.createFilteredText($$1, $$6, Optional.ofNullable($$7)));
        }
        return $$1.createList($$4.stream());
    }

    private static Dynamic<?> createFilteredText(Dynamic<?> $$0, String $$1, Optional<String> $$2) {
        Dynamic $$3 = $$0.emptyMap().set("raw", $$0.createString($$1));
        if ($$2.isPresent()) {
            $$3 = $$3.set("filtered", $$0.createString($$2.get()));
        }
        return $$3;
    }

    private static void fixBucketedMobData(ItemStackData $$0, Dynamic<?> $$1) {
        Dynamic<?> $$2 = $$1.emptyMap();
        for (String $$3 : BUCKETED_MOB_TAGS) {
            $$2 = $$0.moveTagInto($$3, $$2, $$3);
        }
        if (!$$2.equals((Object)$$1.emptyMap())) {
            $$0.setComponent("minecraft:bucket_entity_data", $$2);
        }
    }

    private static void fixLodestoneTracker(ItemStackData $$0, Dynamic<?> $$1) {
        Optional $$2 = $$0.removeTag("LodestonePos").result();
        Optional $$3 = $$0.removeTag("LodestoneDimension").result();
        if ($$2.isEmpty() && $$3.isEmpty()) {
            return;
        }
        boolean $$4 = $$0.removeTag("LodestoneTracked").asBoolean(true);
        Dynamic $$5 = $$1.emptyMap();
        if ($$2.isPresent() && $$3.isPresent()) {
            $$5 = $$5.set("target", $$1.emptyMap().set("pos", (Dynamic)$$2.get()).set("dimension", (Dynamic)$$3.get()));
        }
        if (!$$4) {
            $$5 = $$5.set("tracked", $$1.createBoolean(false));
        }
        $$0.setComponent("minecraft:lodestone_tracker", $$5);
    }

    private static void fixFireworkStar(ItemStackData $$0) {
        $$0.fixSubTag("Explosion", true, $$1 -> {
            $$0.setComponent("minecraft:firework_explosion", ItemStackComponentizationFix.fixFireworkExplosion($$1));
            return $$1.remove("Type").remove("Colors").remove("FadeColors").remove("Trail").remove("Flicker");
        });
    }

    private static void fixFireworkRocket(ItemStackData $$0) {
        $$0.fixSubTag("Fireworks", true, $$1 -> {
            Stream<Dynamic> $$2 = $$1.get("Explosions").asStream().map(ItemStackComponentizationFix::fixFireworkExplosion);
            int $$3 = $$1.get("Flight").asInt(0);
            $$0.setComponent("minecraft:fireworks", $$1.emptyMap().set("explosions", $$1.createList($$2)).set("flight_duration", $$1.createByte((byte)$$3)));
            return $$1.remove("Explosions").remove("Flight");
        });
    }

    private static Dynamic<?> fixFireworkExplosion(Dynamic<?> $$0) {
        $$0 = $$0.set("shape", $$0.createString(switch ($$0.get("Type").asInt(0)) {
            default -> "small_ball";
            case 1 -> "large_ball";
            case 2 -> "star";
            case 3 -> "creeper";
            case 4 -> "burst";
        })).remove("Type");
        $$0 = $$0.renameField("Colors", "colors");
        $$0 = $$0.renameField("FadeColors", "fade_colors");
        $$0 = $$0.renameField("Trail", "has_trail");
        $$0 = $$0.renameField("Flicker", "has_twinkle");
        return $$0;
    }

    public static Dynamic<?> fixProfile(Dynamic<?> $$0) {
        Optional $$1 = $$0.asString().result();
        if ($$1.isPresent()) {
            if (ItemStackComponentizationFix.isValidPlayerName((String)$$1.get())) {
                return $$0.emptyMap().set("name", $$0.createString((String)$$1.get()));
            }
            return $$0.emptyMap();
        }
        String $$2 = $$0.get("Name").asString("");
        Optional $$3 = $$0.get("Id").result();
        Dynamic<?> $$4 = ItemStackComponentizationFix.fixProfileProperties($$0.get("Properties"));
        Dynamic $$5 = $$0.emptyMap();
        if (ItemStackComponentizationFix.isValidPlayerName($$2)) {
            $$5 = $$5.set("name", $$0.createString($$2));
        }
        if ($$3.isPresent()) {
            $$5 = $$5.set("id", (Dynamic)$$3.get());
        }
        if ($$4 != null) {
            $$5 = $$5.set("properties", $$4);
        }
        return $$5;
    }

    private static boolean isValidPlayerName(String $$02) {
        if ($$02.length() > 16) {
            return false;
        }
        return $$02.chars().filter($$0 -> $$0 <= 32 || $$0 >= 127).findAny().isEmpty();
    }

    private static @Nullable Dynamic<?> fixProfileProperties(OptionalDynamic<?> $$03) {
        Map $$12 = $$03.asMap($$0 -> $$0.asString(""), $$02 -> $$02.asList($$0 -> {
            String $$1 = $$0.get("Value").asString("");
            Optional $$2 = $$0.get("Signature").asString().result();
            return Pair.of((Object)$$1, (Object)$$2);
        }));
        if ($$12.isEmpty()) {
            return null;
        }
        return $$03.createList($$12.entrySet().stream().flatMap($$1 -> ((List)$$1.getValue()).stream().map($$2 -> {
            Dynamic $$3 = $$03.emptyMap().set("name", $$03.createString((String)$$1.getKey())).set("value", $$03.createString((String)$$2.getFirst()));
            Optional $$4 = (Optional)$$2.getSecond();
            if ($$4.isPresent()) {
                return $$3.set("signature", $$03.createString((String)$$4.get()));
            }
            return $$3;
        })));
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("ItemStack componentization", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), $$02 -> {
            Optional<Dynamic> $$1 = ItemStackData.read($$02).map($$0 -> {
                ItemStackComponentizationFix.fixItemStack($$0, $$0.tag);
                return $$0.write();
            });
            return (Dynamic)DataFixUtils.orElse($$1, (Object)$$02);
        });
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

        public static Optional<ItemStackData> read(Dynamic<?> $$0) {
            return $$0.get("id").asString().apply2stable(($$1, $$2) -> new ItemStackData((String)$$1, $$2.intValue(), (Dynamic<?>)$$0.remove("id").remove("Count")), $$0.get("Count").asNumber()).result();
        }

        public OptionalDynamic<?> removeTag(String $$0) {
            OptionalDynamic $$1 = this.tag.get($$0);
            this.tag = this.tag.remove($$0);
            return $$1;
        }

        public void setComponent(String $$0, Dynamic<?> $$1) {
            this.components = this.components.set($$0, $$1);
        }

        public void setComponent(String $$0, OptionalDynamic<?> $$12) {
            $$12.result().ifPresent($$1 -> {
                this.components = this.components.set($$0, $$1);
            });
        }

        public Dynamic<?> moveTagInto(String $$0, Dynamic<?> $$1, String $$2) {
            Optional $$3 = this.removeTag($$0).result();
            if ($$3.isPresent()) {
                return $$1.set($$2, (Dynamic)$$3.get());
            }
            return $$1;
        }

        public void moveTagToComponent(String $$0, String $$1, Dynamic<?> $$2) {
            Optional $$3 = this.removeTag($$0).result();
            if ($$3.isPresent() && !((Dynamic)$$3.get()).equals($$2)) {
                this.setComponent($$1, (Dynamic)$$3.get());
            }
        }

        public void moveTagToComponent(String $$0, String $$12) {
            this.removeTag($$0).result().ifPresent($$1 -> this.setComponent($$12, (Dynamic<?>)$$1));
        }

        public void fixSubTag(String $$0, boolean $$1, UnaryOperator<Dynamic<?>> $$2) {
            OptionalDynamic $$3 = this.tag.get($$0);
            if ($$1 && $$3.result().isEmpty()) {
                return;
            }
            Dynamic $$4 = $$3.orElseEmptyMap();
            this.tag = ($$4 = (Dynamic)$$2.apply($$4)).equals((Object)$$4.emptyMap()) ? this.tag.remove($$0) : this.tag.set($$0, $$4);
        }

        public Dynamic<?> write() {
            Dynamic $$0 = this.tag.emptyMap().set("id", this.tag.createString(this.item)).set("count", this.tag.createInt(this.count));
            if (!this.tag.equals((Object)this.tag.emptyMap())) {
                this.components = this.components.set("minecraft:custom_data", this.tag);
            }
            if (!this.components.equals((Object)this.tag.emptyMap())) {
                $$0 = $$0.set("components", this.components);
            }
            return ItemStackData.mergeRemainder($$0, this.remainder);
        }

        private static <T> Dynamic<T> mergeRemainder(Dynamic<T> $$0, Dynamic<?> $$12) {
            DynamicOps $$22 = $$0.getOps();
            return $$22.getMap($$0.getValue()).flatMap($$2 -> $$22.mergeToMap($$12.convert($$22).getValue(), $$2)).map($$1 -> new Dynamic($$22, $$1)).result().orElse($$0);
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

