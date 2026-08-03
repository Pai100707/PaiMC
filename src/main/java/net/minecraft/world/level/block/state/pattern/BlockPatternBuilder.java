package net.minecraft.world.level.block.state.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
   private final List<String[]> pattern = Lists.newArrayList();
   private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
   private int height;
   private int width;
   private final CharSet unknownCharacters = new CharOpenHashSet();

   private BlockPatternBuilder() {
      this.lookup.put(' ', $$0 -> true);
   }

   public BlockPatternBuilder aisle(String... $$0) {
      if (!ArrayUtils.isEmpty($$0) && !StringUtils.isEmpty($$0[0])) {
         if (this.pattern.isEmpty()) {
            this.height = $$0.length;
            this.width = $$0[0].length();
         }

         if ($$0.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + $$0.length + ")");
         } else {
            for (String $$1 : $$0) {
               if ($$1.length() != this.width) {
                  throw new IllegalArgumentException(
                     "Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + $$1.length() + ")"
                  );
               }

               for (char $$2 : $$1.toCharArray()) {
                  if (!this.lookup.containsKey($$2)) {
                     this.unknownCharacters.add($$2);
                  }
               }
            }

            this.pattern.add($$0);
            return this;
         }
      } else {
         throw new IllegalArgumentException("Empty pattern for aisle");
      }
   }

   public static BlockPatternBuilder start() {
      return new BlockPatternBuilder();
   }

   public BlockPatternBuilder where(char $$0, Predicate<BlockInWorld> $$1) {
      this.lookup.put($$0, $$1);
      this.unknownCharacters.remove($$0);
      return this;
   }

   public BlockPattern build() {
      return new BlockPattern(this.createPattern());
   }

   private Predicate<BlockInWorld>[][][] createPattern() {
      if (!this.unknownCharacters.isEmpty()) {
         throw new IllegalStateException("Predicates for character(s) " + this.unknownCharacters + " are missing");
      } else {
         Predicate<BlockInWorld>[][][] $$0 = (Predicate<BlockInWorld>[][][])Array.newInstance(Predicate.class, this.pattern.size(), this.height, this.width);

         for (int $$1 = 0; $$1 < this.pattern.size(); $$1++) {
            for (int $$2 = 0; $$2 < this.height; $$2++) {
               for (int $$3 = 0; $$3 < this.width; $$3++) {
                  $$0[$$1][$$2][$$3] = this.lookup.get(this.pattern.get($$1)[$$2].charAt($$3));
               }
            }
         }

         return $$0;
      }
   }
}
