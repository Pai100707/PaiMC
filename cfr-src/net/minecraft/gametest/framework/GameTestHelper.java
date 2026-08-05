/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.util.Either
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.embedded.EmbeddedChannel
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class GameTestHelper {
    private final GameTestInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHelper(GameTestInfo $$0) {
        this.testInfo = $$0;
    }

    public GameTestAssertException assertionException(Component $$0) {
        return new GameTestAssertException($$0, this.testInfo.getTick());
    }

    public GameTestAssertException assertionException(String $$0, Object ... $$1) {
        return this.assertionException(Component.translatableEscape($$0, $$1));
    }

    public GameTestAssertPosException assertionException(BlockPos $$0, Component $$1) {
        return new GameTestAssertPosException($$1, this.absolutePos($$0), $$0, this.testInfo.getTick());
    }

    public GameTestAssertPosException assertionException(BlockPos $$0, String $$1, Object ... $$2) {
        return this.assertionException($$0, Component.translatableEscape($$1, $$2));
    }

    public ServerLevel getLevel() {
        return this.testInfo.getLevel();
    }

    public BlockState getBlockState(BlockPos $$0) {
        return this.getLevel().getBlockState(this.absolutePos($$0));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos $$0, Class<T> $$1) {
        BlockEntity $$2 = this.getLevel().getBlockEntity(this.absolutePos($$0));
        if ($$2 == null) {
            throw this.assertionException($$0, "test.error.missing_block_entity", new Object[0]);
        }
        if ($$1.isInstance($$2)) {
            return (T)((BlockEntity)$$1.cast($$2));
        }
        throw this.assertionException($$0, "test.error.wrong_block_entity", $$2.getType().builtInRegistryHolder().getRegisteredName());
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class<? extends Entity> $$02) {
        AABB $$1 = this.getBounds();
        List<Entity> $$2 = this.getLevel().getEntitiesOfClass($$02, $$1.inflate(1.0), $$0 -> !($$0 instanceof Player));
        $$2.forEach($$0 -> $$0.kill(this.getLevel()));
    }

    public ItemEntity spawnItem(Item $$0, Vec3 $$1) {
        ServerLevel $$2 = this.getLevel();
        Vec3 $$3 = this.absoluteVec($$1);
        ItemEntity $$4 = new ItemEntity($$2, $$3.x, $$3.y, $$3.z, new ItemStack($$0, 1));
        $$4.setDeltaMovement(0.0, 0.0, 0.0);
        $$2.addFreshEntity($$4);
        return $$4;
    }

    public ItemEntity spawnItem(Item $$0, float $$1, float $$2, float $$3) {
        return this.spawnItem($$0, new Vec3($$1, $$2, $$3));
    }

    public ItemEntity spawnItem(Item $$0, BlockPos $$1) {
        return this.spawnItem($$0, $$1.getX(), $$1.getY(), $$1.getZ());
    }

    public <E extends Entity> E spawn(EntityType<E> $$0, BlockPos $$1) {
        return this.spawn($$0, Vec3.atBottomCenterOf($$1));
    }

    public <E extends Entity> List<E> spawn(EntityType<E> $$0, BlockPos $$1, int $$2) {
        return this.spawn($$0, Vec3.atBottomCenterOf($$1), $$2);
    }

    public <E extends Entity> List<E> spawn(EntityType<E> $$0, Vec3 $$1, int $$2) {
        ArrayList<E> $$3 = new ArrayList<E>();
        for (int $$4 = 0; $$4 < $$2; ++$$4) {
            $$3.add(this.spawn($$0, $$1));
        }
        return $$3;
    }

    public <E extends Entity> E spawn(EntityType<E> $$0, Vec3 $$1) {
        return this.spawn($$0, $$1, null);
    }

    public <E extends Entity> E spawn(EntityType<E> $$0, Vec3 $$1, @Nullable EntitySpawnReason $$2) {
        ServerLevel $$3 = this.getLevel();
        E $$4 = $$0.create($$3, EntitySpawnReason.STRUCTURE);
        if ($$4 == null) {
            throw this.assertionException(BlockPos.containing($$1), "test.error.spawn_failure", $$0.builtInRegistryHolder().getRegisteredName());
        }
        if ($$4 instanceof Mob) {
            Mob $$5 = (Mob)$$4;
            $$5.setPersistenceRequired();
        }
        Vec3 $$6 = this.absoluteVec($$1);
        float $$7 = ((Entity)$$4).rotate(this.getTestRotation());
        ((Entity)$$4).snapTo($$6.x, $$6.y, $$6.z, $$7, ((Entity)$$4).getXRot());
        ((Entity)$$4).setYBodyRot($$7);
        ((Entity)$$4).setYHeadRot($$7);
        if ($$2 != null && $$4 instanceof Mob) {
            Mob $$8 = (Mob)$$4;
            $$8.finalizeSpawn(this.getLevel(), this.getLevel().getCurrentDifficultyAt($$8.blockPosition()), $$2, null);
        }
        $$3.addFreshEntityWithPassengers((Entity)$$4);
        return $$4;
    }

    public <E extends Mob> E spawn(EntityType<E> $$0, int $$1, int $$2, int $$3, EntitySpawnReason $$4) {
        return (E)((Mob)this.spawn($$0, new Vec3($$1, $$2, $$3), $$4));
    }

    public void hurt(Entity $$0, DamageSource $$1, float $$2) {
        $$0.hurtServer(this.getLevel(), $$1, $$2);
    }

    public void kill(Entity $$0) {
        $$0.kill(this.getLevel());
    }

    public <E extends Entity> E findOneEntity(EntityType<E> $$0) {
        return this.findClosestEntity($$0, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E findClosestEntity(EntityType<E> $$0, int $$12, int $$22, int $$3, double $$4) {
        List<E> $$5 = this.findEntities($$0, $$12, $$22, $$3, $$4);
        if ($$5.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_around", $$0.getDescription(), $$12, $$22, $$3);
        }
        if ($$5.size() > 1) {
            throw this.assertionException("test.error.too_many_entities", $$0.toShortString(), $$12, $$22, $$3, $$5.size());
        }
        Vec3 $$6 = this.absoluteVec(new Vec3($$12, $$22, $$3));
        $$5.sort(($$1, $$2) -> {
            double $$3 = $$1.position().distanceTo($$6);
            double $$4 = $$2.position().distanceTo($$6);
            return Double.compare($$3, $$4);
        });
        return (E)((Entity)$$5.get(0));
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> $$0, int $$1, int $$2, int $$3, double $$4) {
        return this.findEntities($$0, Vec3.atBottomCenterOf(new BlockPos($$1, $$2, $$3)), $$4);
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> $$0, Vec3 $$12, double $$2) {
        ServerLevel $$3 = this.getLevel();
        Vec3 $$4 = this.absoluteVec($$12);
        AABB $$5 = this.testInfo.getStructureBounds();
        AABB $$6 = new AABB($$4.add(-$$2, -$$2, -$$2), $$4.add($$2, $$2, $$2));
        return $$3.getEntities($$0, $$5, $$1 -> $$1.getBoundingBox().intersects($$6) && $$1.isAlive());
    }

    public <E extends Entity> E spawn(EntityType<E> $$0, int $$1, int $$2, int $$3) {
        return this.spawn($$0, new BlockPos($$1, $$2, $$3));
    }

    public <E extends Entity> E spawn(EntityType<E> $$0, float $$1, float $$2, float $$3) {
        return this.spawn($$0, new Vec3($$1, $$2, $$3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> $$0, BlockPos $$1) {
        Mob $$2 = (Mob)this.spawn($$0, $$1);
        $$2.removeFreeWill();
        return (E)$$2;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> $$0, int $$1, int $$2, int $$3) {
        return this.spawnWithNoFreeWill($$0, new BlockPos($$1, $$2, $$3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> $$0, Vec3 $$1) {
        Mob $$2 = (Mob)this.spawn($$0, $$1);
        $$2.removeFreeWill();
        return (E)$$2;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> $$0, float $$1, float $$2, float $$3) {
        return this.spawnWithNoFreeWill($$0, new Vec3($$1, $$2, $$3));
    }

    public void moveTo(Mob $$0, float $$1, float $$2, float $$3) {
        Vec3 $$4 = this.absoluteVec(new Vec3($$1, $$2, $$3));
        $$0.snapTo($$4.x, $$4.y, $$4.z, $$0.getYRot(), $$0.getXRot());
    }

    public GameTestSequence walkTo(Mob $$0, BlockPos $$1, float $$2) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            Path $$3 = $$0.getNavigation().createPath(this.absolutePos($$1), 0);
            $$0.getNavigation().moveTo($$3, (double)$$2);
        });
    }

    public void pressButton(int $$0, int $$1, int $$2) {
        this.pressButton(new BlockPos($$0, $$1, $$2));
    }

    public void pressButton(BlockPos $$0) {
        this.assertBlockTag(BlockTags.BUTTONS, $$0);
        BlockPos $$1 = this.absolutePos($$0);
        BlockState $$2 = this.getLevel().getBlockState($$1);
        ButtonBlock $$3 = (ButtonBlock)$$2.getBlock();
        $$3.press($$2, this.getLevel(), $$1, null);
    }

    public void useBlock(BlockPos $$0) {
        this.useBlock($$0, this.makeMockPlayer(GameType.CREATIVE));
    }

    public void useBlock(BlockPos $$0, Player $$1) {
        BlockPos $$2 = this.absolutePos($$0);
        this.useBlock($$0, $$1, new BlockHitResult(Vec3.atCenterOf($$2), Direction.NORTH, $$2, true));
    }

    public void useBlock(BlockPos $$0, Player $$1, BlockHitResult $$2) {
        InteractionHand $$5;
        BlockPos $$3 = this.absolutePos($$0);
        BlockState $$4 = this.getLevel().getBlockState($$3);
        InteractionResult $$6 = $$4.useItemOn($$1.getItemInHand($$5 = InteractionHand.MAIN_HAND), this.getLevel(), $$1, $$5, $$2);
        if ($$6.consumesAction()) {
            return;
        }
        if ($$6 instanceof InteractionResult.TryEmptyHandInteraction && $$4.useWithoutItem(this.getLevel(), $$1, $$2).consumesAction()) {
            return;
        }
        UseOnContext $$7 = new UseOnContext($$1, $$5, $$2);
        $$1.getItemInHand($$5).useOn($$7);
    }

    public LivingEntity makeAboutToDrown(LivingEntity $$0) {
        $$0.setAirSupply(0);
        $$0.setHealth(0.25f);
        return $$0;
    }

    public LivingEntity withLowHealth(LivingEntity $$0) {
        $$0.setHealth(0.25f);
        return $$0;
    }

    public Player makeMockPlayer(final GameType $$0) {
        return new Player(this, this.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public GameType gameMode() {
                return $$0;
            }

            @Override
            public boolean isClientAuthoritative() {
                return false;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie $$0 = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer $$1 = new ServerPlayer(this, this.getLevel().getServer(), this.getLevel(), $$0.gameProfile(), $$0.clientInformation()){

            @Override
            public GameType gameMode() {
                return GameType.CREATIVE;
            }
        };
        Connection $$2 = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel $$3 = new EmbeddedChannel(new ChannelHandler[]{$$2});
        this.getLevel().getServer().getPlayerList().placeNewPlayer($$2, $$1, $$0);
        return $$1;
    }

    public void pullLever(int $$0, int $$1, int $$2) {
        this.pullLever(new BlockPos($$0, $$1, $$2));
    }

    public void pullLever(BlockPos $$0) {
        this.assertBlockPresent(Blocks.LEVER, $$0);
        BlockPos $$1 = this.absolutePos($$0);
        BlockState $$2 = this.getLevel().getBlockState($$1);
        LeverBlock $$3 = (LeverBlock)$$2.getBlock();
        $$3.pull($$2, this.getLevel(), $$1, null);
    }

    public void pulseRedstone(BlockPos $$0, long $$1) {
        this.setBlock($$0, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay($$1, () -> this.setBlock($$0, Blocks.AIR));
    }

    public void destroyBlock(BlockPos $$0) {
        this.getLevel().destroyBlock(this.absolutePos($$0), false, null);
    }

    public void setBlock(int $$0, int $$1, int $$2, Block $$3) {
        this.setBlock(new BlockPos($$0, $$1, $$2), $$3);
    }

    public void setBlock(int $$0, int $$1, int $$2, BlockState $$3) {
        this.setBlock(new BlockPos($$0, $$1, $$2), $$3);
    }

    public void setBlock(BlockPos $$0, Block $$1) {
        this.setBlock($$0, $$1.defaultBlockState());
    }

    public void setBlock(BlockPos $$0, BlockState $$1) {
        this.getLevel().setBlock(this.absolutePos($$0), $$1, 3);
    }

    public void setBlock(BlockPos $$0, Block $$1, Direction $$2) {
        this.setBlock($$0, $$1.defaultBlockState(), $$2);
    }

    public void setBlock(BlockPos $$0, BlockState $$1, Direction $$2) {
        BlockState $$3 = $$1;
        if ($$1.hasProperty(HorizontalDirectionalBlock.FACING)) {
            $$3 = (BlockState)$$1.setValue(HorizontalDirectionalBlock.FACING, $$2);
        }
        if ($$1.hasProperty(BlockStateProperties.FACING)) {
            $$3 = (BlockState)$$1.setValue(BlockStateProperties.FACING, $$2);
        }
        this.getLevel().setBlock(this.absolutePos($$0), $$3, 3);
    }

    public void assertBlockPresent(Block $$0, int $$1, int $$2, int $$3) {
        this.assertBlockPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void assertBlockPresent(Block $$0, BlockPos $$12) {
        BlockState $$22 = this.getBlockState($$12);
        this.assertBlock($$12, $$2 -> $$22.is($$0), $$1 -> Component.translatable("test.error.expected_block", $$0.getName(), $$1.getName()));
    }

    public void assertBlockNotPresent(Block $$0, int $$1, int $$2, int $$3) {
        this.assertBlockNotPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void assertBlockNotPresent(Block $$0, BlockPos $$12) {
        this.assertBlock($$12, $$2 -> !this.getBlockState($$12).is($$0), $$1 -> Component.translatable("test.error.unexpected_block", $$0.getName()));
    }

    public void assertBlockTag(TagKey<Block> $$0, BlockPos $$12) {
        this.assertBlockState($$12, $$1 -> $$1.is($$0), $$1 -> Component.translatable("test.error.expected_block_tag", Component.translationArg($$0.location()), $$1.getBlock().getName()));
    }

    public void succeedWhenBlockPresent(Block $$0, int $$1, int $$2, int $$3) {
        this.succeedWhenBlockPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void succeedWhenBlockPresent(Block $$0, BlockPos $$1) {
        this.succeedWhen(() -> this.assertBlockPresent($$0, $$1));
    }

    public void assertBlock(BlockPos $$0, Predicate<Block> $$12, Function<Block, Component> $$2) {
        this.assertBlockState($$0, $$1 -> $$12.test($$1.getBlock()), $$1 -> (Component)$$2.apply($$1.getBlock()));
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos $$0, Property<T> $$1, T $$2) {
        BlockState $$3 = this.getBlockState($$0);
        boolean $$4 = $$3.hasProperty($$1);
        if (!$$4) {
            throw this.assertionException($$0, "test.error.block_property_missing", $$1.getName(), $$2);
        }
        if (!$$3.getValue($$1).equals($$2)) {
            throw this.assertionException($$0, "test.error.block_property_mismatch", $$1.getName(), $$2, $$3.getValue($$1));
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos $$0, Property<T> $$12, Predicate<T> $$22, Component $$3) {
        this.assertBlockState($$0, $$2 -> {
            if (!$$2.hasProperty($$12)) {
                return false;
            }
            Object $$3 = $$2.getValue($$12);
            return $$22.test($$3);
        }, $$1 -> $$3);
    }

    public void assertBlockState(BlockPos $$0, BlockState $$1) {
        BlockState $$2 = this.getBlockState($$0);
        if (!$$2.equals($$1)) {
            throw this.assertionException($$0, "test.error.state_not_equal", $$1, $$2);
        }
    }

    public void assertBlockState(BlockPos $$0, Predicate<BlockState> $$1, Function<BlockState, Component> $$2) {
        BlockState $$3 = this.getBlockState($$0);
        if (!$$1.test($$3)) {
            throw this.assertionException($$0, $$2.apply($$3));
        }
    }

    public <T extends BlockEntity> void assertBlockEntityData(BlockPos $$0, Class<T> $$1, Predicate<T> $$2, Supplier<Component> $$3) {
        T $$4 = this.getBlockEntity($$0, $$1);
        if (!$$2.test($$4)) {
            throw this.assertionException($$0, $$3.get());
        }
    }

    public void assertRedstoneSignal(BlockPos $$0, Direction $$1, IntPredicate $$2, Supplier<Component> $$3) {
        BlockPos $$4 = this.absolutePos($$0);
        ServerLevel $$5 = this.getLevel();
        BlockState $$6 = $$5.getBlockState($$4);
        int $$7 = $$6.getSignal($$5, $$4, $$1);
        if (!$$2.test($$7)) {
            throw this.assertionException($$0, $$3.get());
        }
    }

    public void assertEntityPresent(EntityType<?> $$0) {
        if (!this.getLevel().hasEntities($$0, this.getBounds(), Entity::isAlive)) {
            throw this.assertionException("test.error.expected_entity_in_test", $$0.getDescription());
        }
    }

    public void assertEntityPresent(EntityType<?> $$0, int $$1, int $$2, int $$3) {
        this.assertEntityPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void assertEntityPresent(EntityType<?> $$0, BlockPos $$1) {
        BlockPos $$2 = this.absolutePos($$1);
        if (!this.getLevel().hasEntities($$0, new AABB($$2), Entity::isAlive)) {
            throw this.assertionException($$1, "test.error.expected_entity", $$0.getDescription());
        }
    }

    public void assertEntityPresent(EntityType<?> $$0, AABB $$1) {
        AABB $$2 = this.absoluteAABB($$1);
        if (!this.getLevel().hasEntities($$0, $$2, Entity::isAlive)) {
            throw this.assertionException(BlockPos.containing($$1.getCenter()), "test.error.expected_entity", $$0.getDescription());
        }
    }

    public void assertEntityPresent(EntityType<?> $$0, AABB $$1, Component $$2) {
        AABB $$3 = this.absoluteAABB($$1);
        if (!this.getLevel().hasEntities($$0, $$3, Entity::isAlive)) {
            throw this.assertionException(BlockPos.containing($$1.getCenter()), $$2);
        }
    }

    public void assertEntitiesPresent(EntityType<?> $$0, int $$1) {
        List<Entity> $$2 = this.getLevel().getEntities($$0, this.getBounds(), Entity::isAlive);
        if ($$2.size() != $$1) {
            throw this.assertionException("test.error.expected_entity_count", $$1, $$0.getDescription(), $$2.size());
        }
    }

    public void assertEntitiesPresent(EntityType<?> $$0, BlockPos $$1, int $$2, double $$3) {
        BlockPos $$4 = this.absolutePos($$1);
        List<?> $$5 = this.getEntities($$0, $$1, $$3);
        if ($$5.size() != $$2) {
            throw this.assertionException($$1, "test.error.expected_entity_count", $$2, $$0.getDescription(), $$5.size());
        }
    }

    public void assertEntityPresent(EntityType<?> $$0, BlockPos $$1, double $$2) {
        List<?> $$3 = this.getEntities($$0, $$1, $$2);
        if ($$3.isEmpty()) {
            BlockPos $$4 = this.absolutePos($$1);
            throw this.assertionException($$1, "test.error.expected_entity", $$0.getDescription());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> $$0, BlockPos $$1, double $$2) {
        BlockPos $$3 = this.absolutePos($$1);
        return this.getLevel().getEntities($$0, new AABB($$3).inflate($$2), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> $$0) {
        return this.getLevel().getEntities($$0, this.getBounds(), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity $$0, int $$1, int $$2, int $$3) {
        this.assertEntityInstancePresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void assertEntityInstancePresent(Entity $$0, BlockPos $$12) {
        BlockPos $$2 = this.absolutePos($$12);
        List<Entity> $$3 = this.getLevel().getEntities($$0.getType(), new AABB($$2), Entity::isAlive);
        $$3.stream().filter($$1 -> $$1 == $$0).findFirst().orElseThrow(() -> this.assertionException($$12, "test.error.expected_entity", $$0.getType().getDescription()));
    }

    public void assertItemEntityCountIs(Item $$0, BlockPos $$1, double $$2, int $$3) {
        BlockPos $$4 = this.absolutePos($$1);
        List<ItemEntity> $$5 = this.getLevel().getEntities(EntityType.ITEM, new AABB($$4).inflate($$2), Entity::isAlive);
        int $$6 = 0;
        for (ItemEntity $$7 : $$5) {
            ItemStack $$8 = $$7.getItem();
            if (!$$8.is($$0)) continue;
            $$6 += $$8.getCount();
        }
        if ($$6 != $$3) {
            throw this.assertionException($$1, "test.error.expected_items_count", $$3, $$0.getName(), $$6);
        }
    }

    public void assertItemEntityPresent(Item $$0, BlockPos $$12, double $$2) {
        BlockPos $$3 = this.absolutePos($$12);
        Predicate<ItemEntity> $$4 = $$1 -> $$1.isAlive() && $$1.getItem().is($$0);
        if (!this.getLevel().hasEntities(EntityType.ITEM, new AABB($$3).inflate($$2), $$4)) {
            throw this.assertionException($$12, "test.error.expected_item", $$0.getName());
        }
    }

    public void assertItemEntityNotPresent(Item $$0, BlockPos $$12, double $$2) {
        BlockPos $$3 = this.absolutePos($$12);
        Predicate<ItemEntity> $$4 = $$1 -> $$1.isAlive() && $$1.getItem().is($$0);
        if (this.getLevel().hasEntities(EntityType.ITEM, new AABB($$3).inflate($$2), $$4)) {
            throw this.assertionException($$12, "test.error.unexpected_item", $$0.getName());
        }
    }

    public void assertItemEntityPresent(Item $$0) {
        Predicate<ItemEntity> $$12 = $$1 -> $$1.isAlive() && $$1.getItem().is($$0);
        if (!this.getLevel().hasEntities(EntityType.ITEM, this.getBounds(), $$12)) {
            throw this.assertionException("test.error.expected_item", $$0.getName());
        }
    }

    public void assertItemEntityNotPresent(Item $$0) {
        Predicate<ItemEntity> $$12 = $$1 -> $$1.isAlive() && $$1.getItem().is($$0);
        if (this.getLevel().hasEntities(EntityType.ITEM, this.getBounds(), $$12)) {
            throw this.assertionException("test.error.unexpected_item", $$0.getName());
        }
    }

    public void assertEntityNotPresent(EntityType<?> $$0) {
        List<Entity> $$1 = this.getLevel().getEntities($$0, this.getBounds(), Entity::isAlive);
        if (!$$1.isEmpty()) {
            throw this.assertionException($$1.getFirst().blockPosition(), "test.error.unexpected_entity", $$0.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityType<?> $$0, int $$1, int $$2, int $$3) {
        this.assertEntityNotPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void assertEntityNotPresent(EntityType<?> $$0, BlockPos $$1) {
        BlockPos $$2 = this.absolutePos($$1);
        if (this.getLevel().hasEntities($$0, new AABB($$2), Entity::isAlive)) {
            throw this.assertionException($$1, "test.error.unexpected_entity", $$0.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityType<?> $$0, AABB $$1) {
        AABB $$2 = this.absoluteAABB($$1);
        List<Entity> $$3 = this.getLevel().getEntities($$0, $$2, Entity::isAlive);
        if (!$$3.isEmpty()) {
            throw this.assertionException($$3.getFirst().blockPosition(), "test.error.unexpected_entity", $$0.getDescription());
        }
    }

    public void assertEntityTouching(EntityType<?> $$0, double $$12, double $$2, double $$3) {
        Vec3 $$4 = new Vec3($$12, $$2, $$3);
        Vec3 $$5 = this.absoluteVec($$4);
        Predicate<Entity> $$6 = $$1 -> $$1.getBoundingBox().intersects($$5, $$5);
        if (!this.getLevel().hasEntities($$0, this.getBounds(), $$6)) {
            throw this.assertionException("test.error.expected_entity_touching", $$0.getDescription(), $$5.x(), $$5.y(), $$5.z(), $$12, $$2, $$3);
        }
    }

    public void assertEntityNotTouching(EntityType<?> $$0, double $$12, double $$2, double $$3) {
        Vec3 $$4 = new Vec3($$12, $$2, $$3);
        Vec3 $$5 = this.absoluteVec($$4);
        Predicate<Entity> $$6 = $$1 -> !$$1.getBoundingBox().intersects($$5, $$5);
        if (!this.getLevel().hasEntities($$0, this.getBounds(), $$6)) {
            throw this.assertionException("test.error.expected_entity_not_touching", $$0.getDescription(), $$5.x(), $$5.y(), $$5.z(), $$12, $$2, $$3);
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos $$0, EntityType<E> $$1, Predicate<E> $$2) {
        BlockPos $$3 = this.absolutePos($$0);
        List<Entity> $$4 = this.getLevel().getEntities($$1, new AABB($$3), Entity::isAlive);
        if ($$4.isEmpty()) {
            throw this.assertionException($$0, "test.error.expected_entity", $$1.getDescription());
        }
        for (Entity $$5 : $$4) {
            if ($$2.test($$5)) continue;
            throw this.assertionException($$5.blockPosition(), "test.error.expected_entity_data_predicate", $$5.getName());
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos $$0, EntityType<E> $$1, Function<? super E, T> $$2, @Nullable T $$3) {
        this.assertEntityData(new AABB($$0), $$1, $$2, $$3);
    }

    public <E extends Entity, T> void assertEntityData(AABB $$0, EntityType<E> $$1, Function<? super E, T> $$2, @Nullable T $$3) {
        List<Entity> $$4 = this.getLevel().getEntities($$1, this.absoluteAABB($$0), Entity::isAlive);
        if ($$4.isEmpty()) {
            throw this.assertionException(BlockPos.containing($$0.getBottomCenter()), "test.error.expected_entity", $$1.getDescription());
        }
        for (Entity $$5 : $$4) {
            T $$6 = $$2.apply($$5);
            if (Objects.equals($$6, $$3)) continue;
            throw this.assertionException(BlockPos.containing($$0.getBottomCenter()), "test.error.expected_entity_data", $$3, $$6);
        }
    }

    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos $$0, EntityType<E> $$1, Item $$2) {
        BlockPos $$3 = this.absolutePos($$0);
        List<LivingEntity> $$4 = this.getLevel().getEntities($$1, new AABB($$3), Entity::isAlive);
        if ($$4.isEmpty()) {
            throw this.assertionException($$0, "test.error.expected_entity", $$1.getDescription());
        }
        for (LivingEntity $$5 : $$4) {
            if (!$$5.isHolding($$2)) continue;
            return;
        }
        throw this.assertionException($$0, "test.error.expected_entity_holding", $$2.getName());
    }

    public <E extends Entity> void assertEntityInventoryContains(BlockPos $$02, EntityType<E> $$12, Item $$2) {
        BlockPos $$3 = this.absolutePos($$02);
        List<Entity> $$4 = this.getLevel().getEntities($$12, new AABB($$3), $$0 -> ((Entity)$$0).isAlive());
        if ($$4.isEmpty()) {
            throw this.assertionException($$02, "test.error.expected_entity", $$12.getDescription());
        }
        for (Entity $$5 : $$4) {
            if (!((InventoryCarrier)((Object)$$5)).getInventory().hasAnyMatching($$1 -> $$1.is($$2))) continue;
            return;
        }
        throw this.assertionException($$02, "test.error.expected_entity_having", $$2.getName());
    }

    public void assertContainerEmpty(BlockPos $$0) {
        BaseContainerBlockEntity $$1 = this.getBlockEntity($$0, BaseContainerBlockEntity.class);
        if (!$$1.isEmpty()) {
            throw this.assertionException($$0, "test.error.expected_empty_container", new Object[0]);
        }
    }

    public void assertContainerContainsSingle(BlockPos $$0, Item $$1) {
        BaseContainerBlockEntity $$2 = this.getBlockEntity($$0, BaseContainerBlockEntity.class);
        if ($$2.countItem($$1) != 1) {
            throw this.assertionException($$0, "test.error.expected_container_contents_single", $$1.getName());
        }
    }

    public void assertContainerContains(BlockPos $$0, Item $$1) {
        BaseContainerBlockEntity $$2 = this.getBlockEntity($$0, BaseContainerBlockEntity.class);
        if ($$2.countItem($$1) == 0) {
            throw this.assertionException($$0, "test.error.expected_container_contents", $$1.getName());
        }
    }

    public void assertSameBlockStates(BoundingBox $$0, BlockPos $$1) {
        BlockPos.betweenClosedStream($$0).forEach($$2 -> {
            BlockPos $$3 = $$1.offset($$2.getX() - $$0.minX(), $$2.getY() - $$0.minY(), $$2.getZ() - $$0.minZ());
            this.assertSameBlockState((BlockPos)$$2, $$3);
        });
    }

    public void assertSameBlockState(BlockPos $$0, BlockPos $$1) {
        BlockState $$3;
        BlockState $$2 = this.getBlockState($$0);
        if ($$2 != ($$3 = this.getBlockState($$1))) {
            throw this.assertionException($$0, "test.error.state_not_equal", $$3, $$2);
        }
    }

    public void assertAtTickTimeContainerContains(long $$0, BlockPos $$1, Item $$2) {
        this.runAtTickTime($$0, () -> this.assertContainerContainsSingle($$1, $$2));
    }

    public void assertAtTickTimeContainerEmpty(long $$0, BlockPos $$1) {
        this.runAtTickTime($$0, () -> this.assertContainerEmpty($$1));
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPos $$0, EntityType<E> $$1, Function<E, T> $$2, T $$3) {
        this.succeedWhen(() -> this.assertEntityData($$0, $$1, $$2, $$3));
    }

    public <E extends Entity> void assertEntityProperty(E $$0, Predicate<E> $$1, Component $$2) {
        if (!$$1.test($$0)) {
            throw this.assertionException($$0.blockPosition(), "test.error.entity_property", $$0.getName(), $$2);
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E $$0, Function<E, T> $$1, T $$2, Component $$3) {
        T $$4 = $$1.apply($$0);
        if (!$$4.equals($$2)) {
            throw this.assertionException($$0.blockPosition(), "test.error.entity_property_details", $$0.getName(), $$3, $$4, $$2);
        }
    }

    public void assertLivingEntityHasMobEffect(LivingEntity $$0, Holder<MobEffect> $$1, int $$2) {
        MobEffectInstance $$3 = $$0.getEffect($$1);
        if ($$3 == null || $$3.getAmplifier() != $$2) {
            throw this.assertionException("test.error.expected_entity_effect", $$0.getName(), PotionContents.getPotionDescription($$1, $$2));
        }
    }

    public void succeedWhenEntityPresent(EntityType<?> $$0, int $$1, int $$2, int $$3) {
        this.succeedWhenEntityPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void succeedWhenEntityPresent(EntityType<?> $$0, BlockPos $$1) {
        this.succeedWhen(() -> this.assertEntityPresent($$0, $$1));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> $$0, int $$1, int $$2, int $$3) {
        this.succeedWhenEntityNotPresent($$0, new BlockPos($$1, $$2, $$3));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> $$0, BlockPos $$1) {
        this.succeedWhen(() -> this.assertEntityNotPresent($$0, $$1));
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.finalCheckAdded = true;
    }

    public void succeedIf(Runnable $$0) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, $$0).thenSucceed();
    }

    public void succeedWhen(Runnable $$0) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil($$0).thenSucceed();
    }

    public void succeedOnTickWhen(int $$0, Runnable $$1) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil($$0, $$1).thenSucceed();
    }

    public void runAtTickTime(long $$0, Runnable $$1) {
        this.testInfo.setRunAtTickTime($$0, $$1);
    }

    public void runAfterDelay(long $$0, Runnable $$1) {
        this.runAtTickTime((long)this.testInfo.getTick() + $$0, $$1);
    }

    public void randomTick(BlockPos $$0) {
        BlockPos $$1 = this.absolutePos($$0);
        ServerLevel $$2 = this.getLevel();
        $$2.getBlockState($$1).randomTick($$2, $$1, $$2.random);
    }

    public void tickBlock(BlockPos $$0) {
        BlockPos $$1 = this.absolutePos($$0);
        ServerLevel $$2 = this.getLevel();
        $$2.getBlockState($$1).tick($$2, $$1, $$2.random);
    }

    public void tickPrecipitation(BlockPos $$0) {
        BlockPos $$1 = this.absolutePos($$0);
        ServerLevel $$2 = this.getLevel();
        $$2.tickPrecipitation($$1);
    }

    public void tickPrecipitation() {
        AABB $$0 = this.getRelativeBounds();
        int $$1 = (int)Math.floor($$0.maxX);
        int $$2 = (int)Math.floor($$0.maxZ);
        int $$3 = (int)Math.floor($$0.maxY);
        for (int $$4 = (int)Math.floor($$0.minX); $$4 < $$1; ++$$4) {
            for (int $$5 = (int)Math.floor($$0.minZ); $$5 < $$2; ++$$5) {
                this.tickPrecipitation(new BlockPos($$4, $$3, $$5));
            }
        }
    }

    public int getHeight(Heightmap.Types $$0, int $$1, int $$2) {
        BlockPos $$3 = this.absolutePos(new BlockPos($$1, 0, $$2));
        return this.relativePos(this.getLevel().getHeightmapPos($$0, $$3)).getY();
    }

    public void fail(Component $$0, BlockPos $$1) {
        throw this.assertionException($$1, $$0);
    }

    public void fail(Component $$0, Entity $$1) {
        throw this.assertionException($$1.blockPosition(), $$0);
    }

    public void fail(Component $$0) {
        throw this.assertionException($$0);
    }

    public void fail(String $$0) {
        throw this.assertionException(Component.literal($$0));
    }

    public void failIf(Runnable $$0) {
        this.testInfo.createSequence().thenWaitUntil($$0).thenFail(() -> this.assertionException("test.error.fail", new Object[0]));
    }

    public void failIfEver(Runnable $$0) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach($$1 -> this.testInfo.setRunAtTickTime($$1, $$0::run));
    }

    public GameTestSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPos absolutePos(BlockPos $$0) {
        BlockPos $$1 = this.testInfo.getTestOrigin();
        BlockPos $$2 = $$1.offset($$0);
        return StructureTemplate.transform($$2, Mirror.NONE, this.testInfo.getRotation(), $$1);
    }

    public BlockPos relativePos(BlockPos $$0) {
        BlockPos $$1 = this.testInfo.getTestOrigin();
        Rotation $$2 = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
        BlockPos $$3 = StructureTemplate.transform($$0, Mirror.NONE, $$2, $$1);
        return $$3.subtract($$1);
    }

    public AABB absoluteAABB(AABB $$0) {
        Vec3 $$1 = this.absoluteVec($$0.getMinPosition());
        Vec3 $$2 = this.absoluteVec($$0.getMaxPosition());
        return new AABB($$1, $$2);
    }

    public AABB relativeAABB(AABB $$0) {
        Vec3 $$1 = this.relativeVec($$0.getMinPosition());
        Vec3 $$2 = this.relativeVec($$0.getMaxPosition());
        return new AABB($$1, $$2);
    }

    public Vec3 absoluteVec(Vec3 $$0) {
        Vec3 $$1 = Vec3.atLowerCornerOf(this.testInfo.getTestOrigin());
        return StructureTemplate.transform($$1.add($$0), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public Vec3 relativeVec(Vec3 $$0) {
        Vec3 $$1 = Vec3.atLowerCornerOf(this.testInfo.getTestOrigin());
        return StructureTemplate.transform($$0.subtract($$1), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public Rotation getTestRotation() {
        return this.testInfo.getRotation();
    }

    public Direction getTestDirection() {
        return this.testInfo.getRotation().rotate(Direction.SOUTH);
    }

    public Direction getAbsoluteDirection(Direction $$0) {
        return this.getTestRotation().rotate($$0);
    }

    public void assertTrue(boolean $$0, Component $$1) {
        if (!$$0) {
            throw this.assertionException($$1);
        }
    }

    public void assertTrue(boolean $$0, String $$1) {
        this.assertTrue($$0, Component.literal($$1));
    }

    public <N> void assertValueEqual(N $$0, N $$1, String $$2) {
        this.assertValueEqual($$0, $$1, Component.literal($$2));
    }

    public <N> void assertValueEqual(N $$0, N $$1, Component $$2) {
        if (!$$0.equals($$1)) {
            throw this.assertionException("test.error.value_not_equal", $$2, $$0, $$1);
        }
    }

    public void assertFalse(boolean $$0, Component $$1) {
        this.assertTrue(!$$0, $$1);
    }

    public void assertFalse(boolean $$0, String $$1) {
        this.assertFalse($$0, Component.literal($$1));
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    public AABB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    public AABB getRelativeBounds() {
        AABB $$0 = this.testInfo.getStructureBounds();
        Rotation $$1 = this.testInfo.getRotation();
        switch ($$1) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new AABB(0.0, 0.0, 0.0, $$0.getZsize(), $$0.getYsize(), $$0.getXsize());
            }
        }
        return new AABB(0.0, 0.0, 0.0, $$0.getXsize(), $$0.getYsize(), $$0.getZsize());
    }

    public void forEveryBlockInStructure(Consumer<BlockPos> $$0) {
        AABB $$1 = this.getRelativeBounds().contract(1.0, 1.0, 1.0);
        BlockPos.MutableBlockPos.betweenClosedStream($$1).forEach($$0);
    }

    public void onEachTick(Runnable $$0) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach($$1 -> this.testInfo.setRunAtTickTime($$1, $$0::run));
    }

    public void placeAt(Player $$0, ItemStack $$1, BlockPos $$2, Direction $$3) {
        BlockPos $$4 = this.absolutePos($$2.relative($$3));
        BlockHitResult $$5 = new BlockHitResult(Vec3.atCenterOf($$4), $$3, $$4, false);
        UseOnContext $$6 = new UseOnContext($$0, InteractionHand.MAIN_HAND, $$5);
        $$1.useOn($$6);
    }

    public void setBiome(ResourceKey<Biome> $$0) {
        AABB $$1 = this.getBounds();
        BlockPos $$2 = BlockPos.containing($$1.minX, $$1.minY, $$1.minZ);
        BlockPos $$3 = BlockPos.containing($$1.maxX, $$1.maxY, $$1.maxZ);
        Either<Integer, CommandSyntaxException> $$4 = FillBiomeCommand.fill(this.getLevel(), $$2, $$3, this.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow($$0));
        if ($$4.right().isPresent()) {
            throw this.assertionException("test.error.set_biome", new Object[0]);
        }
    }
}

