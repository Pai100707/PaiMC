package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NodeEvaluator {
   protected PathfindingContext currentContext;
   protected Mob mob;
   protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap();
   protected int entityWidth;
   protected int entityHeight;
   protected int entityDepth;
   protected boolean canPassDoors = true;
   protected boolean canOpenDoors;
   protected boolean canFloat;
   protected boolean canWalkOverFences;

   public void prepare(net.minecraft.world.level.PathNavigationRegion $$0, Mob $$1) {
      this.currentContext = new PathfindingContext($$0, $$1);
      this.mob = $$1;
      this.nodes.clear();
      this.entityWidth = Mth.floor($$1.getBbWidth() + 1.0F);
      this.entityHeight = Mth.floor($$1.getBbHeight() + 1.0F);
      this.entityDepth = Mth.floor($$1.getBbWidth() + 1.0F);
   }

   public void done() {
      this.currentContext = null;
      this.mob = null;
   }

   protected Node getNode(BlockPos $$0) {
      return this.getNode($$0.getX(), $$0.getY(), $$0.getZ());
   }

   protected Node getNode(int $$0, int $$1, int $$2) {
      return (Node)this.nodes.computeIfAbsent(Node.createHash($$0, $$1, $$2), $$3 -> new Node($$0, $$1, $$2));
   }

   public abstract Node getStart();

   public abstract Target getTarget(double var1, double var3, double var5);

   protected Target getTargetNodeAt(double $$0, double $$1, double $$2) {
      return new Target(this.getNode(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2)));
   }

   public abstract int getNeighbors(Node[] var1, Node var2);

   public abstract PathType getPathTypeOfMob(PathfindingContext var1, int var2, int var3, int var4, Mob var5);

   public abstract PathType getPathType(PathfindingContext var1, int var2, int var3, int var4);

   public PathType getPathType(Mob $$0, BlockPos $$1) {
      return this.getPathType(new PathfindingContext($$0.level(), $$0), $$1.getX(), $$1.getY(), $$1.getZ());
   }

   public void setCanPassDoors(boolean $$0) {
      this.canPassDoors = $$0;
   }

   public void setCanOpenDoors(boolean $$0) {
      this.canOpenDoors = $$0;
   }

   public void setCanFloat(boolean $$0) {
      this.canFloat = $$0;
   }

   public void setCanWalkOverFences(boolean $$0) {
      this.canWalkOverFences = $$0;
   }

   public boolean canPassDoors() {
      return this.canPassDoors;
   }

   public boolean canOpenDoors() {
      return this.canOpenDoors;
   }

   public boolean canFloat() {
      return this.canFloat;
   }

   public boolean canWalkOverFences() {
      return this.canWalkOverFences;
   }

   public static boolean isBurningBlock(BlockState $$0) {
      return $$0.is(BlockTags.FIRE) || $$0.is(Blocks.LAVA) || $$0.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire($$0) || $$0.is(Blocks.LAVA_CAULDRON);
   }
}
