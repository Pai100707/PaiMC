/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.level;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public class Ticket {
    public static final MapCodec<Ticket> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)BuiltInRegistries.TICKET_TYPE.byNameCodec().fieldOf("type").forGetter(Ticket::getType), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("level").forGetter(Ticket::getTicketLevel), (App)Codec.LONG.optionalFieldOf("ticks_left", (Object)0L).forGetter($$0 -> $$0.ticksLeft)).apply((Applicative)$$02, Ticket::new));
    private final TicketType type;
    private final int ticketLevel;
    private long ticksLeft;

    public Ticket(TicketType $$0, int $$1) {
        this($$0, $$1, $$0.timeout());
    }

    private Ticket(TicketType $$0, int $$1, long $$2) {
        this.type = $$0;
        this.ticketLevel = $$1;
        this.ticksLeft = $$2;
    }

    public String toString() {
        if (this.type.hasTimeout()) {
            return "Ticket[" + Util.getRegisteredName(BuiltInRegistries.TICKET_TYPE, this.type) + " " + this.ticketLevel + "] with " + this.ticksLeft + " ticks left ( out of" + this.type.timeout() + ")";
        }
        return "Ticket[" + Util.getRegisteredName(BuiltInRegistries.TICKET_TYPE, this.type) + " " + this.ticketLevel + "] with no timeout";
    }

    public TicketType getType() {
        return this.type;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public void resetTicksLeft() {
        this.ticksLeft = this.type.timeout();
    }

    public void decreaseTicksLeft() {
        if (this.type.hasTimeout()) {
            --this.ticksLeft;
        }
    }

    public boolean isTimedOut() {
        return this.type.hasTimeout() && this.ticksLeft < 0L;
    }
}

