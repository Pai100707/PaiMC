package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller implements ProfilerFiller {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE), 5);
   private final List<com.mojang.jtracy.Zone> activeZones = new ArrayList<>();
   private final Map<String, TracyZoneFiller.PlotAndValue> plots = new HashMap<>();
   private final String name = Thread.currentThread().getName();

   @Override
   public void startTick() {
   }

   @Override
   public void endTick() {
      for (TracyZoneFiller.PlotAndValue $$0 : this.plots.values()) {
         $$0.set(0);
      }
   }

   @Override
   public void push(String $$0) {
      String $$1 = "";
      String $$2 = "";
      int $$3 = 0;
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         Optional<StackFrame> $$4 = STACK_WALKER.walk(
            $$0x -> $$0x.filter(
                  $$0xx -> $$0xx.getDeclaringClass() != TracyZoneFiller.class && $$0xx.getDeclaringClass() != ProfilerFiller.CombinedProfileFiller.class
               )
               .findFirst()
         );
         if ($$4.isPresent()) {
            StackFrame $$5 = $$4.get();
            $$1 = $$5.getMethodName();
            $$2 = $$5.getFileName();
            $$3 = $$5.getLineNumber();
         }
      }

      com.mojang.jtracy.Zone $$6 = TracyClient.beginZone($$0, $$1, $$2, $$3);
      this.activeZones.add($$6);
   }

   @Override
   public void push(Supplier<String> $$0) {
      this.push($$0.get());
   }

   @Override
   public void pop() {
      if (this.activeZones.isEmpty()) {
         LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
      } else {
         com.mojang.jtracy.Zone $$0 = this.activeZones.removeLast();
         $$0.close();
      }
   }

   @Override
   public void popPush(String $$0) {
      this.pop();
      this.push($$0);
   }

   @Override
   public void popPush(Supplier<String> $$0) {
      this.pop();
      this.push($$0.get());
   }

   @Override
   public void markForCharting(MetricCategory $$0) {
   }

   @Override
   public void incrementCounter(String $$0, int $$1) {
      this.plots.computeIfAbsent($$0, $$1x -> new TracyZoneFiller.PlotAndValue(this.name + " " + $$0)).add($$1);
   }

   @Override
   public void incrementCounter(Supplier<String> $$0, int $$1) {
      this.incrementCounter($$0.get(), $$1);
   }

   private com.mojang.jtracy.Zone activeZone() {
      return this.activeZones.getLast();
   }

   @Override
   public void addZoneText(String $$0) {
      this.activeZone().addText($$0);
   }

   @Override
   public void addZoneValue(long $$0) {
      this.activeZone().addValue($$0);
   }

   @Override
   public void setZoneColor(int $$0) {
      this.activeZone().setColor($$0);
   }

   static final class PlotAndValue {
      private final Plot plot;
      private int value;

      PlotAndValue(String $$0) {
         this.plot = TracyClient.createPlot($$0);
         this.value = 0;
      }

      void set(int $$0) {
         this.value = $$0;
         this.plot.setValue($$0);
      }

      void add(int $$0) {
         this.set(this.value + $$0);
      }
   }
}
