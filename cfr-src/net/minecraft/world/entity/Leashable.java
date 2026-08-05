/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface Leashable {
    public static final String LEASH_TAG = "leash";
    public static final double LEASH_TOO_FAR_DIST = 12.0;
    public static final double LEASH_ELASTIC_DIST = 6.0;
    public static final double MAXIMUM_ALLOWED_LEASHED_DIST = 16.0;
    public static final Vec3 AXIS_SPECIFIC_ELASTICITY = new Vec3(0.8, 0.2, 0.8);
    public static final float SPRING_DAMPENING = 0.7f;
    public static final double TORSIONAL_ELASTICITY = 10.0;
    public static final double STIFFNESS = 0.11;
    public static final List<Vec3> ENTITY_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.5));
    public static final List<Vec3> LEASHER_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.0));
    public static final List<Vec3> SHARED_QUAD_ATTACHMENT_POINTS = ImmutableList.of((Object)new Vec3(-0.5, 0.5, 0.5), (Object)new Vec3(-0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, 0.5));

    public @Nullable LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canHaveALeashAttachedTo(Entity $$0) {
        if (this == $$0) {
            return false;
        }
        if (this.leashDistanceTo($$0) > this.leashSnapDistance()) {
            return false;
        }
        return this.canBeLeashed();
    }

    default public double leashDistanceTo(Entity $$0) {
        return $$0.getBoundingBox().getCenter().distanceTo(((Entity)((Object)this)).getBoundingBox().getCenter());
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setDelayedLeashHolderId(int $$0) {
        this.setLeashData(new LeashData($$0));
        Leashable.dropLeash((Entity)((Object)this), false, false);
    }

    default public void readLeashData(ValueInput $$0) {
        LeashData $$1 = $$0.read(LEASH_TAG, LeashData.CODEC).orElse(null);
        if (this.getLeashData() != null && $$1 == null) {
            this.removeLeash();
        }
        this.setLeashData($$1);
    }

    default public void writeLeashData(ValueOutput $$0, @Nullable LeashData $$1) {
        $$0.storeNullable(LEASH_TAG, LeashData.CODEC, $$1);
    }

    private static <E extends Entity> void restoreLeashFromSave(E $$0, LeashData $$1) {
        Level level;
        if ($$1.delayedLeashInfo != null && (level = $$0.level()) instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            Optional $$3 = $$1.delayedLeashInfo.left();
            Optional $$4 = $$1.delayedLeashInfo.right();
            if ($$3.isPresent()) {
                Entity $$5 = $$2.getEntity((UUID)$$3.get());
                if ($$5 != null) {
                    Leashable.setLeashedTo($$0, $$5, true);
                    return;
                }
            } else if ($$4.isPresent()) {
                Leashable.setLeashedTo($$0, LeashFenceKnotEntity.getOrCreateKnot($$2, (BlockPos)$$4.get()), true);
                return;
            }
            if ($$0.tickCount > 100) {
                $$0.spawnAtLocation($$2, Items.LEAD);
                ((Leashable)((Object)$$0)).setLeashData(null);
            }
        }
    }

    default public void dropLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, true);
    }

    default public void removeLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, false);
    }

    default public void onLeashRemoved() {
    }

    private static <E extends Entity> void dropLeash(E $$0, boolean $$1, boolean $$2) {
        LeashData $$3 = ((Leashable)((Object)$$0)).getLeashData();
        if ($$3 != null && $$3.leashHolder != null) {
            ((Leashable)((Object)$$0)).setLeashData(null);
            ((Leashable)((Object)$$0)).onLeashRemoved();
            Level level = $$0.level();
            if (level instanceof ServerLevel) {
                ServerLevel $$4 = (ServerLevel)level;
                if ($$2) {
                    $$0.spawnAtLocation($$4, Items.LEAD);
                }
                if ($$1) {
                    $$4.getChunkSource().sendToTrackingPlayers($$0, new ClientboundSetEntityLinkPacket($$0, null));
                }
                $$3.leashHolder.notifyLeasheeRemoved((Leashable)((Object)$$0));
            }
        }
    }

    public static <E extends Entity> void tickLeash(ServerLevel $$0, E $$1) {
        Entity $$3;
        LeashData $$2 = ((Leashable)((Object)$$1)).getLeashData();
        if ($$2 != null && $$2.delayedLeashInfo != null) {
            Leashable.restoreLeashFromSave($$1, $$2);
        }
        if ($$2 == null || $$2.leashHolder == null) {
            return;
        }
        if (!$$1.canInteractWithLevel() || !$$2.leashHolder.canInteractWithLevel()) {
            if ($$0.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
                ((Leashable)((Object)$$1)).dropLeash();
            } else {
                ((Leashable)((Object)$$1)).removeLeash();
            }
        }
        if (($$3 = ((Leashable)((Object)$$1)).getLeashHolder()) != null && $$3.level() == $$1.level()) {
            double $$4 = ((Leashable)((Object)$$1)).leashDistanceTo($$3);
            ((Leashable)((Object)$$1)).whenLeashedTo($$3);
            if ($$4 > ((Leashable)((Object)$$1)).leashSnapDistance()) {
                $$0.playSound(null, $$3.getX(), $$3.getY(), $$3.getZ(), SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
                ((Leashable)((Object)$$1)).leashTooFarBehaviour();
            } else if ($$4 > ((Leashable)((Object)$$1)).leashElasticDistance() - (double)$$3.getBbWidth() - (double)$$1.getBbWidth() && ((Leashable)((Object)$$1)).checkElasticInteractions($$3, $$2)) {
                ((Leashable)((Object)$$1)).onElasticLeashPull();
            } else {
                ((Leashable)((Object)$$1)).closeRangeLeashBehaviour($$3);
            }
            $$1.setYRot((float)((double)$$1.getYRot() - $$2.angularMomentum));
            $$2.angularMomentum *= (double)Leashable.angularFriction($$1);
        }
    }

    default public void onElasticLeashPull() {
        Entity $$0 = (Entity)((Object)this);
        $$0.checkFallDistanceAccumulation();
    }

    default public double leashSnapDistance() {
        return 12.0;
    }

    default public double leashElasticDistance() {
        return 6.0;
    }

    public static <E extends Entity> float angularFriction(E $$0) {
        if ($$0.onGround()) {
            return $$0.level().getBlockState($$0.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91f;
        }
        if ($$0.isInLiquid()) {
            return 0.8f;
        }
        return 0.91f;
    }

    default public void whenLeashedTo(Entity $$0) {
        $$0.notifyLeashHolder(this);
    }

    default public void leashTooFarBehaviour() {
        this.dropLeash();
    }

    default public void closeRangeLeashBehaviour(Entity $$0) {
    }

    default public boolean checkElasticInteractions(Entity $$0, LeashData $$1) {
        boolean $$2 = $$0.supportQuadLeashAsHolder() && this.supportQuadLeash();
        List<Wrench> $$3 = Leashable.computeElasticInteraction((Entity)((Object)this), $$0, $$2 ? SHARED_QUAD_ATTACHMENT_POINTS : ENTITY_ATTACHMENT_POINT, $$2 ? SHARED_QUAD_ATTACHMENT_POINTS : LEASHER_ATTACHMENT_POINT);
        if ($$3.isEmpty()) {
            return false;
        }
        Wrench $$4 = Wrench.accumulate($$3).scale($$2 ? 0.25 : 1.0);
        $$1.angularMomentum += 10.0 * $$4.torque();
        Vec3 $$5 = Leashable.getHolderMovement($$0).subtract(((Entity)((Object)this)).getKnownMovement());
        ((Entity)((Object)this)).addDeltaMovement($$4.force().multiply(AXIS_SPECIFIC_ELASTICITY).add($$5.scale(0.11)));
        return true;
    }

    private static Vec3 getHolderMovement(Entity $$0) {
        Mob $$1;
        if ($$0 instanceof Mob && ($$1 = (Mob)$$0).isNoAi()) {
            return Vec3.ZERO;
        }
        return $$0.getKnownMovement();
    }

    private static <E extends Entity> List<Wrench> computeElasticInteraction(E $$0, Entity $$1, List<Vec3> $$2, List<Vec3> $$3) {
        double $$4 = ((Leashable)((Object)$$0)).leashElasticDistance();
        Vec3 $$5 = Leashable.getHolderMovement($$0);
        float $$6 = $$0.getYRot() * ((float)Math.PI / 180);
        Vec3 $$7 = new Vec3($$0.getBbWidth(), $$0.getBbHeight(), $$0.getBbWidth());
        float $$8 = $$1.getYRot() * ((float)Math.PI / 180);
        Vec3 $$9 = new Vec3($$1.getBbWidth(), $$1.getBbHeight(), $$1.getBbWidth());
        ArrayList<Wrench> $$10 = new ArrayList<Wrench>();
        for (int $$11 = 0; $$11 < $$2.size(); ++$$11) {
            Vec3 $$12 = $$2.get($$11).multiply($$7).yRot(-$$6);
            Vec3 $$13 = $$0.position().add($$12);
            Vec3 $$14 = $$3.get($$11).multiply($$9).yRot(-$$8);
            Vec3 $$15 = $$1.position().add($$14);
            Leashable.computeDampenedSpringInteraction($$15, $$13, $$4, $$5, $$12).ifPresent($$10::add);
        }
        return $$10;
    }

    private static Optional<Wrench> computeDampenedSpringInteraction(Vec3 $$0, Vec3 $$1, double $$2, Vec3 $$3, Vec3 $$4) {
        boolean $$8;
        double $$5 = $$1.distanceTo($$0);
        if ($$5 < $$2) {
            return Optional.empty();
        }
        Vec3 $$6 = $$0.subtract($$1).normalize().scale($$5 - $$2);
        double $$7 = Wrench.torqueFromForce($$4, $$6);
        boolean bl = $$8 = $$3.dot($$6) >= 0.0;
        if ($$8) {
            $$6 = $$6.scale(0.3f);
        }
        return Optional.of(new Wrench($$6, $$7));
    }

    default public boolean supportQuadLeash() {
        return false;
    }

    default public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets((Entity)((Object)this), 0.0, 0.5, 0.5, 0.5);
    }

    public static Vec3[] createQuadLeashOffsets(Entity $$0, double $$1, double $$2, double $$3, double $$4) {
        float $$5 = $$0.getBbWidth();
        double $$6 = $$1 * (double)$$5;
        double $$7 = $$2 * (double)$$5;
        double $$8 = $$3 * (double)$$5;
        double $$9 = $$4 * (double)$$0.getBbHeight();
        return new Vec3[]{new Vec3(-$$8, $$9, $$7 + $$6), new Vec3(-$$8, $$9, -$$7 + $$6), new Vec3($$8, $$9, -$$7 + $$6), new Vec3($$8, $$9, $$7 + $$6)};
    }

    default public Vec3 getLeashOffset(float $$0) {
        return this.getLeashOffset();
    }

    default public Vec3 getLeashOffset() {
        Entity $$0 = (Entity)((Object)this);
        return new Vec3(0.0, $$0.getEyeHeight(), $$0.getBbWidth() * 0.4f);
    }

    default public void setLeashedTo(Entity $$0, boolean $$1) {
        if (this == $$0) {
            return;
        }
        Leashable.setLeashedTo((Entity)((Object)this), $$0, $$1);
    }

    private static <E extends Entity> void setLeashedTo(E $$0, Entity $$1, boolean $$2) {
        Level level;
        LeashData $$3 = ((Leashable)((Object)$$0)).getLeashData();
        if ($$3 == null) {
            $$3 = new LeashData($$1);
            ((Leashable)((Object)$$0)).setLeashData($$3);
        } else {
            Entity $$4 = $$3.leashHolder;
            $$3.setLeashHolder($$1);
            if ($$4 != null && $$4 != $$1) {
                $$4.notifyLeasheeRemoved((Leashable)((Object)$$0));
            }
        }
        if ($$2 && (level = $$0.level()) instanceof ServerLevel) {
            ServerLevel $$5 = (ServerLevel)level;
            $$5.getChunkSource().sendToTrackingPlayers($$0, new ClientboundSetEntityLinkPacket($$0, $$1));
        }
        if ($$0.isPassenger()) {
            $$0.stopRiding();
        }
    }

    default public @Nullable Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    private static <E extends Entity> @Nullable Entity getLeashHolder(E $$0) {
        Entity entity;
        LeashData $$1 = ((Leashable)((Object)$$0)).getLeashData();
        if ($$1 == null) {
            return null;
        }
        if ($$1.delayedLeashHolderId != 0 && $$0.level().isClientSide() && (entity = $$0.level().getEntity($$1.delayedLeashHolderId)) instanceof Entity) {
            Entity $$2 = entity;
            $$1.setLeashHolder($$2);
        }
        return $$1.leashHolder;
    }

    public static List<Leashable> leashableLeashedTo(Entity $$0) {
        return Leashable.leashableInArea($$0, $$1 -> $$1.getLeashHolder() == $$0);
    }

    public static List<Leashable> leashableInArea(Entity $$0, Predicate<Leashable> $$1) {
        return Leashable.leashableInArea($$0.level(), $$0.getBoundingBox().getCenter(), $$1);
    }

    public static List<Leashable> leashableInArea(Level $$0, Vec3 $$12, Predicate<Leashable> $$2) {
        double $$3 = 32.0;
        AABB $$4 = AABB.ofSize($$12, 32.0, 32.0, 32.0);
        return $$0.getEntitiesOfClass(Entity.class, $$4, $$1 -> {
            Leashable $$2;
            return $$1 instanceof Leashable && $$2.test($$2 = (Leashable)((Object)$$1));
        }).stream().map(Leashable.class::cast).toList();
    }

    public static final class LeashData {
        public static final Codec<LeashData> CODEC = Codec.xor((Codec)UUIDUtil.CODEC.fieldOf("UUID").codec(), BlockPos.CODEC).xmap(LeashData::new, $$0 -> {
            Entity $$1 = $$0.leashHolder;
            if ($$1 instanceof LeashFenceKnotEntity) {
                LeashFenceKnotEntity $$2 = (LeashFenceKnotEntity)$$1;
                return Either.right((Object)$$2.getPos());
            }
            if ($$0.leashHolder != null) {
                return Either.left((Object)$$0.leashHolder.getUUID());
            }
            return Objects.requireNonNull($$0.delayedLeashInfo, "Invalid LeashData had no attachment");
        });
        int delayedLeashHolderId;
        public @Nullable Entity leashHolder;
        public @Nullable Either<UUID, BlockPos> delayedLeashInfo;
        public double angularMomentum;

        private LeashData(Either<UUID, BlockPos> $$0) {
            this.delayedLeashInfo = $$0;
        }

        LeashData(Entity $$0) {
            this.leashHolder = $$0;
        }

        LeashData(int $$0) {
            this.delayedLeashHolderId = $$0;
        }

        public void setLeashHolder(Entity $$0) {
            this.leashHolder = $$0;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }

    public record Wrench(Vec3 force, double torque) {
        static Wrench ZERO = new Wrench(Vec3.ZERO, 0.0);

        static double torqueFromForce(Vec3 $$0, Vec3 $$1) {
            return $$0.z * $$1.x - $$0.x * $$1.z;
        }

        static Wrench accumulate(List<Wrench> $$0) {
            if ($$0.isEmpty()) {
                return ZERO;
            }
            double $$1 = 0.0;
            double $$2 = 0.0;
            double $$3 = 0.0;
            double $$4 = 0.0;
            for (Wrench $$5 : $$0) {
                Vec3 $$6 = $$5.force;
                $$1 += $$6.x;
                $$2 += $$6.y;
                $$3 += $$6.z;
                $$4 += $$5.torque;
            }
            return new Wrench(new Vec3($$1, $$2, $$3), $$4);
        }

        public Wrench scale(double $$0) {
            return new Wrench(this.force.scale($$0), this.torque * $$0);
        }
    }
}

