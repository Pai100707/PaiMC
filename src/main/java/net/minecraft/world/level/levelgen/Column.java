package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Column {
   public static Column.Range around(int $$0, int $$1) {
      return new Column.Range($$0 - 1, $$1 + 1);
   }

   public static Column.Range inside(int $$0, int $$1) {
      return new Column.Range($$0, $$1);
   }

   public static Column below(int $$0) {
      return new Column.Ray($$0, false);
   }

   public static Column fromHighest(int $$0) {
      return new Column.Ray($$0 + 1, false);
   }

   public static Column above(int $$0) {
      return new Column.Ray($$0, true);
   }

   public static Column fromLowest(int $$0) {
      return new Column.Ray($$0 - 1, true);
   }

   public static Column line() {
      return Column.Line.INSTANCE;
   }

   public static Column create(OptionalInt $$0, OptionalInt $$1) {
      if ($$0.isPresent() && $$1.isPresent()) {
         return inside($$0.getAsInt(), $$1.getAsInt());
      } else if ($$0.isPresent()) {
         return above($$0.getAsInt());
      } else {
         return $$1.isPresent() ? below($$1.getAsInt()) : line();
      }
   }

   public abstract OptionalInt getCeiling();

   public abstract OptionalInt getFloor();

   public abstract OptionalInt getHeight();

   public Column withFloor(OptionalInt $$0) {
      return create($$0, this.getCeiling());
   }

   public Column withCeiling(OptionalInt $$0) {
      return create(this.getFloor(), $$0);
   }

   public static Optional<Column> scan(
      net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1, int $$2, Predicate<BlockState> $$3, Predicate<BlockState> $$4
   ) {
      MutableBlockPos $$5 = $$1.mutable();
      if (!$$0.isStateAtPosition($$1, $$3)) {
         return Optional.empty();
      } else {
         int $$6 = $$1.getY();
         OptionalInt $$7 = scanDirection($$0, $$2, $$3, $$4, $$5, $$6, Direction.UP);
         OptionalInt $$8 = scanDirection($$0, $$2, $$3, $$4, $$5, $$6, Direction.DOWN);
         return Optional.of(create($$8, $$7));
      }
   }

   private static OptionalInt scanDirection(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      int $$1,
      Predicate<BlockState> $$2,
      Predicate<BlockState> $$3,
      MutableBlockPos $$4,
      int $$5,
      Direction $$6
   ) {
      $$4.setY($$5);

      for (int $$7 = 1; $$7 < $$1 && $$0.isStateAtPosition($$4, $$2); $$7++) {
         $$4.move($$6);
      }

      return $$0.isStateAtPosition($$4, $$3) ? OptionalInt.of($$4.getY()) : OptionalInt.empty();
   }

   public static final class Line extends Column {
      static final Column.Line INSTANCE = new Column.Line();

      private Line() {
      }

      @Override
      public OptionalInt getCeiling() {
         return OptionalInt.empty();
      }

      @Override
      public OptionalInt getFloor() {
         return OptionalInt.empty();
      }

      @Override
      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      @Override
      public String toString() {
         return "C(-)";
      }
   }

   public static final class Range extends Column {
      private final int floor;
      private final int ceiling;

      protected Range(int $$0, int $$1) {
         this.floor = $$0;
         this.ceiling = $$1;
         if (this.height() < 0) {
            throw new IllegalArgumentException("Column of negative height: " + this);
         }
      }

      @Override
      public OptionalInt getCeiling() {
         return OptionalInt.of(this.ceiling);
      }

      @Override
      public OptionalInt getFloor() {
         return OptionalInt.of(this.floor);
      }

      @Override
      public OptionalInt getHeight() {
         return OptionalInt.of(this.height());
      }

      public int ceiling() {
         return this.ceiling;
      }

      public int floor() {
         return this.floor;
      }

      public int height() {
         return this.ceiling - this.floor - 1;
      }

      @Override
      public String toString() {
         return "C(" + this.ceiling + "-" + this.floor + ")";
      }
   }

   public static final class Ray extends Column {
      private final int edge;
      private final boolean pointingUp;

      public Ray(int $$0, boolean $$1) {
         this.edge = $$0;
         this.pointingUp = $$1;
      }

      @Override
      public OptionalInt getCeiling() {
         return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
      }

      @Override
      public OptionalInt getFloor() {
         return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
      }

      @Override
      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      @Override
      public String toString() {
         return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
      }
   }
}
