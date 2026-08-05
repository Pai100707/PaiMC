/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Stack
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
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

    private static VisibilityRule evaluateVisibilityRule(Advancement $$0, boolean $$1) {
        Optional<DisplayInfo> $$2 = $$0.display();
        if ($$2.isEmpty()) {
            return VisibilityRule.HIDE;
        }
        if ($$1) {
            return VisibilityRule.SHOW;
        }
        if ($$2.get().isHidden()) {
            return VisibilityRule.HIDE;
        }
        return VisibilityRule.NO_CHANGE;
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<VisibilityRule> $$0) {
        for (int $$1 = 0; $$1 <= 2; ++$$1) {
            VisibilityRule $$2 = (VisibilityRule)((Object)$$0.peek($$1));
            if ($$2 == VisibilityRule.SHOW) {
                return true;
            }
            if ($$2 != VisibilityRule.HIDE) continue;
            return false;
        }
        return false;
    }

    private static boolean evaluateVisibility(AdvancementNode $$0, Stack<VisibilityRule> $$1, Predicate<AdvancementNode> $$2, Output $$3) {
        boolean $$4 = $$2.test($$0);
        VisibilityRule $$5 = AdvancementVisibilityEvaluator.evaluateVisibilityRule($$0.advancement(), $$4);
        boolean $$6 = $$4;
        $$1.push((Object)$$5);
        for (AdvancementNode $$7 : $$0.children()) {
            $$6 |= AdvancementVisibilityEvaluator.evaluateVisibility($$7, $$1, $$2, $$3);
        }
        boolean $$8 = $$6 || AdvancementVisibilityEvaluator.evaluateVisiblityForUnfinishedNode($$1);
        $$1.pop();
        $$3.accept($$0, $$8);
        return $$6;
    }

    public static void evaluateVisibility(AdvancementNode $$0, Predicate<AdvancementNode> $$1, Output $$2) {
        AdvancementNode $$3 = $$0.root();
        ObjectArrayList $$4 = new ObjectArrayList();
        for (int $$5 = 0; $$5 <= 2; ++$$5) {
            $$4.push((Object)VisibilityRule.NO_CHANGE);
        }
        AdvancementVisibilityEvaluator.evaluateVisibility($$3, (Stack<VisibilityRule>)$$4, $$1, $$2);
    }

    static enum VisibilityRule {
        SHOW,
        HIDE,
        NO_CHANGE;

    }

    @FunctionalInterface
    public static interface Output {
        public void accept(AdvancementNode var1, boolean var2);
    }
}

