package net.minecraft.core;

public enum AxisCycle {
   NONE {
      @Override
      public int cycle(int $$0, int $$1, int $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$0, $$1, $$2);
      }

      @Override
      public double cycle(double $$0, double $$1, double $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$0, $$1, $$2);
      }

      @Override
      public net.minecraft.core.Direction.Axis cycle(net.minecraft.core.Direction.Axis $$0) {
         return $$0;
      }

      @Override
      public net.minecraft.core.AxisCycle inverse() {
         return this;
      }
   },
   FORWARD {
      @Override
      public int cycle(int $$0, int $$1, int $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$2, $$0, $$1);
      }

      @Override
      public double cycle(double $$0, double $$1, double $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$2, $$0, $$1);
      }

      @Override
      public net.minecraft.core.Direction.Axis cycle(net.minecraft.core.Direction.Axis $$0) {
         return AXIS_VALUES[Math.floorMod($$0.ordinal() + 1, 3)];
      }

      @Override
      public net.minecraft.core.AxisCycle inverse() {
         return BACKWARD;
      }
   },
   BACKWARD {
      @Override
      public int cycle(int $$0, int $$1, int $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$1, $$2, $$0);
      }

      @Override
      public double cycle(double $$0, double $$1, double $$2, net.minecraft.core.Direction.Axis $$3) {
         return $$3.choose($$1, $$2, $$0);
      }

      @Override
      public net.minecraft.core.Direction.Axis cycle(net.minecraft.core.Direction.Axis $$0) {
         return AXIS_VALUES[Math.floorMod($$0.ordinal() - 1, 3)];
      }

      @Override
      public net.minecraft.core.AxisCycle inverse() {
         return FORWARD;
      }
   };

   public static final net.minecraft.core.Direction.Axis[] AXIS_VALUES = net.minecraft.core.Direction.Axis.values();
   public static final net.minecraft.core.AxisCycle[] VALUES = values();

   public abstract int cycle(int var1, int var2, int var3, net.minecraft.core.Direction.Axis var4);

   public abstract double cycle(double var1, double var3, double var5, net.minecraft.core.Direction.Axis var7);

   public abstract net.minecraft.core.Direction.Axis cycle(net.minecraft.core.Direction.Axis var1);

   public abstract net.minecraft.core.AxisCycle inverse();

   public static net.minecraft.core.AxisCycle between(net.minecraft.core.Direction.Axis $$0, net.minecraft.core.Direction.Axis $$1) {
      return VALUES[Math.floorMod($$1.ordinal() - $$0.ordinal(), 3)];
   }
}
