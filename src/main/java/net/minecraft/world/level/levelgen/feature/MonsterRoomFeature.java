package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

   public MonsterRoomFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      Predicate<BlockState> $$1 = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
      BlockPos $$2 = $$0.origin();
      RandomSource $$3 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$4 = $$0.level();
      int $$5 = 3;
      int $$6 = $$3.nextInt(2) + 2;
      int $$7 = -$$6 - 1;
      int $$8 = $$6 + 1;
      int $$9 = -1;
      int $$10 = 4;
      int $$11 = $$3.nextInt(2) + 2;
      int $$12 = -$$11 - 1;
      int $$13 = $$11 + 1;
      int $$14 = 0;

      for (int $$15 = $$7; $$15 <= $$8; $$15++) {
         for (int $$16 = -1; $$16 <= 4; $$16++) {
            for (int $$17 = $$12; $$17 <= $$13; $$17++) {
               BlockPos $$18 = $$2.offset($$15, $$16, $$17);
               boolean $$19 = $$4.getBlockState($$18).isSolid();
               if ($$16 == -1 && !$$19) {
                  return false;
               }

               if ($$16 == 4 && !$$19) {
                  return false;
               }

               if (($$15 == $$7 || $$15 == $$8 || $$17 == $$12 || $$17 == $$13) && $$16 == 0 && $$4.isEmptyBlock($$18) && $$4.isEmptyBlock($$18.above())) {
                  $$14++;
               }
            }
         }
      }

      if ($$14 >= 1 && $$14 <= 5) {
         for (int $$20 = $$7; $$20 <= $$8; $$20++) {
            for (int $$21 = 3; $$21 >= -1; $$21--) {
               for (int $$22 = $$12; $$22 <= $$13; $$22++) {
                  BlockPos $$23 = $$2.offset($$20, $$21, $$22);
                  BlockState $$24 = $$4.getBlockState($$23);
                  if ($$20 == $$7 || $$21 == -1 || $$22 == $$12 || $$20 == $$8 || $$21 == 4 || $$22 == $$13) {
                     if ($$23.getY() >= $$4.getMinY() && !$$4.getBlockState($$23.below()).isSolid()) {
                        $$4.setBlock($$23, AIR, 2);
                     } else if ($$24.isSolid() && !$$24.is(Blocks.CHEST)) {
                        if ($$21 == -1 && $$3.nextInt(4) != 0) {
                           this.safeSetBlock($$4, $$23, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), $$1);
                        } else {
                           this.safeSetBlock($$4, $$23, Blocks.COBBLESTONE.defaultBlockState(), $$1);
                        }
                     }
                  } else if (!$$24.is(Blocks.CHEST) && !$$24.is(Blocks.SPAWNER)) {
                     this.safeSetBlock($$4, $$23, AIR, $$1);
                  }
               }
            }
         }

         for (int $$25 = 0; $$25 < 2; $$25++) {
            for (int $$26 = 0; $$26 < 3; $$26++) {
               int $$27 = $$2.getX() + $$3.nextInt($$6 * 2 + 1) - $$6;
               int $$28 = $$2.getY();
               int $$29 = $$2.getZ() + $$3.nextInt($$11 * 2 + 1) - $$11;
               BlockPos $$30 = new BlockPos($$27, $$28, $$29);
               if ($$4.isEmptyBlock($$30)) {
                  int $$31 = 0;

                  for (Direction $$32 : Plane.HORIZONTAL) {
                     if ($$4.getBlockState($$30.relative($$32)).isSolid()) {
                        $$31++;
                     }
                  }

                  if ($$31 == 1) {
                     this.safeSetBlock($$4, $$30, StructurePiece.reorient($$4, $$30, Blocks.CHEST.defaultBlockState()), $$1);
                     RandomizableContainer.setBlockEntityLootTable($$4, $$3, $$30, BuiltInLootTables.SIMPLE_DUNGEON);
                     break;
                  }
               }
            }
         }

         this.safeSetBlock($$4, $$2, Blocks.SPAWNER.defaultBlockState(), $$1);
         if ($$4.getBlockEntity($$2) instanceof SpawnerBlockEntity $$34) {
            $$34.setEntityId(this.randomEntityId($$3), $$3);
         } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{$$2.getX(), $$2.getY(), $$2.getZ()});
         }

         return true;
      } else {
         return false;
      }
   }

   private EntityType<?> randomEntityId(RandomSource $$0) {
      return (EntityType<?>)Util.getRandom(MOBS, $$0);
   }
}
