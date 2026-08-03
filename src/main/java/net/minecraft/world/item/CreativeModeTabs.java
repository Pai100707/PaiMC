package net.minecraft.world.item;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import org.jspecify.annotations.Nullable;

public class CreativeModeTabs {
   private static final Identifier INVENTORY_BACKGROUND = net.minecraft.world.item.CreativeModeTab.createTextureLocation("inventory");
   private static final Identifier SEARCH_BACKGROUND = net.minecraft.world.item.CreativeModeTab.createTextureLocation("item_search");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> BUILDING_BLOCKS = createKey("building_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> COLORED_BLOCKS = createKey("colored_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> NATURAL_BLOCKS = createKey("natural_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> FUNCTIONAL_BLOCKS = createKey("functional_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> REDSTONE_BLOCKS = createKey("redstone_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> HOTBAR = createKey("hotbar");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> SEARCH = createKey("search");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> TOOLS_AND_UTILITIES = createKey("tools_and_utilities");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> COMBAT = createKey("combat");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> FOOD_AND_DRINKS = createKey("food_and_drinks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> INGREDIENTS = createKey("ingredients");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> SPAWN_EGGS = createKey("spawn_eggs");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> OP_BLOCKS = createKey("op_blocks");
   private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> INVENTORY = createKey("inventory");
   private static final Comparator<Holder<PaintingVariant>> PAINTING_COMPARATOR = Comparator.comparing(
      Holder::value, Comparator.comparingInt(PaintingVariant::area).thenComparing(PaintingVariant::width)
   );
   @Nullable
   private static net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters CACHED_PARAMETERS;

   private static ResourceKey<net.minecraft.world.item.CreativeModeTab> createKey(String $$0) {
      return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace($$0));
   }

   public static net.minecraft.world.item.CreativeModeTab bootstrap(Registry<net.minecraft.world.item.CreativeModeTab> $$0) {
      Registry.register(
         $$0,
         BUILDING_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.buildingBlocks"))
            .icon(() -> new net.minecraft.world.item.ItemStack(Blocks.BRICKS))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.OAK_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.OAK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.OAK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.OAK_FENCE);
               $$1.accept(net.minecraft.world.item.Items.OAK_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.OAK_DOOR);
               $$1.accept(net.minecraft.world.item.Items.OAK_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.OAK_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.OAK_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_LOG);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_SPRUCE_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_SPRUCE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_FENCE);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_DOOR);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_LOG);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_BIRCH_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_BIRCH_WOOD);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_SLAB);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_FENCE);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_DOOR);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_LOG);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_JUNGLE_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_JUNGLE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_FENCE);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_DOOR);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_LOG);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_ACACIA_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_ACACIA_WOOD);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_SLAB);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_FENCE);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_DOOR);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_DARK_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_DARK_OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_FENCE);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_DOOR);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_LOG);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_MANGROVE_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_MANGROVE_WOOD);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_FENCE);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_DOOR);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_LOG);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_CHERRY_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_CHERRY_WOOD);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_SLAB);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_FENCE);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_DOOR);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_PALE_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_PALE_OAK_WOOD);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_FENCE);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_DOOR);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_BAMBOO_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_MOSAIC);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_MOSAIC_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_SLAB);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_MOSAIC_SLAB);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_FENCE);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_DOOR);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_STEM);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_HYPHAE);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_CRIMSON_STEM);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_CRIMSON_HYPHAE);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_SLAB);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_FENCE);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_DOOR);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.WARPED_STEM);
               $$1.accept(net.minecraft.world.item.Items.WARPED_HYPHAE);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_WARPED_STEM);
               $$1.accept(net.minecraft.world.item.Items.STRIPPED_WARPED_HYPHAE);
               $$1.accept(net.minecraft.world.item.Items.WARPED_PLANKS);
               $$1.accept(net.minecraft.world.item.Items.WARPED_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WARPED_SLAB);
               $$1.accept(net.minecraft.world.item.Items.WARPED_FENCE);
               $$1.accept(net.minecraft.world.item.Items.WARPED_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.WARPED_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WARPED_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WARPED_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.WARPED_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.STONE);
               $$1.accept(net.minecraft.world.item.Items.STONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.STONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.STONE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.STONE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.COBBLESTONE);
               $$1.accept(net.minecraft.world.item.Items.COBBLESTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.COBBLESTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COBBLESTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_COBBLESTONE);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_COBBLESTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_COBBLESTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_COBBLESTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_STONE);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_STONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.STONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.CRACKED_STONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.STONE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.STONE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.STONE_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_STONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_STONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_STONE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_STONE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.MOSSY_STONE_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.GRANITE);
               $$1.accept(net.minecraft.world.item.Items.GRANITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.GRANITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.GRANITE_WALL);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_GRANITE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_GRANITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_GRANITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DIORITE);
               $$1.accept(net.minecraft.world.item.Items.DIORITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.DIORITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DIORITE_WALL);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DIORITE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DIORITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DIORITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.ANDESITE);
               $$1.accept(net.minecraft.world.item.Items.ANDESITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.ANDESITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.ANDESITE_WALL);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_ANDESITE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_ANDESITE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_ANDESITE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.COBBLED_DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.COBBLED_DEEPSLATE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.COBBLED_DEEPSLATE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COBBLED_DEEPSLATE_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DEEPSLATE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DEEPSLATE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_DEEPSLATE_WALL);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.CRACKED_DEEPSLATE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_TILES);
               $$1.accept(net.minecraft.world.item.Items.CRACKED_DEEPSLATE_TILES);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_TILE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_TILE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_TILE_WALL);
               $$1.accept(net.minecraft.world.item.Items.REINFORCED_DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.TUFF);
               $$1.accept(net.minecraft.world.item.Items.TUFF_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.TUFF_SLAB);
               $$1.accept(net.minecraft.world.item.Items.TUFF_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_TUFF);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_TUFF);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_TUFF_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_TUFF_SLAB);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_TUFF_WALL);
               $$1.accept(net.minecraft.world.item.Items.TUFF_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.TUFF_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.TUFF_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.TUFF_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_TUFF_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.BRICKS);
               $$1.accept(net.minecraft.world.item.Items.BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.PACKED_MUD);
               $$1.accept(net.minecraft.world.item.Items.MUD_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.MUD_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.MUD_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.MUD_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_RESIN_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.SANDSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.SANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.SANDSTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_SANDSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_SANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.CUT_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.CUT_STANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.RED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.RED_SANDSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.RED_SANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.RED_SANDSTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_RED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_RED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_RED_SANDSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_RED_SANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.CUT_RED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.CUT_RED_SANDSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.SEA_LANTERN);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_WALL);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.DARK_PRISMARINE);
               $$1.accept(net.minecraft.world.item.Items.DARK_PRISMARINE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.DARK_PRISMARINE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.NETHERRACK);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.CRACKED_NETHER_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICK_FENCE);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_NETHER_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.RED_NETHER_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.RED_NETHER_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.RED_NETHER_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.RED_NETHER_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.BASALT);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_BASALT);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BASALT);
               $$1.accept(net.minecraft.world.item.Items.BLACKSTONE);
               $$1.accept(net.minecraft.world.item.Items.GILDED_BLACKSTONE);
               $$1.accept(net.minecraft.world.item.Items.BLACKSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.BLACKSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.BLACKSTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_POLISHED_BLACKSTONE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_SLAB);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_WALL);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.CRACKED_POLISHED_BLACKSTONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.POLISHED_BLACKSTONE_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.END_STONE);
               $$1.accept(net.minecraft.world.item.Items.END_STONE_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.END_STONE_BRICK_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.END_STONE_BRICK_SLAB);
               $$1.accept(net.minecraft.world.item.Items.END_STONE_BRICK_WALL);
               $$1.accept(net.minecraft.world.item.Items.PURPUR_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.PURPUR_PILLAR);
               $$1.accept(net.minecraft.world.item.Items.PURPUR_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.PURPUR_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.IRON_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.IRON_BARS);
               $$1.accept(net.minecraft.world.item.Items.IRON_DOOR);
               $$1.accept(net.minecraft.world.item.Items.IRON_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.IRON_CHAIN);
               $$1.accept(net.minecraft.world.item.Items.GOLD_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.EMERALD_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.LAPIS_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DIAMOND_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.NETHERITE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ_SLAB);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_QUARTZ_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ_BRICKS);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ_PILLAR);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_QUARTZ);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_QUARTZ_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_QUARTZ_SLAB);
               $$1.accept(net.minecraft.world.item.Items.AMETHYST_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.unaffected());
               $$1.accept(net.minecraft.world.item.Items.COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.unaffected());
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.exposed());
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.exposed());
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.weathered());
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.weathered());
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.oxidized());
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.oxidized());
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.WAXED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.WAXED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WAXED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.waxed());
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.waxed());
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.waxedExposed());
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.waxedExposed());
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.waxedWeathered());
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.waxedWeathered());
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_CHISELED_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_GRATE);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_CUT_COPPER);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_BARS.waxedOxidized());
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_DOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.COPPER_CHAIN.waxedOxidized());
            })
            .build()
      );
      Registry.register(
         $$0,
         COLORED_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 1)
            .title(Component.translatable("itemGroup.coloredBlocks"))
            .icon(() -> new net.minecraft.world.item.ItemStack(Blocks.CYAN_WOOL))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.WHITE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_WOOL);
               $$1.accept(net.minecraft.world.item.Items.GRAY_WOOL);
               $$1.accept(net.minecraft.world.item.Items.BLACK_WOOL);
               $$1.accept(net.minecraft.world.item.Items.BROWN_WOOL);
               $$1.accept(net.minecraft.world.item.Items.RED_WOOL);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_WOOL);
               $$1.accept(net.minecraft.world.item.Items.LIME_WOOL);
               $$1.accept(net.minecraft.world.item.Items.GREEN_WOOL);
               $$1.accept(net.minecraft.world.item.Items.CYAN_WOOL);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.BLUE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_WOOL);
               $$1.accept(net.minecraft.world.item.Items.PINK_WOOL);
               $$1.accept(net.minecraft.world.item.Items.WHITE_CARPET);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_CARPET);
               $$1.accept(net.minecraft.world.item.Items.GRAY_CARPET);
               $$1.accept(net.minecraft.world.item.Items.BLACK_CARPET);
               $$1.accept(net.minecraft.world.item.Items.BROWN_CARPET);
               $$1.accept(net.minecraft.world.item.Items.RED_CARPET);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_CARPET);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_CARPET);
               $$1.accept(net.minecraft.world.item.Items.LIME_CARPET);
               $$1.accept(net.minecraft.world.item.Items.GREEN_CARPET);
               $$1.accept(net.minecraft.world.item.Items.CYAN_CARPET);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_CARPET);
               $$1.accept(net.minecraft.world.item.Items.BLUE_CARPET);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_CARPET);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_CARPET);
               $$1.accept(net.minecraft.world.item.Items.PINK_CARPET);
               $$1.accept(net.minecraft.world.item.Items.TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.WHITE_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.GRAY_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BLACK_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BROWN_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.RED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIME_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.GREEN_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.CYAN_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BLUE_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.PINK_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.WHITE_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.GRAY_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.BLACK_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.BROWN_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.RED_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.LIME_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.GREEN_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.CYAN_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.BLUE_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.PINK_CONCRETE);
               $$1.accept(net.minecraft.world.item.Items.WHITE_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.GRAY_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.BLACK_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.BROWN_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.RED_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.LIME_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.GREEN_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.CYAN_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.BLUE_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.PINK_CONCRETE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.WHITE_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.GRAY_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BLACK_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BROWN_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.RED_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIME_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.GREEN_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.CYAN_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.BLUE_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.PINK_GLAZED_TERRACOTTA);
               $$1.accept(net.minecraft.world.item.Items.GLASS);
               $$1.accept(net.minecraft.world.item.Items.TINTED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.WHITE_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.GRAY_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.BLACK_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.BROWN_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.RED_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.LIME_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.GREEN_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.CYAN_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.BLUE_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.PINK_STAINED_GLASS);
               $$1.accept(net.minecraft.world.item.Items.GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.WHITE_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.GRAY_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.BLACK_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.BROWN_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.RED_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.LIME_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.GREEN_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.CYAN_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.BLUE_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.PINK_STAINED_GLASS_PANE);
               $$1.accept(net.minecraft.world.item.Items.SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.WHITE_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.GRAY_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.BLACK_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.BROWN_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.RED_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.LIME_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.GREEN_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.CYAN_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.BLUE_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.PINK_SHULKER_BOX);
               $$1.accept(net.minecraft.world.item.Items.WHITE_BED);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_BED);
               $$1.accept(net.minecraft.world.item.Items.GRAY_BED);
               $$1.accept(net.minecraft.world.item.Items.BLACK_BED);
               $$1.accept(net.minecraft.world.item.Items.BROWN_BED);
               $$1.accept(net.minecraft.world.item.Items.RED_BED);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_BED);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_BED);
               $$1.accept(net.minecraft.world.item.Items.LIME_BED);
               $$1.accept(net.minecraft.world.item.Items.GREEN_BED);
               $$1.accept(net.minecraft.world.item.Items.CYAN_BED);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_BED);
               $$1.accept(net.minecraft.world.item.Items.BLUE_BED);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_BED);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_BED);
               $$1.accept(net.minecraft.world.item.Items.PINK_BED);
               $$1.accept(net.minecraft.world.item.Items.CANDLE);
               $$1.accept(net.minecraft.world.item.Items.WHITE_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.GRAY_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.BLACK_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.BROWN_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.RED_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.LIME_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.GREEN_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.CYAN_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.BLUE_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.PINK_CANDLE);
               $$1.accept(net.minecraft.world.item.Items.WHITE_BANNER);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_BANNER);
               $$1.accept(net.minecraft.world.item.Items.GRAY_BANNER);
               $$1.accept(net.minecraft.world.item.Items.BLACK_BANNER);
               $$1.accept(net.minecraft.world.item.Items.BROWN_BANNER);
               $$1.accept(net.minecraft.world.item.Items.RED_BANNER);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_BANNER);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_BANNER);
               $$1.accept(net.minecraft.world.item.Items.LIME_BANNER);
               $$1.accept(net.minecraft.world.item.Items.GREEN_BANNER);
               $$1.accept(net.minecraft.world.item.Items.CYAN_BANNER);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_BANNER);
               $$1.accept(net.minecraft.world.item.Items.BLUE_BANNER);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_BANNER);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_BANNER);
               $$1.accept(net.minecraft.world.item.Items.PINK_BANNER);
            })
            .build()
      );
      Registry.register(
         $$0,
         NATURAL_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 2)
            .title(Component.translatable("itemGroup.natural"))
            .icon(() -> new net.minecraft.world.item.ItemStack(Blocks.GRASS_BLOCK))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.GRASS_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.PODZOL);
               $$1.accept(net.minecraft.world.item.Items.MYCELIUM);
               $$1.accept(net.minecraft.world.item.Items.DIRT_PATH);
               $$1.accept(net.minecraft.world.item.Items.DIRT);
               $$1.accept(net.minecraft.world.item.Items.COARSE_DIRT);
               $$1.accept(net.minecraft.world.item.Items.ROOTED_DIRT);
               $$1.accept(net.minecraft.world.item.Items.FARMLAND);
               $$1.accept(net.minecraft.world.item.Items.MUD);
               $$1.accept(net.minecraft.world.item.Items.CLAY);
               $$1.accept(net.minecraft.world.item.Items.GRAVEL);
               $$1.accept(net.minecraft.world.item.Items.SAND);
               $$1.accept(net.minecraft.world.item.Items.SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.RED_SAND);
               $$1.accept(net.minecraft.world.item.Items.RED_SANDSTONE);
               $$1.accept(net.minecraft.world.item.Items.ICE);
               $$1.accept(net.minecraft.world.item.Items.PACKED_ICE);
               $$1.accept(net.minecraft.world.item.Items.BLUE_ICE);
               $$1.accept(net.minecraft.world.item.Items.SNOW_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.SNOW);
               $$1.accept(net.minecraft.world.item.Items.MOSS_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.MOSS_CARPET);
               $$1.accept(net.minecraft.world.item.Items.PALE_MOSS_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.PALE_MOSS_CARPET);
               $$1.accept(net.minecraft.world.item.Items.PALE_HANGING_MOSS);
               $$1.accept(net.minecraft.world.item.Items.STONE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE);
               $$1.accept(net.minecraft.world.item.Items.GRANITE);
               $$1.accept(net.minecraft.world.item.Items.DIORITE);
               $$1.accept(net.minecraft.world.item.Items.ANDESITE);
               $$1.accept(net.minecraft.world.item.Items.CALCITE);
               $$1.accept(net.minecraft.world.item.Items.TUFF);
               $$1.accept(net.minecraft.world.item.Items.DRIPSTONE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.POINTED_DRIPSTONE);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE);
               $$1.accept(net.minecraft.world.item.Items.MAGMA_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.OBSIDIAN);
               $$1.accept(net.minecraft.world.item.Items.CRYING_OBSIDIAN);
               $$1.accept(net.minecraft.world.item.Items.NETHERRACK);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_NYLIUM);
               $$1.accept(net.minecraft.world.item.Items.WARPED_NYLIUM);
               $$1.accept(net.minecraft.world.item.Items.SOUL_SAND);
               $$1.accept(net.minecraft.world.item.Items.SOUL_SOIL);
               $$1.accept(net.minecraft.world.item.Items.BONE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BLACKSTONE);
               $$1.accept(net.minecraft.world.item.Items.BASALT);
               $$1.accept(net.minecraft.world.item.Items.SMOOTH_BASALT);
               $$1.accept(net.minecraft.world.item.Items.END_STONE);
               $$1.accept(net.minecraft.world.item.Items.COAL_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_COAL_ORE);
               $$1.accept(net.minecraft.world.item.Items.IRON_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE);
               $$1.accept(net.minecraft.world.item.Items.COPPER_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_COPPER_ORE);
               $$1.accept(net.minecraft.world.item.Items.GOLD_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_GOLD_ORE);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_REDSTONE_ORE);
               $$1.accept(net.minecraft.world.item.Items.EMERALD_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_EMERALD_ORE);
               $$1.accept(net.minecraft.world.item.Items.LAPIS_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_LAPIS_ORE);
               $$1.accept(net.minecraft.world.item.Items.DIAMOND_ORE);
               $$1.accept(net.minecraft.world.item.Items.DEEPSLATE_DIAMOND_ORE);
               $$1.accept(net.minecraft.world.item.Items.NETHER_GOLD_ORE);
               $$1.accept(net.minecraft.world.item.Items.NETHER_QUARTZ_ORE);
               $$1.accept(net.minecraft.world.item.Items.ANCIENT_DEBRIS);
               $$1.accept(net.minecraft.world.item.Items.RAW_IRON_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.RAW_COPPER_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.RAW_GOLD_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.GLOWSTONE);
               $$1.accept(net.minecraft.world.item.Items.AMETHYST_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BUDDING_AMETHYST);
               $$1.accept(net.minecraft.world.item.Items.SMALL_AMETHYST_BUD);
               $$1.accept(net.minecraft.world.item.Items.MEDIUM_AMETHYST_BUD);
               $$1.accept(net.minecraft.world.item.Items.LARGE_AMETHYST_BUD);
               $$1.accept(net.minecraft.world.item.Items.AMETHYST_CLUSTER);
               $$1.accept(net.minecraft.world.item.Items.OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_LOG);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_LOG);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_LOG);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_LOG);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_LOG);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_ROOTS);
               $$1.accept(net.minecraft.world.item.Items.MUDDY_MANGROVE_ROOTS);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_LOG);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_LOG);
               $$1.accept(net.minecraft.world.item.Items.MUSHROOM_STEM);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_STEM);
               $$1.accept(net.minecraft.world.item.Items.WARPED_STEM);
               $$1.accept(net.minecraft.world.item.Items.OAK_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.AZALEA_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.FLOWERING_AZALEA_LEAVES);
               $$1.accept(net.minecraft.world.item.Items.BROWN_MUSHROOM_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.RED_MUSHROOM_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.NETHER_WART_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.WARPED_WART_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.SHROOMLIGHT);
               $$1.accept(net.minecraft.world.item.Items.OAK_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.SPRUCE_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.BIRCH_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.JUNGLE_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.ACACIA_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.DARK_OAK_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.MANGROVE_PROPAGULE);
               $$1.accept(net.minecraft.world.item.Items.CHERRY_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.PALE_OAK_SAPLING);
               $$1.accept(net.minecraft.world.item.Items.AZALEA);
               $$1.accept(net.minecraft.world.item.Items.FLOWERING_AZALEA);
               $$1.accept(net.minecraft.world.item.Items.BROWN_MUSHROOM);
               $$1.accept(net.minecraft.world.item.Items.RED_MUSHROOM);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_FUNGUS);
               $$1.accept(net.minecraft.world.item.Items.WARPED_FUNGUS);
               $$1.accept(net.minecraft.world.item.Items.SHORT_GRASS);
               $$1.accept(net.minecraft.world.item.Items.FERN);
               $$1.accept(net.minecraft.world.item.Items.DRY_SHORT_GRASS);
               $$1.accept(net.minecraft.world.item.Items.BUSH);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BUSH);
               $$1.accept(net.minecraft.world.item.Items.DANDELION);
               $$1.accept(net.minecraft.world.item.Items.POPPY);
               $$1.accept(net.minecraft.world.item.Items.BLUE_ORCHID);
               $$1.accept(net.minecraft.world.item.Items.ALLIUM);
               $$1.accept(net.minecraft.world.item.Items.AZURE_BLUET);
               $$1.accept(net.minecraft.world.item.Items.RED_TULIP);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_TULIP);
               $$1.accept(net.minecraft.world.item.Items.WHITE_TULIP);
               $$1.accept(net.minecraft.world.item.Items.PINK_TULIP);
               $$1.accept(net.minecraft.world.item.Items.OXEYE_DAISY);
               $$1.accept(net.minecraft.world.item.Items.CORNFLOWER);
               $$1.accept(net.minecraft.world.item.Items.LILY_OF_THE_VALLEY);
               $$1.accept(net.minecraft.world.item.Items.TORCHFLOWER);
               $$1.accept(net.minecraft.world.item.Items.CACTUS_FLOWER);
               $$1.accept(net.minecraft.world.item.Items.CLOSED_EYEBLOSSOM);
               $$1.accept(net.minecraft.world.item.Items.OPEN_EYEBLOSSOM);
               $$1.accept(net.minecraft.world.item.Items.WITHER_ROSE);
               $$1.accept(net.minecraft.world.item.Items.PINK_PETALS);
               $$1.accept(net.minecraft.world.item.Items.WILDFLOWERS);
               $$1.accept(net.minecraft.world.item.Items.LEAF_LITTER);
               $$1.accept(net.minecraft.world.item.Items.SPORE_BLOSSOM);
               $$1.accept(net.minecraft.world.item.Items.FIREFLY_BUSH);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO);
               $$1.accept(net.minecraft.world.item.Items.SUGAR_CANE);
               $$1.accept(net.minecraft.world.item.Items.CACTUS);
               $$1.accept(net.minecraft.world.item.Items.CRIMSON_ROOTS);
               $$1.accept(net.minecraft.world.item.Items.WARPED_ROOTS);
               $$1.accept(net.minecraft.world.item.Items.NETHER_SPROUTS);
               $$1.accept(net.minecraft.world.item.Items.WEEPING_VINES);
               $$1.accept(net.minecraft.world.item.Items.TWISTING_VINES);
               $$1.accept(net.minecraft.world.item.Items.VINE);
               $$1.accept(net.minecraft.world.item.Items.TALL_GRASS);
               $$1.accept(net.minecraft.world.item.Items.LARGE_FERN);
               $$1.accept(net.minecraft.world.item.Items.DRY_TALL_GRASS);
               $$1.accept(net.minecraft.world.item.Items.SUNFLOWER);
               $$1.accept(net.minecraft.world.item.Items.LILAC);
               $$1.accept(net.minecraft.world.item.Items.ROSE_BUSH);
               $$1.accept(net.minecraft.world.item.Items.PEONY);
               $$1.accept(net.minecraft.world.item.Items.PITCHER_PLANT);
               $$1.accept(net.minecraft.world.item.Items.BIG_DRIPLEAF);
               $$1.accept(net.minecraft.world.item.Items.SMALL_DRIPLEAF);
               $$1.accept(net.minecraft.world.item.Items.CHORUS_PLANT);
               $$1.accept(net.minecraft.world.item.Items.CHORUS_FLOWER);
               $$1.accept(net.minecraft.world.item.Items.GLOW_LICHEN);
               $$1.accept(net.minecraft.world.item.Items.HANGING_ROOTS);
               $$1.accept(net.minecraft.world.item.Items.FROGSPAWN);
               $$1.accept(net.minecraft.world.item.Items.TURTLE_EGG);
               $$1.accept(net.minecraft.world.item.Items.SNIFFER_EGG);
               $$1.accept(net.minecraft.world.item.Items.DRIED_GHAST);
               $$1.accept(net.minecraft.world.item.Items.WHEAT_SEEDS);
               $$1.accept(net.minecraft.world.item.Items.COCOA_BEANS);
               $$1.accept(net.minecraft.world.item.Items.PUMPKIN_SEEDS);
               $$1.accept(net.minecraft.world.item.Items.MELON_SEEDS);
               $$1.accept(net.minecraft.world.item.Items.BEETROOT_SEEDS);
               $$1.accept(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS);
               $$1.accept(net.minecraft.world.item.Items.PITCHER_POD);
               $$1.accept(net.minecraft.world.item.Items.GLOW_BERRIES);
               $$1.accept(net.minecraft.world.item.Items.SWEET_BERRIES);
               $$1.accept(net.minecraft.world.item.Items.NETHER_WART);
               $$1.accept(net.minecraft.world.item.Items.LILY_PAD);
               $$1.accept(net.minecraft.world.item.Items.SEAGRASS);
               $$1.accept(net.minecraft.world.item.Items.SEA_PICKLE);
               $$1.accept(net.minecraft.world.item.Items.KELP);
               $$1.accept(net.minecraft.world.item.Items.DRIED_KELP_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.TUBE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BRAIN_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BUBBLE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.FIRE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.HORN_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DEAD_TUBE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BRAIN_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BUBBLE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DEAD_FIRE_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DEAD_HORN_CORAL_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.TUBE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.BRAIN_CORAL);
               $$1.accept(net.minecraft.world.item.Items.BUBBLE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.FIRE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.HORN_CORAL);
               $$1.accept(net.minecraft.world.item.Items.DEAD_TUBE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BRAIN_CORAL);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BUBBLE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.DEAD_FIRE_CORAL);
               $$1.accept(net.minecraft.world.item.Items.DEAD_HORN_CORAL);
               $$1.accept(net.minecraft.world.item.Items.TUBE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.BRAIN_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.BUBBLE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.FIRE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.HORN_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.DEAD_TUBE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BRAIN_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.DEAD_BUBBLE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.DEAD_FIRE_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.DEAD_HORN_CORAL_FAN);
               $$1.accept(net.minecraft.world.item.Items.SPONGE);
               $$1.accept(net.minecraft.world.item.Items.WET_SPONGE);
               $$1.accept(net.minecraft.world.item.Items.MELON);
               $$1.accept(net.minecraft.world.item.Items.PUMPKIN);
               $$1.accept(net.minecraft.world.item.Items.CARVED_PUMPKIN);
               $$1.accept(net.minecraft.world.item.Items.JACK_O_LANTERN);
               $$1.accept(net.minecraft.world.item.Items.HAY_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.BEE_NEST);
               $$1.accept(net.minecraft.world.item.Items.HONEYCOMB_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.SLIME_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.HONEY_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.OCHRE_FROGLIGHT);
               $$1.accept(net.minecraft.world.item.Items.VERDANT_FROGLIGHT);
               $$1.accept(net.minecraft.world.item.Items.PEARLESCENT_FROGLIGHT);
               $$1.accept(net.minecraft.world.item.Items.SCULK);
               $$1.accept(net.minecraft.world.item.Items.SCULK_VEIN);
               $$1.accept(net.minecraft.world.item.Items.SCULK_CATALYST);
               $$1.accept(net.minecraft.world.item.Items.SCULK_SHRIEKER);
               $$1.accept(net.minecraft.world.item.Items.SCULK_SENSOR);
               $$1.accept(net.minecraft.world.item.Items.COBWEB);
               $$1.accept(net.minecraft.world.item.Items.BEDROCK);
            })
            .build()
      );
      Registry.register(
         $$0,
         FUNCTIONAL_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 3)
            .title(Component.translatable("itemGroup.functional"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SIGN))
            .displayItems(
               ($$0x, $$1) -> {
                  $$1.accept(net.minecraft.world.item.Items.TORCH);
                  $$1.accept(net.minecraft.world.item.Items.SOUL_TORCH);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_TORCH);
                  $$1.accept(net.minecraft.world.item.Items.REDSTONE_TORCH);
                  $$1.accept(net.minecraft.world.item.Items.LANTERN);
                  $$1.accept(net.minecraft.world.item.Items.SOUL_LANTERN);
                  net.minecraft.world.item.Items.COPPER_LANTERN.forEach($$1::accept);
                  $$1.accept(net.minecraft.world.item.Items.IRON_CHAIN);
                  net.minecraft.world.item.Items.COPPER_CHAIN.forEach($$1::accept);
                  $$1.accept(net.minecraft.world.item.Items.END_ROD);
                  $$1.accept(net.minecraft.world.item.Items.SEA_LANTERN);
                  $$1.accept(net.minecraft.world.item.Items.REDSTONE_LAMP);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_BULB);
                  $$1.accept(net.minecraft.world.item.Items.GLOWSTONE);
                  $$1.accept(net.minecraft.world.item.Items.SHROOMLIGHT);
                  $$1.accept(net.minecraft.world.item.Items.OCHRE_FROGLIGHT);
                  $$1.accept(net.minecraft.world.item.Items.VERDANT_FROGLIGHT);
                  $$1.accept(net.minecraft.world.item.Items.PEARLESCENT_FROGLIGHT);
                  $$1.accept(net.minecraft.world.item.Items.CRYING_OBSIDIAN);
                  $$1.accept(net.minecraft.world.item.Items.GLOW_LICHEN);
                  $$1.accept(net.minecraft.world.item.Items.MAGMA_BLOCK);
                  $$1.accept(net.minecraft.world.item.Items.CRAFTING_TABLE);
                  $$1.accept(net.minecraft.world.item.Items.STONECUTTER);
                  $$1.accept(net.minecraft.world.item.Items.CARTOGRAPHY_TABLE);
                  $$1.accept(net.minecraft.world.item.Items.FLETCHING_TABLE);
                  $$1.accept(net.minecraft.world.item.Items.SMITHING_TABLE);
                  $$1.accept(net.minecraft.world.item.Items.GRINDSTONE);
                  $$1.accept(net.minecraft.world.item.Items.LOOM);
                  $$1.accept(net.minecraft.world.item.Items.FURNACE);
                  $$1.accept(net.minecraft.world.item.Items.SMOKER);
                  $$1.accept(net.minecraft.world.item.Items.BLAST_FURNACE);
                  $$1.accept(net.minecraft.world.item.Items.CAMPFIRE);
                  $$1.accept(net.minecraft.world.item.Items.SOUL_CAMPFIRE);
                  $$1.accept(net.minecraft.world.item.Items.ANVIL);
                  $$1.accept(net.minecraft.world.item.Items.CHIPPED_ANVIL);
                  $$1.accept(net.minecraft.world.item.Items.DAMAGED_ANVIL);
                  $$1.accept(net.minecraft.world.item.Items.COMPOSTER);
                  $$1.accept(net.minecraft.world.item.Items.NOTE_BLOCK);
                  $$1.accept(net.minecraft.world.item.Items.JUKEBOX);
                  $$1.accept(net.minecraft.world.item.Items.ENCHANTING_TABLE);
                  $$1.accept(net.minecraft.world.item.Items.END_CRYSTAL);
                  $$1.accept(net.minecraft.world.item.Items.BREWING_STAND);
                  $$1.accept(net.minecraft.world.item.Items.CAULDRON);
                  $$1.accept(net.minecraft.world.item.Items.BELL);
                  $$1.accept(net.minecraft.world.item.Items.BEACON);
                  $$1.accept(net.minecraft.world.item.Items.CONDUIT);
                  $$1.accept(net.minecraft.world.item.Items.LODESTONE);
                  $$1.accept(net.minecraft.world.item.Items.LADDER);
                  $$1.accept(net.minecraft.world.item.Items.SCAFFOLDING);
                  $$1.accept(net.minecraft.world.item.Items.BEE_NEST);
                  $$1.accept(net.minecraft.world.item.Items.BEEHIVE);
                  $$1.accept(net.minecraft.world.item.Items.SUSPICIOUS_SAND);
                  $$1.accept(net.minecraft.world.item.Items.SUSPICIOUS_GRAVEL);
                  $$1.accept(net.minecraft.world.item.Items.LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.EXPOSED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.WEATHERED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.OXIDIZED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_LIGHTNING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.FLOWER_POT);
                  $$1.accept(net.minecraft.world.item.Items.DECORATED_POT);
                  $$1.accept(net.minecraft.world.item.Items.ARMOR_STAND);
                  $$1.accept(net.minecraft.world.item.Items.ITEM_FRAME);
                  $$1.accept(net.minecraft.world.item.Items.GLOW_ITEM_FRAME);
                  $$1.accept(net.minecraft.world.item.Items.PAINTING);
                  $$0x.holders()
                     .lookup(Registries.PAINTING_VARIANT)
                     .ifPresent(
                        $$2 -> generatePresetPaintings(
                           $$1,
                           $$0x.holders(),
                           (RegistryLookup<PaintingVariant>)$$2,
                           $$0xxx -> $$0xxx.is(PaintingVariantTags.PLACEABLE),
                           net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                        )
                     );
                  $$1.accept(net.minecraft.world.item.Items.BOOKSHELF);
                  $$1.accept(net.minecraft.world.item.Items.CHISELED_BOOKSHELF);
                  $$1.accept(net.minecraft.world.item.Items.OAK_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.SPRUCE_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.BIRCH_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.JUNGLE_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.ACACIA_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.DARK_OAK_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.MANGROVE_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.CHERRY_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.PALE_OAK_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.BAMBOO_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.CRIMSON_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.WARPED_SHELF);
                  $$1.accept(net.minecraft.world.item.Items.LECTERN);
                  $$1.accept(net.minecraft.world.item.Items.TINTED_GLASS);
                  $$1.accept(net.minecraft.world.item.Items.OAK_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.OAK_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.SPRUCE_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.SPRUCE_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.BIRCH_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.BIRCH_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.JUNGLE_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.JUNGLE_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.ACACIA_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.ACACIA_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.DARK_OAK_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.DARK_OAK_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.MANGROVE_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.MANGROVE_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.CHERRY_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.CHERRY_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.PALE_OAK_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.PALE_OAK_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.BAMBOO_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.BAMBOO_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.CRIMSON_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.CRIMSON_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.WARPED_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.WARPED_HANGING_SIGN);
                  $$1.accept(net.minecraft.world.item.Items.CHEST);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.BARREL);
                  $$1.accept(net.minecraft.world.item.Items.ENDER_CHEST);
                  $$1.accept(net.minecraft.world.item.Items.SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.RED_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.LIME_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.PINK_SHULKER_BOX);
                  $$1.accept(net.minecraft.world.item.Items.RESPAWN_ANCHOR);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_BED);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_BED);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_BED);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_BED);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_BED);
                  $$1.accept(net.minecraft.world.item.Items.RED_BED);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_BED);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_BED);
                  $$1.accept(net.minecraft.world.item.Items.LIME_BED);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_BED);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_BED);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_BED);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_BED);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_BED);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_BED);
                  $$1.accept(net.minecraft.world.item.Items.PINK_BED);
                  $$1.accept(net.minecraft.world.item.Items.CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.RED_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIME_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.PINK_CANDLE);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.RED_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.LIME_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_BANNER);
                  $$1.accept(net.minecraft.world.item.Items.PINK_BANNER);
                  $$1.accept(Raid.getOminousBannerInstance($$0x.holders().lookupOrThrow(Registries.BANNER_PATTERN)));
                  $$1.accept(net.minecraft.world.item.Items.SKELETON_SKULL);
                  $$1.accept(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL);
                  $$1.accept(net.minecraft.world.item.Items.PLAYER_HEAD);
                  $$1.accept(net.minecraft.world.item.Items.ZOMBIE_HEAD);
                  $$1.accept(net.minecraft.world.item.Items.CREEPER_HEAD);
                  $$1.accept(net.minecraft.world.item.Items.PIGLIN_HEAD);
                  $$1.accept(net.minecraft.world.item.Items.DRAGON_HEAD);
                  $$1.accept(net.minecraft.world.item.Items.DRAGON_EGG);
                  $$1.accept(net.minecraft.world.item.Items.END_PORTAL_FRAME);
                  $$1.accept(net.minecraft.world.item.Items.VAULT);
                  $$1.accept(net.minecraft.world.item.Items.ENDER_EYE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.EXPOSED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.WEATHERED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.OXIDIZED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_GOLEM_STATUE);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_STONE);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_COBBLESTONE);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_STONE_BRICKS);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_MOSSY_STONE_BRICKS);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_CRACKED_STONE_BRICKS);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_CHISELED_STONE_BRICKS);
                  $$1.accept(net.minecraft.world.item.Items.INFESTED_DEEPSLATE);
               }
            )
            .build()
      );
      Registry.register(
         $$0,
         REDSTONE_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 4)
            .title(Component.translatable("itemGroup.redstone"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.REDSTONE))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.REDSTONE);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_TORCH);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.REPEATER);
               $$1.accept(net.minecraft.world.item.Items.COMPARATOR);
               $$1.accept(net.minecraft.world.item.Items.TARGET);
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.WAXED_EXPOSED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.WAXED_WEATHERED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.WAXED_OXIDIZED_COPPER_BULB);
               $$1.accept(net.minecraft.world.item.Items.LEVER);
               $$1.accept(net.minecraft.world.item.Items.OAK_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.STONE_BUTTON);
               $$1.accept(net.minecraft.world.item.Items.OAK_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.STONE_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
               $$1.accept(net.minecraft.world.item.Items.SCULK_SENSOR);
               $$1.accept(net.minecraft.world.item.Items.CALIBRATED_SCULK_SENSOR);
               $$1.accept(net.minecraft.world.item.Items.SCULK_SHRIEKER);
               $$1.accept(net.minecraft.world.item.Items.AMETHYST_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.WHITE_WOOL);
               $$1.accept(net.minecraft.world.item.Items.TRIPWIRE_HOOK);
               $$1.accept(net.minecraft.world.item.Items.STRING);
               $$1.accept(net.minecraft.world.item.Items.LECTERN);
               $$1.accept(net.minecraft.world.item.Items.DAYLIGHT_DETECTOR);
               $$1.accept(net.minecraft.world.item.Items.WAXED_LIGHTNING_ROD);
               $$1.accept(net.minecraft.world.item.Items.PISTON);
               $$1.accept(net.minecraft.world.item.Items.STICKY_PISTON);
               $$1.accept(net.minecraft.world.item.Items.SLIME_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.HONEY_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.DISPENSER);
               $$1.accept(net.minecraft.world.item.Items.DROPPER);
               $$1.accept(net.minecraft.world.item.Items.CRAFTER);
               $$1.accept(net.minecraft.world.item.Items.HOPPER);
               $$1.accept(net.minecraft.world.item.Items.CHEST);
               $$1.accept(net.minecraft.world.item.Items.WAXED_COPPER_CHEST);
               $$1.accept(net.minecraft.world.item.Items.BARREL);
               $$1.accept(net.minecraft.world.item.Items.CHISELED_BOOKSHELF);
               $$1.accept(net.minecraft.world.item.Items.OAK_SHELF);
               $$1.accept(net.minecraft.world.item.Items.FURNACE);
               $$1.accept(net.minecraft.world.item.Items.TRAPPED_CHEST);
               $$1.accept(net.minecraft.world.item.Items.JUKEBOX);
               $$1.accept(net.minecraft.world.item.Items.DECORATED_POT);
               $$1.accept(net.minecraft.world.item.Items.OBSERVER);
               $$1.accept(net.minecraft.world.item.Items.NOTE_BLOCK);
               $$1.accept(net.minecraft.world.item.Items.COMPOSTER);
               $$1.accept(net.minecraft.world.item.Items.CAULDRON);
               $$1.accept(net.minecraft.world.item.Items.RAIL);
               $$1.accept(net.minecraft.world.item.Items.POWERED_RAIL);
               $$1.accept(net.minecraft.world.item.Items.DETECTOR_RAIL);
               $$1.accept(net.minecraft.world.item.Items.ACTIVATOR_RAIL);
               $$1.accept(net.minecraft.world.item.Items.MINECART);
               $$1.accept(net.minecraft.world.item.Items.HOPPER_MINECART);
               $$1.accept(net.minecraft.world.item.Items.CHEST_MINECART);
               $$1.accept(net.minecraft.world.item.Items.FURNACE_MINECART);
               $$1.accept(net.minecraft.world.item.Items.TNT_MINECART);
               $$1.accept(net.minecraft.world.item.Items.OAK_CHEST_BOAT);
               $$1.accept(net.minecraft.world.item.Items.BAMBOO_CHEST_RAFT);
               $$1.accept(net.minecraft.world.item.Items.OAK_DOOR);
               $$1.accept(net.minecraft.world.item.Items.IRON_DOOR);
               $$1.accept(net.minecraft.world.item.Items.OAK_FENCE_GATE);
               $$1.accept(net.minecraft.world.item.Items.OAK_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.IRON_TRAPDOOR);
               $$1.accept(net.minecraft.world.item.Items.TNT);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_LAMP);
               $$1.accept(net.minecraft.world.item.Items.BELL);
               $$1.accept(net.minecraft.world.item.Items.BIG_DRIPLEAF);
               $$1.accept(net.minecraft.world.item.Items.ARMOR_STAND);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE_ORE);
            })
            .build()
      );
      Registry.register(
         $$0,
         HOTBAR,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 5)
            .title(Component.translatable("itemGroup.hotbar"))
            .icon(() -> new net.minecraft.world.item.ItemStack(Blocks.BOOKSHELF))
            .alignedRight()
            .type(net.minecraft.world.item.CreativeModeTab.Type.HOTBAR)
            .build()
      );
      Registry.register(
         $$0,
         SEARCH,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.TOP, 6)
            .title(Component.translatable("itemGroup.search"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COMPASS))
            .displayItems(($$1, $$2) -> {
               Set<net.minecraft.world.item.ItemStack> $$3 = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();

               for (net.minecraft.world.item.CreativeModeTab $$4 : $$0) {
                  if ($$4.getType() != net.minecraft.world.item.CreativeModeTab.Type.SEARCH) {
                     $$3.addAll($$4.getSearchTabDisplayItems());
                  }
               }

               $$2.acceptAll($$3);
            })
            .backgroundTexture(SEARCH_BACKGROUND)
            .alignedRight()
            .type(net.minecraft.world.item.CreativeModeTab.Type.SEARCH)
            .build()
      );
      Registry.register(
         $$0,
         TOOLS_AND_UTILITIES,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 0)
            .title(Component.translatable("itemGroup.tools"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE))
            .displayItems(
               ($$0x, $$1) -> {
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_AXE);
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_HOE);
                  $$1.accept(net.minecraft.world.item.Items.STONE_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.STONE_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.STONE_AXE);
                  $$1.accept(net.minecraft.world.item.Items.STONE_HOE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_AXE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_HOE);
                  $$1.accept(net.minecraft.world.item.Items.IRON_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.IRON_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.IRON_AXE);
                  $$1.accept(net.minecraft.world.item.Items.IRON_HOE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_AXE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_HOE);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_AXE);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_HOE);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_SHOVEL);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_PICKAXE);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_AXE);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_HOE);
                  $$1.accept(net.minecraft.world.item.Items.BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.WATER_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.COD_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.SALMON_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.TROPICAL_FISH_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.PUFFERFISH_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.AXOLOTL_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.TADPOLE_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.LAVA_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.POWDER_SNOW_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.MILK_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.FISHING_ROD);
                  $$1.accept(net.minecraft.world.item.Items.FLINT_AND_STEEL);
                  $$1.accept(net.minecraft.world.item.Items.FIRE_CHARGE);
                  $$1.accept(net.minecraft.world.item.Items.BONE_MEAL);
                  $$1.accept(net.minecraft.world.item.Items.SHEARS);
                  $$1.accept(net.minecraft.world.item.Items.BRUSH);
                  $$1.accept(net.minecraft.world.item.Items.NAME_TAG);
                  $$1.accept(net.minecraft.world.item.Items.LEAD);
                  $$1.accept(net.minecraft.world.item.Items.BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.RED_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIME_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.PINK_BUNDLE);
                  $$1.accept(net.minecraft.world.item.Items.COMPASS);
                  $$1.accept(net.minecraft.world.item.Items.RECOVERY_COMPASS);
                  $$1.accept(net.minecraft.world.item.Items.CLOCK);
                  $$1.accept(net.minecraft.world.item.Items.SPYGLASS);
                  $$1.accept(net.minecraft.world.item.Items.MAP);
                  $$1.accept(net.minecraft.world.item.Items.WRITABLE_BOOK);
                  $$1.accept(net.minecraft.world.item.Items.WIND_CHARGE);
                  $$1.accept(net.minecraft.world.item.Items.ENDER_PEARL);
                  $$1.accept(net.minecraft.world.item.Items.ENDER_EYE);
                  $$1.accept(net.minecraft.world.item.Items.ELYTRA);
                  generateFireworksAllDurations($$1, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                  $$1.accept(net.minecraft.world.item.Items.SADDLE);
                  $$1.accept(net.minecraft.world.item.Items.WHITE_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.GRAY_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.BLACK_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.RED_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.ORANGE_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.YELLOW_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.LIME_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.GREEN_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.CYAN_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.PURPLE_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.MAGENTA_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.PINK_HARNESS);
                  $$1.accept(net.minecraft.world.item.Items.CARROT_ON_A_STICK);
                  $$1.accept(net.minecraft.world.item.Items.WARPED_FUNGUS_ON_A_STICK);
                  $$1.accept(net.minecraft.world.item.Items.OAK_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.OAK_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.SPRUCE_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.SPRUCE_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.BIRCH_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.BIRCH_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.JUNGLE_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.JUNGLE_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.ACACIA_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.ACACIA_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.DARK_OAK_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.DARK_OAK_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.MANGROVE_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.MANGROVE_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.CHERRY_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.CHERRY_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.PALE_OAK_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.PALE_OAK_CHEST_BOAT);
                  $$1.accept(net.minecraft.world.item.Items.BAMBOO_RAFT);
                  $$1.accept(net.minecraft.world.item.Items.BAMBOO_CHEST_RAFT);
                  $$1.accept(net.minecraft.world.item.Items.RAIL);
                  $$1.accept(net.minecraft.world.item.Items.POWERED_RAIL);
                  $$1.accept(net.minecraft.world.item.Items.DETECTOR_RAIL);
                  $$1.accept(net.minecraft.world.item.Items.ACTIVATOR_RAIL);
                  $$1.accept(net.minecraft.world.item.Items.MINECART);
                  $$1.accept(net.minecraft.world.item.Items.HOPPER_MINECART);
                  $$1.accept(net.minecraft.world.item.Items.CHEST_MINECART);
                  $$1.accept(net.minecraft.world.item.Items.FURNACE_MINECART);
                  $$1.accept(net.minecraft.world.item.Items.TNT_MINECART);
                  $$0x.holders()
                     .lookup(Registries.INSTRUMENT)
                     .ifPresent(
                        $$1x -> generateInstrumentTypes(
                           $$1,
                           $$1x,
                           net.minecraft.world.item.Items.GOAT_HORN,
                           InstrumentTags.GOAT_HORNS,
                           net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                        )
                     );
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_13);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_CAT);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_BLOCKS);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_CHIRP);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_FAR);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_MALL);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_MELLOHI);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_STAL);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_STRAD);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_WARD);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_11);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_CREATOR_MUSIC_BOX);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_WAIT);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_CREATOR);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_PRECIPICE);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_OTHERSIDE);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_RELIC);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_5);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_PIGSTEP);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_TEARS);
                  $$1.accept(net.minecraft.world.item.Items.MUSIC_DISC_LAVA_CHICKEN);
               }
            )
            .build()
      );
      Registry.register(
         $$0,
         COMBAT,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 1)
            .title(Component.translatable("itemGroup.combat"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD))
            .displayItems(
               ($$0x, $$1) -> {
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.STONE_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.IRON_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_SWORD);
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.STONE_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.IRON_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_SPEAR);
                  $$1.accept(net.minecraft.world.item.Items.WOODEN_AXE);
                  $$1.accept(net.minecraft.world.item.Items.STONE_AXE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_AXE);
                  $$1.accept(net.minecraft.world.item.Items.IRON_AXE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_AXE);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_AXE);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_AXE);
                  $$1.accept(net.minecraft.world.item.Items.TRIDENT);
                  $$1.accept(net.minecraft.world.item.Items.MACE);
                  $$1.accept(net.minecraft.world.item.Items.SHIELD);
                  $$1.accept(net.minecraft.world.item.Items.LEATHER_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.LEATHER_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.LEATHER_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.LEATHER_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.CHAINMAIL_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.CHAINMAIL_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.IRON_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.IRON_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.IRON_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.IRON_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_LEGGINGS);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_BOOTS);
                  $$1.accept(net.minecraft.world.item.Items.TURTLE_HELMET);
                  $$1.accept(net.minecraft.world.item.Items.LEATHER_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.IRON_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_HORSE_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.WOLF_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.COPPER_NAUTILUS_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.IRON_NAUTILUS_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_NAUTILUS_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.DIAMOND_NAUTILUS_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.NETHERITE_NAUTILUS_ARMOR);
                  $$1.accept(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
                  $$1.accept(net.minecraft.world.item.Items.TNT);
                  $$1.accept(net.minecraft.world.item.Items.END_CRYSTAL);
                  $$1.accept(net.minecraft.world.item.Items.SNOWBALL);
                  $$1.accept(net.minecraft.world.item.Items.EGG);
                  $$1.accept(net.minecraft.world.item.Items.BROWN_EGG);
                  $$1.accept(net.minecraft.world.item.Items.BLUE_EGG);
                  $$1.accept(net.minecraft.world.item.Items.WIND_CHARGE);
                  $$1.accept(net.minecraft.world.item.Items.BOW);
                  $$1.accept(net.minecraft.world.item.Items.CROSSBOW);
                  generateFireworksAllDurations($$1, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                  $$1.accept(net.minecraft.world.item.Items.ARROW);
                  $$1.accept(net.minecraft.world.item.Items.SPECTRAL_ARROW);
                  $$0x.holders()
                     .lookup(Registries.POTION)
                     .ifPresent(
                        $$2 -> generatePotionEffectTypes(
                           $$1,
                           $$2,
                           net.minecraft.world.item.Items.TIPPED_ARROW,
                           net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS,
                           $$0x.enabledFeatures()
                        )
                     );
               }
            )
            .build()
      );
      Registry.register(
         $$0,
         FOOD_AND_DRINKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 2)
            .title(Component.translatable("itemGroup.foodAndDrink"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE))
            .displayItems(
               ($$0x, $$1) -> {
                  $$1.accept(net.minecraft.world.item.Items.APPLE);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_APPLE);
                  $$1.accept(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE);
                  $$1.accept(net.minecraft.world.item.Items.MELON_SLICE);
                  $$1.accept(net.minecraft.world.item.Items.SWEET_BERRIES);
                  $$1.accept(net.minecraft.world.item.Items.GLOW_BERRIES);
                  $$1.accept(net.minecraft.world.item.Items.CHORUS_FRUIT);
                  $$1.accept(net.minecraft.world.item.Items.CARROT);
                  $$1.accept(net.minecraft.world.item.Items.GOLDEN_CARROT);
                  $$1.accept(net.minecraft.world.item.Items.POTATO);
                  $$1.accept(net.minecraft.world.item.Items.BAKED_POTATO);
                  $$1.accept(net.minecraft.world.item.Items.POISONOUS_POTATO);
                  $$1.accept(net.minecraft.world.item.Items.BEETROOT);
                  $$1.accept(net.minecraft.world.item.Items.DRIED_KELP);
                  $$1.accept(net.minecraft.world.item.Items.BEEF);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_BEEF);
                  $$1.accept(net.minecraft.world.item.Items.PORKCHOP);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_PORKCHOP);
                  $$1.accept(net.minecraft.world.item.Items.MUTTON);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_MUTTON);
                  $$1.accept(net.minecraft.world.item.Items.CHICKEN);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_CHICKEN);
                  $$1.accept(net.minecraft.world.item.Items.RABBIT);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_RABBIT);
                  $$1.accept(net.minecraft.world.item.Items.COD);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_COD);
                  $$1.accept(net.minecraft.world.item.Items.SALMON);
                  $$1.accept(net.minecraft.world.item.Items.COOKED_SALMON);
                  $$1.accept(net.minecraft.world.item.Items.TROPICAL_FISH);
                  $$1.accept(net.minecraft.world.item.Items.PUFFERFISH);
                  $$1.accept(net.minecraft.world.item.Items.BREAD);
                  $$1.accept(net.minecraft.world.item.Items.COOKIE);
                  $$1.accept(net.minecraft.world.item.Items.CAKE);
                  $$1.accept(net.minecraft.world.item.Items.PUMPKIN_PIE);
                  $$1.accept(net.minecraft.world.item.Items.ROTTEN_FLESH);
                  $$1.accept(net.minecraft.world.item.Items.SPIDER_EYE);
                  $$1.accept(net.minecraft.world.item.Items.MUSHROOM_STEW);
                  $$1.accept(net.minecraft.world.item.Items.BEETROOT_SOUP);
                  $$1.accept(net.minecraft.world.item.Items.RABBIT_STEW);
                  generateSuspiciousStews($$1, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                  $$1.accept(net.minecraft.world.item.Items.MILK_BUCKET);
                  $$1.accept(net.minecraft.world.item.Items.HONEY_BOTTLE);
                  generateOminousBottles($$1, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                  $$0x.holders()
                     .lookup(Registries.POTION)
                     .ifPresent(
                        $$2 -> {
                           generatePotionEffectTypes(
                              $$1,
                              $$2,
                              net.minecraft.world.item.Items.POTION,
                              net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS,
                              $$0x.enabledFeatures()
                           );
                           generatePotionEffectTypes(
                              $$1,
                              $$2,
                              net.minecraft.world.item.Items.SPLASH_POTION,
                              net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS,
                              $$0x.enabledFeatures()
                           );
                           generatePotionEffectTypes(
                              $$1,
                              $$2,
                              net.minecraft.world.item.Items.LINGERING_POTION,
                              net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS,
                              $$0x.enabledFeatures()
                           );
                        }
                     );
               }
            )
            .build()
      );
      Registry.register(
         $$0,
         INGREDIENTS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 3)
            .title(Component.translatable("itemGroup.ingredients"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.COAL);
               $$1.accept(net.minecraft.world.item.Items.CHARCOAL);
               $$1.accept(net.minecraft.world.item.Items.RAW_COPPER);
               $$1.accept(net.minecraft.world.item.Items.RAW_IRON);
               $$1.accept(net.minecraft.world.item.Items.RAW_GOLD);
               $$1.accept(net.minecraft.world.item.Items.EMERALD);
               $$1.accept(net.minecraft.world.item.Items.LAPIS_LAZULI);
               $$1.accept(net.minecraft.world.item.Items.DIAMOND);
               $$1.accept(net.minecraft.world.item.Items.ANCIENT_DEBRIS);
               $$1.accept(net.minecraft.world.item.Items.QUARTZ);
               $$1.accept(net.minecraft.world.item.Items.AMETHYST_SHARD);
               $$1.accept(net.minecraft.world.item.Items.COPPER_NUGGET);
               $$1.accept(net.minecraft.world.item.Items.IRON_NUGGET);
               $$1.accept(net.minecraft.world.item.Items.GOLD_NUGGET);
               $$1.accept(net.minecraft.world.item.Items.COPPER_INGOT);
               $$1.accept(net.minecraft.world.item.Items.IRON_INGOT);
               $$1.accept(net.minecraft.world.item.Items.GOLD_INGOT);
               $$1.accept(net.minecraft.world.item.Items.NETHERITE_SCRAP);
               $$1.accept(net.minecraft.world.item.Items.NETHERITE_INGOT);
               $$1.accept(net.minecraft.world.item.Items.STICK);
               $$1.accept(net.minecraft.world.item.Items.FLINT);
               $$1.accept(net.minecraft.world.item.Items.WHEAT);
               $$1.accept(net.minecraft.world.item.Items.BONE);
               $$1.accept(net.minecraft.world.item.Items.BONE_MEAL);
               $$1.accept(net.minecraft.world.item.Items.STRING);
               $$1.accept(net.minecraft.world.item.Items.FEATHER);
               $$1.accept(net.minecraft.world.item.Items.SNOWBALL);
               $$1.accept(net.minecraft.world.item.Items.EGG);
               $$1.accept(net.minecraft.world.item.Items.BROWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BLUE_EGG);
               $$1.accept(net.minecraft.world.item.Items.LEATHER);
               $$1.accept(net.minecraft.world.item.Items.RABBIT_HIDE);
               $$1.accept(net.minecraft.world.item.Items.HONEYCOMB);
               $$1.accept(net.minecraft.world.item.Items.RESIN_CLUMP);
               $$1.accept(net.minecraft.world.item.Items.INK_SAC);
               $$1.accept(net.minecraft.world.item.Items.GLOW_INK_SAC);
               $$1.accept(net.minecraft.world.item.Items.TURTLE_SCUTE);
               $$1.accept(net.minecraft.world.item.Items.ARMADILLO_SCUTE);
               $$1.accept(net.minecraft.world.item.Items.SLIME_BALL);
               $$1.accept(net.minecraft.world.item.Items.CLAY_BALL);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_SHARD);
               $$1.accept(net.minecraft.world.item.Items.PRISMARINE_CRYSTALS);
               $$1.accept(net.minecraft.world.item.Items.NAUTILUS_SHELL);
               $$1.accept(net.minecraft.world.item.Items.HEART_OF_THE_SEA);
               $$1.accept(net.minecraft.world.item.Items.FIRE_CHARGE);
               $$1.accept(net.minecraft.world.item.Items.BLAZE_ROD);
               $$1.accept(net.minecraft.world.item.Items.BREEZE_ROD);
               $$1.accept(net.minecraft.world.item.Items.HEAVY_CORE);
               $$1.accept(net.minecraft.world.item.Items.NETHER_STAR);
               $$1.accept(net.minecraft.world.item.Items.ENDER_PEARL);
               $$1.accept(net.minecraft.world.item.Items.ENDER_EYE);
               $$1.accept(net.minecraft.world.item.Items.SHULKER_SHELL);
               $$1.accept(net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT);
               $$1.accept(net.minecraft.world.item.Items.ECHO_SHARD);
               $$1.accept(net.minecraft.world.item.Items.DISC_FRAGMENT_5);
               $$1.accept(net.minecraft.world.item.Items.WHITE_DYE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_GRAY_DYE);
               $$1.accept(net.minecraft.world.item.Items.GRAY_DYE);
               $$1.accept(net.minecraft.world.item.Items.BLACK_DYE);
               $$1.accept(net.minecraft.world.item.Items.BROWN_DYE);
               $$1.accept(net.minecraft.world.item.Items.RED_DYE);
               $$1.accept(net.minecraft.world.item.Items.ORANGE_DYE);
               $$1.accept(net.minecraft.world.item.Items.YELLOW_DYE);
               $$1.accept(net.minecraft.world.item.Items.LIME_DYE);
               $$1.accept(net.minecraft.world.item.Items.GREEN_DYE);
               $$1.accept(net.minecraft.world.item.Items.CYAN_DYE);
               $$1.accept(net.minecraft.world.item.Items.LIGHT_BLUE_DYE);
               $$1.accept(net.minecraft.world.item.Items.BLUE_DYE);
               $$1.accept(net.minecraft.world.item.Items.PURPLE_DYE);
               $$1.accept(net.minecraft.world.item.Items.MAGENTA_DYE);
               $$1.accept(net.minecraft.world.item.Items.PINK_DYE);
               $$1.accept(net.minecraft.world.item.Items.BOWL);
               $$1.accept(net.minecraft.world.item.Items.BRICK);
               $$1.accept(net.minecraft.world.item.Items.NETHER_BRICK);
               $$1.accept(net.minecraft.world.item.Items.RESIN_BRICK);
               $$1.accept(net.minecraft.world.item.Items.PAPER);
               $$1.accept(net.minecraft.world.item.Items.BOOK);
               $$1.accept(net.minecraft.world.item.Items.FIREWORK_STAR);
               $$1.accept(net.minecraft.world.item.Items.GLASS_BOTTLE);
               $$1.accept(net.minecraft.world.item.Items.NETHER_WART);
               $$1.accept(net.minecraft.world.item.Items.REDSTONE);
               $$1.accept(net.minecraft.world.item.Items.GLOWSTONE_DUST);
               $$1.accept(net.minecraft.world.item.Items.GUNPOWDER);
               $$1.accept(net.minecraft.world.item.Items.DRAGON_BREATH);
               $$1.accept(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE);
               $$1.accept(net.minecraft.world.item.Items.BLAZE_POWDER);
               $$1.accept(net.minecraft.world.item.Items.SUGAR);
               $$1.accept(net.minecraft.world.item.Items.RABBIT_FOOT);
               $$1.accept(net.minecraft.world.item.Items.GLISTERING_MELON_SLICE);
               $$1.accept(net.minecraft.world.item.Items.SPIDER_EYE);
               $$1.accept(net.minecraft.world.item.Items.PUFFERFISH);
               $$1.accept(net.minecraft.world.item.Items.MAGMA_CREAM);
               $$1.accept(net.minecraft.world.item.Items.GOLDEN_CARROT);
               $$1.accept(net.minecraft.world.item.Items.GHAST_TEAR);
               $$1.accept(net.minecraft.world.item.Items.TURTLE_HELMET);
               $$1.accept(net.minecraft.world.item.Items.PHANTOM_MEMBRANE);
               $$1.accept(net.minecraft.world.item.Items.FIELD_MASONED_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.BORDURE_INDENTED_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.FLOWER_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.CREEPER_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.SKULL_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.MOJANG_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.GLOBE_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.PIGLIN_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.FLOW_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.GUSTER_BANNER_PATTERN);
               $$1.accept(net.minecraft.world.item.Items.ANGLER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.ARCHER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.ARMS_UP_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.BLADE_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.BREWER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.BURN_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.DANGER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.EXPLORER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.FLOW_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.FRIEND_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.GUSTER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.HEART_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.HEARTBREAK_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.HOWL_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.MINER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.MOURNER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.PLENTY_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.PRIZE_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.SCRAPE_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.SHEAF_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.SHELTER_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.SKULL_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.SNORT_POTTERY_SHERD);
               $$1.accept(net.minecraft.world.item.Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE);
               $$1.accept(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE);
               $$1.accept(net.minecraft.world.item.Items.TRIAL_KEY);
               $$1.accept(net.minecraft.world.item.Items.OMINOUS_TRIAL_KEY);
               $$0x.holders().lookup(Registries.ENCHANTMENT).ifPresent($$1x -> {
                  generateEnchantmentBookTypesOnlyMaxLevel($$1, $$1x, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                  generateEnchantmentBookTypesAllLevels($$1, $$1x, net.minecraft.world.item.CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
               });
            })
            .build()
      );
      Registry.register(
         $$0,
         SPAWN_EGGS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 4)
            .title(Component.translatable("itemGroup.spawnEggs"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CREEPER_SPAWN_EGG))
            .displayItems(($$0x, $$1) -> {
               $$1.accept(net.minecraft.world.item.Items.SPAWNER);
               $$1.accept(net.minecraft.world.item.Items.TRIAL_SPAWNER);
               $$1.accept(net.minecraft.world.item.Items.CREAKING_HEART);
               $$1.accept(net.minecraft.world.item.Items.CHICKEN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.COW_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PIG_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SHEEP_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CAMEL_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.DONKEY_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.HORSE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.MULE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CAT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PARROT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.WOLF_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ARMADILLO_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BAT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BEE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.FOX_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.GOAT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.LLAMA_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.OCELOT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PANDA_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.POLAR_BEAR_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.RABBIT_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.AXOLOTL_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.COD_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.DOLPHIN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.FROG_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.GLOW_SQUID_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.NAUTILUS_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PUFFERFISH_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SALMON_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SQUID_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.TADPOLE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.TROPICAL_FISH_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.TURTLE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ALLAY_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.MOOSHROOM_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SNIFFER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.COPPER_GOLEM_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.IRON_GOLEM_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SNOW_GOLEM_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.TRADER_LLAMA_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.VILLAGER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.WANDERING_TRADER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BOGGED_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CAMEL_HUSK_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.DROWNED_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.HUSK_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PARCHED_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SKELETON_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SKELETON_HORSE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.STRAY_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOMBIE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOMBIE_HORSE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOMBIE_NAUTILUS_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOMBIE_VILLAGER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CAVE_SPIDER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SPIDER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BREEZE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CREAKING_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.CREEPER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ELDER_GUARDIAN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.GUARDIAN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PHANTOM_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SILVERFISH_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SLIME_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.WARDEN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.WITCH_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.EVOKER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PILLAGER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.RAVAGER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.VEX_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.VINDICATOR_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.BLAZE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.GHAST_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.HAPPY_GHAST_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.HOGLIN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.MAGMA_CUBE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PIGLIN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.PIGLIN_BRUTE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.STRIDER_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.WITHER_SKELETON_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOGLIN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ZOMBIFIED_PIGLIN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ENDERMAN_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.ENDERMITE_SPAWN_EGG);
               $$1.accept(net.minecraft.world.item.Items.SHULKER_SPAWN_EGG);
            })
            .build()
      );
      Registry.register(
         $$0,
         OP_BLOCKS,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 5)
            .title(Component.translatable("itemGroup.op"))
            .icon(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COMMAND_BLOCK))
            .alignedRight()
            .displayItems(
               ($$0x, $$1) -> {
                  if ($$0x.hasPermissions()) {
                     $$1.accept(net.minecraft.world.item.Items.COMMAND_BLOCK);
                     $$1.accept(net.minecraft.world.item.Items.CHAIN_COMMAND_BLOCK);
                     $$1.accept(net.minecraft.world.item.Items.REPEATING_COMMAND_BLOCK);
                     $$1.accept(net.minecraft.world.item.Items.COMMAND_BLOCK_MINECART);
                     $$1.accept(net.minecraft.world.item.Items.JIGSAW);
                     $$1.accept(net.minecraft.world.item.Items.STRUCTURE_BLOCK);
                     $$1.accept(net.minecraft.world.item.Items.STRUCTURE_VOID);
                     $$1.accept(net.minecraft.world.item.Items.BARRIER);
                     $$1.accept(net.minecraft.world.item.Items.DEBUG_STICK);
                     $$1.accept(net.minecraft.world.item.Items.TEST_INSTANCE_BLOCK);

                     for (TestBlockMode $$2 : TestBlockMode.values()) {
                        $$1.accept(TestBlock.setModeOnStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TEST_BLOCK), $$2));
                     }

                     for (int $$3 = 15; $$3 >= 0; $$3--) {
                        $$1.accept(LightBlock.setLightOnStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LIGHT), $$3));
                     }

                     $$0x.holders()
                        .lookup(Registries.PAINTING_VARIANT)
                        .ifPresent(
                           $$2x -> generatePresetPaintings(
                              $$1,
                              $$0x.holders(),
                              $$2x,
                              $$0xxx -> !$$0xxx.is(PaintingVariantTags.PLACEABLE),
                              net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                           )
                        );
                  }
               }
            )
            .build()
      );
      return (net.minecraft.world.item.CreativeModeTab)Registry.register(
         $$0,
         INVENTORY,
         net.minecraft.world.item.CreativeModeTab.builder(net.minecraft.world.item.CreativeModeTab.Row.BOTTOM, 6)
            .title(Component.translatable("itemGroup.inventory"))
            .icon(() -> new net.minecraft.world.item.ItemStack(Blocks.CHEST))
            .backgroundTexture(INVENTORY_BACKGROUND)
            .hideTitle()
            .alignedRight()
            .type(net.minecraft.world.item.CreativeModeTab.Type.INVENTORY)
            .noScrollBar()
            .build()
      );
   }

   public static void validate() {
      Map<Pair<net.minecraft.world.item.CreativeModeTab.Row, Integer>, String> $$0 = new HashMap<>();

      for (ResourceKey<net.minecraft.world.item.CreativeModeTab> $$1 : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
         net.minecraft.world.item.CreativeModeTab $$2 = (net.minecraft.world.item.CreativeModeTab)BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow($$1);
         String $$3 = $$2.getDisplayName().getString();
         String $$4 = $$0.put(Pair.of($$2.row(), $$2.column()), $$3);
         if ($$4 != null) {
            throw new IllegalArgumentException("Duplicate position: " + $$3 + " vs. " + $$4);
         }
      }
   }

   public static net.minecraft.world.item.CreativeModeTab getDefaultTab() {
      return (net.minecraft.world.item.CreativeModeTab)BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(BUILDING_BLOCKS);
   }

   private static void generatePotionEffectTypes(
      net.minecraft.world.item.CreativeModeTab.Output $$0,
      HolderLookup<Potion> $$1,
      net.minecraft.world.item.Item $$2,
      net.minecraft.world.item.CreativeModeTab.TabVisibility $$3,
      FeatureFlagSet $$4
   ) {
      $$1.listElements()
         .filter($$1x -> ((Potion)$$1x.value()).isEnabled($$4))
         .map($$1x -> PotionContents.createItemStack($$2, $$1x))
         .forEach($$2x -> $$0.accept($$2x, $$3));
   }

   private static void generateEnchantmentBookTypesOnlyMaxLevel(
      net.minecraft.world.item.CreativeModeTab.Output $$0, HolderLookup<Enchantment> $$1, net.minecraft.world.item.CreativeModeTab.TabVisibility $$2
   ) {
      $$1.listElements()
         .map($$0x -> EnchantmentHelper.createBook(new EnchantmentInstance($$0x, ((Enchantment)$$0x.value()).getMaxLevel())))
         .forEach($$2x -> $$0.accept($$2x, $$2));
   }

   private static void generateEnchantmentBookTypesAllLevels(
      net.minecraft.world.item.CreativeModeTab.Output $$0, HolderLookup<Enchantment> $$1, net.minecraft.world.item.CreativeModeTab.TabVisibility $$2
   ) {
      $$1.listElements()
         .flatMap(
            $$0x -> IntStream.rangeClosed(((Enchantment)$$0x.value()).getMinLevel(), ((Enchantment)$$0x.value()).getMaxLevel())
               .mapToObj($$1x -> EnchantmentHelper.createBook(new EnchantmentInstance($$0x, $$1x)))
         )
         .forEach($$2x -> $$0.accept($$2x, $$2));
   }

   private static void generateInstrumentTypes(
      net.minecraft.world.item.CreativeModeTab.Output $$0,
      HolderLookup<net.minecraft.world.item.Instrument> $$1,
      net.minecraft.world.item.Item $$2,
      TagKey<net.minecraft.world.item.Instrument> $$3,
      net.minecraft.world.item.CreativeModeTab.TabVisibility $$4
   ) {
      $$1.get($$3)
         .ifPresent($$3x -> $$3x.stream().map($$1xx -> net.minecraft.world.item.InstrumentItem.create($$2, $$1xx)).forEach($$2xx -> $$0.accept($$2xx, $$4)));
   }

   private static void generateSuspiciousStews(net.minecraft.world.item.CreativeModeTab.Output $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1) {
      List<SuspiciousEffectHolder> $$2 = SuspiciousEffectHolder.getAllEffectHolders();
      Set<net.minecraft.world.item.ItemStack> $$3 = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();

      for (SuspiciousEffectHolder $$4 : $$2) {
         net.minecraft.world.item.ItemStack $$5 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SUSPICIOUS_STEW);
         $$5.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, $$4.getSuspiciousEffects());
         $$3.add($$5);
      }

      $$0.acceptAll($$3, $$1);
   }

   private static void generateOminousBottles(net.minecraft.world.item.CreativeModeTab.Output $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1) {
      for (int $$2 = 0; $$2 <= 4; $$2++) {
         net.minecraft.world.item.ItemStack $$3 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OMINOUS_BOTTLE);
         $$3.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier($$2));
         $$0.accept($$3, $$1);
      }
   }

   private static void generateFireworksAllDurations(
      net.minecraft.world.item.CreativeModeTab.Output $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1
   ) {
      for (byte $$2 : net.minecraft.world.item.FireworkRocketItem.CRAFTABLE_DURATIONS) {
         net.minecraft.world.item.ItemStack $$3 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET);
         $$3.set(DataComponents.FIREWORKS, new Fireworks($$2, List.of()));
         $$0.accept($$3, $$1);
      }
   }

   private static void generatePresetPaintings(
      net.minecraft.world.item.CreativeModeTab.Output $$0,
      Provider $$1,
      RegistryLookup<PaintingVariant> $$2,
      Predicate<Holder<PaintingVariant>> $$3,
      net.minecraft.world.item.CreativeModeTab.TabVisibility $$4
   ) {
      RegistryOps<Tag> $$5 = $$1.createSerializationContext(NbtOps.INSTANCE);
      $$2.listElements().filter($$3).sorted(PAINTING_COMPARATOR).forEach($$2x -> {
         net.minecraft.world.item.ItemStack $$3x = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAINTING);
         $$3x.set(DataComponents.PAINTING_VARIANT, $$2x);
         $$0.accept($$3x, $$4);
      });
   }

   public static List<net.minecraft.world.item.CreativeModeTab> tabs() {
      return streamAllTabs().filter(net.minecraft.world.item.CreativeModeTab::shouldDisplay).toList();
   }

   public static List<net.minecraft.world.item.CreativeModeTab> allTabs() {
      return streamAllTabs().toList();
   }

   private static Stream<net.minecraft.world.item.CreativeModeTab> streamAllTabs() {
      return BuiltInRegistries.CREATIVE_MODE_TAB.stream();
   }

   public static net.minecraft.world.item.CreativeModeTab searchTab() {
      return (net.minecraft.world.item.CreativeModeTab)BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(SEARCH);
   }

   private static void buildAllTabContents(net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters $$0) {
      streamAllTabs().filter($$0x -> $$0x.getType() == net.minecraft.world.item.CreativeModeTab.Type.CATEGORY).forEach($$1 -> $$1.buildContents($$0));
      streamAllTabs().filter($$0x -> $$0x.getType() != net.minecraft.world.item.CreativeModeTab.Type.CATEGORY).forEach($$1 -> $$1.buildContents($$0));
   }

   public static boolean tryRebuildTabContents(FeatureFlagSet $$0, boolean $$1, Provider $$2) {
      if (CACHED_PARAMETERS != null && !CACHED_PARAMETERS.needsUpdate($$0, $$1, $$2)) {
         return false;
      } else {
         CACHED_PARAMETERS = new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters($$0, $$1, $$2);
         buildAllTabContents(CACHED_PARAMETERS);
         return true;
      }
   }
}
