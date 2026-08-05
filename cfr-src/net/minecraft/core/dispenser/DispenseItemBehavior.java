/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.core.dispenser.MinecartDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public interface DispenseItemBehavior {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DispenseItemBehavior NOOP = ($$0, $$1) -> $$1;

    public ItemStack dispense(BlockSource var1, ItemStack var2);

    public static void bootStrap() {
        DispenserBlock.registerProjectileBehavior(Items.ARROW);
        DispenserBlock.registerProjectileBehavior(Items.TIPPED_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.SPECTRAL_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.EGG);
        DispenserBlock.registerProjectileBehavior(Items.BLUE_EGG);
        DispenserBlock.registerProjectileBehavior(Items.BROWN_EGG);
        DispenserBlock.registerProjectileBehavior(Items.SNOWBALL);
        DispenserBlock.registerProjectileBehavior(Items.EXPERIENCE_BOTTLE);
        DispenserBlock.registerProjectileBehavior(Items.SPLASH_POTION);
        DispenserBlock.registerProjectileBehavior(Items.LINGERING_POTION);
        DispenserBlock.registerProjectileBehavior(Items.FIREWORK_ROCKET);
        DispenserBlock.registerProjectileBehavior(Items.FIRE_CHARGE);
        DispenserBlock.registerProjectileBehavior(Items.WIND_CHARGE);
        DefaultDispenseItemBehavior $$0 = new DefaultDispenseItemBehavior(){

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                Direction $$2 = $$0.state().getValue(DispenserBlock.FACING);
                EntityType<?> $$3 = ((SpawnEggItem)$$1.getItem()).getType($$1);
                if ($$3 == null) {
                    return $$1;
                }
                try {
                    $$3.spawn($$0.level(), $$1, null, $$0.pos().relative($$2), EntitySpawnReason.DISPENSER, $$2 != Direction.UP, false);
                }
                catch (Exception $$4) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", (Object)$$0.pos(), (Object)$$4);
                    return ItemStack.EMPTY;
                }
                $$1.shrink(1);
                $$0.level().gameEvent(null, GameEvent.ENTITY_PLACE, $$0.pos());
                return $$1;
            }
        };
        for (SpawnEggItem $$1 : SpawnEggItem.eggs()) {
            DispenserBlock.registerBehavior($$1, $$0);
        }
        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior(){

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$12) {
                Consumer<ArmorStand> $$5;
                Direction $$2 = $$0.state().getValue(DispenserBlock.FACING);
                BlockPos $$3 = $$0.pos().relative($$2);
                ServerLevel $$4 = $$0.level();
                ArmorStand $$6 = EntityType.ARMOR_STAND.spawn($$4, $$5 = EntityType.appendDefaultStackConfig($$1 -> $$1.setYRot($$2.toYRot()), $$4, $$12, null), $$3, EntitySpawnReason.DISPENSER, false, false);
                if ($$6 != null) {
                    $$12.shrink(1);
                }
                return $$12;
            }
        });
        DispenserBlock.registerBehavior(Items.CHEST, new OptionalDispenseItemBehavior(){

            @Override
            public ItemStack execute(BlockSource $$02, ItemStack $$1) {
                BlockPos $$2 = $$02.pos().relative($$02.state().getValue(DispenserBlock.FACING));
                List<AbstractChestedHorse> $$3 = $$02.level().getEntitiesOfClass(AbstractChestedHorse.class, new AABB($$2), $$0 -> $$0.isAlive() && !$$0.hasChest());
                for (AbstractChestedHorse $$4 : $$3) {
                    SlotAccess $$5;
                    if (!$$4.isTamed() || ($$5 = $$4.getSlot(499)) == null || !$$5.set($$1)) continue;
                    $$1.shrink(1);
                    this.setSuccess(true);
                    return $$1;
                }
                return super.execute($$02, $$1);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(EntityType.OAK_BOAT));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(EntityType.SPRUCE_BOAT));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(EntityType.BIRCH_BOAT));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(EntityType.JUNGLE_BOAT));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(EntityType.DARK_OAK_BOAT));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(EntityType.ACACIA_BOAT));
        DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenseItemBehavior(EntityType.CHERRY_BOAT));
        DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenseItemBehavior(EntityType.MANGROVE_BOAT));
        DispenserBlock.registerBehavior(Items.PALE_OAK_BOAT, new BoatDispenseItemBehavior(EntityType.PALE_OAK_BOAT));
        DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenseItemBehavior(EntityType.BAMBOO_RAFT));
        DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.OAK_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.SPRUCE_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.BIRCH_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.JUNGLE_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.DARK_OAK_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.ACACIA_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.CHERRY_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.MANGROVE_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.PALE_OAK_CHEST_BOAT, new BoatDispenseItemBehavior(EntityType.PALE_OAK_CHEST_BOAT));
        DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenseItemBehavior(EntityType.BAMBOO_CHEST_RAFT));
        DefaultDispenseItemBehavior $$2 = new DefaultDispenseItemBehavior(){
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                DispensibleContainerItem $$2 = (DispensibleContainerItem)((Object)$$1.getItem());
                BlockPos $$3 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                ServerLevel $$4 = $$0.level();
                if ($$2.emptyContents(null, $$4, $$3, null)) {
                    $$2.checkExtraContent(null, $$4, $$1, $$3);
                    return this.consumeWithRemainder($$0, $$1, new ItemStack(Items.BUCKET));
                }
                return this.defaultDispenseItemBehavior.dispense($$0, $$1);
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, $$2);
        DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior(){

            /*
             * WARNING - void declaration
             */
            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                void $$9;
                ItemStack $$7;
                BlockPos $$3;
                ServerLevel $$2 = $$0.level();
                BlockState $$4 = $$2.getBlockState($$3 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING)));
                Block $$5 = $$4.getBlock();
                if ($$5 instanceof BucketPickup) {
                    BucketPickup $$6 = (BucketPickup)((Object)$$5);
                    $$7 = $$6.pickupBlock(null, $$2, $$3, $$4);
                    if ($$7.isEmpty()) {
                        return super.execute($$0, $$1);
                    }
                } else {
                    return super.execute($$0, $$1);
                }
                $$2.gameEvent(null, GameEvent.FLUID_PICKUP, $$3);
                Item $$8 = $$7.getItem();
                return this.consumeWithRemainder($$0, $$1, new ItemStack((ItemLike)$$9));
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$02, ItemStack $$1) {
                ServerLevel $$2 = $$02.level();
                this.setSuccess(true);
                Direction $$3 = $$02.state().getValue(DispenserBlock.FACING);
                BlockPos $$4 = $$02.pos().relative($$3);
                BlockState $$5 = $$2.getBlockState($$4);
                if (BaseFireBlock.canBePlacedAt($$2, $$4, $$3)) {
                    $$2.setBlockAndUpdate($$4, BaseFireBlock.getState($$2, $$4));
                    $$2.gameEvent(null, GameEvent.BLOCK_PLACE, $$4);
                } else if (CampfireBlock.canLight($$5) || CandleBlock.canLight($$5) || CandleCakeBlock.canLight($$5)) {
                    $$2.setBlockAndUpdate($$4, (BlockState)$$5.setValue(BlockStateProperties.LIT, true));
                    $$2.gameEvent(null, GameEvent.BLOCK_CHANGE, $$4);
                } else if ($$5.getBlock() instanceof TntBlock) {
                    if (TntBlock.prime($$2, $$4)) {
                        $$2.removeBlock($$4, false);
                    } else {
                        this.setSuccess(false);
                    }
                } else {
                    this.setSuccess(false);
                }
                if (this.isSuccess()) {
                    $$1.hurtAndBreak(1, $$2, null, $$0 -> {});
                }
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
                this.setSuccess(true);
                ServerLevel $$2 = $$0.level();
                BlockPos $$3 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                if (BoneMealItem.growCrop($$1, $$2, $$3) || BoneMealItem.growWaterPlant($$1, $$2, $$3, null)) {
                    if (!$$2.isClientSide()) {
                        $$2.levelEvent(1505, $$3, 15);
                    }
                } else {
                    this.setSuccess(false);
                }
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
                ServerLevel $$2 = $$0.level();
                if (!$$2.getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
                    this.setSuccess(false);
                    return $$1;
                }
                BlockPos $$3 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                PrimedTnt $$4 = new PrimedTnt($$2, (double)$$3.getX() + 0.5, $$3.getY(), (double)$$3.getZ() + 0.5, null);
                $$2.addFreshEntity($$4);
                $$2.playSound(null, $$4.getX(), $$4.getY(), $$4.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
                $$2.gameEvent(null, GameEvent.ENTITY_PLACE, $$3);
                $$1.shrink(1);
                this.setSuccess(true);
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
                ServerLevel $$2 = $$0.level();
                Direction $$3 = $$0.state().getValue(DispenserBlock.FACING);
                BlockPos $$4 = $$0.pos().relative($$3);
                if ($$2.isEmptyBlock($$4) && WitherSkullBlock.canSpawnMob($$2, $$4, $$1)) {
                    $$2.setBlock($$4, (BlockState)Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, RotationSegment.convertToSegment($$3)), 3);
                    $$2.gameEvent(null, GameEvent.BLOCK_PLACE, $$4);
                    BlockEntity $$5 = $$2.getBlockEntity($$4);
                    if ($$5 instanceof SkullBlockEntity) {
                        WitherSkullBlock.checkSpawn($$2, $$4, (SkullBlockEntity)$$5);
                    }
                    $$1.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment($$0, $$1));
                }
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
                ServerLevel $$2 = $$0.level();
                BlockPos $$3 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                CarvedPumpkinBlock $$4 = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
                if ($$2.isEmptyBlock($$3) && $$4.canSpawnGolem($$2, $$3)) {
                    if (!$$2.isClientSide()) {
                        $$2.setBlock($$3, $$4.defaultBlockState(), 3);
                        $$2.gameEvent(null, GameEvent.BLOCK_PLACE, $$3);
                    }
                    $$1.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment($$0, $$1));
                }
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());
        for (DyeColor $$3 : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor($$3).asItem(), new ShulkerBoxDispenseBehavior());
        }
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior(){

            private ItemStack takeLiquid(BlockSource $$0, ItemStack $$1, ItemStack $$2) {
                $$0.level().gameEvent(null, GameEvent.FLUID_PICKUP, $$0.pos());
                return this.consumeWithRemainder($$0, $$1, $$2);
            }

            @Override
            public ItemStack execute(BlockSource $$02, ItemStack $$1) {
                this.setSuccess(false);
                ServerLevel $$2 = $$02.level();
                BlockPos $$3 = $$02.pos().relative($$02.state().getValue(DispenserBlock.FACING));
                BlockState $$4 = $$2.getBlockState($$3);
                if ($$4.is(BlockTags.BEEHIVES, $$0 -> $$0.hasProperty(BeehiveBlock.HONEY_LEVEL) && $$0.getBlock() instanceof BeehiveBlock) && $$4.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    ((BeehiveBlock)$$4.getBlock()).releaseBeesAndResetHoneyLevel($$2, $$4, $$3, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.takeLiquid($$02, $$1, new ItemStack(Items.HONEY_BOTTLE));
                }
                if ($$2.getFluidState($$3).is(FluidTags.WATER)) {
                    this.setSuccess(true);
                    return this.takeLiquid($$02, $$1, PotionContents.createItemStack(Items.POTION, Potions.WATER));
                }
                return super.execute($$02, $$1);
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior(){

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                Direction $$2 = $$0.state().getValue(DispenserBlock.FACING);
                BlockPos $$3 = $$0.pos().relative($$2);
                ServerLevel $$4 = $$0.level();
                BlockState $$5 = $$4.getBlockState($$3);
                this.setSuccess(true);
                if ($$5.is(Blocks.RESPAWN_ANCHOR)) {
                    if ($$5.getValue(RespawnAnchorBlock.CHARGE) != 4) {
                        RespawnAnchorBlock.charge(null, $$4, $$3, $$5);
                        $$1.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }
                    return $$1;
                }
                return super.execute($$0, $$1);
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
        DispenserBlock.registerBehavior(Items.BRUSH.asItem(), new OptionalDispenseItemBehavior(){

            @Override
            protected ItemStack execute(BlockSource $$02, ItemStack $$1) {
                BlockPos $$3;
                ServerLevel $$2 = $$02.level();
                List<Entity> $$4 = $$2.getEntitiesOfClass(Armadillo.class, new AABB($$3 = $$02.pos().relative($$02.state().getValue(DispenserBlock.FACING))), EntitySelector.NO_SPECTATORS);
                if ($$4.isEmpty()) {
                    this.setSuccess(false);
                    return $$1;
                }
                for (Armadillo armadillo : $$4) {
                    if (!armadillo.brushOffScute(null, $$1)) continue;
                    $$1.hurtAndBreak(16, $$2, null, $$0 -> {});
                    return $$1;
                }
                this.setSuccess(false);
                return $$1;
            }
        });
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new OptionalDispenseItemBehavior(){

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                BlockPos $$2 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                ServerLevel $$3 = $$0.level();
                BlockState $$4 = $$3.getBlockState($$2);
                Optional<BlockState> $$5 = HoneycombItem.getWaxed($$4);
                if ($$5.isPresent()) {
                    $$3.setBlockAndUpdate($$2, $$5.get());
                    $$3.levelEvent(3003, $$2, 0);
                    $$1.shrink(1);
                    this.setSuccess(true);
                    return $$1;
                }
                return super.execute($$0, $$1);
            }
        });
        DispenserBlock.registerBehavior(Items.POTION, new DefaultDispenseItemBehavior(){
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource $$0, ItemStack $$1) {
                PotionContents $$2 = $$1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                if (!$$2.is(Potions.WATER)) {
                    return this.defaultDispenseItemBehavior.dispense($$0, $$1);
                }
                ServerLevel $$3 = $$0.level();
                BlockPos $$4 = $$0.pos();
                BlockPos $$5 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
                if ($$3.getBlockState($$5).is(BlockTags.CONVERTABLE_TO_MUD)) {
                    if (!$$3.isClientSide()) {
                        for (int $$6 = 0; $$6 < 5; ++$$6) {
                            $$3.sendParticles(ParticleTypes.SPLASH, (double)$$4.getX() + $$3.random.nextDouble(), $$4.getY() + 1, (double)$$4.getZ() + $$3.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                        }
                    }
                    $$3.playSound(null, $$4, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    $$3.gameEvent(null, GameEvent.FLUID_PLACE, $$4);
                    $$3.setBlockAndUpdate($$5, Blocks.MUD.defaultBlockState());
                    return this.consumeWithRemainder($$0, $$1, new ItemStack(Items.GLASS_BOTTLE));
                }
                return this.defaultDispenseItemBehavior.dispense($$0, $$1);
            }
        });
        DispenserBlock.registerBehavior(Items.MINECART, new MinecartDispenseItemBehavior(EntityType.MINECART));
        DispenserBlock.registerBehavior(Items.CHEST_MINECART, new MinecartDispenseItemBehavior(EntityType.CHEST_MINECART));
        DispenserBlock.registerBehavior(Items.FURNACE_MINECART, new MinecartDispenseItemBehavior(EntityType.FURNACE_MINECART));
        DispenserBlock.registerBehavior(Items.TNT_MINECART, new MinecartDispenseItemBehavior(EntityType.TNT_MINECART));
        DispenserBlock.registerBehavior(Items.HOPPER_MINECART, new MinecartDispenseItemBehavior(EntityType.HOPPER_MINECART));
        DispenserBlock.registerBehavior(Items.COMMAND_BLOCK_MINECART, new MinecartDispenseItemBehavior(EntityType.COMMAND_BLOCK_MINECART));
    }
}

