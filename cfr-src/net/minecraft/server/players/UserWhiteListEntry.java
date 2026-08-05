/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;

public class UserWhiteListEntry
extends StoredUserEntry<NameAndId> {
    public UserWhiteListEntry(NameAndId $$0) {
        super($$0);
    }

    public UserWhiteListEntry(JsonObject $$0) {
        super(NameAndId.fromJson($$0));
    }

    @Override
    protected void serialize(JsonObject $$0) {
        if (this.getUser() == null) {
            return;
        }
        ((NameAndId)this.getUser()).appendTo($$0);
    }
}

