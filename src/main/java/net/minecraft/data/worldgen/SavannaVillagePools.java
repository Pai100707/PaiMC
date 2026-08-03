package net.minecraft.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class SavannaVillagePools {
   public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/savanna/town_centers");
   private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/savanna/terminators");
   private static final ResourceKey<StructureTemplatePool> ZOMBIE_TERMINATORS_KEY = Pools.createKey("village/savanna/zombie/terminators");

   public static void bootstrap(BootstrapContext<StructureTemplatePool> param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.OutOfMemoryError: Java heap space
      //   at org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory$FastSparseSet.getCopy(FastSparseSetFactory.java:95)
      //   at org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect.getCopy(SFormsFastMapDirect.java:67)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx.updateLiveMap(SSAUConstructorSparseEx.java:269)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx.varReadSingleVersion(SSAUConstructorSparseEx.java:110)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.varRead(SFormsConstructor.java:167)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent.processSforms(VarExprent.java:516)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.processSforms(Exprent.java:317)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent.processSforms(AssignmentExprent.java:337)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.ssaStatements(SFormsConstructor.java:126)
      //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx.splitVariables(SSAUConstructorSparseEx.java:45)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:65)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:231)
      //
      // Bytecode:
      // 000: aload 0
      // 001: getstatic net/minecraft/core/registries/Registries.PLACED_FEATURE Lnet/minecraft/resources/ResourceKey;
      // 004: invokeinterface net/minecraft/data/worldgen/BootstrapContext.lookup (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/HolderGetter; 2
      // 009: astore 1
      // 00a: aload 1
      // 00b: getstatic net/minecraft/data/worldgen/placement/VillagePlacements.ACACIA_VILLAGE Lnet/minecraft/resources/ResourceKey;
      // 00e: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 013: astore 2
      // 014: aload 1
      // 015: getstatic net/minecraft/data/worldgen/placement/VillagePlacements.PILE_HAY_VILLAGE Lnet/minecraft/resources/ResourceKey;
      // 018: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 01d: astore 3
      // 01e: aload 1
      // 01f: getstatic net/minecraft/data/worldgen/placement/VillagePlacements.PILE_MELON_VILLAGE Lnet/minecraft/resources/ResourceKey;
      // 022: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 027: astore 4
      // 029: aload 0
      // 02a: getstatic net/minecraft/core/registries/Registries.PROCESSOR_LIST Lnet/minecraft/resources/ResourceKey;
      // 02d: invokeinterface net/minecraft/data/worldgen/BootstrapContext.lookup (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/HolderGetter; 2
      // 032: astore 5
      // 034: aload 5
      // 036: getstatic net/minecraft/data/worldgen/ProcessorLists.ZOMBIE_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 039: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 03e: astore 6
      // 040: aload 5
      // 042: getstatic net/minecraft/data/worldgen/ProcessorLists.STREET_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 045: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 04a: astore 7
      // 04c: aload 5
      // 04e: getstatic net/minecraft/data/worldgen/ProcessorLists.FARM_SAVANNA Lnet/minecraft/resources/ResourceKey;
      // 051: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 056: astore 8
      // 058: aload 0
      // 059: getstatic net/minecraft/core/registries/Registries.TEMPLATE_POOL Lnet/minecraft/resources/ResourceKey;
      // 05c: invokeinterface net/minecraft/data/worldgen/BootstrapContext.lookup (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/HolderGetter; 2
      // 061: astore 9
      // 063: aload 9
      // 065: getstatic net/minecraft/data/worldgen/Pools.EMPTY Lnet/minecraft/resources/ResourceKey;
      // 068: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 06d: astore 10
      // 06f: aload 9
      // 071: getstatic net/minecraft/data/worldgen/SavannaVillagePools.TERMINATORS_KEY Lnet/minecraft/resources/ResourceKey;
      // 074: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 079: astore 11
      // 07b: aload 9
      // 07d: getstatic net/minecraft/data/worldgen/SavannaVillagePools.ZOMBIE_TERMINATORS_KEY Lnet/minecraft/resources/ResourceKey;
      // 080: invokeinterface net/minecraft/core/HolderGetter.getOrThrow (Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference; 2
      // 085: astore 12
      // 087: aload 0
      // 088: getstatic net/minecraft/data/worldgen/SavannaVillagePools.START Lnet/minecraft/resources/ResourceKey;
      // 08b: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 08e: dup
      // 08f: aload 10
      // 091: ldc "village/savanna/town_centers/savanna_meeting_point_1"
      // 093: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 096: bipush 100
      // 098: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 09b: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 09e: ldc "village/savanna/town_centers/savanna_meeting_point_2"
      // 0a0: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 0a3: bipush 50
      // 0a5: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0a8: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0ab: ldc "village/savanna/town_centers/savanna_meeting_point_3"
      // 0ad: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 0b0: sipush 150
      // 0b3: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0b6: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0b9: ldc "village/savanna/town_centers/savanna_meeting_point_4"
      // 0bb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 0be: sipush 150
      // 0c1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0c4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0c7: ldc "village/savanna/zombie/town_centers/savanna_meeting_point_1"
      // 0c9: aload 6
      // 0cb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 0ce: bipush 2
      // 0cf: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0d2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0d5: ldc "village/savanna/zombie/town_centers/savanna_meeting_point_2"
      // 0d7: aload 6
      // 0d9: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 0dc: bipush 1
      // 0dd: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0e0: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0e3: ldc "village/savanna/zombie/town_centers/savanna_meeting_point_3"
      // 0e5: aload 6
      // 0e7: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 0ea: bipush 3
      // 0eb: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0ee: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0f1: ldc "village/savanna/zombie/town_centers/savanna_meeting_point_4"
      // 0f3: aload 6
      // 0f5: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 0f8: bipush 3
      // 0f9: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 0fc: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 0ff: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 102: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 105: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 108: invokeinterface net/minecraft/data/worldgen/BootstrapContext.register (Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference; 3
      // 10d: pop
      // 10e: aload 0
      // 10f: ldc "village/savanna/streets"
      // 111: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 114: dup
      // 115: aload 11
      // 117: ldc "village/savanna/streets/corner_01"
      // 119: aload 7
      // 11b: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 11e: bipush 2
      // 11f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 122: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 125: ldc "village/savanna/streets/corner_03"
      // 127: aload 7
      // 129: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 12c: bipush 2
      // 12d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 130: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 133: ldc "village/savanna/streets/straight_02"
      // 135: aload 7
      // 137: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 13a: bipush 4
      // 13b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 13e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 141: ldc "village/savanna/streets/straight_04"
      // 143: aload 7
      // 145: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 148: bipush 7
      // 14a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 14d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 150: ldc "village/savanna/streets/straight_05"
      // 152: aload 7
      // 154: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 157: bipush 3
      // 158: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 15b: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 15e: ldc "village/savanna/streets/straight_06"
      // 160: aload 7
      // 162: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 165: bipush 4
      // 166: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 169: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 16c: ldc "village/savanna/streets/straight_08"
      // 16e: aload 7
      // 170: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 173: bipush 4
      // 174: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 177: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 17a: ldc "village/savanna/streets/straight_09"
      // 17c: aload 7
      // 17e: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 181: bipush 4
      // 182: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 185: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 188: ldc "village/savanna/streets/straight_10"
      // 18a: aload 7
      // 18c: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 18f: bipush 4
      // 190: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 193: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 196: ldc "village/savanna/streets/straight_11"
      // 198: aload 7
      // 19a: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 19d: bipush 4
      // 19e: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1a1: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1a4: ldc "village/savanna/streets/crossroad_02"
      // 1a6: aload 7
      // 1a8: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 1ab: bipush 1
      // 1ac: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1af: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1b2: ldc "village/savanna/streets/crossroad_03"
      // 1b4: aload 7
      // 1b6: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 1b9: bipush 2
      // 1ba: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1bd: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1c0: bipush 7
      // 1c2: anewarray 101
      // 1c5: dup
      // 1c6: bipush 0
      // 1c7: ldc "village/savanna/streets/crossroad_04"
      // 1c9: aload 7
      // 1cb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 1ce: bipush 2
      // 1cf: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1d2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1d5: aastore
      // 1d6: dup
      // 1d7: bipush 1
      // 1d8: ldc "village/savanna/streets/crossroad_05"
      // 1da: aload 7
      // 1dc: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 1df: bipush 2
      // 1e0: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1e3: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1e6: aastore
      // 1e7: dup
      // 1e8: bipush 2
      // 1e9: ldc "village/savanna/streets/crossroad_06"
      // 1eb: aload 7
      // 1ed: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 1f0: bipush 2
      // 1f1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 1f4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 1f7: aastore
      // 1f8: dup
      // 1f9: bipush 3
      // 1fa: ldc "village/savanna/streets/crossroad_07"
      // 1fc: aload 7
      // 1fe: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 201: bipush 2
      // 202: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 205: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 208: aastore
      // 209: dup
      // 20a: bipush 4
      // 20b: ldc "village/savanna/streets/split_01"
      // 20d: aload 7
      // 20f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 212: bipush 2
      // 213: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 216: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 219: aastore
      // 21a: dup
      // 21b: bipush 5
      // 21c: ldc "village/savanna/streets/split_02"
      // 21e: aload 7
      // 220: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 223: bipush 2
      // 224: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 227: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 22a: aastore
      // 22b: dup
      // 22c: bipush 6
      // 22e: ldc "village/savanna/streets/turn_01"
      // 230: aload 7
      // 232: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 235: bipush 3
      // 236: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 239: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 23c: aastore
      // 23d: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 240: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.TERRAIN_MATCHING Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 243: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 246: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 249: aload 0
      // 24a: ldc "village/savanna/zombie/streets"
      // 24c: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 24f: dup
      // 250: aload 12
      // 252: ldc "village/savanna/zombie/streets/corner_01"
      // 254: aload 7
      // 256: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 259: bipush 2
      // 25a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 25d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 260: ldc "village/savanna/zombie/streets/corner_03"
      // 262: aload 7
      // 264: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 267: bipush 2
      // 268: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 26b: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 26e: ldc "village/savanna/zombie/streets/straight_02"
      // 270: aload 7
      // 272: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 275: bipush 4
      // 276: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 279: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 27c: ldc "village/savanna/zombie/streets/straight_04"
      // 27e: aload 7
      // 280: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 283: bipush 7
      // 285: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 288: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 28b: ldc "village/savanna/zombie/streets/straight_05"
      // 28d: aload 7
      // 28f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 292: bipush 3
      // 293: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 296: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 299: ldc "village/savanna/zombie/streets/straight_06"
      // 29b: aload 7
      // 29d: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2a0: bipush 4
      // 2a1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2a4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2a7: ldc "village/savanna/zombie/streets/straight_08"
      // 2a9: aload 7
      // 2ab: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2ae: bipush 4
      // 2af: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2b2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2b5: ldc "village/savanna/zombie/streets/straight_09"
      // 2b7: aload 7
      // 2b9: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2bc: bipush 4
      // 2bd: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2c0: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2c3: ldc "village/savanna/zombie/streets/straight_10"
      // 2c5: aload 7
      // 2c7: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2ca: bipush 4
      // 2cb: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2ce: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2d1: ldc "village/savanna/zombie/streets/straight_11"
      // 2d3: aload 7
      // 2d5: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2d8: bipush 4
      // 2d9: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2dc: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2df: ldc "village/savanna/zombie/streets/crossroad_02"
      // 2e1: aload 7
      // 2e3: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2e6: bipush 1
      // 2e7: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2ea: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2ed: ldc "village/savanna/zombie/streets/crossroad_03"
      // 2ef: aload 7
      // 2f1: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 2f4: bipush 2
      // 2f5: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 2f8: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 2fb: bipush 7
      // 2fd: anewarray 101
      // 300: dup
      // 301: bipush 0
      // 302: ldc "village/savanna/zombie/streets/crossroad_04"
      // 304: aload 7
      // 306: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 309: bipush 2
      // 30a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 30d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 310: aastore
      // 311: dup
      // 312: bipush 1
      // 313: ldc "village/savanna/zombie/streets/crossroad_05"
      // 315: aload 7
      // 317: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 31a: bipush 2
      // 31b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 31e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 321: aastore
      // 322: dup
      // 323: bipush 2
      // 324: ldc "village/savanna/zombie/streets/crossroad_06"
      // 326: aload 7
      // 328: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 32b: bipush 2
      // 32c: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 32f: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 332: aastore
      // 333: dup
      // 334: bipush 3
      // 335: ldc "village/savanna/zombie/streets/crossroad_07"
      // 337: aload 7
      // 339: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 33c: bipush 2
      // 33d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 340: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 343: aastore
      // 344: dup
      // 345: bipush 4
      // 346: ldc "village/savanna/zombie/streets/split_01"
      // 348: aload 7
      // 34a: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 34d: bipush 2
      // 34e: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 351: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 354: aastore
      // 355: dup
      // 356: bipush 5
      // 357: ldc "village/savanna/zombie/streets/split_02"
      // 359: aload 7
      // 35b: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 35e: bipush 2
      // 35f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 362: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 365: aastore
      // 366: dup
      // 367: bipush 6
      // 369: ldc "village/savanna/zombie/streets/turn_01"
      // 36b: aload 7
      // 36d: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 370: bipush 3
      // 371: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 374: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 377: aastore
      // 378: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 37b: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.TERRAIN_MATCHING Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 37e: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 381: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 384: aload 0
      // 385: ldc "village/savanna/houses"
      // 387: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 38a: dup
      // 38b: aload 11
      // 38d: ldc "village/savanna/houses/savanna_small_house_1"
      // 38f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 392: bipush 2
      // 393: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 396: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 399: ldc "village/savanna/houses/savanna_small_house_2"
      // 39b: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 39e: bipush 2
      // 39f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3a2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3a5: ldc "village/savanna/houses/savanna_small_house_3"
      // 3a7: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3aa: bipush 2
      // 3ab: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3ae: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3b1: ldc "village/savanna/houses/savanna_small_house_4"
      // 3b3: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3b6: bipush 2
      // 3b7: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3ba: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3bd: ldc "village/savanna/houses/savanna_small_house_5"
      // 3bf: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3c2: bipush 2
      // 3c3: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3c6: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3c9: ldc "village/savanna/houses/savanna_small_house_6"
      // 3cb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3ce: bipush 2
      // 3cf: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3d2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3d5: ldc "village/savanna/houses/savanna_small_house_7"
      // 3d7: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3da: bipush 2
      // 3db: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3de: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3e1: ldc "village/savanna/houses/savanna_small_house_8"
      // 3e3: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3e6: bipush 2
      // 3e7: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3ea: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3ed: ldc "village/savanna/houses/savanna_medium_house_1"
      // 3ef: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3f2: bipush 2
      // 3f3: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 3f6: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 3f9: ldc "village/savanna/houses/savanna_medium_house_2"
      // 3fb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 3fe: bipush 2
      // 3ff: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 402: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 405: ldc "village/savanna/houses/savanna_butchers_shop_1"
      // 407: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 40a: bipush 2
      // 40b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 40e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 411: ldc "village/savanna/houses/savanna_butchers_shop_2"
      // 413: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 416: bipush 2
      // 417: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 41a: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 41d: bipush 20
      // 41f: anewarray 101
      // 422: dup
      // 423: bipush 0
      // 424: ldc "village/savanna/houses/savanna_tool_smith_1"
      // 426: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 429: bipush 2
      // 42a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 42d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 430: aastore
      // 431: dup
      // 432: bipush 1
      // 433: ldc_w "village/savanna/houses/savanna_fletcher_house_1"
      // 436: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 439: bipush 2
      // 43a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 43d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 440: aastore
      // 441: dup
      // 442: bipush 2
      // 443: ldc_w "village/savanna/houses/savanna_shepherd_1"
      // 446: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 449: bipush 7
      // 44b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 44e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 451: aastore
      // 452: dup
      // 453: bipush 3
      // 454: ldc_w "village/savanna/houses/savanna_armorer_1"
      // 457: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 45a: bipush 1
      // 45b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 45e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 461: aastore
      // 462: dup
      // 463: bipush 4
      // 464: ldc_w "village/savanna/houses/savanna_fisher_cottage_1"
      // 467: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 46a: bipush 3
      // 46b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 46e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 471: aastore
      // 472: dup
      // 473: bipush 5
      // 474: ldc_w "village/savanna/houses/savanna_tannery_1"
      // 477: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 47a: bipush 2
      // 47b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 47e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 481: aastore
      // 482: dup
      // 483: bipush 6
      // 485: ldc_w "village/savanna/houses/savanna_cartographer_1"
      // 488: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 48b: bipush 2
      // 48c: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 48f: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 492: aastore
      // 493: dup
      // 494: bipush 7
      // 496: ldc_w "village/savanna/houses/savanna_library_1"
      // 499: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 49c: bipush 2
      // 49d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4a0: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4a3: aastore
      // 4a4: dup
      // 4a5: bipush 8
      // 4a7: ldc_w "village/savanna/houses/savanna_mason_1"
      // 4aa: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 4ad: bipush 2
      // 4ae: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4b1: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4b4: aastore
      // 4b5: dup
      // 4b6: bipush 9
      // 4b8: ldc_w "village/savanna/houses/savanna_weaponsmith_1"
      // 4bb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 4be: bipush 2
      // 4bf: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4c2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4c5: aastore
      // 4c6: dup
      // 4c7: bipush 10
      // 4c9: ldc_w "village/savanna/houses/savanna_weaponsmith_2"
      // 4cc: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 4cf: bipush 2
      // 4d0: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4d3: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4d6: aastore
      // 4d7: dup
      // 4d8: bipush 11
      // 4da: ldc_w "village/savanna/houses/savanna_temple_1"
      // 4dd: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 4e0: bipush 2
      // 4e1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4e4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4e7: aastore
      // 4e8: dup
      // 4e9: bipush 12
      // 4eb: ldc_w "village/savanna/houses/savanna_temple_2"
      // 4ee: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 4f1: bipush 3
      // 4f2: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 4f5: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 4f8: aastore
      // 4f9: dup
      // 4fa: bipush 13
      // 4fc: ldc_w "village/savanna/houses/savanna_large_farm_1"
      // 4ff: aload 8
      // 501: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 504: bipush 4
      // 505: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 508: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 50b: aastore
      // 50c: dup
      // 50d: bipush 14
      // 50f: ldc_w "village/savanna/houses/savanna_large_farm_2"
      // 512: aload 8
      // 514: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 517: bipush 6
      // 519: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 51c: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 51f: aastore
      // 520: dup
      // 521: bipush 15
      // 523: ldc_w "village/savanna/houses/savanna_small_farm"
      // 526: aload 8
      // 528: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 52b: bipush 4
      // 52c: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 52f: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 532: aastore
      // 533: dup
      // 534: bipush 16
      // 536: ldc_w "village/savanna/houses/savanna_animal_pen_1"
      // 539: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 53c: bipush 2
      // 53d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 540: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 543: aastore
      // 544: dup
      // 545: bipush 17
      // 547: ldc_w "village/savanna/houses/savanna_animal_pen_2"
      // 54a: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 54d: bipush 2
      // 54e: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 551: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 554: aastore
      // 555: dup
      // 556: bipush 18
      // 558: ldc_w "village/savanna/houses/savanna_animal_pen_3"
      // 55b: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 55e: bipush 2
      // 55f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 562: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 565: aastore
      // 566: dup
      // 567: bipush 19
      // 569: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.empty ()Ljava/util/function/Function;
      // 56c: bipush 5
      // 56d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 570: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 573: aastore
      // 574: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 577: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 57a: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 57d: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 580: aload 0
      // 581: ldc_w "village/savanna/zombie/houses"
      // 584: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 587: dup
      // 588: aload 12
      // 58a: ldc_w "village/savanna/zombie/houses/savanna_small_house_1"
      // 58d: aload 6
      // 58f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 592: bipush 2
      // 593: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 596: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 599: ldc_w "village/savanna/zombie/houses/savanna_small_house_2"
      // 59c: aload 6
      // 59e: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5a1: bipush 2
      // 5a2: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5a5: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5a8: ldc_w "village/savanna/zombie/houses/savanna_small_house_3"
      // 5ab: aload 6
      // 5ad: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5b0: bipush 2
      // 5b1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5b4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5b7: ldc_w "village/savanna/zombie/houses/savanna_small_house_4"
      // 5ba: aload 6
      // 5bc: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5bf: bipush 2
      // 5c0: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5c3: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5c6: ldc_w "village/savanna/zombie/houses/savanna_small_house_5"
      // 5c9: aload 6
      // 5cb: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5ce: bipush 2
      // 5cf: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5d2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5d5: ldc_w "village/savanna/zombie/houses/savanna_small_house_6"
      // 5d8: aload 6
      // 5da: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5dd: bipush 2
      // 5de: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5e1: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5e4: ldc_w "village/savanna/zombie/houses/savanna_small_house_7"
      // 5e7: aload 6
      // 5e9: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5ec: bipush 2
      // 5ed: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5f0: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 5f3: ldc_w "village/savanna/zombie/houses/savanna_small_house_8"
      // 5f6: aload 6
      // 5f8: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 5fb: bipush 2
      // 5fc: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 5ff: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 602: ldc_w "village/savanna/zombie/houses/savanna_medium_house_1"
      // 605: aload 6
      // 607: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 60a: bipush 2
      // 60b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 60e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 611: ldc_w "village/savanna/zombie/houses/savanna_medium_house_2"
      // 614: aload 6
      // 616: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 619: bipush 2
      // 61a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 61d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 620: ldc "village/savanna/houses/savanna_butchers_shop_1"
      // 622: aload 6
      // 624: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 627: bipush 2
      // 628: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 62b: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 62e: ldc "village/savanna/houses/savanna_butchers_shop_2"
      // 630: aload 6
      // 632: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 635: bipush 2
      // 636: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 639: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 63c: bipush 20
      // 63e: anewarray 101
      // 641: dup
      // 642: bipush 0
      // 643: ldc "village/savanna/houses/savanna_tool_smith_1"
      // 645: aload 6
      // 647: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 64a: bipush 2
      // 64b: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 64e: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 651: aastore
      // 652: dup
      // 653: bipush 1
      // 654: ldc_w "village/savanna/houses/savanna_fletcher_house_1"
      // 657: aload 6
      // 659: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 65c: bipush 2
      // 65d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 660: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 663: aastore
      // 664: dup
      // 665: bipush 2
      // 666: ldc_w "village/savanna/houses/savanna_shepherd_1"
      // 669: aload 6
      // 66b: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 66e: bipush 2
      // 66f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 672: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 675: aastore
      // 676: dup
      // 677: bipush 3
      // 678: ldc_w "village/savanna/houses/savanna_armorer_1"
      // 67b: aload 6
      // 67d: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 680: bipush 1
      // 681: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 684: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 687: aastore
      // 688: dup
      // 689: bipush 4
      // 68a: ldc_w "village/savanna/houses/savanna_fisher_cottage_1"
      // 68d: aload 6
      // 68f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 692: bipush 2
      // 693: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 696: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 699: aastore
      // 69a: dup
      // 69b: bipush 5
      // 69c: ldc_w "village/savanna/houses/savanna_tannery_1"
      // 69f: aload 6
      // 6a1: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 6a4: bipush 2
      // 6a5: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 6a8: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 6ab: aastore
      // 6ac: dup
      // 6ad: bipush 6
      // 6af: ldc_w "village/savanna/houses/savanna_cartographer_1"
      // 6b2: aload 6
      // 6b4: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 6b7: bipush 2
      // 6b8: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 6bb: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 6be: aastore
      // 6bf: dup
      // 6c0: bipush 7
      // 6c2: ldc_w "village/savanna/houses/savanna_library_1"
      // 6c5: aload 6
      // 6c7: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 6ca: bipush 2
      // 6cb: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 6ce: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 6d1: aastore
      // 6d2: dup
      // 6d3: bipush 8
      // 6d5: ldc_w "village/savanna/houses/savanna_mason_1"
      // 6d8: aload 6
      // 6da: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 6dd: bipush 2
      // 6de: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 6e1: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 6e4: aastore
      // 6e5: dup
      // 6e6: bipush 9
      // 6e8: ldc_w "village/savanna/houses/savanna_weaponsmith_1"
      // 6eb: aload 6
      // 6ed: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 6f0: bipush 2
      // 6f1: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 6f4: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 6f7: aastore
      // 6f8: dup
      // 6f9: bipush 10
      // 6fb: ldc_w "village/savanna/houses/savanna_weaponsmith_2"
      // 6fe: aload 6
      // 700: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 703: bipush 2
      // 704: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 707: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 70a: aastore
      // 70b: dup
      // 70c: bipush 11
      // 70e: ldc_w "village/savanna/houses/savanna_temple_1"
      // 711: aload 6
      // 713: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 716: bipush 1
      // 717: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 71a: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 71d: aastore
      // 71e: dup
      // 71f: bipush 12
      // 721: ldc_w "village/savanna/houses/savanna_temple_2"
      // 724: aload 6
      // 726: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 729: bipush 3
      // 72a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 72d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 730: aastore
      // 731: dup
      // 732: bipush 13
      // 734: ldc_w "village/savanna/houses/savanna_large_farm_1"
      // 737: aload 6
      // 739: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 73c: bipush 4
      // 73d: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 740: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 743: aastore
      // 744: dup
      // 745: bipush 14
      // 747: ldc_w "village/savanna/zombie/houses/savanna_large_farm_2"
      // 74a: aload 6
      // 74c: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 74f: bipush 4
      // 750: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 753: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 756: aastore
      // 757: dup
      // 758: bipush 15
      // 75a: ldc_w "village/savanna/houses/savanna_small_farm"
      // 75d: aload 6
      // 75f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 762: bipush 4
      // 763: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 766: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 769: aastore
      // 76a: dup
      // 76b: bipush 16
      // 76d: ldc_w "village/savanna/houses/savanna_animal_pen_1"
      // 770: aload 6
      // 772: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 775: bipush 2
      // 776: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 779: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 77c: aastore
      // 77d: dup
      // 77e: bipush 17
      // 780: ldc_w "village/savanna/zombie/houses/savanna_animal_pen_2"
      // 783: aload 6
      // 785: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 788: bipush 2
      // 789: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 78c: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 78f: aastore
      // 790: dup
      // 791: bipush 18
      // 793: ldc_w "village/savanna/zombie/houses/savanna_animal_pen_3"
      // 796: aload 6
      // 798: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 79b: bipush 2
      // 79c: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 79f: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 7a2: aastore
      // 7a3: dup
      // 7a4: bipush 19
      // 7a6: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.empty ()Ljava/util/function/Function;
      // 7a9: bipush 5
      // 7aa: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 7ad: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 7b0: aastore
      // 7b1: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 7b4: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 7b7: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 7ba: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 7bd: aload 0
      // 7be: getstatic net/minecraft/data/worldgen/SavannaVillagePools.TERMINATORS_KEY Lnet/minecraft/resources/ResourceKey;
      // 7c1: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 7c4: dup
      // 7c5: aload 10
      // 7c7: ldc_w "village/plains/terminators/terminator_01"
      // 7ca: aload 7
      // 7cc: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 7cf: bipush 1
      // 7d0: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 7d3: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 7d6: ldc_w "village/plains/terminators/terminator_02"
      // 7d9: aload 7
      // 7db: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 7de: bipush 1
      // 7df: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 7e2: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 7e5: ldc_w "village/plains/terminators/terminator_03"
      // 7e8: aload 7
      // 7ea: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 7ed: bipush 1
      // 7ee: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 7f1: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 7f4: ldc_w "village/plains/terminators/terminator_04"
      // 7f7: aload 7
      // 7f9: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 7fc: bipush 1
      // 7fd: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 800: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 803: ldc_w "village/savanna/terminators/terminator_05"
      // 806: aload 7
      // 808: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 80b: bipush 1
      // 80c: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 80f: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 812: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 815: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.TERRAIN_MATCHING Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 818: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 81b: invokeinterface net/minecraft/data/worldgen/BootstrapContext.register (Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference; 3
      // 820: pop
      // 821: aload 0
      // 822: getstatic net/minecraft/data/worldgen/SavannaVillagePools.ZOMBIE_TERMINATORS_KEY Lnet/minecraft/resources/ResourceKey;
      // 825: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 828: dup
      // 829: aload 10
      // 82b: ldc_w "village/plains/terminators/terminator_01"
      // 82e: aload 7
      // 830: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 833: bipush 1
      // 834: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 837: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 83a: ldc_w "village/plains/terminators/terminator_02"
      // 83d: aload 7
      // 83f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 842: bipush 1
      // 843: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 846: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 849: ldc_w "village/plains/terminators/terminator_03"
      // 84c: aload 7
      // 84e: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 851: bipush 1
      // 852: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 855: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 858: ldc_w "village/plains/terminators/terminator_04"
      // 85b: aload 7
      // 85d: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 860: bipush 1
      // 861: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 864: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 867: ldc_w "village/savanna/zombie/terminators/terminator_05"
      // 86a: aload 7
      // 86c: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 86f: bipush 1
      // 870: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 873: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 876: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 879: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.TERRAIN_MATCHING Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 87c: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 87f: invokeinterface net/minecraft/data/worldgen/BootstrapContext.register (Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference; 3
      // 884: pop
      // 885: aload 0
      // 886: ldc_w "village/savanna/trees"
      // 889: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 88c: dup
      // 88d: aload 10
      // 88f: aload 2
      // 890: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 893: bipush 1
      // 894: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 897: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 89a: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 89d: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 8a0: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 8a3: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 8a6: aload 0
      // 8a7: ldc_w "village/savanna/decor"
      // 8aa: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 8ad: dup
      // 8ae: aload 10
      // 8b0: ldc_w "village/savanna/savanna_lamp_post_01"
      // 8b3: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 8b6: bipush 4
      // 8b7: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 8ba: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 8bd: aload 2
      // 8be: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 8c1: bipush 4
      // 8c2: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 8c5: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 8c8: aload 3
      // 8c9: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 8cc: bipush 4
      // 8cd: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 8d0: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 8d3: aload 4
      // 8d5: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 8d8: bipush 1
      // 8d9: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 8dc: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 8df: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.empty ()Ljava/util/function/Function;
      // 8e2: bipush 4
      // 8e3: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 8e6: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 8e9: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 8ec: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 8ef: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 8f2: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 8f5: aload 0
      // 8f6: ldc_w "village/savanna/zombie/decor"
      // 8f9: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 8fc: dup
      // 8fd: aload 10
      // 8ff: ldc_w "village/savanna/savanna_lamp_post_01"
      // 902: aload 6
      // 904: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 907: bipush 4
      // 908: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 90b: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 90e: aload 2
      // 90f: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 912: bipush 4
      // 913: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 916: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 919: aload 3
      // 91a: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 91d: bipush 4
      // 91e: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 921: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 924: aload 4
      // 926: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.feature (Lnet/minecraft/core/Holder;)Ljava/util/function/Function;
      // 929: bipush 1
      // 92a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 92d: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 930: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.empty ()Ljava/util/function/Function;
      // 933: bipush 4
      // 934: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 937: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 93a: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 93d: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 940: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 943: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 946: aload 0
      // 947: ldc_w "village/savanna/villagers"
      // 94a: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 94d: dup
      // 94e: aload 10
      // 950: ldc_w "village/savanna/villagers/nitwit"
      // 953: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 956: bipush 1
      // 957: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 95a: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 95d: ldc_w "village/savanna/villagers/baby"
      // 960: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 963: bipush 1
      // 964: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 967: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 96a: ldc_w "village/savanna/villagers/unemployed"
      // 96d: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 970: bipush 10
      // 972: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 975: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 978: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 97b: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 97e: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 981: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 984: aload 0
      // 985: ldc_w "village/savanna/zombie/villagers"
      // 988: new net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool
      // 98b: dup
      // 98c: aload 10
      // 98e: ldc_w "village/savanna/zombie/villagers/nitwit"
      // 991: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 994: bipush 1
      // 995: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 998: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 99b: ldc_w "village/savanna/zombie/villagers/unemployed"
      // 99e: invokestatic net/minecraft/world/level/levelgen/structure/pools/StructurePoolElement.legacy (Ljava/lang/String;)Ljava/util/function/Function;
      // 9a1: bipush 10
      // 9a3: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
      // 9a6: invokestatic com/mojang/datafixers/util/Pair.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;
      // 9a9: invokestatic com/google/common/collect/ImmutableList.of (Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;
      // 9ac: getstatic net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection.RIGID Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;
      // 9af: invokespecial net/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool.<init> (Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;)V
      // 9b2: invokestatic net/minecraft/data/worldgen/Pools.register (Lnet/minecraft/data/worldgen/BootstrapContext;Ljava/lang/String;Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)V
      // 9b5: return
   }
}
