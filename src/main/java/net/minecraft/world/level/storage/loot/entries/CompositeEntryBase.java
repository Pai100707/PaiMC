package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.util.ProblemReporter.Problem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
   public static final Problem NO_CHILDREN_PROBLEM = new Problem() {
      public String description() {
         return "Empty children list";
      }
   };
   protected final List<LootPoolEntryContainer> children;
   private final ComposableEntryContainer composedChildren;

   protected CompositeEntryBase(List<LootPoolEntryContainer> $$0, List<LootItemCondition> $$1) {
      super($$1);
      this.children = $$0;
      this.composedChildren = this.compose($$0);
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);
      if (this.children.isEmpty()) {
         $$0.reportProblem(NO_CHILDREN_PROBLEM);
      }

      for (int $$1 = 0; $$1 < this.children.size(); $$1++) {
         this.children.get($$1).validate($$0.forChild(new IndexedFieldPathElement("children", $$1)));
      }
   }

   protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> var1);

   @Override
   public final boolean expand(LootContext $$0, Consumer<LootPoolEntry> $$1) {
      return !this.canRun($$0) ? false : this.composedChildren.expand($$0, $$1);
   }

   public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryBase.CompositeEntryConstructor<T> $$0) {
      return RecordCodecBuilder.mapCodec(
         $$1 -> $$1.group(LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter($$0xx -> $$0xx.children))
            .and(commonFields($$1).t1())
            .apply($$1, $$0::create)
      );
   }

   @FunctionalInterface
   public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
      T create(List<LootPoolEntryContainer> var1, List<LootItemCondition> var2);
   }
}
