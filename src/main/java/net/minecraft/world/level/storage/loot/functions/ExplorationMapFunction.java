package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

public class ExplorationMapFunction extends LootItemConditionalFunction {
   public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
   public static final Holder<MapDecorationType> DEFAULT_DECORATION = MapDecorationTypes.WOODLAND_MANSION;
   public static final byte DEFAULT_ZOOM = 2;
   public static final int DEFAULT_SEARCH_RADIUS = 50;
   public static final boolean DEFAULT_SKIP_EXISTING = true;
   public static final MapCodec<ExplorationMapFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               TagKey.codec(Registries.STRUCTURE).optionalFieldOf("destination", DEFAULT_DESTINATION).forGetter($$0x -> $$0x.destination),
               MapDecorationType.CODEC.optionalFieldOf("decoration", DEFAULT_DECORATION).forGetter($$0x -> $$0x.mapDecoration),
               Codec.BYTE.optionalFieldOf("zoom", (byte)2).forGetter($$0x -> $$0x.zoom),
               Codec.INT.optionalFieldOf("search_radius", 50).forGetter($$0x -> $$0x.searchRadius),
               Codec.BOOL.optionalFieldOf("skip_existing_chunks", true).forGetter($$0x -> $$0x.skipKnownStructures)
            )
         )
         .apply($$0, ExplorationMapFunction::new)
   );
   private final TagKey<Structure> destination;
   private final Holder<MapDecorationType> mapDecoration;
   private final byte zoom;
   private final int searchRadius;
   private final boolean skipKnownStructures;

   ExplorationMapFunction(List<LootItemCondition> $$0, TagKey<Structure> $$1, Holder<MapDecorationType> $$2, byte $$3, int $$4, boolean $$5) {
      super($$0);
      this.destination = $$1;
      this.mapDecoration = $$2;
      this.zoom = $$3;
      this.searchRadius = $$4;
      this.skipKnownStructures = $$5;
   }

   @Override
   public LootItemFunctionType<ExplorationMapFunction> getType() {
      return LootItemFunctions.EXPLORATION_MAP;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.ORIGIN);
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if (!$$0.is(Items.MAP)) {
         return $$0;
      } else {
         Vec3 $$2 = $$1.getOptionalParameter(LootContextParams.ORIGIN);
         if ($$2 != null) {
            ServerLevel $$3 = $$1.getLevel();
            BlockPos $$4 = $$3.findNearestMapStructure(this.destination, BlockPos.containing($$2), this.searchRadius, this.skipKnownStructures);
            if ($$4 != null) {
               ItemStack $$5 = MapItem.create($$3, $$4.getX(), $$4.getZ(), this.zoom, true, true);
               MapItem.renderBiomePreviewMap($$3, $$5);
               MapItemSavedData.addTargetDecoration($$5, $$4, "+", this.mapDecoration);
               return $$5;
            }
         }

         return $$0;
      }
   }

   public static ExplorationMapFunction.Builder makeExplorationMap() {
      return new ExplorationMapFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
      private TagKey<Structure> destination = ExplorationMapFunction.DEFAULT_DESTINATION;
      private Holder<MapDecorationType> mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
      private byte zoom = 2;
      private int searchRadius = 50;
      private boolean skipKnownStructures = true;

      protected ExplorationMapFunction.Builder getThis() {
         return this;
      }

      public ExplorationMapFunction.Builder setDestination(TagKey<Structure> $$0) {
         this.destination = $$0;
         return this;
      }

      public ExplorationMapFunction.Builder setMapDecoration(Holder<MapDecorationType> $$0) {
         this.mapDecoration = $$0;
         return this;
      }

      public ExplorationMapFunction.Builder setZoom(byte $$0) {
         this.zoom = $$0;
         return this;
      }

      public ExplorationMapFunction.Builder setSearchRadius(int $$0) {
         this.searchRadius = $$0;
         return this;
      }

      public ExplorationMapFunction.Builder setSkipKnownStructures(boolean $$0) {
         this.skipKnownStructures = $$0;
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
      }
   }
}
