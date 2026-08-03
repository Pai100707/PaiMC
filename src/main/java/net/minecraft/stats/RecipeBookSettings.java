package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
   public static final StreamCodec<FriendlyByteBuf, net.minecraft.stats.RecipeBookSettings> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.stats.RecipeBookSettings.TypeSettings.STREAM_CODEC,
      $$0 -> $$0.crafting,
      net.minecraft.stats.RecipeBookSettings.TypeSettings.STREAM_CODEC,
      $$0 -> $$0.furnace,
      net.minecraft.stats.RecipeBookSettings.TypeSettings.STREAM_CODEC,
      $$0 -> $$0.blastFurnace,
      net.minecraft.stats.RecipeBookSettings.TypeSettings.STREAM_CODEC,
      $$0 -> $$0.smoker,
      net.minecraft.stats.RecipeBookSettings::new
   );
   public static final MapCodec<net.minecraft.stats.RecipeBookSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            net.minecraft.stats.RecipeBookSettings.TypeSettings.CRAFTING_MAP_CODEC.forGetter($$0x -> $$0x.crafting),
            net.minecraft.stats.RecipeBookSettings.TypeSettings.FURNACE_MAP_CODEC.forGetter($$0x -> $$0x.furnace),
            net.minecraft.stats.RecipeBookSettings.TypeSettings.BLAST_FURNACE_MAP_CODEC.forGetter($$0x -> $$0x.blastFurnace),
            net.minecraft.stats.RecipeBookSettings.TypeSettings.SMOKER_MAP_CODEC.forGetter($$0x -> $$0x.smoker)
         )
         .apply($$0, net.minecraft.stats.RecipeBookSettings::new)
   );
   private net.minecraft.stats.RecipeBookSettings.TypeSettings crafting;
   private net.minecraft.stats.RecipeBookSettings.TypeSettings furnace;
   private net.minecraft.stats.RecipeBookSettings.TypeSettings blastFurnace;
   private net.minecraft.stats.RecipeBookSettings.TypeSettings smoker;

   public RecipeBookSettings() {
      this(
         net.minecraft.stats.RecipeBookSettings.TypeSettings.DEFAULT,
         net.minecraft.stats.RecipeBookSettings.TypeSettings.DEFAULT,
         net.minecraft.stats.RecipeBookSettings.TypeSettings.DEFAULT,
         net.minecraft.stats.RecipeBookSettings.TypeSettings.DEFAULT
      );
   }

   private RecipeBookSettings(
      net.minecraft.stats.RecipeBookSettings.TypeSettings $$0,
      net.minecraft.stats.RecipeBookSettings.TypeSettings $$1,
      net.minecraft.stats.RecipeBookSettings.TypeSettings $$2,
      net.minecraft.stats.RecipeBookSettings.TypeSettings $$3
   ) {
      this.crafting = $$0;
      this.furnace = $$1;
      this.blastFurnace = $$2;
      this.smoker = $$3;
   }

   @VisibleForTesting
   public net.minecraft.stats.RecipeBookSettings.TypeSettings getSettings(RecipeBookType $$0) {
      return switch ($$0) {
         case CRAFTING -> this.crafting;
         case FURNACE -> this.furnace;
         case BLAST_FURNACE -> this.blastFurnace;
         case SMOKER -> this.smoker;
         default -> throw new MatchException(null, null);
      };
   }

   private void updateSettings(RecipeBookType $$0, UnaryOperator<net.minecraft.stats.RecipeBookSettings.TypeSettings> $$1) {
      switch ($$0) {
         case CRAFTING:
            this.crafting = $$1.apply(this.crafting);
            break;
         case FURNACE:
            this.furnace = $$1.apply(this.furnace);
            break;
         case BLAST_FURNACE:
            this.blastFurnace = $$1.apply(this.blastFurnace);
            break;
         case SMOKER:
            this.smoker = $$1.apply(this.smoker);
      }
   }

   public boolean isOpen(RecipeBookType $$0) {
      return this.getSettings($$0).open;
   }

   public void setOpen(RecipeBookType $$0, boolean $$1) {
      this.updateSettings($$0, $$1x -> $$1x.setOpen($$1));
   }

   public boolean isFiltering(RecipeBookType $$0) {
      return this.getSettings($$0).filtering;
   }

   public void setFiltering(RecipeBookType $$0, boolean $$1) {
      this.updateSettings($$0, $$1x -> $$1x.setFiltering($$1));
   }

   public net.minecraft.stats.RecipeBookSettings copy() {
      return new net.minecraft.stats.RecipeBookSettings(this.crafting, this.furnace, this.blastFurnace, this.smoker);
   }

   public void replaceFrom(net.minecraft.stats.RecipeBookSettings $$0) {
      this.crafting = $$0.crafting;
      this.furnace = $$0.furnace;
      this.blastFurnace = $$0.blastFurnace;
      this.smoker = $$0.smoker;
   }

   public record TypeSettings(boolean open, boolean filtering) {
      public static final net.minecraft.stats.RecipeBookSettings.TypeSettings DEFAULT = new net.minecraft.stats.RecipeBookSettings.TypeSettings(false, false);
      public static final MapCodec<net.minecraft.stats.RecipeBookSettings.TypeSettings> CRAFTING_MAP_CODEC = codec("isGuiOpen", "isFilteringCraftable");
      public static final MapCodec<net.minecraft.stats.RecipeBookSettings.TypeSettings> FURNACE_MAP_CODEC = codec(
         "isFurnaceGuiOpen", "isFurnaceFilteringCraftable"
      );
      public static final MapCodec<net.minecraft.stats.RecipeBookSettings.TypeSettings> BLAST_FURNACE_MAP_CODEC = codec(
         "isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"
      );
      public static final MapCodec<net.minecraft.stats.RecipeBookSettings.TypeSettings> SMOKER_MAP_CODEC = codec(
         "isSmokerGuiOpen", "isSmokerFilteringCraftable"
      );
      public static final StreamCodec<ByteBuf, net.minecraft.stats.RecipeBookSettings.TypeSettings> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.BOOL,
         net.minecraft.stats.RecipeBookSettings.TypeSettings::open,
         ByteBufCodecs.BOOL,
         net.minecraft.stats.RecipeBookSettings.TypeSettings::filtering,
         net.minecraft.stats.RecipeBookSettings.TypeSettings::new
      );

      @Override
      public String toString() {
         return "[open=" + this.open + ", filtering=" + this.filtering + "]";
      }

      public net.minecraft.stats.RecipeBookSettings.TypeSettings setOpen(boolean $$0) {
         return new net.minecraft.stats.RecipeBookSettings.TypeSettings($$0, this.filtering);
      }

      public net.minecraft.stats.RecipeBookSettings.TypeSettings setFiltering(boolean $$0) {
         return new net.minecraft.stats.RecipeBookSettings.TypeSettings(this.open, $$0);
      }

      private static MapCodec<net.minecraft.stats.RecipeBookSettings.TypeSettings> codec(String $$0, String $$1) {
         return RecordCodecBuilder.mapCodec(
            $$2 -> $$2.group(
                  Codec.BOOL.optionalFieldOf($$0, false).forGetter(net.minecraft.stats.RecipeBookSettings.TypeSettings::open),
                  Codec.BOOL.optionalFieldOf($$1, false).forGetter(net.minecraft.stats.RecipeBookSettings.TypeSettings::filtering)
               )
               .apply($$2, net.minecraft.stats.RecipeBookSettings.TypeSettings::new)
         );
      }
   }
}
