package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.BitSet;
import java.util.Objects;

public class LastSeenMessagesTracker {
   private final LastSeenTrackedEntry[] trackedMessages;
   private int tail;
   private int offset;
   
   private MessageSignature lastTrackedMessage;

   public LastSeenMessagesTracker(int $$0) {
      this.trackedMessages = new LastSeenTrackedEntry[$$0];
   }

   public boolean addPending(MessageSignature $$0, boolean $$1) {
      if (Objects.equals($$0, this.lastTrackedMessage)) {
         return false;
      } else {
         this.lastTrackedMessage = $$0;
         this.addEntry($$1 ? new LastSeenTrackedEntry($$0, true) : null);
         return true;
      }
   }

   private void addEntry(LastSeenTrackedEntry $$0) {
      int $$1 = this.tail;
      this.tail = ($$1 + 1) % this.trackedMessages.length;
      this.offset++;
      this.trackedMessages[$$1] = $$0;
   }

   public void ignorePending(MessageSignature $$0) {
      for (int $$1 = 0; $$1 < this.trackedMessages.length; $$1++) {
         LastSeenTrackedEntry $$2 = this.trackedMessages[$$1];
         if ($$2 != null && $$2.pending() && $$0.equals($$2.signature())) {
            this.trackedMessages[$$1] = null;
            break;
         }
      }
   }

   public int getAndClearOffset() {
      int $$0 = this.offset;
      this.offset = 0;
      return $$0;
   }

   public LastSeenMessagesTracker.Update generateAndApplyUpdate() {
      int $$0 = this.getAndClearOffset();
      BitSet $$1 = new BitSet(this.trackedMessages.length);
      ObjectList<MessageSignature> $$2 = new ObjectArrayList(this.trackedMessages.length);

      for (int $$3 = 0; $$3 < this.trackedMessages.length; $$3++) {
         int $$4 = (this.tail + $$3) % this.trackedMessages.length;
         LastSeenTrackedEntry $$5 = this.trackedMessages[$$4];
         if ($$5 != null) {
            $$1.set($$3, true);
            $$2.add($$5.signature());
            this.trackedMessages[$$4] = $$5.acknowledge();
         }
      }

      LastSeenMessages $$6 = new LastSeenMessages($$2);
      LastSeenMessages.Update $$7 = new LastSeenMessages.Update($$0, $$1, $$6.computeChecksum());
      return new LastSeenMessagesTracker.Update($$6, $$7);
   }

   public int offset() {
      return this.offset;
   }

   public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
   }
}
