package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String templateName;
   protected StructureTemplate template;
   protected StructurePlaceSettings placeSettings;
   protected BlockPos templatePosition;

   public TemplateStructurePiece(
      StructurePieceType $$0, int $$1, StructureTemplateManager $$2, Identifier $$3, String $$4, StructurePlaceSettings $$5, BlockPos $$6
   ) {
      super($$0, $$1, $$2.getOrCreate($$3).getBoundingBox($$5, $$6));
      this.setOrientation(Direction.NORTH);
      this.templateName = $$4;
      this.templatePosition = $$6;
      this.template = $$2.getOrCreate($$3);
      this.placeSettings = $$5;
   }

   public TemplateStructurePiece(StructurePieceType $$0, CompoundTag $$1, StructureTemplateManager $$2, Function<Identifier, StructurePlaceSettings> $$3) {
      super($$0, $$1);
      this.setOrientation(Direction.NORTH);
      this.templateName = $$1.getStringOr("Template", "");
      this.templatePosition = new BlockPos($$1.getIntOr("TPX", 0), $$1.getIntOr("TPY", 0), $$1.getIntOr("TPZ", 0));
      Identifier $$4 = this.makeTemplateLocation();
      this.template = $$2.getOrCreate($$4);
      this.placeSettings = $$3.apply($$4);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
   }

   protected Identifier makeTemplateLocation() {
      return Identifier.parse(this.templateName);
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      $$1.putInt("TPX", this.templatePosition.getX());
      $$1.putInt("TPY", this.templatePosition.getY());
      $$1.putInt("TPZ", this.templatePosition.getZ());
      $$1.putString("Template", this.templateName);
   }

   @Override
   public void postProcess(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      net.minecraft.world.level.ChunkPos $$5,
      BlockPos $$6
   ) {
      this.placeSettings.setBoundingBox($$4);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if (this.template.placeInWorld($$0, this.templatePosition, $$6, this.placeSettings, $$3, 2)) {
         for (StructureTemplate.StructureBlockInfo $$8 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
            if ($$8.nbt() != null) {
               StructureMode $$9 = (StructureMode)$$8.nbt().read("mode", StructureMode.LEGACY_CODEC).orElseThrow();
               if ($$9 == StructureMode.DATA) {
                  this.handleDataMarker($$8.nbt().getStringOr("metadata", ""), $$8.pos(), $$0, $$3, $$4);
               }
            }
         }

         for (StructureTemplate.StructureBlockInfo $$11 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
            if ($$11.nbt() != null) {
               String $$12 = $$11.nbt().getStringOr("final_state", "minecraft:air");
               BlockState $$13 = Blocks.AIR.defaultBlockState();

               try {
                  $$13 = BlockStateParser.parseForBlock($$0.holderLookup(Registries.BLOCK), $$12, true).blockState();
               } catch (CommandSyntaxException var15) {
                  LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", $$12, $$11.pos());
               }

               $$0.setBlock($$11.pos(), $$13, 3);
            }
         }
      }
   }

   protected abstract void handleDataMarker(String var1, BlockPos var2, net.minecraft.world.level.ServerLevelAccessor var3, RandomSource var4, BoundingBox var5);

   @Deprecated
   @Override
   public void move(int $$0, int $$1, int $$2) {
      super.move($$0, $$1, $$2);
      this.templatePosition = this.templatePosition.offset($$0, $$1, $$2);
   }

   @Override
   public Rotation getRotation() {
      return this.placeSettings.getRotation();
   }

   public StructureTemplate template() {
      return this.template;
   }

   public BlockPos templatePosition() {
      return this.templatePosition;
   }

   public StructurePlaceSettings placeSettings() {
      return this.placeSettings;
   }
}
