/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;
import org.jspecify.annotations.Nullable;

public class LastSeenMessagesTracker {
    private final @Nullable LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    private @Nullable MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int $$0) {
        this.trackedMessages = new LastSeenTrackedEntry[$$0];
    }

    public boolean addPending(MessageSignature $$0, boolean $$1) {
        if (Objects.equals($$0, this.lastTrackedMessage)) {
            return false;
        }
        this.lastTrackedMessage = $$0;
        this.addEntry($$1 ? new LastSeenTrackedEntry($$0, true) : null);
        return true;
    }

    private void addEntry(@Nullable LastSeenTrackedEntry $$0) {
        int $$1 = this.tail;
        this.tail = ($$1 + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[$$1] = $$0;
    }

    public void ignorePending(MessageSignature $$0) {
        for (int $$1 = 0; $$1 < this.trackedMessages.length; ++$$1) {
            LastSeenTrackedEntry $$2 = this.trackedMessages[$$1];
            if ($$2 == null || !$$2.pending() || !$$0.equals($$2.signature())) continue;
            this.trackedMessages[$$1] = null;
            break;
        }
    }

    public int getAndClearOffset() {
        int $$0 = this.offset;
        this.offset = 0;
        return $$0;
    }

    public Update generateAndApplyUpdate() {
        int $$0 = this.getAndClearOffset();
        BitSet $$1 = new BitSet(this.trackedMessages.length);
        ObjectArrayList $$2 = new ObjectArrayList(this.trackedMessages.length);
        for (int $$3 = 0; $$3 < this.trackedMessages.length; ++$$3) {
            int $$4 = (this.tail + $$3) % this.trackedMessages.length;
            LastSeenTrackedEntry $$5 = this.trackedMessages[$$4];
            if ($$5 == null) continue;
            $$1.set($$3, true);
            $$2.add((Object)$$5.signature());
            this.trackedMessages[$$4] = $$5.acknowledge();
        }
        LastSeenMessages $$6 = new LastSeenMessages((List<MessageSignature>)$$2);
        LastSeenMessages.Update $$7 = new LastSeenMessages.Update($$0, $$1, $$6.computeChecksum());
        return new Update($$6, $$7);
    }

    public int offset() {
        return this.offset;
    }

    public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}

