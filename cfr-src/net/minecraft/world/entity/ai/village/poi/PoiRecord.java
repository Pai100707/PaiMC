/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class PoiRecord {
    private final BlockPos pos;
    private final Holder<PoiType> poiType;
    private int freeTickets;
    private final Runnable setDirty;

    PoiRecord(BlockPos $$0, Holder<PoiType> $$1, int $$2, Runnable $$3) {
        this.pos = $$0.immutable();
        this.poiType = $$1;
        this.freeTickets = $$2;
        this.setDirty = $$3;
    }

    public PoiRecord(BlockPos $$0, Holder<PoiType> $$1, Runnable $$2) {
        this($$0, $$1, $$1.value().maxTickets(), $$2);
    }

    public Packed pack() {
        return new Packed(this.pos, this.poiType, this.freeTickets);
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets() {
        return this.freeTickets;
    }

    protected boolean acquireTicket() {
        if (this.freeTickets <= 0) {
            return false;
        }
        --this.freeTickets;
        this.setDirty.run();
        return true;
    }

    protected boolean releaseTicket() {
        if (this.freeTickets >= this.poiType.value().maxTickets()) {
            return false;
        }
        ++this.freeTickets;
        this.setDirty.run();
        return true;
    }

    public boolean hasSpace() {
        return this.freeTickets > 0;
    }

    public boolean isOccupied() {
        return this.freeTickets != this.poiType.value().maxTickets();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Holder<PoiType> getPoiType() {
        return this.poiType;
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        return Objects.equals(this.pos, ((PoiRecord)$$0).pos);
    }

    public int hashCode() {
        return this.pos.hashCode();
    }

    public record Packed(BlockPos pos, Holder<PoiType> poiType, int freeTickets) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(Packed::pos), (App)RegistryFixedCodec.create(Registries.POINT_OF_INTEREST_TYPE).fieldOf("type").forGetter(Packed::poiType), (App)Codec.INT.fieldOf("free_tickets").orElse((Object)0).forGetter(Packed::freeTickets)).apply((Applicative)$$0, Packed::new));

        public PoiRecord unpack(Runnable $$0) {
            return new PoiRecord(this.pos, this.poiType, this.freeTickets, $$0);
        }
    }
}

