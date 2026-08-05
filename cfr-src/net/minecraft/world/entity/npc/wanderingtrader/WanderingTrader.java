/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.npc.wanderingtrader;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public class WanderingTrader
extends AbstractVillager
implements Consumable.OverrideConsumeSound {
    private static final int DEFAULT_DESPAWN_DELAY = 0;
    private @Nullable BlockPos wanderTarget;
    private int despawnDelay = 0;

    public WanderingTrader(EntityType<? extends WanderingTrader> $$0, Level $$1) {
        super((EntityType<? extends AbstractVillager>)$$0, $$1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new UseItemGoal<WanderingTrader>(this, PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, $$0 -> this.level().isDarkOutside() && !$$0.isInvisible()));
        this.goalSelector.addGoal(0, new UseItemGoal<WanderingTrader>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, $$0 -> this.level().isBrightOutside() && $$0.isInvisible()));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (!$$2.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if ($$1 == InteractionHand.MAIN_HAND) {
                $$0.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!this.level().isClientSide()) {
                if (this.getOffers().isEmpty()) {
                    return InteractionResult.CONSUME;
                }
                this.setTradingPlayer($$0);
                this.openTradingScreen($$0, this.getDisplayName(), 1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    protected void updateTrades(ServerLevel $$0) {
        MerchantOffers $$1 = this.getOffers();
        for (Pair<VillagerTrades.ItemListing[], Integer> $$2 : VillagerTrades.WANDERING_TRADER_TRADES) {
            VillagerTrades.ItemListing[] $$3 = (VillagerTrades.ItemListing[])$$2.getLeft();
            this.addOffersFromItemListings($$0, $$1, $$3, (Integer)$$2.getRight());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("DespawnDelay", this.despawnDelay);
        $$0.storeNullable("wander_target", BlockPos.CODEC, this.wanderTarget);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.despawnDelay = $$0.getIntOr("DespawnDelay", 0);
        this.wanderTarget = $$0.read("wander_target", BlockPos.CODEC).orElse(null);
        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer $$0) {
        if ($$0.shouldRewardExp()) {
            int $$1 = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), $$1));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTrading()) {
            return SoundEvents.WANDERING_TRADER_TRADE;
        }
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    public SoundEvent getConsumeSound(ItemStack $$0) {
        if ($$0.is(Items.MILK_BUCKET)) {
            return SoundEvents.WANDERING_TRADER_DRINK_MILK;
        }
        return SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean $$0) {
        return $$0 ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int $$0) {
        this.despawnDelay = $$0;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.discard();
        }
    }

    public void setWanderTarget(@Nullable BlockPos $$0) {
        this.wanderTarget = $$0;
    }

    @Nullable BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class WanderToPositionGoal
    extends Goal {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(WanderingTrader $$0, double $$1, double $$2) {
            this.trader = $$0;
            this.stopDistance = $$1;
            this.speedModifier = $$2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void stop() {
            this.trader.setWanderTarget(null);
            WanderingTrader.this.navigation.stop();
        }

        @Override
        public boolean canUse() {
            BlockPos $$0 = this.trader.getWanderTarget();
            return $$0 != null && this.isTooFarAway($$0, this.stopDistance);
        }

        @Override
        public void tick() {
            BlockPos $$0 = this.trader.getWanderTarget();
            if ($$0 != null && WanderingTrader.this.navigation.isDone()) {
                if (this.isTooFarAway($$0, 10.0)) {
                    Vec3 $$1 = new Vec3((double)$$0.getX() - this.trader.getX(), (double)$$0.getY() - this.trader.getY(), (double)$$0.getZ() - this.trader.getZ()).normalize();
                    Vec3 $$2 = $$1.scale(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo($$2.x, $$2.y, $$2.z, this.speedModifier);
                } else {
                    WanderingTrader.this.navigation.moveTo($$0.getX(), $$0.getY(), $$0.getZ(), this.speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos $$0, double $$1) {
            return !$$0.closerToCenterThan(this.trader.position(), $$1);
        }
    }
}

