package net.minecraft.util.profiling;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public class InactiveProfiler implements ProfileCollector {
   public static final InactiveProfiler INSTANCE = new InactiveProfiler();

   private InactiveProfiler() {
   }

   @Override
   public void startTick() {
   }

   @Override
   public void endTick() {
   }

   @Override
   public void push(String $$0) {
   }

   @Override
   public void push(Supplier<String> $$0) {
   }

   @Override
   public void markForCharting(MetricCategory $$0) {
   }

   @Override
   public void pop() {
   }

   @Override
   public void popPush(String $$0) {
   }

   @Override
   public void popPush(Supplier<String> $$0) {
   }

   @Override
   public Zone zone(String $$0) {
      return Zone.INACTIVE;
   }

   @Override
   public Zone zone(Supplier<String> $$0) {
      return Zone.INACTIVE;
   }

   @Override
   public void incrementCounter(String $$0, int $$1) {
   }

   @Override
   public void incrementCounter(Supplier<String> $$0, int $$1) {
   }

   @Override
   public ProfileResults getResults() {
      return EmptyProfileResults.EMPTY;
   }

   
   @Override
   public ActiveProfiler.PathEntry getEntry(String $$0) {
      return null;
   }

   @Override
   public Set<Pair<String, MetricCategory>> getChartedPaths() {
      return ImmutableSet.of();
   }
}
