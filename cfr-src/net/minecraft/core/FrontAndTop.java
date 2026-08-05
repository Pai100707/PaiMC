/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;

public enum FrontAndTop implements StringRepresentable
{
    DOWN_EAST("down_east", Direction.DOWN, Direction.EAST),
    DOWN_NORTH("down_north", Direction.DOWN, Direction.NORTH),
    DOWN_SOUTH("down_south", Direction.DOWN, Direction.SOUTH),
    DOWN_WEST("down_west", Direction.DOWN, Direction.WEST),
    UP_EAST("up_east", Direction.UP, Direction.EAST),
    UP_NORTH("up_north", Direction.UP, Direction.NORTH),
    UP_SOUTH("up_south", Direction.UP, Direction.SOUTH),
    UP_WEST("up_west", Direction.UP, Direction.WEST),
    WEST_UP("west_up", Direction.WEST, Direction.UP),
    EAST_UP("east_up", Direction.EAST, Direction.UP),
    NORTH_UP("north_up", Direction.NORTH, Direction.UP),
    SOUTH_UP("south_up", Direction.SOUTH, Direction.UP);

    private static final int NUM_DIRECTIONS;
    private static final FrontAndTop[] BY_TOP_FRONT;
    private final String name;
    private final Direction top;
    private final Direction front;

    private static int lookupKey(Direction $$0, Direction $$1) {
        return $$0.ordinal() * NUM_DIRECTIONS + $$1.ordinal();
    }

    private FrontAndTop(String $$0, Direction $$1, Direction $$2) {
        this.name = $$0;
        this.front = $$1;
        this.top = $$2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static FrontAndTop fromFrontAndTop(Direction $$0, Direction $$1) {
        return BY_TOP_FRONT[FrontAndTop.lookupKey($$0, $$1)];
    }

    public Direction front() {
        return this.front;
    }

    public Direction top() {
        return this.top;
    }

    static {
        NUM_DIRECTIONS = Direction.values().length;
        BY_TOP_FRONT = Util.make(new FrontAndTop[NUM_DIRECTIONS * NUM_DIRECTIONS], $$0 -> {
            FrontAndTop[] frontAndTopArray = FrontAndTop.values();
            int n = frontAndTopArray.length;
            for (int i = 0; i < n; ++i) {
                FrontAndTop $$1;
                $$0[FrontAndTop.lookupKey((Direction)$$1.front, (Direction)$$1.top)] = $$1 = frontAndTopArray[i];
            }
        });
    }
}

