/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import net.minecraft.core.Direction;

public enum AxisCycle {
    NONE{

        @Override
        public int cycle(int $$0, int $$1, int $$2, Direction.Axis $$3) {
            return $$3.choose($$0, $$1, $$2);
        }

        @Override
        public double cycle(double $$0, double $$1, double $$2, Direction.Axis $$3) {
            return $$3.choose($$0, $$1, $$2);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis $$0) {
            return $$0;
        }

        @Override
        public AxisCycle inverse() {
            return this;
        }
    }
    ,
    FORWARD{

        @Override
        public int cycle(int $$0, int $$1, int $$2, Direction.Axis $$3) {
            return $$3.choose($$2, $$0, $$1);
        }

        @Override
        public double cycle(double $$0, double $$1, double $$2, Direction.Axis $$3) {
            return $$3.choose($$2, $$0, $$1);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis $$0) {
            return AXIS_VALUES[Math.floorMod($$0.ordinal() + 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return BACKWARD;
        }
    }
    ,
    BACKWARD{

        @Override
        public int cycle(int $$0, int $$1, int $$2, Direction.Axis $$3) {
            return $$3.choose($$1, $$2, $$0);
        }

        @Override
        public double cycle(double $$0, double $$1, double $$2, Direction.Axis $$3) {
            return $$3.choose($$1, $$2, $$0);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis $$0) {
            return AXIS_VALUES[Math.floorMod($$0.ordinal() - 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return FORWARD;
        }
    };

    public static final Direction.Axis[] AXIS_VALUES;
    public static final AxisCycle[] VALUES;

    public abstract int cycle(int var1, int var2, int var3, Direction.Axis var4);

    public abstract double cycle(double var1, double var3, double var5, Direction.Axis var7);

    public abstract Direction.Axis cycle(Direction.Axis var1);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Direction.Axis $$0, Direction.Axis $$1) {
        return VALUES[Math.floorMod($$1.ordinal() - $$0.ordinal(), 3)];
    }

    static {
        AXIS_VALUES = Direction.Axis.values();
        VALUES = AxisCycle.values();
    }
}

