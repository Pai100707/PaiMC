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
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

public class ServerOpList
extends StoredUserList<NameAndId, ServerOpListEntry> {
    public ServerOpList(File $$0, NotificationService $$1) {
        super($$0, $$1);
    }

    @Override
    protected StoredUserEntry<NameAndId> createEntry(JsonObject $$0) {
        return new ServerOpListEntry($$0);
    }

    @Override
    public String[] getUserList() {
        return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
    }

    @Override
    public boolean add(ServerOpListEntry $$0) {
        if (super.add($$0)) {
            if ($$0.getUser() != null) {
                this.notificationService.playerOped($$0);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(NameAndId $$0) {
        ServerOpListEntry $$1 = (ServerOpListEntry)this.get($$0);
        if (super.remove($$0)) {
            if ($$1 != null) {
                this.notificationService.playerDeoped($$1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (ServerOpListEntry $$0 : this.getEntries()) {
            if ($$0.getUser() == null) continue;
            this.notificationService.playerDeoped($$0);
        }
        super.clear();
    }

    public boolean canBypassPlayerLimit(NameAndId $$0) {
        ServerOpListEntry $$1 = (ServerOpListEntry)this.get($$0);
        if ($$1 != null) {
            return $$1.getBypassesPlayerLimit();
        }
        return false;
    }

    @Override
    protected String getKeyForUser(NameAndId $$0) {
        return $$0.id().toString();
    }

    @Override
    protected /* synthetic */ String getKeyForUser(Object object) {
        return this.getKeyForUser((NameAndId)object);
    }
}

