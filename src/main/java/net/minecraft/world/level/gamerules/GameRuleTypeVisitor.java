package net.minecraft.world.level.gamerules;

public interface GameRuleTypeVisitor {
   default <T> void visit(GameRule<T> $$0) {
   }

   default void visitBoolean(GameRule<Boolean> $$0) {
   }

   default void visitInteger(GameRule<Integer> $$0) {
   }
}
