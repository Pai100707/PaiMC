/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record LocalCoordinates(double left, double up, double forwards) implements Coordinates
{
    public static final char PREFIX_LOCAL_COORDINATE = '^';

    @Override
    public Vec3 getPosition(CommandSourceStack $$0) {
        Vec3 $$1 = $$0.getAnchor().apply($$0);
        return Vec3.applyLocalCoordinatesToRotation($$0.getRotation(), new Vec3(this.left, this.up, this.forwards)).add($$1.x, $$1.y, $$1.z);
    }

    @Override
    public Vec2 getRotation(CommandSourceStack $$0) {
        return Vec2.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return true;
    }

    @Override
    public boolean isYRelative() {
        return true;
    }

    @Override
    public boolean isZRelative() {
        return true;
    }

    public static LocalCoordinates parse(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        double $$2 = LocalCoordinates.readDouble($$0, $$1);
        if (!$$0.canRead() || $$0.peek() != ' ') {
            $$0.setCursor($$1);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)$$0);
        }
        $$0.skip();
        double $$3 = LocalCoordinates.readDouble($$0, $$1);
        if (!$$0.canRead() || $$0.peek() != ' ') {
            $$0.setCursor($$1);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)$$0);
        }
        $$0.skip();
        double $$4 = LocalCoordinates.readDouble($$0, $$1);
        return new LocalCoordinates($$2, $$3, $$4);
    }

    private static double readDouble(StringReader $$0, int $$1) throws CommandSyntaxException {
        if (!$$0.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext((ImmutableStringReader)$$0);
        }
        if ($$0.peek() != '^') {
            $$0.setCursor($$1);
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext((ImmutableStringReader)$$0);
        }
        $$0.skip();
        return $$0.canRead() && $$0.peek() != ' ' ? $$0.readDouble() : 0.0;
    }
}

