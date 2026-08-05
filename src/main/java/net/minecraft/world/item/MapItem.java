package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData.HoldingPlayer;

public class MapItem extends net.minecraft.world.item.Item {
   public static final int IMAGE_WIDTH = 128;
   public static final int IMAGE_HEIGHT = 128;

   public MapItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static net.minecraft.world.item.ItemStack create(ServerLevel $$0, int $$1, int $$2, byte $$3, boolean $$4, boolean $$5) {
      net.minecraft.world.item.ItemStack $$6 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FILLED_MAP);
      MapId $$7 = createNewSavedData($$0, $$1, $$2, $$3, $$4, $$5, $$0.dimension());
      $$6.set(DataComponents.MAP_ID, $$7);
      return $$6;
   }

   
   public static MapItemSavedData getSavedData(MapId $$0, Level $$1) {
      return $$0 == null ? null : $$1.getMapData($$0);
   }

   
   public static MapItemSavedData getSavedData(net.minecraft.world.item.ItemStack $$0, Level $$1) {
      MapId $$2 = (MapId)$$0.get(DataComponents.MAP_ID);
      return getSavedData($$2, $$1);
   }

   private static MapId createNewSavedData(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4, boolean $$5, ResourceKey<Level> $$6) {
      MapItemSavedData $$7 = MapItemSavedData.createFresh($$1, $$2, (byte)$$3, $$4, $$5, $$6);
      MapId $$8 = $$0.getFreeMapId();
      $$0.setMapData($$8, $$7);
      return $$8;
   }

   public void update(Level $$0, Entity $$1, MapItemSavedData $$2) {
      if ($$0.dimension() == $$2.dimension && $$1 instanceof Player) {
         int $$3 = 1 << $$2.scale;
         int $$4 = $$2.centerX;
         int $$5 = $$2.centerZ;
         int $$6 = Mth.floor($$1.getX() - $$4) / $$3 + 64;
         int $$7 = Mth.floor($$1.getZ() - $$5) / $$3 + 64;
         int $$8 = 128 / $$3;
         if ($$0.dimensionType().hasCeiling()) {
            $$8 /= 2;
         }

         HoldingPlayer $$9 = $$2.getHoldingPlayer((Player)$$1);
         $$9.step++;
         MutableBlockPos $$10 = new MutableBlockPos();
         MutableBlockPos $$11 = new MutableBlockPos();
         boolean $$12 = false;

         for (int $$13 = $$6 - $$8 + 1; $$13 < $$6 + $$8; $$13++) {
            if (($$13 & 15) == ($$9.step & 15) || $$12) {
               $$12 = false;
               double $$14 = 0.0;

               for (int $$15 = $$7 - $$8 - 1; $$15 < $$7 + $$8; $$15++) {
                  if ($$13 >= 0 && $$15 >= -1 && $$13 < 128 && $$15 < 128) {
                     int $$16 = Mth.square($$13 - $$6) + Mth.square($$15 - $$7);
                     boolean $$17 = $$16 > ($$8 - 2) * ($$8 - 2);
                     int $$18 = ($$4 / $$3 + $$13 - 64) * $$3;
                     int $$19 = ($$5 / $$3 + $$15 - 64) * $$3;
                     Multiset<MapColor> $$20 = LinkedHashMultiset.create();
                     LevelChunk $$21 = $$0.getChunk(SectionPos.blockToSectionCoord($$18), SectionPos.blockToSectionCoord($$19));
                     if (!$$21.isEmpty()) {
                        int $$22 = 0;
                        double $$23 = 0.0;
                        if ($$0.dimensionType().hasCeiling()) {
                           int $$24 = $$18 + $$19 * 231871;
                           $$24 = $$24 * $$24 * 31287121 + $$24 * 11;
                           if (($$24 >> 20 & 1) == 0) {
                              $$20.add(Blocks.DIRT.defaultBlockState().getMapColor($$0, BlockPos.ZERO), 10);
                           } else {
                              $$20.add(Blocks.STONE.defaultBlockState().getMapColor($$0, BlockPos.ZERO), 100);
                           }

                           $$23 = 100.0;
                        } else {
                           for (int $$25 = 0; $$25 < $$3; $$25++) {
                              for (int $$26 = 0; $$26 < $$3; $$26++) {
                                 $$10.set($$18 + $$25, 0, $$19 + $$26);
                                 int $$27 = $$21.getHeight(Types.WORLD_SURFACE, $$10.getX(), $$10.getZ()) + 1;
                                 BlockState $$31;
                                 if ($$27 <= $$0.getMinY()) {
                                    $$31 = Blocks.BEDROCK.defaultBlockState();
                                 } else {
                                    do {
                                       $$10.setY(--$$27);
                                       $$31 = $$21.getBlockState($$10);
                                    } while ($$31.getMapColor($$0, $$10) == MapColor.NONE && $$27 > $$0.getMinY());

                                    if ($$27 > $$0.getMinY() && !$$31.getFluidState().isEmpty()) {
                                       int $$29 = $$27 - 1;
                                       $$11.set($$10);

                                       BlockState $$30;
                                       do {
                                          $$11.setY($$29--);
                                          $$30 = $$21.getBlockState($$11);
                                          $$22++;
                                       } while ($$29 > $$0.getMinY() && !$$30.getFluidState().isEmpty());

                                       $$31 = this.getCorrectStateForFluidBlock($$0, $$31, $$10);
                                    }
                                 }

                                 $$2.checkBanners($$0, $$10.getX(), $$10.getZ());
                                 $$23 += (double)$$27 / ($$3 * $$3);
                                 $$20.add($$31.getMapColor($$0, $$10));
                              }
                           }
                        }

                        $$22 /= $$3 * $$3;
                        MapColor $$32 = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst($$20), MapColor.NONE);
                        Brightness $$34;
                        if ($$32 == MapColor.WATER) {
                           double $$33 = $$22 * 0.1 + ($$13 + $$15 & 1) * 0.2;
                           if ($$33 < 0.5) {
                              $$34 = Brightness.HIGH;
                           } else if ($$33 > 0.9) {
                              $$34 = Brightness.LOW;
                           } else {
                              $$34 = Brightness.NORMAL;
                           }
                        } else {
                           double $$37 = ($$23 - $$14) * 4.0 / ($$3 + 4) + (($$13 + $$15 & 1) - 0.5) * 0.4;
                           if ($$37 > 0.6) {
                              $$34 = Brightness.HIGH;
                           } else if ($$37 < -0.6) {
                              $$34 = Brightness.LOW;
                           } else {
                              $$34 = Brightness.NORMAL;
                           }
                        }

                        $$14 = $$23;
                        if ($$15 >= 0 && $$16 < $$8 * $$8 && (!$$17 || ($$13 + $$15 & 1) != 0)) {
                           $$12 |= $$2.updateColor($$13, $$15, $$32.getPackedId($$34));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private BlockState getCorrectStateForFluidBlock(Level $$0, BlockState $$1, BlockPos $$2) {
      FluidState $$3 = $$1.getFluidState();
      return !$$3.isEmpty() && !$$1.isFaceSturdy($$0, $$2, Direction.UP) ? $$3.createLegacyBlock() : $$1;
   }

   private static boolean isBiomeWatery(boolean[] $$0, int $$1, int $$2) {
      return $$0[$$2 * 128 + $$1];
   }

   public static void renderBiomePreviewMap(ServerLevel $$0, net.minecraft.world.item.ItemStack $$1) {
      MapItemSavedData $$2 = getSavedData($$1, $$0);
      if ($$2 != null) {
         if ($$0.dimension() == $$2.dimension) {
            int $$3 = 1 << $$2.scale;
            int $$4 = $$2.centerX;
            int $$5 = $$2.centerZ;
            boolean[] $$6 = new boolean[16384];
            int $$7 = $$4 / $$3 - 64;
            int $$8 = $$5 / $$3 - 64;
            MutableBlockPos $$9 = new MutableBlockPos();

            for (int $$10 = 0; $$10 < 128; $$10++) {
               for (int $$11 = 0; $$11 < 128; $$11++) {
                  Holder<Biome> $$12 = $$0.getBiome($$9.set(($$7 + $$11) * $$3, 0, ($$8 + $$10) * $$3));
                  $$6[$$10 * 128 + $$11] = $$12.is(BiomeTags.WATER_ON_MAP_OUTLINES);
               }
            }

            for (int $$13 = 1; $$13 < 127; $$13++) {
               for (int $$14 = 1; $$14 < 127; $$14++) {
                  int $$15 = 0;

                  for (int $$16 = -1; $$16 < 2; $$16++) {
                     for (int $$17 = -1; $$17 < 2; $$17++) {
                        if (($$16 != 0 || $$17 != 0) && isBiomeWatery($$6, $$13 + $$16, $$14 + $$17)) {
                           $$15++;
                        }
                     }
                  }

                  Brightness $$18 = Brightness.LOWEST;
                  MapColor $$19 = MapColor.NONE;
                  if (isBiomeWatery($$6, $$13, $$14)) {
                     $$19 = MapColor.COLOR_ORANGE;
                     if ($$15 > 7 && $$14 % 2 == 0) {
                        switch (($$13 + (int)(Mth.sin($$14 + 0.0F) * 7.0F)) / 8 % 5) {
                           case 0:
                           case 4:
                              $$18 = Brightness.LOW;
                              break;
                           case 1:
                           case 3:
                              $$18 = Brightness.NORMAL;
                              break;
                           case 2:
                              $$18 = Brightness.HIGH;
                        }
                     } else if ($$15 > 7) {
                        $$19 = MapColor.NONE;
                     } else if ($$15 > 5) {
                        $$18 = Brightness.NORMAL;
                     } else if ($$15 > 3) {
                        $$18 = Brightness.LOW;
                     } else if ($$15 > 1) {
                        $$18 = Brightness.LOW;
                     }
                  } else if ($$15 > 0) {
                     $$19 = MapColor.COLOR_BROWN;
                     if ($$15 > 3) {
                        $$18 = Brightness.NORMAL;
                     } else {
                        $$18 = Brightness.LOWEST;
                     }
                  }

                  if ($$19 != MapColor.NONE) {
                     $$2.setColor($$13, $$14, $$19.getPackedId($$18));
                  }
               }
            }
         }
      }
   }

   @Override
   public void inventoryTick(net.minecraft.world.item.ItemStack $$0, ServerLevel $$1, Entity $$2, EquipmentSlot $$3) {
      MapItemSavedData $$4 = getSavedData($$0, $$1);
      if ($$4 != null) {
         if ($$2 instanceof Player $$5) {
            $$4.tickCarriedBy($$5, $$0);
         }

         if (!$$4.locked && $$3 != null && $$3.getType() == Type.HAND) {
            this.update($$1, $$2, $$4);
         }
      }
   }

   @Override
   public void onCraftedPostProcess(net.minecraft.world.item.ItemStack $$0, Level $$1) {
      MapPostProcessing $$2 = $$0.remove(DataComponents.MAP_POST_PROCESSING);
      if ($$2 != null) {
         if ($$1 instanceof ServerLevel $$3) {
            switch ($$2) {
               case LOCK:
                  lockMap($$0, $$3);
                  break;
               case SCALE:
                  scaleMap($$0, $$3);
            }
         }
      }
   }

   private static void scaleMap(net.minecraft.world.item.ItemStack $$0, ServerLevel $$1) {
      MapItemSavedData $$2 = getSavedData($$0, $$1);
      if ($$2 != null) {
         MapId $$3 = $$1.getFreeMapId();
         $$1.setMapData($$3, $$2.scaled());
         $$0.set(DataComponents.MAP_ID, $$3);
      }
   }

   private static void lockMap(net.minecraft.world.item.ItemStack $$0, ServerLevel $$1) {
      MapItemSavedData $$2 = getSavedData($$0, $$1);
      if ($$2 != null) {
         MapId $$3 = $$1.getFreeMapId();
         MapItemSavedData $$4 = $$2.locked();
         $$1.setMapData($$3, $$4);
         $$0.set(DataComponents.MAP_ID, $$3);
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos());
      if ($$1.is(BlockTags.BANNERS)) {
         if (!$$0.getLevel().isClientSide()) {
            MapItemSavedData $$2 = getSavedData($$0.getItemInHand(), $$0.getLevel());
            if ($$2 != null && !$$2.toggleBanner($$0.getLevel(), $$0.getClickedPos())) {
               return InteractionResult.FAIL;
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.useOn($$0);
      }
   }
}
