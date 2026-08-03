package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.IdentifierException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class StructureBlockEntity extends BlockEntity implements BoundingBoxRenderable {
   private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
   public static final int MAX_OFFSET_PER_AXIS = 48;
   public static final int MAX_SIZE_PER_AXIS = 48;
   public static final String AUTHOR_TAG = "author";
   private static final String DEFAULT_AUTHOR = "";
   private static final String DEFAULT_METADATA = "";
   private static final BlockPos DEFAULT_POS = new BlockPos(0, 1, 0);
   private static final Vec3i DEFAULT_SIZE = Vec3i.ZERO;
   private static final Rotation DEFAULT_ROTATION = Rotation.NONE;
   private static final Mirror DEFAULT_MIRROR = Mirror.NONE;
   private static final boolean DEFAULT_IGNORE_ENTITIES = true;
   private static final boolean DEFAULT_STRICT = false;
   private static final boolean DEFAULT_POWERED = false;
   private static final boolean DEFAULT_SHOW_AIR = false;
   private static final boolean DEFAULT_SHOW_BOUNDING_BOX = true;
   private static final float DEFAULT_INTEGRITY = 1.0F;
   private static final long DEFAULT_SEED = 0L;
   @Nullable
   private Identifier structureName;
   private String author = "";
   private String metaData = "";
   private BlockPos structurePos = DEFAULT_POS;
   private Vec3i structureSize = DEFAULT_SIZE;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private StructureMode mode;
   private boolean ignoreEntities = true;
   private boolean strict = false;
   private boolean powered = false;
   private boolean showAir = false;
   private boolean showBoundingBox = true;
   private float integrity = 1.0F;
   private long seed = 0L;

   public StructureBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.STRUCTURE_BLOCK, $$0, $$1);
      this.mode = $$1.getValue(StructureBlock.MODE);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.putString("name", this.getStructureName());
      $$0.putString("author", this.author);
      $$0.putString("metadata", this.metaData);
      $$0.putInt("posX", this.structurePos.getX());
      $$0.putInt("posY", this.structurePos.getY());
      $$0.putInt("posZ", this.structurePos.getZ());
      $$0.putInt("sizeX", this.structureSize.getX());
      $$0.putInt("sizeY", this.structureSize.getY());
      $$0.putInt("sizeZ", this.structureSize.getZ());
      $$0.store("rotation", Rotation.LEGACY_CODEC, this.rotation);
      $$0.store("mirror", Mirror.LEGACY_CODEC, this.mirror);
      $$0.store("mode", StructureMode.LEGACY_CODEC, this.mode);
      $$0.putBoolean("ignoreEntities", this.ignoreEntities);
      $$0.putBoolean("strict", this.strict);
      $$0.putBoolean("powered", this.powered);
      $$0.putBoolean("showair", this.showAir);
      $$0.putBoolean("showboundingbox", this.showBoundingBox);
      $$0.putFloat("integrity", this.integrity);
      $$0.putLong("seed", this.seed);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.setStructureName($$0.getStringOr("name", ""));
      this.author = $$0.getStringOr("author", "");
      this.metaData = $$0.getStringOr("metadata", "");
      int $$1 = Mth.clamp($$0.getIntOr("posX", DEFAULT_POS.getX()), -48, 48);
      int $$2 = Mth.clamp($$0.getIntOr("posY", DEFAULT_POS.getY()), -48, 48);
      int $$3 = Mth.clamp($$0.getIntOr("posZ", DEFAULT_POS.getZ()), -48, 48);
      this.structurePos = new BlockPos($$1, $$2, $$3);
      int $$4 = Mth.clamp($$0.getIntOr("sizeX", DEFAULT_SIZE.getX()), 0, 48);
      int $$5 = Mth.clamp($$0.getIntOr("sizeY", DEFAULT_SIZE.getY()), 0, 48);
      int $$6 = Mth.clamp($$0.getIntOr("sizeZ", DEFAULT_SIZE.getZ()), 0, 48);
      this.structureSize = new Vec3i($$4, $$5, $$6);
      this.rotation = $$0.<Rotation>read("rotation", Rotation.LEGACY_CODEC).orElse(DEFAULT_ROTATION);
      this.mirror = $$0.<Mirror>read("mirror", Mirror.LEGACY_CODEC).orElse(DEFAULT_MIRROR);
      this.mode = $$0.<StructureMode>read("mode", StructureMode.LEGACY_CODEC).orElse(StructureMode.DATA);
      this.ignoreEntities = $$0.getBooleanOr("ignoreEntities", true);
      this.strict = $$0.getBooleanOr("strict", false);
      this.powered = $$0.getBooleanOr("powered", false);
      this.showAir = $$0.getBooleanOr("showair", false);
      this.showBoundingBox = $$0.getBooleanOr("showboundingbox", true);
      this.integrity = $$0.getFloatOr("integrity", 1.0F);
      this.seed = $$0.getLongOr("seed", 0L);
      this.updateBlockState();
   }

   private void updateBlockState() {
      if (this.level != null) {
         BlockPos $$0 = this.getBlockPos();
         BlockState $$1 = this.level.getBlockState($$0);
         if ($$1.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock($$0, $$1.setValue(StructureBlock.MODE, this.mode), 2);
         }
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public boolean usedBy(Player $$0) {
      if (!$$0.canUseGameMasterBlocks()) {
         return false;
      } else {
         if ($$0.level().isClientSide()) {
            $$0.openStructureBlock(this);
         }

         return true;
      }
   }

   public String getStructureName() {
      return this.structureName == null ? "" : this.structureName.toString();
   }

   public boolean hasStructureName() {
      return this.structureName != null;
   }

   public void setStructureName(@Nullable String $$0) {
      this.setStructureName(StringUtil.isNullOrEmpty($$0) ? null : Identifier.tryParse($$0));
   }

   public void setStructureName(@Nullable Identifier $$0) {
      this.structureName = $$0;
   }

   public void createdBy(LivingEntity $$0) {
      this.author = $$0.getPlainTextName();
   }

   public BlockPos getStructurePos() {
      return this.structurePos;
   }

   public void setStructurePos(BlockPos $$0) {
      this.structurePos = $$0;
   }

   public Vec3i getStructureSize() {
      return this.structureSize;
   }

   public void setStructureSize(Vec3i $$0) {
      this.structureSize = $$0;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public void setMirror(Mirror $$0) {
      this.mirror = $$0;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public void setRotation(Rotation $$0) {
      this.rotation = $$0;
   }

   public String getMetaData() {
      return this.metaData;
   }

   public void setMetaData(String $$0) {
      this.metaData = $$0;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public void setMode(StructureMode $$0) {
      this.mode = $$0;
      BlockState $$1 = this.level.getBlockState(this.getBlockPos());
      if ($$1.is(Blocks.STRUCTURE_BLOCK)) {
         this.level.setBlock(this.getBlockPos(), $$1.setValue(StructureBlock.MODE, $$0), 2);
      }
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public boolean isStrict() {
      return this.strict;
   }

   public void setIgnoreEntities(boolean $$0) {
      this.ignoreEntities = $$0;
   }

   public void setStrict(boolean $$0) {
      this.strict = $$0;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public void setIntegrity(float $$0) {
      this.integrity = $$0;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long $$0) {
      this.seed = $$0;
   }

   public boolean detectSize() {
      if (this.mode != StructureMode.SAVE) {
         return false;
      } else {
         BlockPos $$0 = this.getBlockPos();
         int $$1 = 80;
         BlockPos $$2 = new BlockPos($$0.getX() - 80, this.level.getMinY(), $$0.getZ() - 80);
         BlockPos $$3 = new BlockPos($$0.getX() + 80, this.level.getMaxY(), $$0.getZ() + 80);
         Stream<BlockPos> $$4 = this.getRelatedCorners($$2, $$3);
         return calculateEnclosingBoundingBox($$0, $$4).filter($$1x -> {
            int $$2x = $$1x.maxX() - $$1x.minX();
            int $$3x = $$1x.maxY() - $$1x.minY();
            int $$4x = $$1x.maxZ() - $$1x.minZ();
            if ($$2x > 1 && $$3x > 1 && $$4x > 1) {
               this.structurePos = new BlockPos($$1x.minX() - $$0.getX() + 1, $$1x.minY() - $$0.getY() + 1, $$1x.minZ() - $$0.getZ() + 1);
               this.structureSize = new Vec3i($$2x - 1, $$3x - 1, $$4x - 1);
               this.setChanged();
               BlockState $$5 = this.level.getBlockState($$0);
               this.level.sendBlockUpdated($$0, $$5, $$5, 3);
               return true;
            } else {
               return false;
            }
         }).isPresent();
      }
   }

   private Stream<BlockPos> getRelatedCorners(BlockPos $$0, BlockPos $$1) {
      return BlockPos.betweenClosedStream($$0, $$1)
         .filter($$0x -> this.level.getBlockState($$0x).is(Blocks.STRUCTURE_BLOCK))
         .map(this.level::getBlockEntity)
         .filter($$0x -> $$0x instanceof StructureBlockEntity)
         .map($$0x -> (StructureBlockEntity)$$0x)
         .filter($$0x -> $$0x.mode == StructureMode.CORNER && Objects.equals(this.structureName, $$0x.structureName))
         .map(BlockEntity::getBlockPos);
   }

   private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos $$0, Stream<BlockPos> $$1) {
      Iterator<BlockPos> $$2 = $$1.iterator();
      if (!$$2.hasNext()) {
         return Optional.empty();
      } else {
         BlockPos $$3 = $$2.next();
         BoundingBox $$4 = new BoundingBox($$3);
         if ($$2.hasNext()) {
            $$2.forEachRemaining($$4::encapsulate);
         } else {
            $$4.encapsulate($$0);
         }

         return Optional.of($$4);
      }
   }

   public boolean saveStructure() {
      return this.mode != StructureMode.SAVE ? false : this.saveStructure(true);
   }

   public boolean saveStructure(boolean $$0) {
      if (this.structureName != null && this.level instanceof ServerLevel $$1) {
         BlockPos var4 = this.getBlockPos().offset(this.structurePos);
         return saveStructure($$1, this.structureName, var4, this.structureSize, this.ignoreEntities, this.author, $$0, List.of());
      } else {
         return false;
      }
   }

   public static boolean saveStructure(ServerLevel $$0, Identifier $$1, BlockPos $$2, Vec3i $$3, boolean $$4, String $$5, boolean $$6, List<Block> $$7) {
      StructureTemplateManager $$8 = $$0.getStructureManager();

      StructureTemplate $$9;
      try {
         $$9 = $$8.getOrCreate($$1);
      } catch (IdentifierException var12) {
         return false;
      }

      $$9.fillFromWorld($$0, $$2, $$3, !$$4, Stream.concat($$7.stream(), Stream.of(Blocks.STRUCTURE_VOID)).toList());
      $$9.setAuthor($$5);
      if ($$6) {
         try {
            return $$8.save($$1);
         } catch (IdentifierException var11) {
            return false;
         }
      } else {
         return true;
      }
   }

   public static RandomSource createRandom(long $$0) {
      return $$0 == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create($$0);
   }

   public boolean placeStructureIfSameSize(ServerLevel $$0) {
      if (this.mode == StructureMode.LOAD && this.structureName != null) {
         StructureTemplate $$1 = $$0.getStructureManager().get(this.structureName).orElse(null);
         if ($$1 == null) {
            return false;
         } else if ($$1.getSize().equals(this.structureSize)) {
            this.placeStructure($$0, $$1);
            return true;
         } else {
            this.loadStructureInfo($$1);
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean loadStructureInfo(ServerLevel $$0) {
      StructureTemplate $$1 = this.getStructureTemplate($$0);
      if ($$1 == null) {
         return false;
      } else {
         this.loadStructureInfo($$1);
         return true;
      }
   }

   private void loadStructureInfo(StructureTemplate $$0) {
      this.author = !StringUtil.isNullOrEmpty($$0.getAuthor()) ? $$0.getAuthor() : "";
      this.structureSize = $$0.getSize();
      this.setChanged();
   }

   public void placeStructure(ServerLevel $$0) {
      StructureTemplate $$1 = this.getStructureTemplate($$0);
      if ($$1 != null) {
         this.placeStructure($$0, $$1);
      }
   }

   @Nullable
   private StructureTemplate getStructureTemplate(ServerLevel $$0) {
      return this.structureName == null ? null : $$0.getStructureManager().get(this.structureName).orElse(null);
   }

   private void placeStructure(ServerLevel $$0, StructureTemplate $$1) {
      this.loadStructureInfo($$1);
      StructurePlaceSettings $$2 = new StructurePlaceSettings()
         .setMirror(this.mirror)
         .setRotation(this.rotation)
         .setIgnoreEntities(this.ignoreEntities)
         .setKnownShape(this.strict);
      if (this.integrity < 1.0F) {
         $$2.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
      }

      BlockPos $$3 = this.getBlockPos().offset(this.structurePos);
      if (SharedConstants.DEBUG_STRUCTURE_EDIT_MODE) {
         BlockPos.betweenClosed($$3, $$3.offset(this.structureSize)).forEach($$1x -> $$0.setBlock($$1x, Blocks.STRUCTURE_VOID.defaultBlockState(), 2));
      }

      $$1.placeInWorld($$0, $$3, $$3, $$2, createRandom(this.seed), 2 | (this.strict ? 816 : 0));
   }

   public void unloadStructure() {
      if (this.structureName != null) {
         ServerLevel $$0 = (ServerLevel)this.level;
         StructureTemplateManager $$1 = $$0.getStructureManager();
         $$1.remove(this.structureName);
      }
   }

   public boolean isStructureLoadable() {
      if (this.mode == StructureMode.LOAD && !this.level.isClientSide() && this.structureName != null) {
         ServerLevel $$0 = (ServerLevel)this.level;
         StructureTemplateManager $$1 = $$0.getStructureManager();

         try {
            return $$1.get(this.structureName).isPresent();
         } catch (IdentifierException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean $$0) {
      this.powered = $$0;
   }

   public boolean getShowAir() {
      return this.showAir;
   }

   public void setShowAir(boolean $$0) {
      this.showAir = $$0;
   }

   public boolean getShowBoundingBox() {
      return this.showBoundingBox;
   }

   public void setShowBoundingBox(boolean $$0) {
      this.showBoundingBox = $$0;
   }

   @Override
   public BoundingBoxRenderable.Mode renderMode() {
      if (this.mode != StructureMode.SAVE && this.mode != StructureMode.LOAD) {
         return BoundingBoxRenderable.Mode.NONE;
      } else if (this.mode == StructureMode.SAVE && this.showAir) {
         return BoundingBoxRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS;
      } else {
         return this.mode != StructureMode.SAVE && !this.showBoundingBox ? BoundingBoxRenderable.Mode.NONE : BoundingBoxRenderable.Mode.BOX;
      }
   }

   @Override
   public BoundingBoxRenderable.RenderableBox getRenderableBox() {
      BlockPos $$0 = this.getStructurePos();
      Vec3i $$1 = this.getStructureSize();
      int $$2 = $$0.getX();
      int $$3 = $$0.getZ();
      int $$4 = $$0.getY();
      int $$5 = $$4 + $$1.getY();
      int $$6;
      int $$7;
      switch (this.mirror) {
         case LEFT_RIGHT:
            $$6 = $$1.getX();
            $$7 = -$$1.getZ();
            break;
         case FRONT_BACK:
            $$6 = -$$1.getX();
            $$7 = $$1.getZ();
            break;
         default:
            $$6 = $$1.getX();
            $$7 = $$1.getZ();
      }

      int $$24;
      int $$25;
      int $$26;
      int $$27;
      switch (this.rotation) {
         case CLOCKWISE_90:
            $$24 = $$7 < 0 ? $$2 : $$2 + 1;
            $$25 = $$6 < 0 ? $$3 + 1 : $$3;
            $$26 = $$24 - $$7;
            $$27 = $$25 + $$6;
            break;
         case CLOCKWISE_180:
            $$24 = $$6 < 0 ? $$2 : $$2 + 1;
            $$25 = $$7 < 0 ? $$3 : $$3 + 1;
            $$26 = $$24 - $$6;
            $$27 = $$25 - $$7;
            break;
         case COUNTERCLOCKWISE_90:
            $$24 = $$7 < 0 ? $$2 + 1 : $$2;
            $$25 = $$6 < 0 ? $$3 : $$3 + 1;
            $$26 = $$24 + $$7;
            $$27 = $$25 - $$6;
            break;
         default:
            $$24 = $$6 < 0 ? $$2 + 1 : $$2;
            $$25 = $$7 < 0 ? $$3 + 1 : $$3;
            $$26 = $$24 + $$6;
            $$27 = $$25 + $$7;
      }

      return BoundingBoxRenderable.RenderableBox.fromCorners($$24, $$4, $$25, $$26, $$5, $$27);
   }

   public static enum UpdateType {
      UPDATE_DATA,
      SAVE_AREA,
      LOAD_AREA,
      SCAN_AREA;
   }
}
