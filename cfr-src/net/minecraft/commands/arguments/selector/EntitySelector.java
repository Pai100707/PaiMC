/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntitySelector {
    public static final int INFINITE = Integer.MAX_VALUE;
    public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = ($$0, $$1) -> {};
    private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>(){

        @Override
        public Entity tryCast(Entity $$0) {
            return $$0;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int maxResults;
    private final boolean includesEntities;
    private final boolean worldLimited;
    private final List<Predicate<Entity>> contextFreePredicates;
    private final @Nullable MinMaxBounds.Doubles range;
    private final Function<Vec3, Vec3> position;
    private final @Nullable AABB aabb;
    private final BiConsumer<Vec3, List<? extends Entity>> order;
    private final boolean currentEntity;
    private final @Nullable String playerName;
    private final @Nullable UUID entityUUID;
    private final EntityTypeTest<Entity, ?> type;
    private final boolean usesSelector;

    public EntitySelector(int $$0, boolean $$1, boolean $$2, List<Predicate<Entity>> $$3, @Nullable MinMaxBounds.Doubles $$4, Function<Vec3, Vec3> $$5, @Nullable AABB $$6, BiConsumer<Vec3, List<? extends Entity>> $$7, boolean $$8, @Nullable String $$9, @Nullable UUID $$10, @Nullable EntityType<?> $$11, boolean $$12) {
        this.maxResults = $$0;
        this.includesEntities = $$1;
        this.worldLimited = $$2;
        this.contextFreePredicates = $$3;
        this.range = $$4;
        this.position = $$5;
        this.aabb = $$6;
        this.order = $$7;
        this.currentEntity = $$8;
        this.playerName = $$9;
        this.entityUUID = $$10;
        this.type = $$11 == null ? ANY_TYPE : $$11;
        this.usesSelector = $$12;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public boolean includesEntities() {
        return this.includesEntities;
    }

    public boolean isSelfSelector() {
        return this.currentEntity;
    }

    public boolean isWorldLimited() {
        return this.worldLimited;
    }

    public boolean usesSelector() {
        return this.usesSelector;
    }

    private void checkPermissions(CommandSourceStack $$0) throws CommandSyntaxException {
        if (this.usesSelector && !$$0.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS)) {
            throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
        }
    }

    public Entity findSingleEntity(CommandSourceStack $$0) throws CommandSyntaxException {
        this.checkPermissions($$0);
        List<? extends Entity> $$1 = this.findEntities($$0);
        if ($$1.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }
        if ($$1.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
        }
        return $$1.get(0);
    }

    public List<? extends Entity> findEntities(CommandSourceStack $$0) throws CommandSyntaxException {
        this.checkPermissions($$0);
        if (!this.includesEntities) {
            return this.findPlayers($$0);
        }
        if (this.playerName != null) {
            ServerPlayer $$1 = $$0.getServer().getPlayerList().getPlayerByName(this.playerName);
            if ($$1 == null) {
                return List.of();
            }
            return List.of($$1);
        }
        if (this.entityUUID != null) {
            for (ServerLevel $$2 : $$0.getServer().getAllLevels()) {
                Entity $$3 = $$2.getEntity(this.entityUUID);
                if ($$3 == null) continue;
                if (!$$3.getType().isEnabled($$0.enabledFeatures())) break;
                return List.of($$3);
            }
            return List.of();
        }
        Vec3 $$4 = this.position.apply($$0.getPosition());
        AABB $$5 = this.getAbsoluteAabb($$4);
        if (this.currentEntity) {
            Predicate<Entity> $$6 = this.getPredicate($$4, $$5, null);
            if ($$0.getEntity() != null && $$6.test($$0.getEntity())) {
                return List.of($$0.getEntity());
            }
            return List.of();
        }
        Predicate<Entity> $$7 = this.getPredicate($$4, $$5, $$0.enabledFeatures());
        ObjectArrayList $$8 = new ObjectArrayList();
        if (this.isWorldLimited()) {
            this.addEntities((List<Entity>)$$8, $$0.getLevel(), $$5, $$7);
        } else {
            for (ServerLevel $$9 : $$0.getServer().getAllLevels()) {
                this.addEntities((List<Entity>)$$8, $$9, $$5, $$7);
            }
        }
        return this.sortAndLimit($$4, (List)$$8);
    }

    private void addEntities(List<Entity> $$0, ServerLevel $$1, @Nullable AABB $$2, Predicate<Entity> $$3) {
        int $$4 = this.getResultLimit();
        if ($$0.size() >= $$4) {
            return;
        }
        if ($$2 != null) {
            $$1.getEntities(this.type, $$2, $$3, $$0, $$4);
        } else {
            $$1.getEntities(this.type, $$3, $$0, $$4);
        }
    }

    private int getResultLimit() {
        return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
    }

    public ServerPlayer findSinglePlayer(CommandSourceStack $$0) throws CommandSyntaxException {
        this.checkPermissions($$0);
        List<ServerPlayer> $$1 = this.findPlayers($$0);
        if ($$1.size() != 1) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        return $$1.get(0);
    }

    public List<ServerPlayer> findPlayers(CommandSourceStack $$0) throws CommandSyntaxException {
        ObjectArrayList $$9;
        this.checkPermissions($$0);
        if (this.playerName != null) {
            ServerPlayer $$1 = $$0.getServer().getPlayerList().getPlayerByName(this.playerName);
            if ($$1 == null) {
                return List.of();
            }
            return List.of($$1);
        }
        if (this.entityUUID != null) {
            ServerPlayer $$2 = $$0.getServer().getPlayerList().getPlayer(this.entityUUID);
            if ($$2 == null) {
                return List.of();
            }
            return List.of($$2);
        }
        Vec3 $$3 = this.position.apply($$0.getPosition());
        AABB $$4 = this.getAbsoluteAabb($$3);
        Predicate<Entity> $$5 = this.getPredicate($$3, $$4, null);
        if (this.currentEntity) {
            ServerPlayer $$6;
            Entity entity = $$0.getEntity();
            if (entity instanceof ServerPlayer && $$5.test($$6 = (ServerPlayer)entity)) {
                return List.of($$6);
            }
            return List.of();
        }
        int $$7 = this.getResultLimit();
        if (this.isWorldLimited()) {
            List<ServerPlayer> $$8 = $$0.getLevel().getPlayers($$5, $$7);
        } else {
            $$9 = new ObjectArrayList();
            for (ServerPlayer $$10 : $$0.getServer().getPlayerList().getPlayers()) {
                if (!$$5.test($$10)) continue;
                $$9.add($$10);
                if ($$9.size() < $$7) continue;
                return $$9;
            }
        }
        return this.sortAndLimit($$3, (List)$$9);
    }

    private @Nullable AABB getAbsoluteAabb(Vec3 $$0) {
        return this.aabb != null ? this.aabb.move($$0) : null;
    }

    private Predicate<Entity> getPredicate(Vec3 $$0, @Nullable AABB $$12, @Nullable FeatureFlagSet $$2) {
        ObjectArrayList $$9;
        boolean $$5;
        boolean $$4;
        boolean $$3 = $$2 != null;
        int $$6 = ($$3 ? 1 : 0) + (($$4 = $$12 != null) ? 1 : 0) + (($$5 = this.range != null) ? 1 : 0);
        if ($$6 == 0) {
            List<Predicate<Entity>> $$7 = this.contextFreePredicates;
        } else {
            ObjectArrayList $$8 = new ObjectArrayList(this.contextFreePredicates.size() + $$6);
            $$8.addAll(this.contextFreePredicates);
            if ($$3) {
                $$8.add($$1 -> $$1.getType().isEnabled($$2));
            }
            if ($$4) {
                $$8.add($$1 -> $$12.intersects($$1.getBoundingBox()));
            }
            if ($$5) {
                $$8.add($$1 -> this.range.matchesSqr($$1.distanceToSqr($$0)));
            }
            $$9 = $$8;
        }
        return Util.allOf($$9);
    }

    private <T extends Entity> List<T> sortAndLimit(Vec3 $$0, List<T> $$1) {
        if ($$1.size() > 1) {
            this.order.accept($$0, $$1);
        }
        return $$1.subList(0, Math.min(this.maxResults, $$1.size()));
    }

    public static Component joinNames(List<? extends Entity> $$0) {
        return ComponentUtils.formatList($$0, Entity::getDisplayName);
    }
}

