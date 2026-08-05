/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.core;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public enum Direction8 {
    NORTH(Direction.NORTH),
    NORTH_EAST(Direction.NORTH, Direction.EAST),
    EAST(Direction.EAST),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    SOUTH(Direction.SOUTH),
    SOUTH_WEST(Direction.SOUTH, Direction.WEST),
    WEST(Direction.WEST),
    NORTH_WEST(Direction.NORTH, Direction.WEST);

    private final Set<Direction> directions;
    private final Vec3i step;

    private Direction8(Direction ... $$0) {
        this.directions = Sets.immutableEnumSet(Arrays.asList($$0));
        this.step = new Vec3i(0, 0, 0);
        for (Direction $$1 : $$0) {
            this.step.setX(this.step.getX() + $$1.getStepX()).setY(this.step.getY() + $$1.getStepY()).setZ(this.step.getZ() + $$1.getStepZ());
        }
    }

    public Set<Direction> getDirections() {
        return this.directions;
    }

    public int getStepX() {
        return this.step.getX();
    }

    public int getStepZ() {
        return this.step.getZ();
    }
}

