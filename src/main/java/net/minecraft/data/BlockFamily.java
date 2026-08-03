package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class BlockFamily {
   private final Block baseBlock;
   final Map<net.minecraft.data.BlockFamily.Variant, Block> variants = Maps.newHashMap();
   boolean generateModel = true;
   boolean generateRecipe = true;
   @Nullable
   String recipeGroupPrefix;
   @Nullable
   String recipeUnlockedBy;

   BlockFamily(Block $$0) {
      this.baseBlock = $$0;
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public Map<net.minecraft.data.BlockFamily.Variant, Block> getVariants() {
      return this.variants;
   }

   public Block get(net.minecraft.data.BlockFamily.Variant $$0) {
      return this.variants.get($$0);
   }

   public boolean shouldGenerateModel() {
      return this.generateModel;
   }

   public boolean shouldGenerateRecipe() {
      return this.generateRecipe;
   }

   public Optional<String> getRecipeGroupPrefix() {
      return StringUtil.isBlank(this.recipeGroupPrefix) ? Optional.empty() : Optional.of(this.recipeGroupPrefix);
   }

   public Optional<String> getRecipeUnlockedBy() {
      return StringUtil.isBlank(this.recipeUnlockedBy) ? Optional.empty() : Optional.of(this.recipeUnlockedBy);
   }

   public static class Builder {
      private final net.minecraft.data.BlockFamily family;

      public Builder(Block $$0) {
         this.family = new net.minecraft.data.BlockFamily($$0);
      }

      public net.minecraft.data.BlockFamily getFamily() {
         return this.family;
      }

      public net.minecraft.data.BlockFamily.Builder button(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.BUTTON, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder chiseled(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.CHISELED, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder mosaic(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.MOSAIC, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder cracked(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.CRACKED, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder cut(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.CUT, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder door(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.DOOR, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder customFence(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.CUSTOM_FENCE, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder fence(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.FENCE, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder customFenceGate(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.CUSTOM_FENCE_GATE, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder fenceGate(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.FENCE_GATE, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder sign(Block $$0, Block $$1) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.SIGN, $$0);
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.WALL_SIGN, $$1);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder slab(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.SLAB, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder stairs(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.STAIRS, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder pressurePlate(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.PRESSURE_PLATE, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder polished(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.POLISHED, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder trapdoor(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.TRAPDOOR, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder wall(Block $$0) {
         this.family.variants.put(net.minecraft.data.BlockFamily.Variant.WALL, $$0);
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder dontGenerateModel() {
         this.family.generateModel = false;
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder dontGenerateRecipe() {
         this.family.generateRecipe = false;
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder recipeGroupPrefix(String $$0) {
         this.family.recipeGroupPrefix = $$0;
         return this;
      }

      public net.minecraft.data.BlockFamily.Builder recipeUnlockedBy(String $$0) {
         this.family.recipeUnlockedBy = $$0;
         return this;
      }
   }

   public static enum Variant {
      BUTTON("button"),
      CHISELED("chiseled"),
      CRACKED("cracked"),
      CUT("cut"),
      DOOR("door"),
      CUSTOM_FENCE("fence"),
      FENCE("fence"),
      CUSTOM_FENCE_GATE("fence_gate"),
      FENCE_GATE("fence_gate"),
      MOSAIC("mosaic"),
      SIGN("sign"),
      SLAB("slab"),
      STAIRS("stairs"),
      PRESSURE_PLATE("pressure_plate"),
      POLISHED("polished"),
      TRAPDOOR("trapdoor"),
      WALL("wall"),
      WALL_SIGN("wall_sign");

      private final String recipeGroup;

      private Variant(final String $$0) {
         this.recipeGroup = $$0;
      }

      public String getRecipeGroup() {
         return this.recipeGroup;
      }
   }
}
