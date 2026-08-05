package net.minecraft.util.datafix.fixes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class ParticleUnflatteningFix extends DataFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public ParticleUnflatteningFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.PARTICLE);
      Type<?> $$1 = this.getOutputSchema().getType(References.PARTICLE);
      return this.writeFixAndRead("ParticleUnflatteningFix", $$0, $$1, this::fix);
   }

   private <T> Dynamic<T> fix(Dynamic<T> $$0) {
      Optional<String> $$1 = $$0.asString().result();
      if ($$1.isEmpty()) {
         return $$0;
      } else {
         String $$2 = $$1.get();
         String[] $$3 = $$2.split(" ", 2);
         String $$4 = NamespacedSchema.ensureNamespaced($$3[0]);
         Dynamic<T> $$5 = $$0.createMap(Map.of($$0.createString("type"), $$0.createString($$4)));

         return switch ($$4) {
            case "minecraft:item" -> $$3.length > 1 ? this.updateItem($$5, $$3[1]) : $$5;
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust", "minecraft:dust_pillar" -> $$3.length > 1
               ? this.updateBlock($$5, $$3[1])
               : $$5;
            case "minecraft:dust" -> $$3.length > 1 ? this.updateDust($$5, $$3[1]) : $$5;
            case "minecraft:dust_color_transition" -> $$3.length > 1 ? this.updateDustTransition($$5, $$3[1]) : $$5;
            case "minecraft:sculk_charge" -> $$3.length > 1 ? this.updateSculkCharge($$5, $$3[1]) : $$5;
            case "minecraft:vibration" -> $$3.length > 1 ? this.updateVibration($$5, $$3[1]) : $$5;
            case "minecraft:shriek" -> $$3.length > 1 ? this.updateShriek($$5, $$3[1]) : $$5;
            default -> $$5;
         };
      }
   }

   private <T> Dynamic<T> updateItem(Dynamic<T> $$0, String $$1) {
      int $$2 = $$1.indexOf("{");
      Dynamic<T> $$3 = $$0.createMap(Map.of($$0.createString("Count"), $$0.createInt(1)));
      if ($$2 == -1) {
         $$3 = $$3.set("id", $$0.createString($$1));
      } else {
         $$3 = $$3.set("id", $$0.createString($$1.substring(0, $$2)));
         Dynamic<T> $$4 = parseTag($$0.getOps(), $$1.substring($$2));
         if ($$4 != null) {
            $$3 = $$3.set("tag", $$4);
         }
      }

      return $$0.set("item", $$3);
   }

   
   private static <T> Dynamic<T> parseTag(DynamicOps<T> $$0, String $$1) {
      try {
         return new Dynamic($$0, TagParser.create($$0).parseFully($$1));
      } catch (Exception var3) {
         LOGGER.warn("Failed to parse tag: {}", $$1, var3);
         return null;
      }
   }

   private <T> Dynamic<T> updateBlock(Dynamic<T> $$0, String $$1) {
      int $$2 = $$1.indexOf("[");
      Dynamic<T> $$3 = $$0.emptyMap();
      if ($$2 == -1) {
         $$3 = $$3.set("Name", $$0.createString(NamespacedSchema.ensureNamespaced($$1)));
      } else {
         $$3 = $$3.set("Name", $$0.createString(NamespacedSchema.ensureNamespaced($$1.substring(0, $$2))));
         Map<Dynamic<T>, Dynamic<T>> $$4 = parseBlockProperties($$0, $$1.substring($$2));
         if (!$$4.isEmpty()) {
            $$3 = $$3.set("Properties", $$0.createMap($$4));
         }
      }

      return $$0.set("block_state", $$3);
   }

   private static <T> Map<Dynamic<T>, Dynamic<T>> parseBlockProperties(Dynamic<T> $$0, String $$1) {
      try {
         Map<Dynamic<T>, Dynamic<T>> $$2 = new HashMap<>();
         StringReader $$3 = new StringReader($$1);
         $$3.expect('[');
         $$3.skipWhitespace();

         while ($$3.canRead() && $$3.peek() != ']') {
            $$3.skipWhitespace();
            String $$4 = $$3.readString();
            $$3.skipWhitespace();
            $$3.expect('=');
            $$3.skipWhitespace();
            String $$5 = $$3.readString();
            $$3.skipWhitespace();
            $$2.put($$0.createString($$4), $$0.createString($$5));
            if ($$3.canRead()) {
               if ($$3.peek() != ',') {
                  break;
               }

               $$3.skip();
            }
         }

         $$3.expect(']');
         return $$2;
      } catch (Exception var6) {
         LOGGER.warn("Failed to parse block properties: {}", $$1, var6);
         return Map.of();
      }
   }

   private static <T> Dynamic<T> readVector(Dynamic<T> $$0, StringReader $$1) throws CommandSyntaxException {
      float $$2 = $$1.readFloat();
      $$1.expect(' ');
      float $$3 = $$1.readFloat();
      $$1.expect(' ');
      float $$4 = $$1.readFloat();
      return $$0.createList(Stream.of($$2, $$3, $$4).map($$0::createFloat));
   }

   private <T> Dynamic<T> updateDust(Dynamic<T> $$0, String $$1) {
      try {
         StringReader $$2 = new StringReader($$1);
         Dynamic<T> $$3 = readVector($$0, $$2);
         $$2.expect(' ');
         float $$4 = $$2.readFloat();
         return $$0.set("color", $$3).set("scale", $$0.createFloat($$4));
      } catch (Exception var6) {
         LOGGER.warn("Failed to parse particle options: {}", $$1, var6);
         return $$0;
      }
   }

   private <T> Dynamic<T> updateDustTransition(Dynamic<T> $$0, String $$1) {
      try {
         StringReader $$2 = new StringReader($$1);
         Dynamic<T> $$3 = readVector($$0, $$2);
         $$2.expect(' ');
         float $$4 = $$2.readFloat();
         $$2.expect(' ');
         Dynamic<T> $$5 = readVector($$0, $$2);
         return $$0.set("from_color", $$3).set("to_color", $$5).set("scale", $$0.createFloat($$4));
      } catch (Exception var7) {
         LOGGER.warn("Failed to parse particle options: {}", $$1, var7);
         return $$0;
      }
   }

   private <T> Dynamic<T> updateSculkCharge(Dynamic<T> $$0, String $$1) {
      try {
         StringReader $$2 = new StringReader($$1);
         float $$3 = $$2.readFloat();
         return $$0.set("roll", $$0.createFloat($$3));
      } catch (Exception var5) {
         LOGGER.warn("Failed to parse particle options: {}", $$1, var5);
         return $$0;
      }
   }

   private <T> Dynamic<T> updateVibration(Dynamic<T> $$0, String $$1) {
      try {
         StringReader $$2 = new StringReader($$1);
         float $$3 = (float)$$2.readDouble();
         $$2.expect(' ');
         float $$4 = (float)$$2.readDouble();
         $$2.expect(' ');
         float $$5 = (float)$$2.readDouble();
         $$2.expect(' ');
         int $$6 = $$2.readInt();
         Dynamic<T> $$7 = $$0.createIntList(
            IntStream.of(net.minecraft.util.Mth.floor($$3), net.minecraft.util.Mth.floor($$4), net.minecraft.util.Mth.floor($$5))
         );
         Dynamic<T> $$8 = $$0.createMap(Map.of($$0.createString("type"), $$0.createString("minecraft:block"), $$0.createString("pos"), $$7));
         return $$0.set("destination", $$8).set("arrival_in_ticks", $$0.createInt($$6));
      } catch (Exception var10) {
         LOGGER.warn("Failed to parse particle options: {}", $$1, var10);
         return $$0;
      }
   }

   private <T> Dynamic<T> updateShriek(Dynamic<T> $$0, String $$1) {
      try {
         StringReader $$2 = new StringReader($$1);
         int $$3 = $$2.readInt();
         return $$0.set("delay", $$0.createInt($$3));
      } catch (Exception var5) {
         LOGGER.warn("Failed to parse particle options: {}", $$1, var5);
         return $$0;
      }
   }
}
