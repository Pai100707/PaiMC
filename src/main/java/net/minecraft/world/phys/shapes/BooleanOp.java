package net.minecraft.world.phys.shapes;

public interface BooleanOp {
   BooleanOp FALSE = ($$0, $$1) -> false;
   BooleanOp NOT_OR = ($$0, $$1) -> !$$0 && !$$1;
   BooleanOp ONLY_SECOND = ($$0, $$1) -> $$1 && !$$0;
   BooleanOp NOT_FIRST = ($$0, $$1) -> !$$0;
   BooleanOp ONLY_FIRST = ($$0, $$1) -> $$0 && !$$1;
   BooleanOp NOT_SECOND = ($$0, $$1) -> !$$1;
   BooleanOp NOT_SAME = ($$0, $$1) -> $$0 != $$1;
   BooleanOp NOT_AND = ($$0, $$1) -> !$$0 || !$$1;
   BooleanOp AND = ($$0, $$1) -> $$0 && $$1;
   BooleanOp SAME = ($$0, $$1) -> $$0 == $$1;
   BooleanOp SECOND = ($$0, $$1) -> $$1;
   BooleanOp CAUSES = ($$0, $$1) -> !$$0 || $$1;
   BooleanOp FIRST = ($$0, $$1) -> $$0;
   BooleanOp CAUSED_BY = ($$0, $$1) -> $$0 || !$$1;
   BooleanOp OR = ($$0, $$1) -> $$0 || $$1;
   BooleanOp TRUE = ($$0, $$1) -> true;

   boolean apply(boolean var1, boolean var2);
}
