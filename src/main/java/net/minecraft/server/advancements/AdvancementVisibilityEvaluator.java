package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
   private static final int VISIBILITY_DEPTH = 2;

   private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement $$0, boolean $$1) {
      Optional<DisplayInfo> $$2 = $$0.display();
      if ($$2.isEmpty()) {
         return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
      } else if ($$1) {
         return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
      } else {
         return $$2.get().isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
      }
   }

   private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> $$0) {
      for (int $$1 = 0; $$1 <= 2; $$1++) {
         AdvancementVisibilityEvaluator.VisibilityRule $$2 = (AdvancementVisibilityEvaluator.VisibilityRule)$$0.peek($$1);
         if ($$2 == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
            return true;
         }

         if ($$2 == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
            return false;
         }
      }

      return false;
   }

   private static boolean evaluateVisibility(
      AdvancementNode $$0, Stack<AdvancementVisibilityEvaluator.VisibilityRule> $$1, Predicate<AdvancementNode> $$2, AdvancementVisibilityEvaluator.Output $$3
   ) {
      boolean $$4 = $$2.test($$0);
      AdvancementVisibilityEvaluator.VisibilityRule $$5 = evaluateVisibilityRule($$0.advancement(), $$4);
      boolean $$6 = $$4;
      $$1.push($$5);

      for (AdvancementNode $$7 : $$0.children()) {
         $$6 |= evaluateVisibility($$7, $$1, $$2, $$3);
      }

      boolean $$8 = $$6 || evaluateVisiblityForUnfinishedNode($$1);
      $$1.pop();
      $$3.accept($$0, $$8);
      return $$6;
   }

   public static void evaluateVisibility(AdvancementNode $$0, Predicate<AdvancementNode> $$1, AdvancementVisibilityEvaluator.Output $$2) {
      AdvancementNode $$3 = $$0.root();
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> $$4 = new ObjectArrayList();

      for (int $$5 = 0; $$5 <= 2; $$5++) {
         $$4.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }

      evaluateVisibility($$3, $$4, $$1, $$2);
   }

   @FunctionalInterface
   public interface Output {
      void accept(AdvancementNode var1, boolean var2);
   }

   static enum VisibilityRule {
      SHOW,
      HIDE,
      NO_CHANGE;
   }
}
