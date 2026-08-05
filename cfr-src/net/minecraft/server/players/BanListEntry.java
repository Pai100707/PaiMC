/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.StoredUserEntry;
import org.jspecify.annotations.Nullable;

public abstract class BanListEntry<T>
extends StoredUserEntry<T> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    public static final String EXPIRES_NEVER = "forever";
    protected final Date created;
    protected final String source;
    protected final @Nullable Date expires;
    protected final @Nullable String reason;

    public BanListEntry(@Nullable T $$0, @Nullable Date $$1, @Nullable String $$2, @Nullable Date $$3, @Nullable String $$4) {
        super($$0);
        this.created = $$1 == null ? new Date() : $$1;
        this.source = $$2 == null ? "(Unknown)" : $$2;
        this.expires = $$3;
        this.reason = $$4;
    }

    protected BanListEntry(@Nullable T $$0, JsonObject $$1) {
        super($$0);
        Object $$7;
        Date $$4;
        try {
            Date $$2 = $$1.has("created") ? DATE_FORMAT.parse($$1.get("created").getAsString()) : new Date();
        }
        catch (ParseException $$3) {
            $$4 = new Date();
        }
        this.created = $$4;
        this.source = $$1.has("source") ? $$1.get("source").getAsString() : "(Unknown)";
        try {
            Date $$5 = $$1.has("expires") ? DATE_FORMAT.parse($$1.get("expires").getAsString()) : null;
        }
        catch (ParseException $$6) {
            $$7 = null;
        }
        this.expires = $$7;
        this.reason = $$1.has("reason") ? $$1.get("reason").getAsString() : null;
    }

    public Date getCreated() {
        return this.created;
    }

    public String getSource() {
        return this.source;
    }

    public @Nullable Date getExpires() {
        return this.expires;
    }

    public @Nullable String getReason() {
        return this.reason;
    }

    public Component getReasonMessage() {
        String $$0 = this.getReason();
        return $$0 == null ? Component.translatable("multiplayer.disconnect.banned.reason.default") : Component.literal($$0);
    }

    public abstract Component getDisplayName();

    @Override
    boolean hasExpired() {
        if (this.expires == null) {
            return false;
        }
        return this.expires.before(new Date());
    }

    @Override
    protected void serialize(JsonObject $$0) {
        $$0.addProperty("created", DATE_FORMAT.format(this.created));
        $$0.addProperty("source", this.source);
        $$0.addProperty("expires", this.expires == null ? EXPIRES_NEVER : DATE_FORMAT.format(this.expires));
        $$0.addProperty("reason", this.reason);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        BanListEntry $$1 = (BanListEntry)$$0;
        return Objects.equals(this.source, $$1.source) && Objects.equals(this.expires, $$1.expires) && Objects.equals(this.reason, $$1.reason) && Objects.equals(this.getUser(), $$1.getUser());
    }
}

