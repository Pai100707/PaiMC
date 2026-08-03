package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockBox(net.minecraft.core.BlockPos min, net.minecraft.core.BlockPos max) implements Iterable<net.minecraft.core.BlockPos> {
   public static final StreamCodec<ByteBuf, net.minecraft.core.BlockBox> STREAM_CODEC = new StreamCodec<ByteBuf, net.minecraft.core.BlockBox>() {
      public net.minecraft.core.BlockBox decode(ByteBuf $$0) {
         return new net.minecraft.core.BlockBox(FriendlyByteBuf.readBlockPos($$0), FriendlyByteBuf.readBlockPos($$0));
      }

      public void encode(ByteBuf $$0, net.minecraft.core.BlockBox $$1) {
         FriendlyByteBuf.writeBlockPos($$0, $$1.min());
         FriendlyByteBuf.writeBlockPos($$0, $$1.max());
      }
   };

   public BlockBox(final net.minecraft.core.BlockPos min, final net.minecraft.core.BlockPos max) {
      this.min = net.minecraft.core.BlockPos.min(min, max);
      this.max = net.minecraft.core.BlockPos.max(min, max);
   }

   public static net.minecraft.core.BlockBox of(net.minecraft.core.BlockPos $$0) {
      return new net.minecraft.core.BlockBox($$0, $$0);
   }

   public static net.minecraft.core.BlockBox of(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1) {
      return new net.minecraft.core.BlockBox($$0, $$1);
   }

   public net.minecraft.core.BlockBox include(net.minecraft.core.BlockPos $$0) {
      return new net.minecraft.core.BlockBox(net.minecraft.core.BlockPos.min(this.min, $$0), net.minecraft.core.BlockPos.max(this.max, $$0));
   }

   public boolean isBlock() {
      return this.min.equals(this.max);
   }

   public boolean contains(net.minecraft.core.BlockPos $$0) {
      return $$0.getX() >= this.min.getX()
         && $$0.getY() >= this.min.getY()
         && $$0.getZ() >= this.min.getZ()
         && $$0.getX() <= this.max.getX()
         && $$0.getY() <= this.max.getY()
         && $$0.getZ() <= this.max.getZ();
   }

   public AABB aabb() {
      return AABB.encapsulatingFullBlocks(this.min, this.max);
   }

   @Override
   public Iterator<net.minecraft.core.BlockPos> iterator() {
      return net.minecraft.core.BlockPos.betweenClosed(this.min, this.max).iterator();
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

   public net.minecraft.core.BlockBox extend(net.minecraft.core.Direction $$0, int $$1) {
      if ($$1 == 0) {
         return this;
      } else {
         return $$0.getAxisDirection() == net.minecraft.core.Direction.AxisDirection.POSITIVE
            ? of(this.min, net.minecraft.core.BlockPos.max(this.min, this.max.relative($$0, $$1)))
            : of(net.minecraft.core.BlockPos.min(this.min.relative($$0, $$1), this.max), this.max);
      }
   }

   public net.minecraft.core.BlockBox move(net.minecraft.core.Direction $$0, int $$1) {
      return $$1 == 0 ? this : new net.minecraft.core.BlockBox(this.min.relative($$0, $$1), this.max.relative($$0, $$1));
   }

   public net.minecraft.core.BlockBox offset(net.minecraft.core.Vec3i $$0) {
      return new net.minecraft.core.BlockBox(this.min.offset($$0), this.max.offset($$0));
   }
}
