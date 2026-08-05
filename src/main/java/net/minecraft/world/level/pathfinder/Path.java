package net.minecraft.world.level.pathfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Path {
   public static final StreamCodec<FriendlyByteBuf, Path> STREAM_CODEC = StreamCodec.of(($$0, $$1) -> $$1.writeToStream($$0), Path::createFromStream);
   private final List<Node> nodes;
   
   private Path.DebugData debugData;
   private int nextNodeIndex;
   private final BlockPos target;
   private final float distToTarget;
   private final boolean reached;

   public Path(List<Node> $$0, BlockPos $$1, boolean $$2) {
      this.nodes = $$0;
      this.target = $$1;
      this.distToTarget = $$0.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
      this.reached = $$2;
   }

   public void advance() {
      this.nextNodeIndex++;
   }

   public boolean notStarted() {
      return this.nextNodeIndex <= 0;
   }

   public boolean isDone() {
      return this.nextNodeIndex >= this.nodes.size();
   }

   
   public Node getEndNode() {
      return !this.nodes.isEmpty() ? this.nodes.get(this.nodes.size() - 1) : null;
   }

   public Node getNode(int $$0) {
      return this.nodes.get($$0);
   }

   public void truncateNodes(int $$0) {
      if (this.nodes.size() > $$0) {
         this.nodes.subList($$0, this.nodes.size()).clear();
      }
   }

   public void replaceNode(int $$0, Node $$1) {
      this.nodes.set($$0, $$1);
   }

   public int getNodeCount() {
      return this.nodes.size();
   }

   public int getNextNodeIndex() {
      return this.nextNodeIndex;
   }

   public void setNextNodeIndex(int $$0) {
      this.nextNodeIndex = $$0;
   }

   public Vec3 getEntityPosAtNode(Entity $$0, int $$1) {
      Node $$2 = this.nodes.get($$1);
      double $$3 = $$2.x + (int)($$0.getBbWidth() + 1.0F) * 0.5;
      double $$4 = $$2.y;
      double $$5 = $$2.z + (int)($$0.getBbWidth() + 1.0F) * 0.5;
      return new Vec3($$3, $$4, $$5);
   }

   public BlockPos getNodePos(int $$0) {
      return this.nodes.get($$0).asBlockPos();
   }

   public Vec3 getNextEntityPos(Entity $$0) {
      return this.getEntityPosAtNode($$0, this.nextNodeIndex);
   }

   public BlockPos getNextNodePos() {
      return this.nodes.get(this.nextNodeIndex).asBlockPos();
   }

   public Node getNextNode() {
      return this.nodes.get(this.nextNodeIndex);
   }

   
   public Node getPreviousNode() {
      return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
   }

   public boolean sameAs(Path $$0) {
      return $$0 != null && this.nodes.equals($$0.nodes);
   }

   @Override
   public boolean equals(Object $$0) {
      return !($$0 instanceof Path $$1)
         ? false
         : this.nextNodeIndex == $$1.nextNodeIndex
            && this.debugData == $$1.debugData
            && this.reached == $$1.reached
            && this.target.equals($$1.target)
            && this.nodes.equals($$1.nodes);
   }

   @Override
   public int hashCode() {
      return this.nextNodeIndex + this.nodes.hashCode() * 31;
   }

   public boolean canReach() {
      return this.reached;
   }

   @VisibleForDebug
   void setDebug(Node[] $$0, Node[] $$1, Set<Target> $$2) {
      this.debugData = new Path.DebugData($$0, $$1, $$2);
   }

   
   public Path.DebugData debugData() {
      return this.debugData;
   }

   public void writeToStream(FriendlyByteBuf $$0) {
      if (this.debugData != null && !this.debugData.targetNodes.isEmpty()) {
         $$0.writeBoolean(this.reached);
         $$0.writeInt(this.nextNodeIndex);
         $$0.writeBlockPos(this.target);
         $$0.writeCollection(this.nodes, ($$0x, $$1) -> $$1.writeToStream($$0x));
         this.debugData.write($$0);
      } else {
         throw new IllegalStateException("Missing debug data");
      }
   }

   public static Path createFromStream(FriendlyByteBuf $$0) {
      boolean $$1 = $$0.readBoolean();
      int $$2 = $$0.readInt();
      BlockPos $$3 = $$0.readBlockPos();
      List<Node> $$4 = $$0.readList(Node::createFromStream);
      Path.DebugData $$5 = Path.DebugData.read($$0);
      Path $$6 = new Path($$4, $$3, $$1);
      $$6.debugData = $$5;
      $$6.nextNodeIndex = $$2;
      return $$6;
   }

   @Override
   public String toString() {
      return "Path(length=" + this.nodes.size() + ")";
   }

   public BlockPos getTarget() {
      return this.target;
   }

   public float getDistToTarget() {
      return this.distToTarget;
   }

   static Node[] readNodeArray(FriendlyByteBuf $$0) {
      Node[] $$1 = new Node[$$0.readVarInt()];

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         $$1[$$2] = Node.createFromStream($$0);
      }

      return $$1;
   }

   static void writeNodeArray(FriendlyByteBuf $$0, Node[] $$1) {
      $$0.writeVarInt($$1.length);

      for (Node $$2 : $$1) {
         $$2.writeToStream($$0);
      }
   }

   public Path copy() {
      Path $$0 = new Path(this.nodes, this.target, this.reached);
      $$0.debugData = this.debugData;
      $$0.nextNodeIndex = this.nextNodeIndex;
      return $$0;
   }

   public record DebugData(Node[] openSet, Node[] closedSet, Set<Target> targetNodes) {

      public void write(FriendlyByteBuf $$0) {
         $$0.writeCollection(this.targetNodes, ($$0x, $$1) -> $$1.writeToStream($$0x));
         Path.writeNodeArray($$0, this.openSet);
         Path.writeNodeArray($$0, this.closedSet);
      }

      public static Path.DebugData read(FriendlyByteBuf $$0) {
         HashSet<Target> $$1 = (HashSet<Target>)$$0.readCollection(HashSet::new, Target::createFromStream);
         Node[] $$2 = Path.readNodeArray($$0);
         Node[] $$3 = Path.readNodeArray($$0);
         return new Path.DebugData($$2, $$3, $$1);
      }
   }
}
