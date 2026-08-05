package net.minecraft.world.level.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Node {
   public final int x;
   public final int y;
   public final int z;
   private final int hash;
   public int heapIdx = -1;
   public float g;
   public float h;
   public float f;
   
   public Node cameFrom;
   public boolean closed;
   public float walkedDistance;
   public float costMalus;
   public PathType type = PathType.BLOCKED;

   public Node(int $$0, int $$1, int $$2) {
      this.x = $$0;
      this.y = $$1;
      this.z = $$2;
      this.hash = createHash($$0, $$1, $$2);
   }

   public Node cloneAndMove(int $$0, int $$1, int $$2) {
      Node $$3 = new Node($$0, $$1, $$2);
      $$3.heapIdx = this.heapIdx;
      $$3.g = this.g;
      $$3.h = this.h;
      $$3.f = this.f;
      $$3.cameFrom = this.cameFrom;
      $$3.closed = this.closed;
      $$3.walkedDistance = this.walkedDistance;
      $$3.costMalus = this.costMalus;
      $$3.type = this.type;
      return $$3;
   }

   public static int createHash(int $$0, int $$1, int $$2) {
      return $$1 & 0xFF | ($$0 & 32767) << 8 | ($$2 & 32767) << 24 | ($$0 < 0 ? Integer.MIN_VALUE : 0) | ($$2 < 0 ? 32768 : 0);
   }

   public float distanceTo(Node $$0) {
      float $$1 = $$0.x - this.x;
      float $$2 = $$0.y - this.y;
      float $$3 = $$0.z - this.z;
      return Mth.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
   }

   public float distanceToXZ(Node $$0) {
      float $$1 = $$0.x - this.x;
      float $$2 = $$0.z - this.z;
      return Mth.sqrt($$1 * $$1 + $$2 * $$2);
   }

   public float distanceTo(BlockPos $$0) {
      float $$1 = $$0.getX() - this.x;
      float $$2 = $$0.getY() - this.y;
      float $$3 = $$0.getZ() - this.z;
      return Mth.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
   }

   public float distanceToSqr(Node $$0) {
      float $$1 = $$0.x - this.x;
      float $$2 = $$0.y - this.y;
      float $$3 = $$0.z - this.z;
      return $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
   }

   public float distanceToSqr(BlockPos $$0) {
      float $$1 = $$0.getX() - this.x;
      float $$2 = $$0.getY() - this.y;
      float $$3 = $$0.getZ() - this.z;
      return $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
   }

   public float distanceManhattan(Node $$0) {
      float $$1 = Math.abs($$0.x - this.x);
      float $$2 = Math.abs($$0.y - this.y);
      float $$3 = Math.abs($$0.z - this.z);
      return $$1 + $$2 + $$3;
   }

   public float distanceManhattan(BlockPos $$0) {
      float $$1 = Math.abs($$0.getX() - this.x);
      float $$2 = Math.abs($$0.getY() - this.y);
      float $$3 = Math.abs($$0.getZ() - this.z);
      return $$1 + $$2 + $$3;
   }

   public BlockPos asBlockPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public Vec3 asVec3() {
      return new Vec3(this.x, this.y, this.z);
   }

   @Override
   public boolean equals(Object $$0) {
      return !($$0 instanceof Node $$1) ? false : this.hash == $$1.hash && this.x == $$1.x && this.y == $$1.y && this.z == $$1.z;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   public boolean inOpenSet() {
      return this.heapIdx >= 0;
   }

   @Override
   public String toString() {
      return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   public void writeToStream(FriendlyByteBuf $$0) {
      $$0.writeInt(this.x);
      $$0.writeInt(this.y);
      $$0.writeInt(this.z);
      $$0.writeFloat(this.walkedDistance);
      $$0.writeFloat(this.costMalus);
      $$0.writeBoolean(this.closed);
      $$0.writeEnum(this.type);
      $$0.writeFloat(this.f);
   }

   public static Node createFromStream(FriendlyByteBuf $$0) {
      Node $$1 = new Node($$0.readInt(), $$0.readInt(), $$0.readInt());
      readContents($$0, $$1);
      return $$1;
   }

   protected static void readContents(FriendlyByteBuf $$0, Node $$1) {
      $$1.walkedDistance = $$0.readFloat();
      $$1.costMalus = $$0.readFloat();
      $$1.closed = $$0.readBoolean();
      $$1.type = (PathType)$$0.readEnum(PathType.class);
      $$1.f = $$0.readFloat();
   }
}
