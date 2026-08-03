package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public final class ShapedRecipePattern {
   private static final int MAX_SIZE = 3;
   public static final char EMPTY_SLOT = ' ';
   public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.Data.MAP_CODEC
      .flatXmap(
         ShapedRecipePattern::unpack,
         $$0 -> $$0.data.<DataResult>map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"))
      );
   public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      $$0 -> $$0.width,
      ByteBufCodecs.VAR_INT,
      $$0 -> $$0.height,
      Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
      $$0 -> $$0.ingredients,
      ShapedRecipePattern::createFromNetwork
   );
   private final int width;
   private final int height;
   private final List<Optional<Ingredient>> ingredients;
   private final Optional<ShapedRecipePattern.Data> data;
   private final int ingredientCount;
   private final boolean symmetrical;

   public ShapedRecipePattern(int $$0, int $$1, List<Optional<Ingredient>> $$2, Optional<ShapedRecipePattern.Data> $$3) {
      this.width = $$0;
      this.height = $$1;
      this.ingredients = $$2;
      this.data = $$3;
      this.ingredientCount = (int)$$2.stream().flatMap(Optional::stream).count();
      this.symmetrical = Util.isSymmetrical($$0, $$1, $$2);
   }

   private static ShapedRecipePattern createFromNetwork(Integer $$0, Integer $$1, List<Optional<Ingredient>> $$2) {
      return new ShapedRecipePattern($$0, $$1, $$2, Optional.empty());
   }

   public static ShapedRecipePattern of(Map<Character, Ingredient> $$0, String... $$1) {
      return of($$0, List.of($$1));
   }

   public static ShapedRecipePattern of(Map<Character, Ingredient> $$0, List<String> $$1) {
      ShapedRecipePattern.Data $$2 = new ShapedRecipePattern.Data($$0, $$1);
      return (ShapedRecipePattern)unpack($$2).getOrThrow();
   }

   private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.Data $$0) {
      String[] $$1 = shrink($$0.pattern);
      int $$2 = $$1[0].length();
      int $$3 = $$1.length;
      List<Optional<Ingredient>> $$4 = new ArrayList<>($$2 * $$3);
      CharSet $$5 = new CharArraySet($$0.key.keySet());

      for (String $$6 : $$1) {
         for (int $$7 = 0; $$7 < $$6.length(); $$7++) {
            char $$8 = $$6.charAt($$7);
            Optional<Ingredient> $$9;
            if ($$8 == ' ') {
               $$9 = Optional.empty();
            } else {
               Ingredient $$10 = $$0.key.get($$8);
               if ($$10 == null) {
                  return DataResult.error(() -> "Pattern references symbol '" + $$8 + "' but it's not defined in the key");
               }

               $$9 = Optional.of($$10);
            }

            $$5.remove($$8);
            $$4.add($$9);
         }
      }

      return !$$5.isEmpty()
         ? DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + $$5)
         : DataResult.success(new ShapedRecipePattern($$2, $$3, $$4, Optional.of($$0)));
   }

   @VisibleForTesting
   static String[] shrink(List<String> $$0) {
      int $$1 = Integer.MAX_VALUE;
      int $$2 = 0;
      int $$3 = 0;
      int $$4 = 0;

      for (int $$5 = 0; $$5 < $$0.size(); $$5++) {
         String $$6 = $$0.get($$5);
         $$1 = Math.min($$1, firstNonEmpty($$6));
         int $$7 = lastNonEmpty($$6);
         $$2 = Math.max($$2, $$7);
         if ($$7 < 0) {
            if ($$3 == $$5) {
               $$3++;
            }

            $$4++;
         } else {
            $$4 = 0;
         }
      }

      if ($$0.size() == $$4) {
         return new String[0];
      } else {
         String[] $$8 = new String[$$0.size() - $$4 - $$3];

         for (int $$9 = 0; $$9 < $$8.length; $$9++) {
            $$8[$$9] = $$0.get($$9 + $$3).substring($$1, $$2 + 1);
         }

         return $$8;
      }
   }

   private static int firstNonEmpty(String $$0) {
      int $$1 = 0;

      while ($$1 < $$0.length() && $$0.charAt($$1) == ' ') {
         $$1++;
      }

      return $$1;
   }

   private static int lastNonEmpty(String $$0) {
      int $$1 = $$0.length() - 1;

      while ($$1 >= 0 && $$0.charAt($$1) == ' ') {
         $$1--;
      }

      return $$1;
   }

   public boolean matches(CraftingInput $$0) {
      if ($$0.ingredientCount() != this.ingredientCount) {
         return false;
      } else {
         if ($$0.width() == this.width && $$0.height() == this.height) {
            if (!this.symmetrical && this.matches($$0, true)) {
               return true;
            }

            if (this.matches($$0, false)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean matches(CraftingInput $$0, boolean $$1) {
      for (int $$2 = 0; $$2 < this.height; $$2++) {
         for (int $$3 = 0; $$3 < this.width; $$3++) {
            Optional<Ingredient> $$4;
            if ($$1) {
               $$4 = this.ingredients.get(this.width - $$3 - 1 + $$2 * this.width);
            } else {
               $$4 = this.ingredients.get($$3 + $$2 * this.width);
            }

            net.minecraft.world.item.ItemStack $$6 = $$0.getItem($$3, $$2);
            if (!Ingredient.testOptionalIngredient($$4, $$6)) {
               return false;
            }
         }
      }

      return true;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public List<Optional<Ingredient>> ingredients() {
      return this.ingredients;
   }

   public record Data(Map<Character, Ingredient> key, List<String> pattern) {
      private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap($$0 -> {
         if ($$0.size() > 3) {
            return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
         } else if ($$0.isEmpty()) {
            return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
         } else {
            int $$1 = ((String)$$0.getFirst()).length();

            for (String $$2 : $$0) {
               if ($$2.length() > 3) {
                  return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
               }

               if ($$1 != $$2.length()) {
                  return DataResult.error(() -> "Invalid pattern: each row must be the same width");
               }
            }

            return DataResult.success($$0);
         }
      }, Function.identity());
      private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap($$0 -> {
         if ($$0.length() != 1) {
            return DataResult.error(() -> "Invalid key entry: '" + $$0 + "' is an invalid symbol (must be 1 character only).");
         } else {
            return " ".equals($$0) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success($$0.charAt(0));
         }
      }, String::valueOf);
      public static final MapCodec<ShapedRecipePattern.Data> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC).fieldOf("key").forGetter($$0x -> $$0x.key),
               PATTERN_CODEC.fieldOf("pattern").forGetter($$0x -> $$0x.pattern)
            )
            .apply($$0, ShapedRecipePattern.Data::new)
      );
   }
}
