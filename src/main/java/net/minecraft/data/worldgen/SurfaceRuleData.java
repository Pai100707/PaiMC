package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;

public class SurfaceRuleData {
   private static final RuleSource AIR = makeStateRule(Blocks.AIR);
   private static final RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);
   private static final RuleSource WHITE_TERRACOTTA = makeStateRule(Blocks.WHITE_TERRACOTTA);
   private static final RuleSource ORANGE_TERRACOTTA = makeStateRule(Blocks.ORANGE_TERRACOTTA);
   private static final RuleSource TERRACOTTA = makeStateRule(Blocks.TERRACOTTA);
   private static final RuleSource RED_SAND = makeStateRule(Blocks.RED_SAND);
   private static final RuleSource RED_SANDSTONE = makeStateRule(Blocks.RED_SANDSTONE);
   private static final RuleSource STONE = makeStateRule(Blocks.STONE);
   private static final RuleSource DEEPSLATE = makeStateRule(Blocks.DEEPSLATE);
   private static final RuleSource DIRT = makeStateRule(Blocks.DIRT);
   private static final RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
   private static final RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
   private static final RuleSource MYCELIUM = makeStateRule(Blocks.MYCELIUM);
   private static final RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
   private static final RuleSource CALCITE = makeStateRule(Blocks.CALCITE);
   private static final RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
   private static final RuleSource SAND = makeStateRule(Blocks.SAND);
   private static final RuleSource SANDSTONE = makeStateRule(Blocks.SANDSTONE);
   private static final RuleSource PACKED_ICE = makeStateRule(Blocks.PACKED_ICE);
   private static final RuleSource SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK);
   private static final RuleSource MUD = makeStateRule(Blocks.MUD);
   private static final RuleSource POWDER_SNOW = makeStateRule(Blocks.POWDER_SNOW);
   private static final RuleSource ICE = makeStateRule(Blocks.ICE);
   private static final RuleSource WATER = makeStateRule(Blocks.WATER);
   private static final RuleSource LAVA = makeStateRule(Blocks.LAVA);
   private static final RuleSource NETHERRACK = makeStateRule(Blocks.NETHERRACK);
   private static final RuleSource SOUL_SAND = makeStateRule(Blocks.SOUL_SAND);
   private static final RuleSource SOUL_SOIL = makeStateRule(Blocks.SOUL_SOIL);
   private static final RuleSource BASALT = makeStateRule(Blocks.BASALT);
   private static final RuleSource BLACKSTONE = makeStateRule(Blocks.BLACKSTONE);
   private static final RuleSource WARPED_WART_BLOCK = makeStateRule(Blocks.WARPED_WART_BLOCK);
   private static final RuleSource WARPED_NYLIUM = makeStateRule(Blocks.WARPED_NYLIUM);
   private static final RuleSource NETHER_WART_BLOCK = makeStateRule(Blocks.NETHER_WART_BLOCK);
   private static final RuleSource CRIMSON_NYLIUM = makeStateRule(Blocks.CRIMSON_NYLIUM);
   private static final RuleSource ENDSTONE = makeStateRule(Blocks.END_STONE);

   private static RuleSource makeStateRule(Block $$0) {
      return SurfaceRules.state($$0.defaultBlockState());
   }

   public static RuleSource overworld() {
      return overworldLike(true, false, true);
   }

   public static RuleSource overworldLike(boolean $$0, boolean $$1, boolean $$2) {
      ConditionSource $$3 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(97), 2);
      ConditionSource $$4 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
      ConditionSource $$5 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(63), -1);
      ConditionSource $$6 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1);
      ConditionSource $$7 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0);
      ConditionSource $$8 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
      ConditionSource $$9 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0);
      ConditionSource $$10 = SurfaceRules.waterBlockCheck(-1, 0);
      ConditionSource $$11 = SurfaceRules.waterBlockCheck(0, 0);
      ConditionSource $$12 = SurfaceRules.waterStartCheck(-6, -1);
      ConditionSource $$13 = SurfaceRules.hole();
      ConditionSource $$14 = SurfaceRules.isBiome(new ResourceKey[]{Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN});
      ConditionSource $$15 = SurfaceRules.steep();
      RuleSource $$16 = SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$11, GRASS_BLOCK), DIRT});
      RuleSource $$17 = SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SANDSTONE), SAND});
      RuleSource $$18 = SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL});
      ConditionSource $$19 = SurfaceRules.isBiome(new ResourceKey[]{Biomes.WARM_OCEAN, Biomes.BEACH, Biomes.SNOWY_BEACH});
      ConditionSource $$20 = SurfaceRules.isBiome(new ResourceKey[]{Biomes.DESERT});
      RuleSource $$21 = SurfaceRules.sequence(
         new RuleSource[]{
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.STONY_PEAKS}),
               SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.CALCITE, -0.0125, 0.0125), CALCITE), STONE})
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.STONY_SHORE}),
               SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05, 0.05), $$18), STONE})
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.WINDSWEPT_HILLS}), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE)),
            SurfaceRules.ifTrue($$19, $$17),
            SurfaceRules.ifTrue($$20, $$17),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.DRIPSTONE_CAVES}), STONE)
         }
      );
      RuleSource $$22 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.45, 0.58), SurfaceRules.ifTrue($$11, POWDER_SNOW));
      RuleSource $$23 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.35, 0.6), SurfaceRules.ifTrue($$11, POWDER_SNOW));
      RuleSource $$24 = SurfaceRules.sequence(
         new RuleSource[]{
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.FROZEN_PEAKS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue($$15, PACKED_ICE),
                     SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, -0.5, 0.2), PACKED_ICE),
                     SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, -0.0625, 0.025), ICE),
                     SurfaceRules.ifTrue($$11, SNOW_BLOCK)
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.SNOWY_SLOPES}),
               SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$15, STONE), $$22, SurfaceRules.ifTrue($$11, SNOW_BLOCK)})
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.JAGGED_PEAKS}), STONE),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.GROVE}), SurfaceRules.sequence(new RuleSource[]{$$22, DIRT})),
            $$21,
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.WINDSWEPT_SAVANNA}), SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), STONE)),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.WINDSWEPT_GRAVELLY_HILLS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), $$18),
                     SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE),
                     SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0), DIRT),
                     $$18
                  }
               )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.MANGROVE_SWAMP}), MUD),
            DIRT
         }
      );
      RuleSource $$25 = SurfaceRules.sequence(
         new RuleSource[]{
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.FROZEN_PEAKS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue($$15, PACKED_ICE),
                     SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, 0.0, 0.2), PACKED_ICE),
                     SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, 0.0, 0.025), ICE),
                     SurfaceRules.ifTrue($$11, SNOW_BLOCK)
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.SNOWY_SLOPES}),
               SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$15, STONE), $$23, SurfaceRules.ifTrue($$11, SNOW_BLOCK)})
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.JAGGED_PEAKS}),
               SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$15, STONE), SurfaceRules.ifTrue($$11, SNOW_BLOCK)})
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.GROVE}), SurfaceRules.sequence(new RuleSource[]{$$23, SurfaceRules.ifTrue($$11, SNOW_BLOCK)})
            ),
            $$21,
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.WINDSWEPT_SAVANNA}),
               SurfaceRules.sequence(
                  new RuleSource[]{SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), STONE), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.5), COARSE_DIRT)}
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.WINDSWEPT_GRAVELLY_HILLS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), $$18),
                     SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE),
                     SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0), $$16),
                     $$18
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA}),
               SurfaceRules.sequence(
                  new RuleSource[]{SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), COARSE_DIRT), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95), PODZOL)}
               )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.ICE_SPIKES}), SurfaceRules.ifTrue($$11, SNOW_BLOCK)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.MANGROVE_SWAMP}), MUD),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.MUSHROOM_FIELDS}), MYCELIUM),
            $$16
         }
      );
      ConditionSource $$26 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909, -0.5454);
      ConditionSource $$27 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818, 0.1818);
      ConditionSource $$28 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454, 0.909);
      RuleSource $$29 = SurfaceRules.sequence(
         new RuleSource[]{
            SurfaceRules.ifTrue(
               SurfaceRules.ON_FLOOR,
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(new ResourceKey[]{Biomes.WOODED_BADLANDS}),
                        SurfaceRules.ifTrue(
                           $$3,
                           SurfaceRules.sequence(
                              new RuleSource[]{
                                 SurfaceRules.ifTrue($$26, COARSE_DIRT), SurfaceRules.ifTrue($$27, COARSE_DIRT), SurfaceRules.ifTrue($$28, COARSE_DIRT), $$16
                              }
                           )
                        )
                     ),
                     SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(new ResourceKey[]{Biomes.SWAMP}),
                        SurfaceRules.ifTrue(
                           $$8, SurfaceRules.ifTrue(SurfaceRules.not($$9), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))
                        )
                     ),
                     SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(new ResourceKey[]{Biomes.MANGROVE_SWAMP}),
                        SurfaceRules.ifTrue(
                           $$7, SurfaceRules.ifTrue(SurfaceRules.not($$9), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))
                        )
                     )
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.sequence(
                           new RuleSource[]{
                              SurfaceRules.ifTrue($$4, ORANGE_TERRACOTTA),
                              SurfaceRules.ifTrue(
                                 $$6,
                                 SurfaceRules.sequence(
                                    new RuleSource[]{
                                       SurfaceRules.ifTrue($$26, TERRACOTTA),
                                       SurfaceRules.ifTrue($$27, TERRACOTTA),
                                       SurfaceRules.ifTrue($$28, TERRACOTTA),
                                       SurfaceRules.bandlands()
                                    }
                                 )
                              ),
                              SurfaceRules.ifTrue(
                                 $$10, SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, RED_SANDSTONE), RED_SAND})
                              ),
                              SurfaceRules.ifTrue(SurfaceRules.not($$13), ORANGE_TERRACOTTA),
                              SurfaceRules.ifTrue($$12, WHITE_TERRACOTTA),
                              $$18
                           }
                        )
                     ),
                     SurfaceRules.ifTrue(
                        $$5,
                        SurfaceRules.sequence(
                           new RuleSource[]{SurfaceRules.ifTrue($$9, SurfaceRules.ifTrue(SurfaceRules.not($$6), ORANGE_TERRACOTTA)), SurfaceRules.bandlands()}
                        )
                     ),
                     SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.ifTrue($$12, WHITE_TERRACOTTA))
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.ON_FLOOR,
               SurfaceRules.ifTrue(
                  $$10,
                  SurfaceRules.sequence(
                     new RuleSource[]{
                        SurfaceRules.ifTrue(
                           $$14,
                           SurfaceRules.ifTrue(
                              $$13,
                              SurfaceRules.sequence(
                                 new RuleSource[]{SurfaceRules.ifTrue($$11, AIR), SurfaceRules.ifTrue(SurfaceRules.temperature(), ICE), WATER}
                              )
                           )
                        ),
                        $$25
                     }
                  )
               )
            ),
            SurfaceRules.ifTrue(
               $$12,
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue($$14, SurfaceRules.ifTrue($$13, WATER))),
                     SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, $$24),
                     SurfaceRules.ifTrue($$19, SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SANDSTONE)),
                     SurfaceRules.ifTrue($$20, SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, SANDSTONE))
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.ON_FLOOR,
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS}), STONE),
                     SurfaceRules.ifTrue(SurfaceRules.isBiome(new ResourceKey[]{Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN}), $$17),
                     $$18
                  }
               )
            )
         }
      );
      Builder<RuleSource> $$30 = ImmutableList.builder();
      if ($$1) {
         $$30.add(
            SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK)
         );
      }

      if ($$2) {
         $$30.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK));
      }

      RuleSource $$31 = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), $$29);
      $$30.add($$0 ? $$31 : $$29);
      $$30.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), DEEPSLATE));
      return SurfaceRules.sequence((RuleSource[])$$30.build().toArray(RuleSource[]::new));
   }

   public static RuleSource nether() {
      ConditionSource $$0 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
      ConditionSource $$1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(32), 0);
      ConditionSource $$2 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(30), 0);
      ConditionSource $$3 = SurfaceRules.not(SurfaceRules.yStartCheck(VerticalAnchor.absolute(35), 0));
      ConditionSource $$4 = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);
      ConditionSource $$5 = SurfaceRules.hole();
      ConditionSource $$6 = SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012);
      ConditionSource $$7 = SurfaceRules.noiseCondition(Noises.GRAVEL_LAYER, -0.012);
      ConditionSource $$8 = SurfaceRules.noiseCondition(Noises.PATCH, -0.012);
      ConditionSource $$9 = SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.54);
      ConditionSource $$10 = SurfaceRules.noiseCondition(Noises.NETHER_WART, 1.17);
      ConditionSource $$11 = SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.0);
      RuleSource $$12 = SurfaceRules.ifTrue($$8, SurfaceRules.ifTrue($$2, SurfaceRules.ifTrue($$3, GRAVEL)));
      return SurfaceRules.sequence(
         new RuleSource[]{
            SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK),
            SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK),
            SurfaceRules.ifTrue($$4, NETHERRACK),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.BASALT_DELTAS}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, BASALT),
                     SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(new RuleSource[]{$$12, SurfaceRules.ifTrue($$11, BASALT), BLACKSTONE}))
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.SOUL_SAND_VALLEY}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$11, SOUL_SAND), SOUL_SOIL})),
                     SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(new RuleSource[]{$$12, SurfaceRules.ifTrue($$11, SOUL_SAND), SOUL_SOIL})
                     )
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.ON_FLOOR,
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(SurfaceRules.not($$1), SurfaceRules.ifTrue($$5, LAVA)),
                     SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(new ResourceKey[]{Biomes.WARPED_FOREST}),
                        SurfaceRules.ifTrue(
                           SurfaceRules.not($$9),
                           SurfaceRules.ifTrue($$0, SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$10, WARPED_WART_BLOCK), WARPED_NYLIUM}))
                        )
                     ),
                     SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(new ResourceKey[]{Biomes.CRIMSON_FOREST}),
                        SurfaceRules.ifTrue(
                           SurfaceRules.not($$9),
                           SurfaceRules.ifTrue($$0, SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$10, NETHER_WART_BLOCK), CRIMSON_NYLIUM}))
                        )
                     )
                  }
               )
            ),
            SurfaceRules.ifTrue(
               SurfaceRules.isBiome(new ResourceKey[]{Biomes.NETHER_WASTES}),
               SurfaceRules.sequence(
                  new RuleSource[]{
                     SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.ifTrue(
                           $$6,
                           SurfaceRules.sequence(
                              new RuleSource[]{
                                 SurfaceRules.ifTrue(SurfaceRules.not($$5), SurfaceRules.ifTrue($$2, SurfaceRules.ifTrue($$3, SOUL_SAND))), NETHERRACK
                              }
                           )
                        )
                     ),
                     SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                           $$0,
                           SurfaceRules.ifTrue(
                              $$3,
                              SurfaceRules.ifTrue(
                                 $$7,
                                 SurfaceRules.sequence(new RuleSource[]{SurfaceRules.ifTrue($$1, GRAVEL), SurfaceRules.ifTrue(SurfaceRules.not($$5), GRAVEL)})
                              )
                           )
                        )
                     )
                  }
               )
            ),
            NETHERRACK
         }
      );
   }

   public static RuleSource end() {
      return ENDSTONE;
   }

   public static RuleSource air() {
      return AIR;
   }

   private static ConditionSource surfaceNoiseAbove(double $$0) {
      return SurfaceRules.noiseCondition(Noises.SURFACE, $$0 / 8.25, Double.MAX_VALUE);
   }
}
