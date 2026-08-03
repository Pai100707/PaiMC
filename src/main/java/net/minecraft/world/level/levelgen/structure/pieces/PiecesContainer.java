package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.slf4j.Logger;

public record PiecesContainer(List<StructurePiece> pieces) {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier JIGSAW_RENAME = Identifier.withDefaultNamespace("jigsaw");
   private static final Map<Identifier, Identifier> RENAMES = ImmutableMap.builder()
      .put(Identifier.withDefaultNamespace("nvi"), JIGSAW_RENAME)
      .put(Identifier.withDefaultNamespace("pcp"), JIGSAW_RENAME)
      .put(Identifier.withDefaultNamespace("bastionremnant"), JIGSAW_RENAME)
      .put(Identifier.withDefaultNamespace("runtime"), JIGSAW_RENAME)
      .build();

   public PiecesContainer(final List<StructurePiece> pieces) {
      this.pieces = List.copyOf(pieces);
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public boolean isInsidePiece(BlockPos $$0) {
      for (StructurePiece $$1 : this.pieces) {
         if ($$1.getBoundingBox().isInside($$0)) {
            return true;
         }
      }

      return false;
   }

   public Tag save(StructurePieceSerializationContext $$0) {
      ListTag $$1 = new ListTag();

      for (StructurePiece $$2 : this.pieces) {
         $$1.add($$2.createTag($$0));
      }

      return $$1;
   }

   public static PiecesContainer load(ListTag $$0, StructurePieceSerializationContext $$1) {
      List<StructurePiece> $$2 = Lists.newArrayList();

      for (int $$3 = 0; $$3 < $$0.size(); $$3++) {
         CompoundTag $$4 = $$0.getCompoundOrEmpty($$3);
         String $$5 = $$4.getStringOr("id", "").toLowerCase(Locale.ROOT);
         Identifier $$6 = Identifier.parse($$5);
         Identifier $$7 = RENAMES.getOrDefault($$6, $$6);
         StructurePieceType $$8 = (StructurePieceType)BuiltInRegistries.STRUCTURE_PIECE.getValue($$7);
         if ($$8 == null) {
            LOGGER.error("Unknown structure piece id: {}", $$7);
         } else {
            try {
               StructurePiece $$9 = $$8.load($$1, $$4);
               $$2.add($$9);
            } catch (Exception var10) {
               LOGGER.error("Exception loading structure piece with id {}", $$7, var10);
            }
         }
      }

      return new PiecesContainer($$2);
   }

   public BoundingBox calculateBoundingBox() {
      return StructurePiece.createBoundingBox(this.pieces.stream());
   }
}
