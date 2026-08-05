/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import net.minecraft.server.players.UserWhiteListEntry;

public class UserWhiteList
extends StoredUserList<NameAndId, UserWhiteListEntry> {
    public UserWhiteList(File $$0, NotificationService $$1) {
        super($$0, $$1);
    }

    @Override
    protected StoredUserEntry<NameAndId> createEntry(JsonObject $$0) {
        return new UserWhiteListEntry($$0);
    }

    public boolean isWhiteListed(NameAndId $$0) {
        return this.contains($$0);
    }

    @Override
    public boolean add(UserWhiteListEntry $$0) {
        if (super.add($$0)) {
            if ($$0.getUser() != null) {
                this.notificationService.playerAddedToAllowlist((NameAndId)$$0.getUser());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(NameAndId $$0) {
        if (super.remove($$0)) {
            this.notificationService.playerRemovedFromAllowlist($$0);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (UserWhiteListEntry $$0 : this.getEntries()) {
            if ($$0.getUser() == null) continue;
            this.notificationService.playerRemovedFromAllowlist((NameAndId)$$0.getUser());
        }
        super.clear();
    }

    @Override
    public String[] getUserList() {
        return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
    }

    @Override
    protected String getKeyForUser(NameAndId $$0) {
        return $$0.id().toString();
    }

    @Override
    protected /* synthetic */ String getKeyForUser(Object object) {
        return this.getKeyForUser((NameAndId)object);
    }

    @Override
    public /* synthetic */ boolean remove(Object object) {
        return this.remove((NameAndId)object);
    }
}

