package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Queue;
import net.minecraft.util.ArrayListDeque;

public class SuppressedExceptionCollector {
   private static final int LATEST_ENTRY_COUNT = 8;
   private final Queue<net.minecraft.server.SuppressedExceptionCollector.LongEntry> latestEntries = new ArrayListDeque();
   private final Object2IntLinkedOpenHashMap<net.minecraft.server.SuppressedExceptionCollector.ShortEntry> entryCounts = new Object2IntLinkedOpenHashMap();

   private static long currentTimeMs() {
      return System.currentTimeMillis();
   }

   public synchronized void addEntry(String $$0, Throwable $$1) {
      long $$2 = currentTimeMs();
      String $$3 = $$1.getMessage();
      this.latestEntries.add(new net.minecraft.server.SuppressedExceptionCollector.LongEntry($$2, $$0, (Class<? extends Throwable>)$$1.getClass(), $$3));

      while (this.latestEntries.size() > 8) {
         this.latestEntries.remove();
      }

      net.minecraft.server.SuppressedExceptionCollector.ShortEntry $$4 = new net.minecraft.server.SuppressedExceptionCollector.ShortEntry(
         $$0, (Class<? extends Throwable>)$$1.getClass()
      );
      int $$5 = this.entryCounts.getInt($$4);
      this.entryCounts.putAndMoveToFirst($$4, $$5 + 1);
   }

   public synchronized String dump() {
      long $$0 = currentTimeMs();
      StringBuilder $$1 = new StringBuilder();
      if (!this.latestEntries.isEmpty()) {
         $$1.append("\n\t\tLatest entries:\n");

         for (net.minecraft.server.SuppressedExceptionCollector.LongEntry $$2 : this.latestEntries) {
            $$1.append("\t\t\t")
               .append($$2.location)
               .append(":")
               .append($$2.cls)
               .append(": ")
               .append($$2.message)
               .append(" (")
               .append($$0 - $$2.timestampMs)
               .append("ms ago)")
               .append("\n");
         }
      }

      if (!this.entryCounts.isEmpty()) {
         if ($$1.isEmpty()) {
            $$1.append("\n");
         }

         $$1.append("\t\tEntry counts:\n");
         ObjectIterator var6 = Object2IntMaps.fastIterable(this.entryCounts).iterator();

         while (var6.hasNext()) {
            Entry<net.minecraft.server.SuppressedExceptionCollector.ShortEntry> $$3 = (Entry<net.minecraft.server.SuppressedExceptionCollector.ShortEntry>)var6.next();
            $$1.append("\t\t\t")
               .append(((net.minecraft.server.SuppressedExceptionCollector.ShortEntry)$$3.getKey()).location)
               .append(":")
               .append(((net.minecraft.server.SuppressedExceptionCollector.ShortEntry)$$3.getKey()).cls)
               .append(" x ")
               .append($$3.getIntValue())
               .append("\n");
         }
      }

      return $$1.isEmpty() ? "~~NONE~~" : $$1.toString();
   }

   record LongEntry(long timestampMs, String location, Class<? extends Throwable> cls, String message) {
   }

   record ShortEntry(String location, Class<? extends Throwable> cls) {
   }
}
