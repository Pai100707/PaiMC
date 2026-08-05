/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockBox(BlockPos min, BlockPos max) implements Iterable<BlockPos>
{
    public static final StreamCodec<ByteBuf, BlockBox> STREAM_CODEC = new StreamCodec<ByteBuf, BlockBox>(){

        @Override
        public BlockBox decode(ByteBuf $$0) {
            return new BlockBox(FriendlyByteBuf.readBlockPos($$0), FriendlyByteBuf.readBlockPos($$0));
        }

        @Override
        public void encode(ByteBuf $$0, BlockBox $$1) {
            FriendlyByteBuf.writeBlockPos($$0, $$1.min());
            FriendlyByteBuf.writeBlockPos($$0, $$1.max());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockBox)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public BlockBox(BlockPos $$0, BlockPos $$1) {
        this.min = BlockPos.min($$0, $$1);
        this.max = BlockPos.max($$0, $$1);
    }

    public static BlockBox of(BlockPos $$0) {
        return new BlockBox($$0, $$0);
    }

    public static BlockBox of(BlockPos $$0, BlockPos $$1) {
        return new BlockBox($$0, $$1);
    }

    public BlockBox include(BlockPos $$0) {
        return new BlockBox(BlockPos.min(this.min, $$0), BlockPos.max(this.max, $$0));
    }

    public boolean isBlock() {
        return this.min.equals(this.max);
    }

    public boolean contains(BlockPos $$0) {
        return $$0.getX() >= this.min.getX() && $$0.getY() >= this.min.getY() && $$0.getZ() >= this.min.getZ() && $$0.getX() <= this.max.getX() && $$0.getY() <= this.max.getY() && $$0.getZ() <= this.max.getZ();
    }

    public AABB aabb() {
        return AABB.encapsulatingFullBlocks(this.min, this.max);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.betweenClosed(this.min, this.max).iterator();
    }

    public int sizeX() {
        return this.max.getX() - this.min.getX() + 1;
    }

    public int sizeY() {
        return this.max.getY() - this.min.getY() + 1;
    }

    public int sizeZ() {
        return this.max.getZ() - this.min.getZ() + 1;
    }

    public BlockBox extend(Direction $$0, int $$1) {
        if ($$1 == 0) {
            return this;
        }
        if ($$0.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            return BlockBox.of(this.min, BlockPos.max(this.min, this.max.relative($$0, $$1)));
        }
        return BlockBox.of(BlockPos.min(this.min.relative($$0, $$1), this.max), this.max);
    }

    public BlockBox move(Direction $$0, int $$1) {
        if ($$1 == 0) {
            return this;
        }
        return new BlockBox(this.min.relative($$0, $$1), this.max.relative($$0, $$1));
    }

    public BlockBox offset(Vec3i $$0) {
        return new BlockBox(this.min.offset($$0), this.max.offset($$0));
    }
}

