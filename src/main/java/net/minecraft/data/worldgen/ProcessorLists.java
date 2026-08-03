package net.minecraft.data.worldgen;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.storage.loot.LootTable;

public class ProcessorLists {
   private static final ResourceKey<StructureProcessorList> EMPTY = createKey("empty");
   public static final ResourceKey<StructureProcessorList> ZOMBIE_PLAINS = createKey("zombie_plains");
   public static final ResourceKey<StructureProcessorList> ZOMBIE_SAVANNA = createKey("zombie_savanna");
   public static final ResourceKey<StructureProcessorList> ZOMBIE_SNOWY = createKey("zombie_snowy");
   public static final ResourceKey<StructureProcessorList> ZOMBIE_TAIGA = createKey("zombie_taiga");
   public static final ResourceKey<StructureProcessorList> ZOMBIE_DESERT = createKey("zombie_desert");
   public static final ResourceKey<StructureProcessorList> MOSSIFY_10_PERCENT = createKey("mossify_10_percent");
   public static final ResourceKey<StructureProcessorList> MOSSIFY_20_PERCENT = createKey("mossify_20_percent");
   public static final ResourceKey<StructureProcessorList> MOSSIFY_70_PERCENT = createKey("mossify_70_percent");
   public static final ResourceKey<StructureProcessorList> STREET_PLAINS = createKey("street_plains");
   public static final ResourceKey<StructureProcessorList> STREET_SAVANNA = createKey("street_savanna");
   public static final ResourceKey<StructureProcessorList> STREET_SNOWY_OR_TAIGA = createKey("street_snowy_or_taiga");
   public static final ResourceKey<StructureProcessorList> FARM_PLAINS = createKey("farm_plains");
   public static final ResourceKey<StructureProcessorList> FARM_SAVANNA = createKey("farm_savanna");
   public static final ResourceKey<StructureProcessorList> FARM_SNOWY = createKey("farm_snowy");
   public static final ResourceKey<StructureProcessorList> FARM_TAIGA = createKey("farm_taiga");
   public static final ResourceKey<StructureProcessorList> FARM_DESERT = createKey("farm_desert");
   public static final ResourceKey<StructureProcessorList> OUTPOST_ROT = createKey("outpost_rot");
   public static final ResourceKey<StructureProcessorList> BOTTOM_RAMPART = createKey("bottom_rampart");
   public static final ResourceKey<StructureProcessorList> TREASURE_ROOMS = createKey("treasure_rooms");
   public static final ResourceKey<StructureProcessorList> HOUSING = createKey("housing");
   public static final ResourceKey<StructureProcessorList> SIDE_WALL_DEGRADATION = createKey("side_wall_degradation");
   public static final ResourceKey<StructureProcessorList> STABLE_DEGRADATION = createKey("stable_degradation");
   public static final ResourceKey<StructureProcessorList> BASTION_GENERIC_DEGRADATION = createKey("bastion_generic_degradation");
   public static final ResourceKey<StructureProcessorList> RAMPART_DEGRADATION = createKey("rampart_degradation");
   public static final ResourceKey<StructureProcessorList> ENTRANCE_REPLACEMENT = createKey("entrance_replacement");
   public static final ResourceKey<StructureProcessorList> BRIDGE = createKey("bridge");
   public static final ResourceKey<StructureProcessorList> ROOF = createKey("roof");
   public static final ResourceKey<StructureProcessorList> HIGH_WALL = createKey("high_wall");
   public static final ResourceKey<StructureProcessorList> HIGH_RAMPART = createKey("high_rampart");
   public static final ResourceKey<StructureProcessorList> FOSSIL_ROT = createKey("fossil_rot");
   public static final ResourceKey<StructureProcessorList> FOSSIL_COAL = createKey("fossil_coal");
   public static final ResourceKey<StructureProcessorList> FOSSIL_DIAMONDS = createKey("fossil_diamonds");
   public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_START_DEGRADATION = createKey("ancient_city_start_degradation");
   public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_GENERIC_DEGRADATION = createKey("ancient_city_generic_degradation");
   public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_WALLS_DEGRADATION = createKey("ancient_city_walls_degradation");
   public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_HOUSES_ARCHAEOLOGY = createKey("trail_ruins_houses_archaeology");
   public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_ROADS_ARCHAEOLOGY = createKey("trail_ruins_roads_archaeology");
   public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY = createKey("trail_ruins_tower_top_archaeology");
   public static final ResourceKey<StructureProcessorList> TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION = createKey("trial_chambers_copper_bulb_degradation");

   private static ResourceKey<StructureProcessorList> createKey(String $$0) {
      return ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace($$0));
   }

   private static void register(BootstrapContext<StructureProcessorList> $$0, ResourceKey<StructureProcessorList> $$1, List<StructureProcessor> $$2) {
      $$0.register($$1, new StructureProcessorList($$2));
   }

   public static void bootstrap(BootstrapContext<StructureProcessorList> param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 0000: aload 0
      // 0001: getstatic net/minecraft/core/registries/Registries.BLOCK Lnet/minecraft/resources/ResourceKey;
      // 0004: invokeinterface net/minecraft/data/worldgen/BootstrapContext.lookup (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/HolderGetter; 2
      // 0009: astore 1
      // 000a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 000d: dup
      // 000e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0011: dup
      // 0012: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0015: ldc 0.01
      // 0017: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 001a: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 001d: getstatic net/minecraft/world/level/block/Blocks.GILDED_BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0020: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0023: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0026: astore 2
      // 0027: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 002a: dup
      // 002b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 002e: dup
      // 002f: getstatic net/minecraft/world/level/block/Blocks.GILDED_BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0032: ldc 0.5
      // 0034: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0037: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 003a: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 003d: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0040: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0043: astore 3
      // 0044: aload 0
      // 0045: getstatic net/minecraft/data/worldgen/ProcessorLists.EMPTY Lnet/minecraft/resources/ResourceKey;
      // 0048: invokestatic com/google/common/collect/ImmutableList.of ()Lcom/google/common/collect/ImmutableList;
      // 004b: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 004e: aload 0
      // 004f: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_PLAINS Lnet/minecraft/resources/ResourceKey;
      // 0052: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0055: dup
      // 0056: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0059: dup
      // 005a: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 005d: dup
      // 005e: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 0061: ldc 0.8
      // 0063: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0066: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0069: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 006c: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 006f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0072: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0075: dup
      // 0076: new net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest
      // 0079: dup
      // 007a: getstatic net/minecraft/tags/BlockTags.DOORS Lnet/minecraft/tags/TagKey;
      // 007d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.<init> (Lnet/minecraft/tags/TagKey;)V
      // 0080: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0083: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0086: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0089: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 008c: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 008f: dup
      // 0090: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0093: dup
      // 0094: getstatic net/minecraft/world/level/block/Blocks.TORCH Lnet/minecraft/world/level/block/Block;
      // 0097: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 009a: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 009d: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 00a0: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 00a3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 00a6: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 00a9: dup
      // 00aa: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 00ad: dup
      // 00ae: getstatic net/minecraft/world/level/block/Blocks.WALL_TORCH Lnet/minecraft/world/level/block/Block;
      // 00b1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 00b4: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 00b7: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 00ba: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 00bd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 00c0: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 00c3: dup
      // 00c4: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 00c7: dup
      // 00c8: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 00cb: ldc 0.07
      // 00cd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 00d0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 00d3: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 00d6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 00d9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 00dc: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 00df: dup
      // 00e0: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 00e3: dup
      // 00e4: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 00e7: ldc 0.07
      // 00e9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 00ec: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 00ef: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 00f2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 00f5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 00f8: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 00fb: dup
      // 00fc: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 00ff: dup
      // 0100: getstatic net/minecraft/world/level/block/Blocks.WHITE_TERRACOTTA Lnet/minecraft/world/level/block/Block;
      // 0103: ldc 0.07
      // 0105: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0108: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 010b: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 010e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0111: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0114: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0117: dup
      // 0118: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 011b: dup
      // 011c: getstatic net/minecraft/world/level/block/Blocks.OAK_LOG Lnet/minecraft/world/level/block/Block;
      // 011f: ldc 0.05
      // 0121: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0124: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0127: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 012a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 012d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0130: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0133: dup
      // 0134: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0137: dup
      // 0138: getstatic net/minecraft/world/level/block/Blocks.OAK_PLANKS Lnet/minecraft/world/level/block/Block;
      // 013b: ldc 0.1
      // 013d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0140: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0143: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0146: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0149: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 014c: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 014f: dup
      // 0150: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0153: dup
      // 0154: getstatic net/minecraft/world/level/block/Blocks.OAK_STAIRS Lnet/minecraft/world/level/block/Block;
      // 0157: ldc 0.1
      // 0159: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 015c: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 015f: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0162: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0165: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0168: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 016b: dup
      // 016c: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 016f: dup
      // 0170: getstatic net/minecraft/world/level/block/Blocks.STRIPPED_OAK_LOG Lnet/minecraft/world/level/block/Block;
      // 0173: ldc 0.02
      // 0175: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0178: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 017b: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 017e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0181: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0184: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0187: dup
      // 0188: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 018b: dup
      // 018c: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 018f: ldc 0.5
      // 0191: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0194: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0197: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 019a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 019d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 01a0: bipush 5
      // 01a1: anewarray 121
      // 01a4: dup
      // 01a5: bipush 0
      // 01a6: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 01a9: dup
      // 01aa: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 01ad: dup
      // 01ae: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 01b1: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 01b4: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 01b7: bipush 1
      // 01b8: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 01bb: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 01be: checkcast net/minecraft/world/level/block/state/BlockState
      // 01c1: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 01c4: bipush 1
      // 01c5: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 01c8: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 01cb: checkcast net/minecraft/world/level/block/state/BlockState
      // 01ce: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 01d1: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 01d4: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 01d7: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 01da: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 01dd: bipush 1
      // 01de: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 01e1: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 01e4: checkcast net/minecraft/world/level/block/state/BlockState
      // 01e7: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 01ea: bipush 1
      // 01eb: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 01ee: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 01f1: checkcast net/minecraft/world/level/block/state/BlockState
      // 01f4: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 01f7: aastore
      // 01f8: dup
      // 01f9: bipush 1
      // 01fa: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 01fd: dup
      // 01fe: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 0201: dup
      // 0202: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 0205: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0208: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 020b: bipush 1
      // 020c: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 020f: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0212: checkcast net/minecraft/world/level/block/state/BlockState
      // 0215: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0218: bipush 1
      // 0219: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 021c: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 021f: checkcast net/minecraft/world/level/block/state/BlockState
      // 0222: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0225: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0228: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 022b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 022e: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0231: bipush 1
      // 0232: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0235: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0238: checkcast net/minecraft/world/level/block/state/BlockState
      // 023b: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 023e: bipush 1
      // 023f: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0242: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0245: checkcast net/minecraft/world/level/block/state/BlockState
      // 0248: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 024b: aastore
      // 024c: dup
      // 024d: bipush 2
      // 024e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0251: dup
      // 0252: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0255: dup
      // 0256: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0259: ldc_w 0.3
      // 025c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 025f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0262: getstatic net/minecraft/world/level/block/Blocks.CARROTS Lnet/minecraft/world/level/block/Block;
      // 0265: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0268: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 026b: aastore
      // 026c: dup
      // 026d: bipush 3
      // 026e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0271: dup
      // 0272: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0275: dup
      // 0276: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0279: ldc_w 0.2
      // 027c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 027f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0282: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 0285: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0288: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 028b: aastore
      // 028c: dup
      // 028d: bipush 4
      // 028e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0291: dup
      // 0292: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0295: dup
      // 0296: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0299: ldc 0.1
      // 029b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 029e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 02a1: getstatic net/minecraft/world/level/block/Blocks.BEETROOTS Lnet/minecraft/world/level/block/Block;
      // 02a4: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 02a7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 02aa: aastore
      // 02ab: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 02ae: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 02b1: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 02b4: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 02b7: aload 0
      // 02b8: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 02bb: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 02be: dup
      // 02bf: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 02c2: dup
      // 02c3: new net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest
      // 02c6: dup
      // 02c7: getstatic net/minecraft/tags/BlockTags.DOORS Lnet/minecraft/tags/TagKey;
      // 02ca: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.<init> (Lnet/minecraft/tags/TagKey;)V
      // 02cd: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 02d0: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 02d3: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 02d6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 02d9: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 02dc: dup
      // 02dd: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 02e0: dup
      // 02e1: getstatic net/minecraft/world/level/block/Blocks.TORCH Lnet/minecraft/world/level/block/Block;
      // 02e4: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 02e7: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 02ea: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 02ed: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 02f0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 02f3: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 02f6: dup
      // 02f7: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 02fa: dup
      // 02fb: getstatic net/minecraft/world/level/block/Blocks.WALL_TORCH Lnet/minecraft/world/level/block/Block;
      // 02fe: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0301: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0304: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0307: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 030a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 030d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0310: dup
      // 0311: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0314: dup
      // 0315: getstatic net/minecraft/world/level/block/Blocks.ACACIA_PLANKS Lnet/minecraft/world/level/block/Block;
      // 0318: ldc_w 0.2
      // 031b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 031e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0321: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0324: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0327: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 032a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 032d: dup
      // 032e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0331: dup
      // 0332: getstatic net/minecraft/world/level/block/Blocks.ACACIA_STAIRS Lnet/minecraft/world/level/block/Block;
      // 0335: ldc_w 0.2
      // 0338: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 033b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 033e: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0341: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0344: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0347: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 034a: dup
      // 034b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 034e: dup
      // 034f: getstatic net/minecraft/world/level/block/Blocks.ACACIA_LOG Lnet/minecraft/world/level/block/Block;
      // 0352: ldc 0.05
      // 0354: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0357: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 035a: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 035d: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0360: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0363: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0366: dup
      // 0367: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 036a: dup
      // 036b: getstatic net/minecraft/world/level/block/Blocks.ACACIA_WOOD Lnet/minecraft/world/level/block/Block;
      // 036e: ldc 0.05
      // 0370: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0373: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0376: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0379: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 037c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 037f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0382: dup
      // 0383: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0386: dup
      // 0387: getstatic net/minecraft/world/level/block/Blocks.ORANGE_TERRACOTTA Lnet/minecraft/world/level/block/Block;
      // 038a: ldc 0.05
      // 038c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 038f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0392: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0395: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0398: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 039b: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 039e: dup
      // 039f: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 03a2: dup
      // 03a3: getstatic net/minecraft/world/level/block/Blocks.YELLOW_TERRACOTTA Lnet/minecraft/world/level/block/Block;
      // 03a6: ldc 0.05
      // 03a8: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 03ab: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 03ae: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 03b1: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 03b4: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 03b7: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 03ba: dup
      // 03bb: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 03be: dup
      // 03bf: getstatic net/minecraft/world/level/block/Blocks.RED_TERRACOTTA Lnet/minecraft/world/level/block/Block;
      // 03c2: ldc 0.05
      // 03c4: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 03c7: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 03ca: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 03cd: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 03d0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 03d3: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 03d6: dup
      // 03d7: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 03da: dup
      // 03db: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 03de: ldc 0.5
      // 03e0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 03e3: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 03e6: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 03e9: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 03ec: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 03ef: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 03f2: dup
      // 03f3: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 03f6: dup
      // 03f7: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 03fa: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 03fd: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0400: bipush 1
      // 0401: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0404: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0407: checkcast net/minecraft/world/level/block/state/BlockState
      // 040a: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 040d: bipush 1
      // 040e: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0411: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0414: checkcast net/minecraft/world/level/block/state/BlockState
      // 0417: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 041a: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 041d: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 0420: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0423: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0426: bipush 1
      // 0427: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 042a: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 042d: checkcast net/minecraft/world/level/block/state/BlockState
      // 0430: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0433: bipush 1
      // 0434: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0437: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 043a: checkcast net/minecraft/world/level/block/state/BlockState
      // 043d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0440: bipush 2
      // 0441: anewarray 121
      // 0444: dup
      // 0445: bipush 0
      // 0446: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0449: dup
      // 044a: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 044d: dup
      // 044e: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 0451: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0454: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0457: bipush 1
      // 0458: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 045b: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 045e: checkcast net/minecraft/world/level/block/state/BlockState
      // 0461: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0464: bipush 1
      // 0465: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0468: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 046b: checkcast net/minecraft/world/level/block/state/BlockState
      // 046e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0471: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0474: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 0477: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 047a: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 047d: bipush 1
      // 047e: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0481: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0484: checkcast net/minecraft/world/level/block/state/BlockState
      // 0487: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 048a: bipush 1
      // 048b: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 048e: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0491: checkcast net/minecraft/world/level/block/state/BlockState
      // 0494: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0497: aastore
      // 0498: dup
      // 0499: bipush 1
      // 049a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 049d: dup
      // 049e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 04a1: dup
      // 04a2: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 04a5: ldc 0.1
      // 04a7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 04aa: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 04ad: getstatic net/minecraft/world/level/block/Blocks.MELON_STEM Lnet/minecraft/world/level/block/Block;
      // 04b0: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 04b3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 04b6: aastore
      // 04b7: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 04ba: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 04bd: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 04c0: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 04c3: aload 0
      // 04c4: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_SNOWY Lnet/minecraft/resources/ResourceKey;
      // 04c7: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 04ca: dup
      // 04cb: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 04ce: dup
      // 04cf: new net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest
      // 04d2: dup
      // 04d3: getstatic net/minecraft/tags/BlockTags.DOORS Lnet/minecraft/tags/TagKey;
      // 04d6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.<init> (Lnet/minecraft/tags/TagKey;)V
      // 04d9: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 04dc: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 04df: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 04e2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 04e5: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 04e8: dup
      // 04e9: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 04ec: dup
      // 04ed: getstatic net/minecraft/world/level/block/Blocks.TORCH Lnet/minecraft/world/level/block/Block;
      // 04f0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 04f3: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 04f6: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 04f9: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 04fc: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 04ff: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0502: dup
      // 0503: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0506: dup
      // 0507: getstatic net/minecraft/world/level/block/Blocks.WALL_TORCH Lnet/minecraft/world/level/block/Block;
      // 050a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 050d: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0510: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0513: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0516: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0519: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 051c: dup
      // 051d: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0520: dup
      // 0521: getstatic net/minecraft/world/level/block/Blocks.LANTERN Lnet/minecraft/world/level/block/Block;
      // 0524: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0527: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 052a: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 052d: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0530: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0533: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0536: dup
      // 0537: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 053a: dup
      // 053b: getstatic net/minecraft/world/level/block/Blocks.SPRUCE_PLANKS Lnet/minecraft/world/level/block/Block;
      // 053e: ldc_w 0.2
      // 0541: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0544: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0547: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 054a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 054d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0550: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0553: dup
      // 0554: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0557: dup
      // 0558: getstatic net/minecraft/world/level/block/Blocks.SPRUCE_SLAB Lnet/minecraft/world/level/block/Block;
      // 055b: ldc_w 0.4
      // 055e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0561: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0564: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0567: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 056a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 056d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0570: dup
      // 0571: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0574: dup
      // 0575: getstatic net/minecraft/world/level/block/Blocks.STRIPPED_SPRUCE_LOG Lnet/minecraft/world/level/block/Block;
      // 0578: ldc 0.05
      // 057a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 057d: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0580: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0583: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0586: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0589: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 058c: dup
      // 058d: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0590: dup
      // 0591: getstatic net/minecraft/world/level/block/Blocks.STRIPPED_SPRUCE_WOOD Lnet/minecraft/world/level/block/Block;
      // 0594: ldc 0.05
      // 0596: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0599: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 059c: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 059f: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 05a2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 05a5: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 05a8: dup
      // 05a9: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 05ac: dup
      // 05ad: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 05b0: ldc 0.5
      // 05b2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 05b5: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 05b8: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 05bb: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 05be: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 05c1: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 05c4: dup
      // 05c5: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 05c8: dup
      // 05c9: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 05cc: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 05cf: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 05d2: bipush 1
      // 05d3: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 05d6: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 05d9: checkcast net/minecraft/world/level/block/state/BlockState
      // 05dc: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 05df: bipush 1
      // 05e0: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 05e3: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 05e6: checkcast net/minecraft/world/level/block/state/BlockState
      // 05e9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 05ec: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 05ef: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 05f2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 05f5: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 05f8: bipush 1
      // 05f9: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 05fc: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 05ff: checkcast net/minecraft/world/level/block/state/BlockState
      // 0602: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0605: bipush 1
      // 0606: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0609: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 060c: checkcast net/minecraft/world/level/block/state/BlockState
      // 060f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0612: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0615: dup
      // 0616: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 0619: dup
      // 061a: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 061d: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0620: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0623: bipush 1
      // 0624: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0627: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 062a: checkcast net/minecraft/world/level/block/state/BlockState
      // 062d: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0630: bipush 1
      // 0631: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0634: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0637: checkcast net/minecraft/world/level/block/state/BlockState
      // 063a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 063d: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0640: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 0643: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0646: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0649: bipush 1
      // 064a: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 064d: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0650: checkcast net/minecraft/world/level/block/state/BlockState
      // 0653: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0656: bipush 1
      // 0657: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 065a: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 065d: checkcast net/minecraft/world/level/block/state/BlockState
      // 0660: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0663: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0666: dup
      // 0667: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 066a: dup
      // 066b: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 066e: ldc 0.1
      // 0670: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0673: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0676: getstatic net/minecraft/world/level/block/Blocks.CARROTS Lnet/minecraft/world/level/block/Block;
      // 0679: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 067c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 067f: bipush 1
      // 0680: anewarray 121
      // 0683: dup
      // 0684: bipush 0
      // 0685: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0688: dup
      // 0689: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 068c: dup
      // 068d: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0690: ldc 0.8
      // 0692: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0695: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0698: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 069b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 069e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 06a1: aastore
      // 06a2: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 06a5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 06a8: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 06ab: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 06ae: aload 0
      // 06af: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_TAIGA Lnet/minecraft/resources/ResourceKey;
      // 06b2: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 06b5: dup
      // 06b6: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 06b9: dup
      // 06ba: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 06bd: dup
      // 06be: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 06c1: ldc 0.8
      // 06c3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 06c6: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 06c9: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 06cc: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 06cf: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 06d2: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 06d5: dup
      // 06d6: new net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest
      // 06d9: dup
      // 06da: getstatic net/minecraft/tags/BlockTags.DOORS Lnet/minecraft/tags/TagKey;
      // 06dd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.<init> (Lnet/minecraft/tags/TagKey;)V
      // 06e0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 06e3: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 06e6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 06e9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 06ec: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 06ef: dup
      // 06f0: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 06f3: dup
      // 06f4: getstatic net/minecraft/world/level/block/Blocks.TORCH Lnet/minecraft/world/level/block/Block;
      // 06f7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 06fa: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 06fd: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0700: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0703: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0706: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0709: dup
      // 070a: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 070d: dup
      // 070e: getstatic net/minecraft/world/level/block/Blocks.WALL_TORCH Lnet/minecraft/world/level/block/Block;
      // 0711: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0714: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0717: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 071a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 071d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0720: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0723: dup
      // 0724: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0727: dup
      // 0728: getstatic net/minecraft/world/level/block/Blocks.CAMPFIRE Lnet/minecraft/world/level/block/Block;
      // 072b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 072e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0731: getstatic net/minecraft/world/level/block/Blocks.CAMPFIRE Lnet/minecraft/world/level/block/Block;
      // 0734: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0737: getstatic net/minecraft/world/level/block/CampfireBlock.LIT Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 073a: bipush 0
      // 073b: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 073e: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0741: checkcast net/minecraft/world/level/block/state/BlockState
      // 0744: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0747: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 074a: dup
      // 074b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 074e: dup
      // 074f: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 0752: ldc_w 0.08
      // 0755: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0758: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 075b: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 075e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0761: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0764: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0767: dup
      // 0768: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 076b: dup
      // 076c: getstatic net/minecraft/world/level/block/Blocks.SPRUCE_LOG Lnet/minecraft/world/level/block/Block;
      // 076f: ldc_w 0.08
      // 0772: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0775: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0778: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 077b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 077e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0781: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0784: dup
      // 0785: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0788: dup
      // 0789: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 078c: ldc 0.5
      // 078e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0791: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0794: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0797: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 079a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 079d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 07a0: dup
      // 07a1: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 07a4: dup
      // 07a5: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 07a8: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 07ab: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 07ae: bipush 1
      // 07af: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 07b2: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 07b5: checkcast net/minecraft/world/level/block/state/BlockState
      // 07b8: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 07bb: bipush 1
      // 07bc: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 07bf: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 07c2: checkcast net/minecraft/world/level/block/state/BlockState
      // 07c5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 07c8: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 07cb: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 07ce: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 07d1: getstatic net/minecraft/world/level/block/IronBarsBlock.NORTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 07d4: bipush 1
      // 07d5: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 07d8: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 07db: checkcast net/minecraft/world/level/block/state/BlockState
      // 07de: getstatic net/minecraft/world/level/block/IronBarsBlock.SOUTH Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 07e1: bipush 1
      // 07e2: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 07e5: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 07e8: checkcast net/minecraft/world/level/block/state/BlockState
      // 07eb: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 07ee: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 07f1: dup
      // 07f2: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest
      // 07f5: dup
      // 07f6: getstatic net/minecraft/world/level/block/Blocks.GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 07f9: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 07fc: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 07ff: bipush 1
      // 0800: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0803: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0806: checkcast net/minecraft/world/level/block/state/BlockState
      // 0809: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 080c: bipush 1
      // 080d: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0810: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0813: checkcast net/minecraft/world/level/block/state/BlockState
      // 0816: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockStateMatchTest.<init> (Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0819: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 081c: getstatic net/minecraft/world/level/block/Blocks.BROWN_STAINED_GLASS_PANE Lnet/minecraft/world/level/block/Block;
      // 081f: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0822: getstatic net/minecraft/world/level/block/IronBarsBlock.EAST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0825: bipush 1
      // 0826: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0829: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 082c: checkcast net/minecraft/world/level/block/state/BlockState
      // 082f: getstatic net/minecraft/world/level/block/IronBarsBlock.WEST Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 0832: bipush 1
      // 0833: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0836: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 0839: checkcast net/minecraft/world/level/block/state/BlockState
      // 083c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 083f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0842: dup
      // 0843: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0846: dup
      // 0847: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 084a: ldc_w 0.3
      // 084d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0850: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0853: getstatic net/minecraft/world/level/block/Blocks.PUMPKIN_STEM Lnet/minecraft/world/level/block/Block;
      // 0856: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0859: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 085c: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 085f: dup
      // 0860: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0863: dup
      // 0864: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0867: ldc_w 0.2
      // 086a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 086d: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0870: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 0873: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0876: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0879: bipush 0
      // 087a: anewarray 121
      // 087d: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0880: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0883: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0886: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0889: aload 0
      // 088a: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_DESERT Lnet/minecraft/resources/ResourceKey;
      // 088d: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0890: dup
      // 0891: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0894: dup
      // 0895: new net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest
      // 0898: dup
      // 0899: getstatic net/minecraft/tags/BlockTags.DOORS Lnet/minecraft/tags/TagKey;
      // 089c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/TagMatchTest.<init> (Lnet/minecraft/tags/TagKey;)V
      // 089f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 08a2: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 08a5: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 08a8: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 08ab: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 08ae: dup
      // 08af: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 08b2: dup
      // 08b3: getstatic net/minecraft/world/level/block/Blocks.TORCH Lnet/minecraft/world/level/block/Block;
      // 08b6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 08b9: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 08bc: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 08bf: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 08c2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 08c5: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 08c8: dup
      // 08c9: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 08cc: dup
      // 08cd: getstatic net/minecraft/world/level/block/Blocks.WALL_TORCH Lnet/minecraft/world/level/block/Block;
      // 08d0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 08d3: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 08d6: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 08d9: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 08dc: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 08df: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 08e2: dup
      // 08e3: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 08e6: dup
      // 08e7: getstatic net/minecraft/world/level/block/Blocks.SMOOTH_SANDSTONE Lnet/minecraft/world/level/block/Block;
      // 08ea: ldc_w 0.08
      // 08ed: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 08f0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 08f3: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 08f6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 08f9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 08fc: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 08ff: dup
      // 0900: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0903: dup
      // 0904: getstatic net/minecraft/world/level/block/Blocks.CUT_SANDSTONE Lnet/minecraft/world/level/block/Block;
      // 0907: ldc 0.1
      // 0909: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 090c: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 090f: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0912: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0915: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0918: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 091b: dup
      // 091c: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 091f: dup
      // 0920: getstatic net/minecraft/world/level/block/Blocks.TERRACOTTA Lnet/minecraft/world/level/block/Block;
      // 0923: ldc_w 0.08
      // 0926: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0929: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 092c: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 092f: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0932: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0935: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0938: dup
      // 0939: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 093c: dup
      // 093d: getstatic net/minecraft/world/level/block/Blocks.SMOOTH_SANDSTONE_STAIRS Lnet/minecraft/world/level/block/Block;
      // 0940: ldc_w 0.08
      // 0943: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0946: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0949: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 094c: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 094f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0952: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0955: dup
      // 0956: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0959: dup
      // 095a: getstatic net/minecraft/world/level/block/Blocks.SMOOTH_SANDSTONE_SLAB Lnet/minecraft/world/level/block/Block;
      // 095d: ldc_w 0.08
      // 0960: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0963: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0966: getstatic net/minecraft/world/level/block/Blocks.COBWEB Lnet/minecraft/world/level/block/Block;
      // 0969: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 096c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 096f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0972: dup
      // 0973: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0976: dup
      // 0977: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 097a: ldc_w 0.2
      // 097d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0980: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0983: getstatic net/minecraft/world/level/block/Blocks.BEETROOTS Lnet/minecraft/world/level/block/Block;
      // 0986: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0989: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 098c: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 098f: dup
      // 0990: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0993: dup
      // 0994: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0997: ldc 0.1
      // 0999: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 099c: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 099f: getstatic net/minecraft/world/level/block/Blocks.MELON_STEM Lnet/minecraft/world/level/block/Block;
      // 09a2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 09a5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 09a8: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 09ab: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 09ae: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 09b1: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 09b4: aload 0
      // 09b5: getstatic net/minecraft/data/worldgen/ProcessorLists.MOSSIFY_10_PERCENT Lnet/minecraft/resources/ResourceKey;
      // 09b8: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 09bb: dup
      // 09bc: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 09bf: dup
      // 09c0: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 09c3: dup
      // 09c4: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 09c7: ldc 0.1
      // 09c9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 09cc: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 09cf: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 09d2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 09d5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 09d8: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 09db: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 09de: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 09e1: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 09e4: aload 0
      // 09e5: getstatic net/minecraft/data/worldgen/ProcessorLists.MOSSIFY_20_PERCENT Lnet/minecraft/resources/ResourceKey;
      // 09e8: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 09eb: dup
      // 09ec: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 09ef: dup
      // 09f0: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 09f3: dup
      // 09f4: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 09f7: ldc_w 0.2
      // 09fa: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 09fd: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0a00: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 0a03: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0a06: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0a09: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0a0c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0a0f: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0a12: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0a15: aload 0
      // 0a16: getstatic net/minecraft/data/worldgen/ProcessorLists.MOSSIFY_70_PERCENT Lnet/minecraft/resources/ResourceKey;
      // 0a19: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0a1c: dup
      // 0a1d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0a20: dup
      // 0a21: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0a24: dup
      // 0a25: getstatic net/minecraft/world/level/block/Blocks.COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 0a28: ldc_w 0.7
      // 0a2b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0a2e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0a31: getstatic net/minecraft/world/level/block/Blocks.MOSSY_COBBLESTONE Lnet/minecraft/world/level/block/Block;
      // 0a34: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0a37: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0a3a: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0a3d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0a40: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0a43: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0a46: aload 0
      // 0a47: getstatic net/minecraft/data/worldgen/ProcessorLists.STREET_PLAINS Lnet/minecraft/resources/ResourceKey;
      // 0a4a: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0a4d: dup
      // 0a4e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0a51: dup
      // 0a52: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0a55: dup
      // 0a56: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0a59: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0a5c: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0a5f: dup
      // 0a60: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0a63: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0a66: getstatic net/minecraft/world/level/block/Blocks.OAK_PLANKS Lnet/minecraft/world/level/block/Block;
      // 0a69: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0a6c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0a6f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0a72: dup
      // 0a73: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0a76: dup
      // 0a77: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0a7a: ldc 0.1
      // 0a7c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0a7f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0a82: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0a85: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0a88: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0a8b: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0a8e: dup
      // 0a8f: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0a92: dup
      // 0a93: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0a96: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0a99: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0a9c: dup
      // 0a9d: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0aa0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0aa3: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0aa6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0aa9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0aac: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0aaf: dup
      // 0ab0: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0ab3: dup
      // 0ab4: getstatic net/minecraft/world/level/block/Blocks.DIRT Lnet/minecraft/world/level/block/Block;
      // 0ab7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0aba: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0abd: dup
      // 0abe: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0ac1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0ac4: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0ac7: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0aca: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0acd: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0ad0: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0ad3: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0ad6: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0ad9: aload 0
      // 0ada: getstatic net/minecraft/data/worldgen/ProcessorLists.STREET_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 0add: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0ae0: dup
      // 0ae1: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0ae4: dup
      // 0ae5: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0ae8: dup
      // 0ae9: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0aec: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0aef: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0af2: dup
      // 0af3: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0af6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0af9: getstatic net/minecraft/world/level/block/Blocks.ACACIA_PLANKS Lnet/minecraft/world/level/block/Block;
      // 0afc: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0aff: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0b02: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0b05: dup
      // 0b06: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0b09: dup
      // 0b0a: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0b0d: ldc_w 0.2
      // 0b10: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0b13: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0b16: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0b19: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0b1c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0b1f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0b22: dup
      // 0b23: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b26: dup
      // 0b27: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0b2a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b2d: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b30: dup
      // 0b31: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0b34: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b37: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0b3a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0b3d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0b40: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0b43: dup
      // 0b44: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b47: dup
      // 0b48: getstatic net/minecraft/world/level/block/Blocks.DIRT Lnet/minecraft/world/level/block/Block;
      // 0b4b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b4e: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b51: dup
      // 0b52: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0b55: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b58: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0b5b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0b5e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0b61: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0b64: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0b67: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0b6a: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0b6d: aload 0
      // 0b6e: getstatic net/minecraft/data/worldgen/ProcessorLists.STREET_SNOWY_OR_TAIGA Lnet/minecraft/resources/ResourceKey;
      // 0b71: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0b74: dup
      // 0b75: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0b78: dup
      // 0b79: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b7c: dup
      // 0b7d: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0b80: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b83: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b86: dup
      // 0b87: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0b8a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0b8d: getstatic net/minecraft/world/level/block/Blocks.SPRUCE_PLANKS Lnet/minecraft/world/level/block/Block;
      // 0b90: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0b93: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0b96: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0b99: dup
      // 0b9a: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0b9d: dup
      // 0b9e: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0ba1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0ba4: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0ba7: dup
      // 0ba8: getstatic net/minecraft/world/level/block/Blocks.ICE Lnet/minecraft/world/level/block/Block;
      // 0bab: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0bae: getstatic net/minecraft/world/level/block/Blocks.SPRUCE_PLANKS Lnet/minecraft/world/level/block/Block;
      // 0bb1: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0bb4: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0bb7: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0bba: dup
      // 0bbb: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0bbe: dup
      // 0bbf: getstatic net/minecraft/world/level/block/Blocks.DIRT_PATH Lnet/minecraft/world/level/block/Block;
      // 0bc2: ldc_w 0.2
      // 0bc5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0bc8: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0bcb: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0bce: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0bd1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0bd4: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0bd7: dup
      // 0bd8: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0bdb: dup
      // 0bdc: getstatic net/minecraft/world/level/block/Blocks.GRASS_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0bdf: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0be2: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0be5: dup
      // 0be6: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0be9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0bec: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0bef: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0bf2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0bf5: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0bf8: dup
      // 0bf9: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0bfc: dup
      // 0bfd: getstatic net/minecraft/world/level/block/Blocks.DIRT Lnet/minecraft/world/level/block/Block;
      // 0c00: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0c03: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 0c06: dup
      // 0c07: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0c0a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 0c0d: getstatic net/minecraft/world/level/block/Blocks.WATER Lnet/minecraft/world/level/block/Block;
      // 0c10: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0c13: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0c16: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0c19: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0c1c: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0c1f: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0c22: aload 0
      // 0c23: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_PLAINS Lnet/minecraft/resources/ResourceKey;
      // 0c26: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0c29: dup
      // 0c2a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0c2d: dup
      // 0c2e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0c31: dup
      // 0c32: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0c35: ldc_w 0.3
      // 0c38: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0c3b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0c3e: getstatic net/minecraft/world/level/block/Blocks.CARROTS Lnet/minecraft/world/level/block/Block;
      // 0c41: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0c44: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0c47: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0c4a: dup
      // 0c4b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0c4e: dup
      // 0c4f: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0c52: ldc_w 0.2
      // 0c55: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0c58: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0c5b: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 0c5e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0c61: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0c64: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0c67: dup
      // 0c68: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0c6b: dup
      // 0c6c: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0c6f: ldc 0.1
      // 0c71: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0c74: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0c77: getstatic net/minecraft/world/level/block/Blocks.BEETROOTS Lnet/minecraft/world/level/block/Block;
      // 0c7a: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0c7d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0c80: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0c83: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0c86: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0c89: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0c8c: aload 0
      // 0c8d: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 0c90: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0c93: dup
      // 0c94: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0c97: dup
      // 0c98: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0c9b: dup
      // 0c9c: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0c9f: ldc 0.1
      // 0ca1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0ca4: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0ca7: getstatic net/minecraft/world/level/block/Blocks.MELON_STEM Lnet/minecraft/world/level/block/Block;
      // 0caa: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0cad: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0cb0: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0cb3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0cb6: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0cb9: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0cbc: aload 0
      // 0cbd: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_SNOWY Lnet/minecraft/resources/ResourceKey;
      // 0cc0: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0cc3: dup
      // 0cc4: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0cc7: dup
      // 0cc8: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0ccb: dup
      // 0ccc: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0ccf: ldc 0.1
      // 0cd1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0cd4: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0cd7: getstatic net/minecraft/world/level/block/Blocks.CARROTS Lnet/minecraft/world/level/block/Block;
      // 0cda: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0cdd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0ce0: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0ce3: dup
      // 0ce4: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0ce7: dup
      // 0ce8: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0ceb: ldc 0.8
      // 0ced: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0cf0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0cf3: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 0cf6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0cf9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0cfc: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0cff: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0d02: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0d05: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0d08: aload 0
      // 0d09: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_TAIGA Lnet/minecraft/resources/ResourceKey;
      // 0d0c: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0d0f: dup
      // 0d10: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0d13: dup
      // 0d14: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0d17: dup
      // 0d18: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0d1b: ldc_w 0.3
      // 0d1e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0d21: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0d24: getstatic net/minecraft/world/level/block/Blocks.PUMPKIN_STEM Lnet/minecraft/world/level/block/Block;
      // 0d27: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0d2a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0d2d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0d30: dup
      // 0d31: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0d34: dup
      // 0d35: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0d38: ldc_w 0.2
      // 0d3b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0d3e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0d41: getstatic net/minecraft/world/level/block/Blocks.POTATOES Lnet/minecraft/world/level/block/Block;
      // 0d44: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0d47: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0d4a: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0d4d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0d50: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0d53: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0d56: aload 0
      // 0d57: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_DESERT Lnet/minecraft/resources/ResourceKey;
      // 0d5a: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0d5d: dup
      // 0d5e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0d61: dup
      // 0d62: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0d65: dup
      // 0d66: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0d69: ldc_w 0.2
      // 0d6c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0d6f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0d72: getstatic net/minecraft/world/level/block/Blocks.BEETROOTS Lnet/minecraft/world/level/block/Block;
      // 0d75: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0d78: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0d7b: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0d7e: dup
      // 0d7f: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0d82: dup
      // 0d83: getstatic net/minecraft/world/level/block/Blocks.WHEAT Lnet/minecraft/world/level/block/Block;
      // 0d86: ldc 0.1
      // 0d88: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0d8b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0d8e: getstatic net/minecraft/world/level/block/Blocks.MELON_STEM Lnet/minecraft/world/level/block/Block;
      // 0d91: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0d94: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0d97: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0d9a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0d9d: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0da0: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0da3: aload 0
      // 0da4: getstatic net/minecraft/data/worldgen/ProcessorLists.OUTPOST_ROT Lnet/minecraft/resources/ResourceKey;
      // 0da7: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 0daa: dup
      // 0dab: ldc 0.05
      // 0dad: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (F)V
      // 0db0: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0db3: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0db6: aload 0
      // 0db7: getstatic net/minecraft/data/worldgen/ProcessorLists.BOTTOM_RAMPART Lnet/minecraft/resources/ResourceKey;
      // 0dba: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0dbd: dup
      // 0dbe: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0dc1: dup
      // 0dc2: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0dc5: dup
      // 0dc6: getstatic net/minecraft/world/level/block/Blocks.MAGMA_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0dc9: ldc_w 0.75
      // 0dcc: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0dcf: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0dd2: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0dd5: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0dd8: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0ddb: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0dde: dup
      // 0ddf: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0de2: dup
      // 0de3: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0de6: ldc_w 0.15
      // 0de9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0dec: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0def: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0df2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0df5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0df8: aload 3
      // 0df9: aload 2
      // 0dfa: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0dfd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0e00: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0e03: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0e06: aload 0
      // 0e07: getstatic net/minecraft/data/worldgen/ProcessorLists.TREASURE_ROOMS Lnet/minecraft/resources/ResourceKey;
      // 0e0a: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0e0d: dup
      // 0e0e: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0e11: dup
      // 0e12: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0e15: dup
      // 0e16: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0e19: ldc_w 0.35
      // 0e1c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0e1f: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0e22: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0e25: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0e28: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0e2b: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0e2e: dup
      // 0e2f: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0e32: dup
      // 0e33: getstatic net/minecraft/world/level/block/Blocks.CHISELED_POLISHED_BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0e36: ldc 0.1
      // 0e38: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0e3b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0e3e: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0e41: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0e44: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0e47: aload 3
      // 0e48: aload 2
      // 0e49: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0e4c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0e4f: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0e52: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0e55: aload 0
      // 0e56: getstatic net/minecraft/data/worldgen/ProcessorLists.HOUSING Lnet/minecraft/resources/ResourceKey;
      // 0e59: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0e5c: dup
      // 0e5d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0e60: dup
      // 0e61: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0e64: dup
      // 0e65: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0e68: ldc_w 0.3
      // 0e6b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0e6e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0e71: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0e74: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0e77: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0e7a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0e7d: dup
      // 0e7e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0e81: dup
      // 0e82: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0e85: ldc_w 1.0E-4
      // 0e88: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0e8b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0e8e: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0e91: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0e94: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0e97: aload 3
      // 0e98: aload 2
      // 0e99: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0e9c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0e9f: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0ea2: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0ea5: aload 0
      // 0ea6: getstatic net/minecraft/data/worldgen/ProcessorLists.SIDE_WALL_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 0ea9: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0eac: dup
      // 0ead: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0eb0: dup
      // 0eb1: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0eb4: dup
      // 0eb5: getstatic net/minecraft/world/level/block/Blocks.CHISELED_POLISHED_BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0eb8: ldc 0.5
      // 0eba: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0ebd: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0ec0: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0ec3: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0ec6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0ec9: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0ecc: dup
      // 0ecd: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0ed0: dup
      // 0ed1: getstatic net/minecraft/world/level/block/Blocks.GOLD_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0ed4: ldc 0.1
      // 0ed6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0ed9: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0edc: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0edf: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0ee2: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0ee5: aload 3
      // 0ee6: aload 2
      // 0ee7: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0eea: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0eed: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0ef0: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0ef3: aload 0
      // 0ef4: getstatic net/minecraft/data/worldgen/ProcessorLists.STABLE_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 0ef7: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0efa: dup
      // 0efb: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0efe: dup
      // 0eff: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0f02: dup
      // 0f03: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0f06: ldc 0.1
      // 0f08: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0f0b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0f0e: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0f11: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0f14: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0f17: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0f1a: dup
      // 0f1b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0f1e: dup
      // 0f1f: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0f22: ldc_w 1.0E-4
      // 0f25: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0f28: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0f2b: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0f2e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0f31: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0f34: aload 3
      // 0f35: aload 2
      // 0f36: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0f39: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0f3c: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0f3f: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0f42: aload 0
      // 0f43: getstatic net/minecraft/data/worldgen/ProcessorLists.BASTION_GENERIC_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 0f46: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0f49: dup
      // 0f4a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0f4d: dup
      // 0f4e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0f51: dup
      // 0f52: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0f55: ldc_w 0.3
      // 0f58: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0f5b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0f5e: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0f61: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0f64: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0f67: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0f6a: dup
      // 0f6b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0f6e: dup
      // 0f6f: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0f72: ldc_w 1.0E-4
      // 0f75: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0f78: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0f7b: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 0f7e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0f81: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0f84: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0f87: dup
      // 0f88: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0f8b: dup
      // 0f8c: getstatic net/minecraft/world/level/block/Blocks.GOLD_BLOCK Lnet/minecraft/world/level/block/Block;
      // 0f8f: ldc_w 0.3
      // 0f92: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0f95: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0f98: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0f9b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0f9e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0fa1: aload 3
      // 0fa2: aload 2
      // 0fa3: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0fa6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 0fa9: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 0fac: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 0faf: aload 0
      // 0fb0: getstatic net/minecraft/data/worldgen/ProcessorLists.RAMPART_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 0fb3: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 0fb6: dup
      // 0fb7: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0fba: dup
      // 0fbb: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0fbe: dup
      // 0fbf: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0fc2: ldc_w 0.4
      // 0fc5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0fc8: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0fcb: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0fce: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0fd1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0fd4: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0fd7: dup
      // 0fd8: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0fdb: dup
      // 0fdc: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 0fdf: ldc 0.01
      // 0fe1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 0fe4: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 0fe7: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0fea: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 0fed: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 0ff0: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 0ff3: dup
      // 0ff4: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 0ff7: dup
      // 0ff8: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 0ffb: ldc_w 1.0E-4
      // 0ffe: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1001: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1004: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 1007: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 100a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 100d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1010: dup
      // 1011: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1014: dup
      // 1015: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 1018: ldc_w 1.0E-4
      // 101b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 101e: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1021: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 1024: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1027: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 102a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 102d: dup
      // 102e: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1031: dup
      // 1032: getstatic net/minecraft/world/level/block/Blocks.GOLD_BLOCK Lnet/minecraft/world/level/block/Block;
      // 1035: ldc_w 0.3
      // 1038: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 103b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 103e: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1041: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1044: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1047: aload 3
      // 1048: aload 2
      // 1049: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 104c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 104f: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1052: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1055: aload 0
      // 1056: getstatic net/minecraft/data/worldgen/ProcessorLists.ENTRANCE_REPLACEMENT Lnet/minecraft/resources/ResourceKey;
      // 1059: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 105c: dup
      // 105d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1060: dup
      // 1061: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1064: dup
      // 1065: getstatic net/minecraft/world/level/block/Blocks.CHISELED_POLISHED_BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 1068: ldc 0.5
      // 106a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 106d: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1070: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 1073: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1076: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1079: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 107c: dup
      // 107d: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1080: dup
      // 1081: getstatic net/minecraft/world/level/block/Blocks.GOLD_BLOCK Lnet/minecraft/world/level/block/Block;
      // 1084: ldc_w 0.6
      // 1087: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 108a: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 108d: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1090: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1093: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1096: aload 3
      // 1097: aload 2
      // 1098: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 109b: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 109e: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 10a1: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 10a4: aload 0
      // 10a5: getstatic net/minecraft/data/worldgen/ProcessorLists.BRIDGE Lnet/minecraft/resources/ResourceKey;
      // 10a8: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 10ab: dup
      // 10ac: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 10af: dup
      // 10b0: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 10b3: dup
      // 10b4: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 10b7: ldc_w 0.3
      // 10ba: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 10bd: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 10c0: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 10c3: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 10c6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 10c9: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 10cc: dup
      // 10cd: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 10d0: dup
      // 10d1: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 10d4: ldc_w 1.0E-4
      // 10d7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 10da: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 10dd: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 10e0: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 10e3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 10e6: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 10e9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 10ec: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 10ef: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 10f2: aload 0
      // 10f3: getstatic net/minecraft/data/worldgen/ProcessorLists.ROOF Lnet/minecraft/resources/ResourceKey;
      // 10f6: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 10f9: dup
      // 10fa: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 10fd: dup
      // 10fe: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1101: dup
      // 1102: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1105: ldc_w 0.3
      // 1108: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 110b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 110e: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1111: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1114: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1117: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 111a: dup
      // 111b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 111e: dup
      // 111f: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1122: ldc_w 0.15
      // 1125: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1128: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 112b: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 112e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1131: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1134: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1137: dup
      // 1138: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 113b: dup
      // 113c: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 113f: ldc_w 0.3
      // 1142: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1145: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1148: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 114b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 114e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1151: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1154: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1157: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 115a: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 115d: aload 0
      // 115e: getstatic net/minecraft/data/worldgen/ProcessorLists.HIGH_WALL Lnet/minecraft/resources/ResourceKey;
      // 1161: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 1164: dup
      // 1165: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1168: dup
      // 1169: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 116c: dup
      // 116d: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1170: ldc 0.01
      // 1172: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1175: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1178: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 117b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 117e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1181: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1184: dup
      // 1185: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1188: dup
      // 1189: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 118c: ldc 0.5
      // 118e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1191: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1194: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1197: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 119a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 119d: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 11a0: dup
      // 11a1: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 11a4: dup
      // 11a5: getstatic net/minecraft/world/level/block/Blocks.POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 11a8: ldc_w 0.3
      // 11ab: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 11ae: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 11b1: getstatic net/minecraft/world/level/block/Blocks.BLACKSTONE Lnet/minecraft/world/level/block/Block;
      // 11b4: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 11b7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 11ba: aload 3
      // 11bb: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 11be: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 11c1: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 11c4: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 11c7: aload 0
      // 11c8: getstatic net/minecraft/data/worldgen/ProcessorLists.HIGH_RAMPART Lnet/minecraft/resources/ResourceKey;
      // 11cb: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 11ce: dup
      // 11cf: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 11d2: dup
      // 11d3: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 11d6: dup
      // 11d7: getstatic net/minecraft/world/level/block/Blocks.GOLD_BLOCK Lnet/minecraft/world/level/block/Block;
      // 11da: ldc_w 0.3
      // 11dd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 11e0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 11e3: getstatic net/minecraft/world/level/block/Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 11e6: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 11e9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 11ec: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 11ef: dup
      // 11f0: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 11f3: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 11f6: new net/minecraft/world/level/levelgen/structure/templatesystem/AxisAlignedLinearPosTest
      // 11f9: dup
      // 11fa: fconst_0
      // 11fb: ldc 0.05
      // 11fd: bipush 0
      // 11fe: bipush 100
      // 1200: getstatic net/minecraft/core/Direction$Axis.Y Lnet/minecraft/core/Direction$Axis;
      // 1203: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/AxisAlignedLinearPosTest.<init> (FFIILnet/minecraft/core/Direction$Axis;)V
      // 1206: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 1209: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 120c: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/PosRuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 120f: aload 3
      // 1210: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1213: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1216: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1219: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 121c: aload 0
      // 121d: getstatic net/minecraft/data/worldgen/ProcessorLists.FOSSIL_ROT Lnet/minecraft/resources/ResourceKey;
      // 1220: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 1223: dup
      // 1224: ldc_w 0.9
      // 1227: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (F)V
      // 122a: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 122d: dup
      // 122e: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 1231: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 1234: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1237: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 123a: aload 0
      // 123b: getstatic net/minecraft/data/worldgen/ProcessorLists.FOSSIL_COAL Lnet/minecraft/resources/ResourceKey;
      // 123e: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 1241: dup
      // 1242: ldc 0.1
      // 1244: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (F)V
      // 1247: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 124a: dup
      // 124b: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 124e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 1251: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1254: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1257: aload 0
      // 1258: getstatic net/minecraft/data/worldgen/ProcessorLists.FOSSIL_DIAMONDS Lnet/minecraft/resources/ResourceKey;
      // 125b: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 125e: dup
      // 125f: ldc 0.1
      // 1261: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (F)V
      // 1264: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 1267: dup
      // 1268: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 126b: dup
      // 126c: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest
      // 126f: dup
      // 1270: getstatic net/minecraft/world/level/block/Blocks.COAL_ORE Lnet/minecraft/world/level/block/Block;
      // 1273: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;)V
      // 1276: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1279: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_DIAMOND_ORE Lnet/minecraft/world/level/block/Block;
      // 127c: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 127f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1282: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1285: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1288: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 128b: dup
      // 128c: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 128f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 1292: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1295: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1298: aload 0
      // 1299: getstatic net/minecraft/data/worldgen/ProcessorLists.ANCIENT_CITY_START_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 129c: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 129f: dup
      // 12a0: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 12a3: dup
      // 12a4: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 12a7: dup
      // 12a8: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 12ab: ldc_w 0.3
      // 12ae: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 12b1: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 12b4: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 12b7: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 12ba: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 12bd: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 12c0: dup
      // 12c1: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 12c4: dup
      // 12c5: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 12c8: ldc_w 0.3
      // 12cb: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 12ce: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 12d1: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 12d4: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 12d7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 12da: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 12dd: dup
      // 12de: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 12e1: dup
      // 12e2: getstatic net/minecraft/world/level/block/Blocks.SOUL_LANTERN Lnet/minecraft/world/level/block/Block;
      // 12e5: ldc 0.05
      // 12e7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 12ea: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 12ed: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 12f0: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 12f3: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 12f6: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 12f9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 12fc: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 12ff: dup
      // 1300: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 1303: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 1306: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1309: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 130c: aload 0
      // 130d: getstatic net/minecraft/data/worldgen/ProcessorLists.ANCIENT_CITY_GENERIC_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 1310: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 1313: dup
      // 1314: aload 1
      // 1315: getstatic net/minecraft/tags/BlockTags.ANCIENT_CITY_REPLACEABLE Lnet/minecraft/tags/TagKey;
      // 1318: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/tags/TagKey;)Lnet/minecraft/core/HolderSet$Named; 2
      // 131d: ldc_w 0.95
      // 1320: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (Lnet/minecraft/core/HolderSet;F)V
      // 1323: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 1326: dup
      // 1327: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 132a: dup
      // 132b: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 132e: dup
      // 132f: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1332: ldc_w 0.3
      // 1335: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1338: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 133b: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 133e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1341: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1344: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1347: dup
      // 1348: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 134b: dup
      // 134c: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 134f: ldc_w 0.3
      // 1352: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1355: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1358: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 135b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 135e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1361: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1364: dup
      // 1365: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1368: dup
      // 1369: getstatic net/minecraft/world/level/block/Blocks.SOUL_LANTERN Lnet/minecraft/world/level/block/Block;
      // 136c: ldc 0.05
      // 136e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1371: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1374: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 1377: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 137a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 137d: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1380: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1383: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 1386: dup
      // 1387: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 138a: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 138d: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1390: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1393: aload 0
      // 1394: getstatic net/minecraft/data/worldgen/ProcessorLists.ANCIENT_CITY_WALLS_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 1397: new net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor
      // 139a: dup
      // 139b: aload 1
      // 139c: getstatic net/minecraft/tags/BlockTags.ANCIENT_CITY_REPLACEABLE Lnet/minecraft/tags/TagKey;
      // 139f: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/tags/TagKey;)Lnet/minecraft/core/HolderSet$Named; 2
      // 13a4: ldc_w 0.95
      // 13a7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.<init> (Lnet/minecraft/core/HolderSet;F)V
      // 13aa: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 13ad: dup
      // 13ae: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 13b1: dup
      // 13b2: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 13b5: dup
      // 13b6: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 13b9: ldc_w 0.3
      // 13bc: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 13bf: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 13c2: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_BRICKS Lnet/minecraft/world/level/block/Block;
      // 13c5: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 13c8: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 13cb: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 13ce: dup
      // 13cf: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 13d2: dup
      // 13d3: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 13d6: ldc_w 0.3
      // 13d9: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 13dc: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 13df: getstatic net/minecraft/world/level/block/Blocks.CRACKED_DEEPSLATE_TILES Lnet/minecraft/world/level/block/Block;
      // 13e2: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 13e5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 13e8: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 13eb: dup
      // 13ec: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 13ef: dup
      // 13f0: getstatic net/minecraft/world/level/block/Blocks.DEEPSLATE_TILE_SLAB Lnet/minecraft/world/level/block/Block;
      // 13f3: ldc_w 0.3
      // 13f6: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 13f9: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 13fc: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 13ff: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1402: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1405: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1408: dup
      // 1409: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 140c: dup
      // 140d: getstatic net/minecraft/world/level/block/Blocks.SOUL_LANTERN Lnet/minecraft/world/level/block/Block;
      // 1410: ldc 0.05
      // 1412: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1415: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1418: getstatic net/minecraft/world/level/block/Blocks.AIR Lnet/minecraft/world/level/block/Block;
      // 141b: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 141e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1421: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1424: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1427: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 142a: dup
      // 142b: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 142e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 1431: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 1434: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1437: aload 0
      // 1438: getstatic net/minecraft/data/worldgen/ProcessorLists.TRAIL_RUINS_HOUSES_ARCHAEOLOGY Lnet/minecraft/resources/ResourceKey;
      // 143b: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 143e: dup
      // 143f: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1442: dup
      // 1443: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1446: dup
      // 1447: getstatic net/minecraft/world/level/block/Blocks.GRAVEL Lnet/minecraft/world/level/block/Block;
      // 144a: ldc_w 0.2
      // 144d: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1450: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1453: getstatic net/minecraft/world/level/block/Blocks.DIRT Lnet/minecraft/world/level/block/Block;
      // 1456: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1459: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 145c: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 145f: dup
      // 1460: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1463: dup
      // 1464: getstatic net/minecraft/world/level/block/Blocks.GRAVEL Lnet/minecraft/world/level/block/Block;
      // 1467: ldc 0.1
      // 1469: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 146c: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 146f: getstatic net/minecraft/world/level/block/Blocks.COARSE_DIRT Lnet/minecraft/world/level/block/Block;
      // 1472: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1475: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1478: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 147b: dup
      // 147c: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 147f: dup
      // 1480: getstatic net/minecraft/world/level/block/Blocks.MUD_BRICKS Lnet/minecraft/world/level/block/Block;
      // 1483: ldc 0.1
      // 1485: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1488: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 148b: getstatic net/minecraft/world/level/block/Blocks.PACKED_MUD Lnet/minecraft/world/level/block/Block;
      // 148e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1491: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1494: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 1497: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 149a: getstatic net/minecraft/world/level/storage/loot/BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON Lnet/minecraft/resources/ResourceKey;
      // 149d: bipush 6
      // 149f: invokestatic net/minecraft/data/worldgen/ProcessorLists.trailsArchyLootProcessor (Lnet/minecraft/resources/ResourceKey;I)Lnet/minecraft/world/level/levelgen/structure/templatesystem/CappedProcessor;
      // 14a2: getstatic net/minecraft/world/level/storage/loot/BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE Lnet/minecraft/resources/ResourceKey;
      // 14a5: bipush 3
      // 14a6: invokestatic net/minecraft/data/worldgen/ProcessorLists.trailsArchyLootProcessor (Lnet/minecraft/resources/ResourceKey;I)Lnet/minecraft/world/level/levelgen/structure/templatesystem/CappedProcessor;
      // 14a9: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 14ac: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 14af: aload 0
      // 14b0: getstatic net/minecraft/data/worldgen/ProcessorLists.TRAIL_RUINS_ROADS_ARCHAEOLOGY Lnet/minecraft/resources/ResourceKey;
      // 14b3: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 14b6: dup
      // 14b7: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 14ba: dup
      // 14bb: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 14be: dup
      // 14bf: getstatic net/minecraft/world/level/block/Blocks.GRAVEL Lnet/minecraft/world/level/block/Block;
      // 14c2: ldc_w 0.2
      // 14c5: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 14c8: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 14cb: getstatic net/minecraft/world/level/block/Blocks.DIRT Lnet/minecraft/world/level/block/Block;
      // 14ce: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 14d1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 14d4: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 14d7: dup
      // 14d8: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 14db: dup
      // 14dc: getstatic net/minecraft/world/level/block/Blocks.GRAVEL Lnet/minecraft/world/level/block/Block;
      // 14df: ldc 0.1
      // 14e1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 14e4: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 14e7: getstatic net/minecraft/world/level/block/Blocks.COARSE_DIRT Lnet/minecraft/world/level/block/Block;
      // 14ea: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 14ed: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 14f0: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 14f3: dup
      // 14f4: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 14f7: dup
      // 14f8: getstatic net/minecraft/world/level/block/Blocks.MUD_BRICKS Lnet/minecraft/world/level/block/Block;
      // 14fb: ldc 0.1
      // 14fd: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1500: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1503: getstatic net/minecraft/world/level/block/Blocks.PACKED_MUD Lnet/minecraft/world/level/block/Block;
      // 1506: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1509: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 150c: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 150f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 1512: getstatic net/minecraft/world/level/storage/loot/BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON Lnet/minecraft/resources/ResourceKey;
      // 1515: bipush 2
      // 1516: invokestatic net/minecraft/data/worldgen/ProcessorLists.trailsArchyLootProcessor (Lnet/minecraft/resources/ResourceKey;I)Lnet/minecraft/world/level/levelgen/structure/templatesystem/CappedProcessor;
      // 1519: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 151c: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 151f: aload 0
      // 1520: getstatic net/minecraft/data/worldgen/ProcessorLists.TRAIL_RUINS_TOWER_TOP_ARCHAEOLOGY Lnet/minecraft/resources/ResourceKey;
      // 1523: getstatic net/minecraft/world/level/storage/loot/BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON Lnet/minecraft/resources/ResourceKey;
      // 1526: bipush 2
      // 1527: invokestatic net/minecraft/data/worldgen/ProcessorLists.trailsArchyLootProcessor (Lnet/minecraft/resources/ResourceKey;I)Lnet/minecraft/world/level/levelgen/structure/templatesystem/CappedProcessor;
      // 152a: invokestatic java/util/List.of (Ljava/lang/Object;)Ljava/util/List;
      // 152d: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 1530: aload 0
      // 1531: getstatic net/minecraft/data/worldgen/ProcessorLists.TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION Lnet/minecraft/resources/ResourceKey;
      // 1534: new net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor
      // 1537: dup
      // 1538: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 153b: dup
      // 153c: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 153f: dup
      // 1540: getstatic net/minecraft/world/level/block/Blocks.WAXED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 1543: ldc 0.1
      // 1545: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1548: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 154b: getstatic net/minecraft/world/level/block/Blocks.WAXED_OXIDIZED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 154e: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 1551: getstatic net/minecraft/world/level/block/CopperBulbBlock.LIT Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 1554: bipush 1
      // 1555: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 1558: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 155b: checkcast net/minecraft/world/level/block/state/BlockState
      // 155e: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 1561: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 1564: dup
      // 1565: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1568: dup
      // 1569: getstatic net/minecraft/world/level/block/Blocks.WAXED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 156c: ldc_w 0.33333334
      // 156f: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 1572: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 1575: getstatic net/minecraft/world/level/block/Blocks.WAXED_WEATHERED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 1578: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 157b: getstatic net/minecraft/world/level/block/CopperBulbBlock.LIT Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 157e: bipush 1
      // 157f: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 1582: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 1585: checkcast net/minecraft/world/level/block/state/BlockState
      // 1588: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 158b: new net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule
      // 158e: dup
      // 158f: new net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest
      // 1592: dup
      // 1593: getstatic net/minecraft/world/level/block/Blocks.WAXED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 1596: ldc 0.5
      // 1598: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.<init> (Lnet/minecraft/world/level/block/Block;F)V
      // 159b: getstatic net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.INSTANCE Lnet/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest;
      // 159e: getstatic net/minecraft/world/level/block/Blocks.WAXED_EXPOSED_COPPER_BULB Lnet/minecraft/world/level/block/Block;
      // 15a1: invokevirtual net/minecraft/world/level/block/Block.defaultBlockState ()Lnet/minecraft/world/level/block/state/BlockState;
      // 15a4: getstatic net/minecraft/world/level/block/CopperBulbBlock.LIT Lnet/minecraft/world/level/block/state/properties/BooleanProperty;
      // 15a7: bipush 1
      // 15a8: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 15ab: invokevirtual net/minecraft/world/level/block/state/BlockState.setValue (Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;
      // 15ae: checkcast net/minecraft/world/level/block/state/BlockState
      // 15b1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProcessorRule.<init> (Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/levelgen/structure/templatesystem/RuleTest;Lnet/minecraft/world/level/block/state/BlockState;)V
      // 15b4: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 15b7: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.<init> (Ljava/util/List;)V
      // 15ba: new net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor
      // 15bd: dup
      // 15be: getstatic net/minecraft/tags/BlockTags.FEATURES_CANNOT_REPLACE Lnet/minecraft/tags/TagKey;
      // 15c1: invokespecial net/minecraft/world/level/levelgen/structure/templatesystem/ProtectedBlockProcessor.<init> (Lnet/minecraft/tags/TagKey;)V
      // 15c4: invokestatic java/util/List.of (Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;
      // 15c7: invokestatic net/minecraft/data/worldgen/ProcessorLists.register (Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V
      // 15ca: return
   }

   private static CappedProcessor trailsArchyLootProcessor(ResourceKey<LootTable> $$0, int $$1) {
      return new CappedProcessor(
         new RuleProcessor(
            List.of(
               new ProcessorRule(
                  new TagMatchTest(BlockTags.TRAIL_RUINS_REPLACEABLE),
                  AlwaysTrueTest.INSTANCE,
                  PosAlwaysTrueTest.INSTANCE,
                  Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),
                  new AppendLoot($$0)
               )
            )
         ),
         ConstantInt.of($$1)
      );
   }
}
