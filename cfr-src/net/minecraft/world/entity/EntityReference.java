/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class EntityReference<StoredEntityType extends UniquelyIdentifyable> {
    private static final Codec<? extends EntityReference<?>> CODEC = UUIDUtil.CODEC.xmap(EntityReference::new, EntityReference::getUUID);
    private static final StreamCodec<ByteBuf, ? extends EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(EntityReference::new, EntityReference::getUUID);
    private Either<UUID, StoredEntityType> entity;

    public static <Type extends UniquelyIdentifyable> Codec<EntityReference<Type>> codec() {
        return CODEC;
    }

    public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, EntityReference<Type>> streamCodec() {
        return STREAM_CODEC;
    }

    private EntityReference(StoredEntityType $$0) {
        this.entity = Either.right($$0);
    }

    private EntityReference(UUID $$0) {
        this.entity = Either.left((Object)$$0);
    }

    public static <T extends UniquelyIdentifyable> @Nullable EntityReference<T> of(@Nullable T $$0) {
        return $$0 != null ? new EntityReference<T>($$0) : null;
    }

    public static <T extends UniquelyIdentifyable> EntityReference<T> of(UUID $$0) {
        return new EntityReference($$0);
    }

    public UUID getUUID() {
        return (UUID)this.entity.map($$0 -> $$0, UniquelyIdentifyable::getUUID);
    }

    public @Nullable StoredEntityType getEntity(UUIDLookup<? extends UniquelyIdentifyable> $$0, Class<StoredEntityType> $$1) {
        StoredEntityType $$5;
        Optional $$4;
        Optional $$2 = this.entity.right();
        if ($$2.isPresent()) {
            UniquelyIdentifyable $$3 = (UniquelyIdentifyable)$$2.get();
            if ($$3.isRemoved()) {
                this.entity = Either.left((Object)$$3.getUUID());
            } else {
                return (StoredEntityType)$$3;
            }
        }
        if (($$4 = this.entity.left()).isPresent() && ($$5 = this.resolve($$0.lookup((UUID)$$4.get()), $$1)) != null && !$$5.isRemoved()) {
            this.entity = Either.right($$5);
            return $$5;
        }
        return null;
    }

    public @Nullable StoredEntityType getEntity(Level $$0, Class<StoredEntityType> $$1) {
        if (Player.class.isAssignableFrom($$1)) {
            return this.getEntity($$0::getPlayerInAnyDimension, $$1);
        }
        return this.getEntity($$0::getEntityInAnyDimension, $$1);
    }

    private @Nullable StoredEntityType resolve(@Nullable UniquelyIdentifyable $$0, Class<StoredEntityType> $$1) {
        if ($$0 != null && $$1.isAssignableFrom($$0.getClass())) {
            return (StoredEntityType)((UniquelyIdentifyable)$$1.cast($$0));
        }
        return null;
    }

    public boolean matches(StoredEntityType $$0) {
        return this.getUUID().equals($$0.getUUID());
    }

    public void store(ValueOutput $$0, String $$1) {
        $$0.store($$1, UUIDUtil.CODEC, this.getUUID());
    }

    public static void store(@Nullable EntityReference<?> $$0, ValueOutput $$1, String $$2) {
        if ($$0 != null) {
            $$0.store($$1, $$2);
        }
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable StoredEntityType get(@Nullable EntityReference<StoredEntityType> $$0, Level $$1, Class<StoredEntityType> $$2) {
        return $$0 != null ? (StoredEntityType)$$0.getEntity($$1, $$2) : null;
    }

    public static @Nullable Entity getEntity(@Nullable EntityReference<Entity> $$0, Level $$1) {
        return EntityReference.get($$0, $$1, Entity.class);
    }

    public static @Nullable LivingEntity getLivingEntity(@Nullable EntityReference<LivingEntity> $$0, Level $$1) {
        return EntityReference.get($$0, $$1, LivingEntity.class);
    }

    public static @Nullable Player getPlayer(@Nullable EntityReference<Player> $$0, Level $$1) {
        return EntityReference.get($$0, $$1, Player.class);
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable EntityReference<StoredEntityType> read(ValueInput $$0, String $$1) {
        return $$0.read($$1, EntityReference.codec()).orElse(null);
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable EntityReference<StoredEntityType> readWithOldOwnerConversion(ValueInput $$0, String $$12, Level $$2) {
        Optional<UUID> $$3 = $$0.read($$12, UUIDUtil.CODEC);
        if ($$3.isPresent()) {
            return EntityReference.of($$3.get());
        }
        return $$0.getString($$12).map($$1 -> OldUsersConverter.convertMobOwnerIfNecessary($$2.getServer(), $$1)).map(EntityReference::new).orElse(null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if ($$0 == this) {
            return true;
        }
        if (!($$0 instanceof EntityReference)) return false;
        EntityReference $$1 = (EntityReference)$$0;
        if (!this.getUUID().equals($$1.getUUID())) return false;
        return true;
    }

    public int hashCode() {
        return this.getUUID().hashCode();
    }
}

