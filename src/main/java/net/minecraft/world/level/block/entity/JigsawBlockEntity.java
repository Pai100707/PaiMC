package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class JigsawBlockEntity extends BlockEntity {
   public static final Codec<ResourceKey<StructureTemplatePool>> POOL_CODEC = ResourceKey.codec(Registries.TEMPLATE_POOL);
   public static final Identifier EMPTY_ID = Identifier.withDefaultNamespace("empty");
   private static final int DEFAULT_PLACEMENT_PRIORITY = 0;
   private static final int DEFAULT_SELECTION_PRIORITY = 0;
   public static final String TARGET = "target";
   public static final String POOL = "pool";
   public static final String JOINT = "joint";
   public static final String PLACEMENT_PRIORITY = "placement_priority";
   public static final String SELECTION_PRIORITY = "selection_priority";
   public static final String NAME = "name";
   public static final String FINAL_STATE = "final_state";
   public static final String DEFAULT_FINAL_STATE = "minecraft:air";
   private Identifier name = EMPTY_ID;
   private Identifier target = EMPTY_ID;
   private ResourceKey<StructureTemplatePool> pool = Pools.EMPTY;
   private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
   private String finalState = "minecraft:air";
   private int placementPriority = 0;
   private int selectionPriority = 0;

   public JigsawBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.JIGSAW, $$0, $$1);
   }

   public Identifier getName() {
      return this.name;
   }

   public Identifier getTarget() {
      return this.target;
   }

   public ResourceKey<StructureTemplatePool> getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JigsawBlockEntity.JointType getJoint() {
      return this.joint;
   }

   public int getPlacementPriority() {
      return this.placementPriority;
   }

   public int getSelectionPriority() {
      return this.selectionPriority;
   }

   public void setName(Identifier $$0) {
      this.name = $$0;
   }

   public void setTarget(Identifier $$0) {
      this.target = $$0;
   }

   public void setPool(ResourceKey<StructureTemplatePool> $$0) {
      this.pool = $$0;
   }

   public void setFinalState(String $$0) {
      this.finalState = $$0;
   }

   public void setJoint(JigsawBlockEntity.JointType $$0) {
      this.joint = $$0;
   }

   public void setPlacementPriority(int $$0) {
      this.placementPriority = $$0;
   }

   public void setSelectionPriority(int $$0) {
      this.selectionPriority = $$0;
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.store("name", Identifier.CODEC, this.name);
      $$0.store("target", Identifier.CODEC, this.target);
      $$0.store("pool", POOL_CODEC, this.pool);
      $$0.putString("final_state", this.finalState);
      $$0.store("joint", JigsawBlockEntity.JointType.CODEC, this.joint);
      $$0.putInt("placement_priority", this.placementPriority);
      $$0.putInt("selection_priority", this.selectionPriority);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.name = $$0.<Identifier>read("name", Identifier.CODEC).orElse(EMPTY_ID);
      this.target = $$0.<Identifier>read("target", Identifier.CODEC).orElse(EMPTY_ID);
      this.pool = $$0.<ResourceKey<StructureTemplatePool>>read("pool", POOL_CODEC).orElse(Pools.EMPTY);
      this.finalState = $$0.getStringOr("final_state", "minecraft:air");
      this.joint = $$0.<JigsawBlockEntity.JointType>read("joint", JigsawBlockEntity.JointType.CODEC)
         .orElseGet(() -> StructureTemplate.getDefaultJointType(this.getBlockState()));
      this.placementPriority = $$0.getIntOr("placement_priority", 0);
      this.selectionPriority = $$0.getIntOr("selection_priority", 0);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public void generate(ServerLevel $$0, int $$1, boolean $$2) {
      BlockPos $$3 = this.getBlockPos().relative(((FrontAndTop)this.getBlockState().getValue(JigsawBlock.ORIENTATION)).front());
      Registry<StructureTemplatePool> $$4 = $$0.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
      Holder<StructureTemplatePool> $$5 = $$4.getOrThrow(this.pool);
      JigsawPlacement.generateJigsaw($$0, $$5, this.target, $$1, $$3, $$2);
   }

   public static enum JointType implements StringRepresentable {
      ROLLABLE("rollable"),
      ALIGNED("aligned");

      public static final EnumCodec<JigsawBlockEntity.JointType> CODEC = StringRepresentable.fromEnum(JigsawBlockEntity.JointType::values);
      private final String name;

      private JointType(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }

      public Component getTranslatedName() {
         return Component.translatable("jigsaw_block.joint." + this.name);
      }
   }
}
