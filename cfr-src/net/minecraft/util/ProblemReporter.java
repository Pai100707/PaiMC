/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface ProblemReporter {
    public static final ProblemReporter DISCARDING = new ProblemReporter(){

        @Override
        public ProblemReporter forChild(PathElement $$0) {
            return this;
        }

        @Override
        public void report(Problem $$0) {
        }
    };

    public ProblemReporter forChild(PathElement var1);

    public void report(Problem var1);

    public static class ScopedCollector
    extends Collector
    implements AutoCloseable {
        private final Logger logger;

        public ScopedCollector(Logger $$0) {
            this.logger = $$0;
        }

        public ScopedCollector(PathElement $$0, Logger $$1) {
            super($$0);
            this.logger = $$1;
        }

        @Override
        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", (Object)this.logger.getName(), (Object)this.getTreeReport());
            }
        }
    }

    public static class Collector
    implements ProblemReporter {
        public static final PathElement EMPTY_ROOT = () -> "";
        private final @Nullable Collector parent;
        private final PathElement element;
        private final Set<Entry> problems;

        public Collector() {
            this(EMPTY_ROOT);
        }

        public Collector(PathElement $$0) {
            this.parent = null;
            this.problems = new LinkedHashSet<Entry>();
            this.element = $$0;
        }

        private Collector(Collector $$0, PathElement $$1) {
            this.problems = $$0.problems;
            this.parent = $$0;
            this.element = $$1;
        }

        @Override
        public ProblemReporter forChild(PathElement $$0) {
            return new Collector(this, $$0);
        }

        @Override
        public void report(Problem $$0) {
            this.problems.add(new Entry(this, $$0));
        }

        public boolean isEmpty() {
            return this.problems.isEmpty();
        }

        public void forEach(BiConsumer<String, Problem> $$0) {
            ArrayList<PathElement> $$1 = new ArrayList<PathElement>();
            StringBuilder $$2 = new StringBuilder();
            for (Entry $$3 : this.problems) {
                Collector $$4 = $$3.source;
                while ($$4 != null) {
                    $$1.add($$4.element);
                    $$4 = $$4.parent;
                }
                for (int $$5 = $$1.size() - 1; $$5 >= 0; --$$5) {
                    $$2.append(((PathElement)$$1.get($$5)).get());
                }
                $$0.accept($$2.toString(), $$3.problem());
                $$2.setLength(0);
                $$1.clear();
            }
        }

        public String getReport() {
            HashMultimap $$02 = HashMultimap.create();
            this.forEach((arg_0, arg_1) -> ((Multimap)$$02).put(arg_0, arg_1));
            return $$02.asMap().entrySet().stream().map($$0 -> " at " + (String)$$0.getKey() + ": " + ((Collection)$$0.getValue()).stream().map(Problem::description).collect(Collectors.joining("; "))).collect(Collectors.joining("\n"));
        }

        public String getTreeReport() {
            ArrayList<PathElement> $$0 = new ArrayList<PathElement>();
            ProblemTreeNode $$1 = new ProblemTreeNode(this.element);
            for (Entry $$2 : this.problems) {
                Collector $$3 = $$2.source;
                while ($$3 != this) {
                    $$0.add($$3.element);
                    $$3 = $$3.parent;
                }
                ProblemTreeNode $$4 = $$1;
                for (int $$5 = $$0.size() - 1; $$5 >= 0; --$$5) {
                    $$4 = $$4.child((PathElement)$$0.get($$5));
                }
                $$0.clear();
                $$4.problems.add($$2.problem);
            }
            return String.join((CharSequence)"\n", $$1.getLines());
        }

        static final class Entry
        extends Record {
            final Collector source;
            final Problem problem;

            Entry(Collector $$0, Problem $$1) {
                this.source = $$0;
                this.problem = $$1;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            @Override
            public final boolean equals(Object $$0) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this, $$0);
            }

            public Collector source() {
                return this.source;
            }

            public Problem problem() {
                return this.problem;
            }
        }

        static final class ProblemTreeNode
        extends Record {
            private final PathElement element;
            final List<Problem> problems;
            private final Map<PathElement, ProblemTreeNode> children;

            public ProblemTreeNode(PathElement $$0) {
                this($$0, new ArrayList<Problem>(), new LinkedHashMap<PathElement, ProblemTreeNode>());
            }

            private ProblemTreeNode(PathElement $$0, List<Problem> $$1, Map<PathElement, ProblemTreeNode> $$2) {
                this.element = $$0;
                this.problems = $$1;
                this.children = $$2;
            }

            public ProblemTreeNode child(PathElement $$0) {
                return this.children.computeIfAbsent($$0, ProblemTreeNode::new);
            }

            public List<String> getLines() {
                int $$02 = this.problems.size();
                int $$12 = this.children.size();
                if ($$02 == 0 && $$12 == 0) {
                    return List.of();
                }
                if ($$02 == 0 && $$12 == 1) {
                    ArrayList<String> $$22 = new ArrayList<String>();
                    this.children.forEach(($$1, $$2) -> $$22.addAll($$2.getLines()));
                    $$22.set(0, this.element.get() + (String)$$22.get(0));
                    return $$22;
                }
                if ($$02 == 1 && $$12 == 0) {
                    return List.of(this.element.get() + ": " + this.problems.getFirst().description());
                }
                ArrayList<String> $$3 = new ArrayList<String>();
                this.children.forEach(($$1, $$2) -> $$3.addAll($$2.getLines()));
                $$3.replaceAll($$0 -> "  " + $$0);
                for (Problem $$4 : this.problems) {
                    $$3.add("  " + $$4.description());
                }
                $$3.addFirst(this.element.get() + ":");
                return $$3;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            @Override
            public final boolean equals(Object $$0) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this, $$0);
            }

            public PathElement element() {
                return this.element;
            }

            public List<Problem> problems() {
                return this.problems;
            }

            public Map<PathElement, ProblemTreeNode> children() {
                return this.children;
            }
        }
    }

    public record ElementReferencePathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "->{" + String.valueOf(this.id.identifier()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record IndexedPathElement(int index) implements PathElement
    {
        @Override
        public String get() {
            return "[" + this.index + "]";
        }
    }

    public record IndexedFieldPathElement(String name, int index) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name + "[" + this.index + "]";
        }
    }

    public record FieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name;
        }
    }

    public record RootElementPathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "{" + String.valueOf(this.id.identifier()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record RootFieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return this.name;
        }
    }

    @FunctionalInterface
    public static interface PathElement {
        public String get();
    }

    public static interface Problem {
        public String description();
    }
}

