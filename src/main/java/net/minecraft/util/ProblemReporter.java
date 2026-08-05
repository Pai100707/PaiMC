package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface ProblemReporter {
   net.minecraft.util.ProblemReporter DISCARDING = new net.minecraft.util.ProblemReporter() {
      @Override
      public net.minecraft.util.ProblemReporter forChild(net.minecraft.util.ProblemReporter.PathElement $$0) {
         return this;
      }

      @Override
      public void report(net.minecraft.util.ProblemReporter.Problem $$0) {
      }
   };

   net.minecraft.util.ProblemReporter forChild(net.minecraft.util.ProblemReporter.PathElement var1);

   void report(net.minecraft.util.ProblemReporter.Problem var1);

   public static class Collector implements net.minecraft.util.ProblemReporter {
      public static final net.minecraft.util.ProblemReporter.PathElement EMPTY_ROOT = () -> "";
      
      private final net.minecraft.util.ProblemReporter.Collector parent;
      private final net.minecraft.util.ProblemReporter.PathElement element;
      private final Set<net.minecraft.util.ProblemReporter.Collector.Entry> problems;

      public Collector() {
         this(EMPTY_ROOT);
      }

      public Collector(net.minecraft.util.ProblemReporter.PathElement $$0) {
         this.parent = null;
         this.problems = new LinkedHashSet<>();
         this.element = $$0;
      }

      private Collector(net.minecraft.util.ProblemReporter.Collector $$0, net.minecraft.util.ProblemReporter.PathElement $$1) {
         this.problems = $$0.problems;
         this.parent = $$0;
         this.element = $$1;
      }

      @Override
      public net.minecraft.util.ProblemReporter forChild(net.minecraft.util.ProblemReporter.PathElement $$0) {
         return new net.minecraft.util.ProblemReporter.Collector(this, $$0);
      }

      @Override
      public void report(net.minecraft.util.ProblemReporter.Problem $$0) {
         this.problems.add(new net.minecraft.util.ProblemReporter.Collector.Entry(this, $$0));
      }

      public boolean isEmpty() {
         return this.problems.isEmpty();
      }

      public void forEach(BiConsumer<String, net.minecraft.util.ProblemReporter.Problem> $$0) {
         List<net.minecraft.util.ProblemReporter.PathElement> $$1 = new ArrayList<>();
         StringBuilder $$2 = new StringBuilder();

         for (net.minecraft.util.ProblemReporter.Collector.Entry $$3 : this.problems) {
            for (net.minecraft.util.ProblemReporter.Collector $$4 = $$3.source; $$4 != null; $$4 = $$4.parent) {
               $$1.add($$4.element);
            }

            for (int $$5 = $$1.size() - 1; $$5 >= 0; $$5--) {
               $$2.append($$1.get($$5).get());
            }

            $$0.accept($$2.toString(), $$3.problem());
            $$2.setLength(0);
            $$1.clear();
         }
      }

      public String getReport() {
         Multimap<String, net.minecraft.util.ProblemReporter.Problem> $$0 = HashMultimap.create();
         this.forEach($$0::put);
         return $$0.asMap()
            .entrySet()
            .stream()
            .map(
               $$0x -> " at "
                  + (String)$$0x.getKey()
                  + ": "
                  + ((Collection)$$0x.getValue()).stream().map(net.minecraft.util.ProblemReporter.Problem::description).collect(Collectors.joining("; "))
            )
            .collect(Collectors.joining("\n"));
      }

      public String getTreeReport() {
         List<net.minecraft.util.ProblemReporter.PathElement> $$0 = new ArrayList<>();
         net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode $$1 = new net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode(this.element);

         for (net.minecraft.util.ProblemReporter.Collector.Entry $$2 : this.problems) {
            for (net.minecraft.util.ProblemReporter.Collector $$3 = $$2.source; $$3 != this; $$3 = $$3.parent) {
               $$0.add($$3.element);
            }

            net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode $$4 = $$1;

            for (int $$5 = $$0.size() - 1; $$5 >= 0; $$5--) {
               $$4 = $$4.child($$0.get($$5));
            }

            $$0.clear();
            $$4.problems.add($$2.problem);
         }

         return String.join("\n", $$1.getLines());
      }

      record Entry(net.minecraft.util.ProblemReporter.Collector source, net.minecraft.util.ProblemReporter.Problem problem) {
      }

      record ProblemTreeNode(
         net.minecraft.util.ProblemReporter.PathElement element,
         List<net.minecraft.util.ProblemReporter.Problem> problems,
         Map<net.minecraft.util.ProblemReporter.PathElement, net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode> children
      ) {

         public ProblemTreeNode(net.minecraft.util.ProblemReporter.PathElement $$0) {
            this($$0, new ArrayList<>(), new LinkedHashMap<>());
         }

         public net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode child(net.minecraft.util.ProblemReporter.PathElement $$0) {
            return this.children.computeIfAbsent($$0, net.minecraft.util.ProblemReporter.Collector.ProblemTreeNode::new);
         }

         public List<String> getLines() {
            int $$0 = this.problems.size();
            int $$1 = this.children.size();
            if ($$0 == 0 && $$1 == 0) {
               return List.of();
            } else if ($$0 == 0 && $$1 == 1) {
               List<String> $$2 = new ArrayList<>();
               this.children.forEach(($$1x, $$2x) -> $$2.addAll($$2x.getLines()));
               $$2.set(0, this.element.get() + $$2.get(0));
               return $$2;
            } else if ($$0 == 1 && $$1 == 0) {
               return List.of(this.element.get() + ": " + this.problems.getFirst().description());
            } else {
               List<String> $$3 = new ArrayList<>();
               this.children.forEach(($$1x, $$2) -> $$3.addAll($$2.getLines()));
               $$3.replaceAll($$0x -> "  " + $$0x);

               for (net.minecraft.util.ProblemReporter.Problem $$4 : this.problems) {
                  $$3.add("  " + $$4.description());
               }

               $$3.addFirst(this.element.get() + ":");
               return $$3;
            }
         }
      }
   }

   public record ElementReferencePathElement(ResourceKey<?> id) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return "->{" + this.id.identifier() + "@" + this.id.registry() + "}";
      }
   }

   public record FieldPathElement(String name) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return "." + this.name;
      }
   }

   public record IndexedFieldPathElement(String name, int index) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return "." + this.name + "[" + this.index + "]";
      }
   }

   public record IndexedPathElement(int index) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return "[" + this.index + "]";
      }
   }

   @FunctionalInterface
   public interface PathElement {
      String get();
   }

   public interface Problem {
      String description();
   }

   public record RootElementPathElement(ResourceKey<?> id) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return "{" + this.id.identifier() + "@" + this.id.registry() + "}";
      }
   }

   public record RootFieldPathElement(String name) implements net.minecraft.util.ProblemReporter.PathElement {
      @Override
      public String get() {
         return this.name;
      }
   }

   public static class ScopedCollector extends net.minecraft.util.ProblemReporter.Collector implements AutoCloseable {
      private final Logger logger;

      public ScopedCollector(Logger $$0) {
         this.logger = $$0;
      }

      public ScopedCollector(net.minecraft.util.ProblemReporter.PathElement $$0, Logger $$1) {
         super($$0);
         this.logger = $$1;
      }

      @Override
      public void close() {
         if (!this.isEmpty()) {
            this.logger.warn("[{}] Serialization errors:\n{}", this.logger.getName(), this.getTreeReport());
         }
      }
   }
}
