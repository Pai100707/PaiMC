package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ReportType;
import net.minecraft.SharedConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class FilledProfileResults implements ProfileResults {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry() {
      @Override
      public long getDuration() {
         return 0L;
      }

      @Override
      public long getMaxDuration() {
         return 0L;
      }

      @Override
      public long getCount() {
         return 0L;
      }

      @Override
      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.emptyMap();
      }
   };
   private static final Splitter SPLITTER = Splitter.on('\u001e');
   private static final Comparator<Entry<String, FilledProfileResults.CounterCollector>> COUNTER_ENTRY_COMPARATOR = Entry.<String, FilledProfileResults.CounterCollector>comparingByValue(
         Comparator.comparingLong($$0 -> $$0.totalValue)
      )
      .reversed();
   private final Map<String, ? extends ProfilerPathEntry> entries;
   private final long startTimeNano;
   private final int startTimeTicks;
   private final long endTimeNano;
   private final int endTimeTicks;
   private final int tickDuration;

   public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> $$0, long $$1, int $$2, long $$3, int $$4) {
      this.entries = $$0;
      this.startTimeNano = $$1;
      this.startTimeTicks = $$2;
      this.endTimeNano = $$3;
      this.endTimeTicks = $$4;
      this.tickDuration = $$4 - $$2;
   }

   private ProfilerPathEntry getEntry(String $$0) {
      ProfilerPathEntry $$1 = this.entries.get($$0);
      return $$1 != null ? $$1 : EMPTY;
   }

   @Override
   public List<ResultField> getTimes(String $$0) {
      String $$1 = $$0;
      ProfilerPathEntry $$2 = this.getEntry("root");
      long $$3 = $$2.getDuration();
      ProfilerPathEntry $$4 = this.getEntry($$0);
      long $$5 = $$4.getDuration();
      long $$6 = $$4.getCount();
      List<ResultField> $$7 = Lists.newArrayList();
      if (!$$0.isEmpty()) {
         $$0 = $$0 + "\u001e";
      }

      long $$8 = 0L;

      for (String $$9 : this.entries.keySet()) {
         if (isDirectChild($$0, $$9)) {
            $$8 += this.getEntry($$9).getDuration();
         }
      }

      float $$10 = (float)$$8;
      if ($$8 < $$5) {
         $$8 = $$5;
      }

      if ($$3 < $$8) {
         $$3 = $$8;
      }

      for (String $$11 : this.entries.keySet()) {
         if (isDirectChild($$0, $$11)) {
            ProfilerPathEntry $$12 = this.getEntry($$11);
            long $$13 = $$12.getDuration();
            double $$14 = $$13 * 100.0 / $$8;
            double $$15 = $$13 * 100.0 / $$3;
            String $$16 = $$11.substring($$0.length());
            $$7.add(new ResultField($$16, $$14, $$15, $$12.getCount()));
         }
      }

      if ((float)$$8 > $$10) {
         $$7.add(new ResultField("unspecified", ((float)$$8 - $$10) * 100.0 / $$8, ((float)$$8 - $$10) * 100.0 / $$3, $$6));
      }

      Collections.sort($$7);
      $$7.add(0, new ResultField($$1, 100.0, $$8 * 100.0 / $$3, $$6));
      return $$7;
   }

   private static boolean isDirectChild(String $$0, String $$1) {
      return $$1.length() > $$0.length() && $$1.startsWith($$0) && $$1.indexOf(30, $$0.length() + 1) < 0;
   }

   private Map<String, FilledProfileResults.CounterCollector> getCounterValues() {
      Map<String, FilledProfileResults.CounterCollector> $$0 = Maps.newTreeMap();
      this.entries.forEach(($$1, $$2) -> {
         Object2LongMap<String> $$3 = $$2.getCounters();
         if (!$$3.isEmpty()) {
            List<String> $$4 = SPLITTER.splitToList($$1);
            $$3.forEach(($$2x, $$3x) -> $$0.computeIfAbsent($$2x, $$0xxx -> new FilledProfileResults.CounterCollector()).addValue($$4.iterator(), $$3x));
         }
      });
      return $$0;
   }

   @Override
   public long getStartTimeNano() {
      return this.startTimeNano;
   }

   @Override
   public int getStartTimeTicks() {
      return this.startTimeTicks;
   }

   @Override
   public long getEndTimeNano() {
      return this.endTimeNano;
   }

   @Override
   public int getEndTimeTicks() {
      return this.endTimeTicks;
   }

   @Override
   public boolean saveResults(Path $$0) {
      Writer $$1 = null;

      boolean var4;
      try {
         Files.createDirectories($$0.getParent());
         $$1 = Files.newBufferedWriter($$0, StandardCharsets.UTF_8);
         $$1.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
         return true;
      } catch (Throwable var8) {
         LOGGER.error("Could not save profiler results to {}", $$0, var8);
         var4 = false;
      } finally {
         IOUtils.closeQuietly($$1);
      }

      return var4;
   }

   protected String getProfilerResults(long $$0, int $$1) {
      StringBuilder $$2 = new StringBuilder();
      ReportType.PROFILE.appendHeader($$2, List.of());
      $$2.append("Version: ").append(SharedConstants.getCurrentVersion().id()).append('\n');
      $$2.append("Time span: ").append($$0 / 1000000L).append(" ms\n");
      $$2.append("Tick span: ").append($$1).append(" ticks\n");
      $$2.append("// This is approximately ")
         .append(String.format(Locale.ROOT, "%.2f", $$1 / ((float)$$0 / 1.0E9F)))
         .append(" ticks per second. It should be ")
         .append(20)
         .append(" ticks per second\n\n");
      $$2.append("--- BEGIN PROFILE DUMP ---\n\n");
      this.appendProfilerResults(0, "root", $$2);
      $$2.append("--- END PROFILE DUMP ---\n\n");
      Map<String, FilledProfileResults.CounterCollector> $$3 = this.getCounterValues();
      if (!$$3.isEmpty()) {
         $$2.append("--- BEGIN COUNTER DUMP ---\n\n");
         this.appendCounters($$3, $$2, $$1);
         $$2.append("--- END COUNTER DUMP ---\n\n");
      }

      return $$2.toString();
   }

   @Override
   public String getProfilerResults() {
      StringBuilder $$0 = new StringBuilder();
      this.appendProfilerResults(0, "root", $$0);
      return $$0.toString();
   }

   private static StringBuilder indentLine(StringBuilder $$0, int $$1) {
      $$0.append(String.format(Locale.ROOT, "[%02d] ", $$1));

      for (int $$2 = 0; $$2 < $$1; $$2++) {
         $$0.append("|   ");
      }

      return $$0;
   }

   private void appendProfilerResults(int $$0, String $$1, StringBuilder $$2) {
      List<ResultField> $$3 = this.getTimes($$1);
      Object2LongMap<String> $$4 = ((ProfilerPathEntry)ObjectUtils.firstNonNull(new ProfilerPathEntry[]{this.entries.get($$1), EMPTY})).getCounters();
      $$4.forEach(
         ($$2x, $$3x) -> indentLine($$2, $$0).append('#').append($$2x).append(' ').append($$3x).append('/').append($$3x / this.tickDuration).append('\n')
      );
      if ($$3.size() >= 3) {
         for (int $$5 = 1; $$5 < $$3.size(); $$5++) {
            ResultField $$6 = $$3.get($$5);
            indentLine($$2, $$0)
               .append($$6.name)
               .append('(')
               .append($$6.count)
               .append('/')
               .append(String.format(Locale.ROOT, "%.0f", (float)$$6.count / this.tickDuration))
               .append(')')
               .append(" - ")
               .append(String.format(Locale.ROOT, "%.2f", $$6.percentage))
               .append("%/")
               .append(String.format(Locale.ROOT, "%.2f", $$6.globalPercentage))
               .append("%\n");
            if (!"unspecified".equals($$6.name)) {
               try {
                  this.appendProfilerResults($$0 + 1, $$1 + "\u001e" + $$6.name, $$2);
               } catch (Exception var9) {
                  $$2.append("[[ EXCEPTION ").append(var9).append(" ]]");
               }
            }
         }
      }
   }

   private void appendCounterResults(int $$0, String $$1, FilledProfileResults.CounterCollector $$2, int $$3, StringBuilder $$4) {
      indentLine($$4, $$0)
         .append($$1)
         .append(" total:")
         .append($$2.selfValue)
         .append('/')
         .append($$2.totalValue)
         .append(" average: ")
         .append($$2.selfValue / $$3)
         .append('/')
         .append($$2.totalValue / $$3)
         .append('\n');
      $$2.children
         .entrySet()
         .stream()
         .sorted(COUNTER_ENTRY_COMPARATOR)
         .forEach($$3x -> this.appendCounterResults($$0 + 1, (String)$$3x.getKey(), (FilledProfileResults.CounterCollector)$$3x.getValue(), $$3, $$4));
   }

   private void appendCounters(Map<String, FilledProfileResults.CounterCollector> $$0, StringBuilder $$1, int $$2) {
      $$0.forEach(($$2x, $$3) -> {
         $$1.append("-- Counter: ").append($$2x).append(" --\n");
         this.appendCounterResults(0, "root", $$3.children.get("root"), $$2, $$1);
         $$1.append("\n\n");
      });
   }

   @Override
   public int getTickDuration() {
      return this.tickDuration;
   }

   static class CounterCollector {
      long selfValue;
      long totalValue;
      final Map<String, FilledProfileResults.CounterCollector> children = Maps.newHashMap();

      public void addValue(Iterator<String> $$0, long $$1) {
         this.totalValue += $$1;
         if (!$$0.hasNext()) {
            this.selfValue += $$1;
         } else {
            this.children.computeIfAbsent($$0.next(), $$0x -> new FilledProfileResults.CounterCollector()).addValue($$0, $$1);
         }
      }
   }
}
