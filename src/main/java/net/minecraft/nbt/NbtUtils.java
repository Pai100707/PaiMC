package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public final class NbtUtils {
   private static final Comparator<net.minecraft.nbt.ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.<net.minecraft.nbt.ListTag>comparingInt(
         $$0 -> $$0.getIntOr(1, 0)
      )
      .thenComparingInt($$0 -> $$0.getIntOr(0, 0))
      .thenComparingInt($$0 -> $$0.getIntOr(2, 0));
   private static final Comparator<net.minecraft.nbt.ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.<net.minecraft.nbt.ListTag>comparingDouble(
         $$0 -> $$0.getDoubleOr(1, 0.0)
      )
      .thenComparingDouble($$0 -> $$0.getDoubleOr(0, 0.0))
      .thenComparingDouble($$0 -> $$0.getDoubleOr(2, 0.0));
   private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
   public static final String SNBT_DATA_TAG = "data";
   private static final char PROPERTIES_START = '{';
   private static final char PROPERTIES_END = '}';
   private static final String ELEMENT_SEPARATOR = ",";
   private static final char KEY_VALUE_SEPARATOR = ':';
   private static final Splitter COMMA_SPLITTER = Splitter.on(",");
   private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int INDENT = 2;
   private static final int NOT_FOUND = -1;

   private NbtUtils() {
   }

   @VisibleForTesting
   public static boolean compareNbt(net.minecraft.nbt.Tag $$0, net.minecraft.nbt.Tag $$1, boolean $$2) {
      if ($$0 == $$1) {
         return true;
      } else if ($$0 == null) {
         return true;
      } else if ($$1 == null) {
         return false;
      } else if (!$$0.getClass().equals($$1.getClass())) {
         return false;
      } else if ($$0 instanceof net.minecraft.nbt.CompoundTag $$3) {
         net.minecraft.nbt.CompoundTag $$4 = (net.minecraft.nbt.CompoundTag)$$1;
         if ($$4.size() < $$3.size()) {
            return false;
         } else {
            for (Entry<String, net.minecraft.nbt.Tag> $$5 : $$3.entrySet()) {
               net.minecraft.nbt.Tag $$6 = $$5.getValue();
               if (!compareNbt($$6, $$4.get($$5.getKey()), $$2)) {
                  return false;
               }
            }

            return true;
         }
      } else if ($$0 instanceof net.minecraft.nbt.ListTag $$7 && $$2) {
         net.minecraft.nbt.ListTag $$8 = (net.minecraft.nbt.ListTag)$$1;
         if ($$7.isEmpty()) {
            return $$8.isEmpty();
         } else if ($$8.size() < $$7.size()) {
            return false;
         } else {
            for (net.minecraft.nbt.Tag $$9 : $$7) {
               boolean $$10 = false;

               for (net.minecraft.nbt.Tag $$11 : $$8) {
                  if (compareNbt($$9, $$11, $$2)) {
                     $$10 = true;
                     break;
                  }
               }

               if (!$$10) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return $$0.equals($$1);
      }
   }

   public static BlockState readBlockState(HolderGetter<Block> $$0, net.minecraft.nbt.CompoundTag $$1) {
      Optional<? extends Holder<Block>> $$2 = $$1.read("Name", BLOCK_NAME_CODEC).flatMap($$0::get);
      if ($$2.isEmpty()) {
         return Blocks.AIR.defaultBlockState();
      } else {
         Block $$3 = (Block)$$2.get().value();
         BlockState $$4 = $$3.defaultBlockState();
         Optional<net.minecraft.nbt.CompoundTag> $$5 = $$1.getCompound("Properties");
         if ($$5.isPresent()) {
            StateDefinition<Block, BlockState> $$6 = $$3.getStateDefinition();

            for (String $$7 : $$5.get().keySet()) {
               Property<?> $$8 = $$6.getProperty($$7);
               if ($$8 != null) {
                  $$4 = (BlockState & StateHolder)setValueHelper($$4, $$8, $$7, $$5.get(), $$1);
               }
            }
         }

         return $$4;
      }
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(
      S $$0, Property<T> $$1, String $$2, net.minecraft.nbt.CompoundTag $$3, net.minecraft.nbt.CompoundTag $$4
   ) {
      Optional<T> $$5 = $$3.getString($$2).flatMap($$1::getValue);
      if ($$5.isPresent()) {
         return (S)$$0.setValue($$1, $$5.get());
      } else {
         LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{$$2, $$3.get($$2), $$4});
         return $$0;
      }
   }

   public static net.minecraft.nbt.CompoundTag writeBlockState(BlockState $$0) {
      net.minecraft.nbt.CompoundTag $$1 = new net.minecraft.nbt.CompoundTag();
      $$1.putString("Name", BuiltInRegistries.BLOCK.getKey($$0.getBlock()).toString());
      Map<Property<?>, Comparable<?>> $$2 = $$0.getValues();
      if (!$$2.isEmpty()) {
         net.minecraft.nbt.CompoundTag $$3 = new net.minecraft.nbt.CompoundTag();

         for (Entry<Property<?>, Comparable<?>> $$4 : $$2.entrySet()) {
            Property<?> $$5 = $$4.getKey();
            $$3.putString($$5.getName(), getName($$5, $$4.getValue()));
         }

         $$1.put("Properties", $$3);
      }

      return $$1;
   }

   public static net.minecraft.nbt.CompoundTag writeFluidState(FluidState $$0) {
      net.minecraft.nbt.CompoundTag $$1 = new net.minecraft.nbt.CompoundTag();
      $$1.putString("Name", BuiltInRegistries.FLUID.getKey($$0.getType()).toString());
      Map<Property<?>, Comparable<?>> $$2 = $$0.getValues();
      if (!$$2.isEmpty()) {
         net.minecraft.nbt.CompoundTag $$3 = new net.minecraft.nbt.CompoundTag();

         for (Entry<Property<?>, Comparable<?>> $$4 : $$2.entrySet()) {
            Property<?> $$5 = $$4.getKey();
            $$3.putString($$5.getName(), getName($$5, $$4.getValue()));
         }

         $$1.put("Properties", $$3);
      }

      return $$1;
   }

   private static <T extends Comparable<T>> String getName(Property<T> $$0, Comparable<?> $$1) {
      return $$0.getName($$1);
   }

   public static String prettyPrint(net.minecraft.nbt.Tag $$0) {
      return prettyPrint($$0, false);
   }

   public static String prettyPrint(net.minecraft.nbt.Tag $$0, boolean $$1) {
      return prettyPrint(new StringBuilder(), $$0, 0, $$1).toString();
   }

   public static StringBuilder prettyPrint(StringBuilder $$0, net.minecraft.nbt.Tag $$1, int $$2, boolean $$3) {
      return switch ($$1) {
         case net.minecraft.nbt.PrimitiveTag $$4 -> $$0.append($$4);
         case net.minecraft.nbt.EndTag $$5 -> $$0;
         case net.minecraft.nbt.ByteArrayTag $$6 -> {
            byte[] $$7 = $$6.getAsByteArray();
            int $$8 = $$7.length;
            indent($$2, $$0).append("byte[").append($$8).append("] {\n");
            if ($$3) {
               indent($$2 + 1, $$0);

               for (int $$9 = 0; $$9 < $$7.length; $$9++) {
                  if ($$9 != 0) {
                     $$0.append(',');
                  }

                  if ($$9 % 16 == 0 && $$9 / 16 > 0) {
                     $$0.append('\n');
                     if ($$9 < $$7.length) {
                        indent($$2 + 1, $$0);
                     }
                  } else if ($$9 != 0) {
                     $$0.append(' ');
                  }

                  $$0.append(String.format(Locale.ROOT, "0x%02X", $$7[$$9] & 255));
               }
            } else {
               indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
            }

            $$0.append('\n');
            indent($$2, $$0).append('}');
            yield $$0;
         }
         case net.minecraft.nbt.ListTag $$10 -> {
            int $$11 = $$10.size();
            indent($$2, $$0).append("list").append("[").append($$11).append("] [");
            if ($$11 != 0) {
               $$0.append('\n');
            }

            for (int $$12 = 0; $$12 < $$11; $$12++) {
               if ($$12 != 0) {
                  $$0.append(",\n");
               }

               indent($$2 + 1, $$0);
               prettyPrint($$0, $$10.get($$12), $$2 + 1, $$3);
            }

            if ($$11 != 0) {
               $$0.append('\n');
            }

            indent($$2, $$0).append(']');
            yield $$0;
         }
         case net.minecraft.nbt.IntArrayTag $$13 -> {
            int[] $$14 = $$13.getAsIntArray();
            int $$15 = 0;

            for (int $$16 : $$14) {
               $$15 = Math.max($$15, String.format(Locale.ROOT, "%X", $$16).length());
            }

            int $$17 = $$14.length;
            indent($$2, $$0).append("int[").append($$17).append("] {\n");
            if ($$3) {
               indent($$2 + 1, $$0);

               for (int $$18 = 0; $$18 < $$14.length; $$18++) {
                  if ($$18 != 0) {
                     $$0.append(',');
                  }

                  if ($$18 % 16 == 0 && $$18 / 16 > 0) {
                     $$0.append('\n');
                     if ($$18 < $$14.length) {
                        indent($$2 + 1, $$0);
                     }
                  } else if ($$18 != 0) {
                     $$0.append(' ');
                  }

                  $$0.append(String.format(Locale.ROOT, "0x%0" + $$15 + "X", $$14[$$18]));
               }
            } else {
               indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
            }

            $$0.append('\n');
            indent($$2, $$0).append('}');
            yield $$0;
         }
         case net.minecraft.nbt.CompoundTag $$19 -> {
            List<String> $$20 = Lists.newArrayList($$19.keySet());
            Collections.sort($$20);
            indent($$2, $$0).append('{');
            if ($$0.length() - $$0.lastIndexOf("\n") > 2 * ($$2 + 1)) {
               $$0.append('\n');
               indent($$2 + 1, $$0);
            }

            int $$21 = $$20.stream().mapToInt(String::length).max().orElse(0);
            String $$22 = Strings.repeat(" ", $$21);

            for (int $$23 = 0; $$23 < $$20.size(); $$23++) {
               if ($$23 != 0) {
                  $$0.append(",\n");
               }

               String $$24 = $$20.get($$23);
               indent($$2 + 1, $$0).append('"').append($$24).append('"').append($$22, 0, $$22.length() - $$24.length()).append(": ");
               prettyPrint($$0, $$19.get($$24), $$2 + 1, $$3);
            }

            if (!$$20.isEmpty()) {
               $$0.append('\n');
            }

            indent($$2, $$0).append('}');
            yield $$0;
         }
         case net.minecraft.nbt.LongArrayTag $$25 -> {
            long[] $$26 = $$25.getAsLongArray();
            long $$27 = 0L;

            for (long $$28 : $$26) {
               $$27 = Math.max($$27, (long)String.format(Locale.ROOT, "%X", $$28).length());
            }

            long $$29 = $$26.length;
            indent($$2, $$0).append("long[").append($$29).append("] {\n");
            if ($$3) {
               indent($$2 + 1, $$0);

               for (int $$30 = 0; $$30 < $$26.length; $$30++) {
                  if ($$30 != 0) {
                     $$0.append(',');
                  }

                  if ($$30 % 16 == 0 && $$30 / 16 > 0) {
                     $$0.append('\n');
                     if ($$30 < $$26.length) {
                        indent($$2 + 1, $$0);
                     }
                  } else if ($$30 != 0) {
                     $$0.append(' ');
                  }

                  $$0.append(String.format(Locale.ROOT, "0x%0" + $$27 + "X", $$26[$$30]));
               }
            } else {
               indent($$2 + 1, $$0).append(" // Skipped, supply withBinaryBlobs true");
            }

            $$0.append('\n');
            indent($$2, $$0).append('}');
            yield $$0;
         }
         default -> throw new MatchException(null, null);
      };
   }

   private static StringBuilder indent(int $$0, StringBuilder $$1) {
      int $$2 = $$1.lastIndexOf("\n") + 1;
      int $$3 = $$1.length() - $$2;

      for (int $$4 = 0; $$4 < 2 * $$0 - $$3; $$4++) {
         $$1.append(' ');
      }

      return $$1;
   }

   public static Component toPrettyComponent(net.minecraft.nbt.Tag $$0) {
      return new net.minecraft.nbt.TextComponentTagVisitor("").visit($$0);
   }

   public static String structureToSnbt(net.minecraft.nbt.CompoundTag $$0) {
      return new net.minecraft.nbt.SnbtPrinterTagVisitor().visit(packStructureTemplate($$0));
   }

   public static net.minecraft.nbt.CompoundTag snbtToStructure(String $$0) throws CommandSyntaxException {
      return unpackStructureTemplate(net.minecraft.nbt.TagParser.parseCompoundFully($$0));
   }

   @VisibleForTesting
   static net.minecraft.nbt.CompoundTag packStructureTemplate(net.minecraft.nbt.CompoundTag $$0) {
      Optional<net.minecraft.nbt.ListTag> $$1 = $$0.getList("palettes");
      net.minecraft.nbt.ListTag $$2;
      if ($$1.isPresent()) {
         $$2 = $$1.get().getListOrEmpty(0);
      } else {
         $$2 = $$0.getListOrEmpty("palette");
      }

      net.minecraft.nbt.ListTag $$4 = $$2.compoundStream()
         .map(net.minecraft.nbt.NbtUtils::packBlockState)
         .map(net.minecraft.nbt.StringTag::valueOf)
         .collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new));
      $$0.put("palette", $$4);
      if ($$1.isPresent()) {
         net.minecraft.nbt.ListTag $$5 = new net.minecraft.nbt.ListTag();
         $$1.get().stream().flatMap($$0x -> $$0x.asList().stream()).forEach($$2x -> {
            net.minecraft.nbt.CompoundTag $$3x = new net.minecraft.nbt.CompoundTag();

            for (int $$4x = 0; $$4x < $$2x.size(); $$4x++) {
               $$3x.putString($$4.getString($$4x).orElseThrow(), packBlockState($$2x.getCompound($$4x).orElseThrow()));
            }

            $$5.add($$3x);
         });
         $$0.put("palettes", $$5);
      }

      Optional<net.minecraft.nbt.ListTag> $$6 = $$0.getList("entities");
      if ($$6.isPresent()) {
         net.minecraft.nbt.ListTag $$7 = $$6.get()
            .compoundStream()
            .sorted(Comparator.comparing($$0x -> $$0x.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_DOUBLE_COMPARATOR)))
            .collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new));
         $$0.put("entities", $$7);
      }

      net.minecraft.nbt.ListTag $$8 = $$0.getList("blocks")
         .stream()
         .flatMap(net.minecraft.nbt.ListTag::compoundStream)
         .sorted(Comparator.comparing($$0x -> $$0x.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_INT_COMPARATOR)))
         .peek($$1x -> $$1x.putString("state", $$4.getString($$1x.getIntOr("state", 0)).orElseThrow()))
         .collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new));
      $$0.put("data", $$8);
      $$0.remove("blocks");
      return $$0;
   }

   @VisibleForTesting
   static net.minecraft.nbt.CompoundTag unpackStructureTemplate(net.minecraft.nbt.CompoundTag $$0) {
      net.minecraft.nbt.ListTag $$1 = $$0.getListOrEmpty("palette");
      Map<String, net.minecraft.nbt.Tag> $$2 = $$1.stream()
         .flatMap($$0x -> $$0x.asString().stream())
         .collect(ImmutableMap.toImmutableMap(Function.identity(), net.minecraft.nbt.NbtUtils::unpackBlockState));
      Optional<net.minecraft.nbt.ListTag> $$3 = $$0.getList("palettes");
      if ($$3.isPresent()) {
         $$0.put(
            "palettes",
            $$3.get()
               .compoundStream()
               .map(
                  $$1x -> $$2.keySet()
                     .stream()
                     .map($$1xx -> $$1x.getString($$1xx).orElseThrow())
                     .map(net.minecraft.nbt.NbtUtils::unpackBlockState)
                     .collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new))
               )
               .collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new))
         );
         $$0.remove("palette");
      } else {
         $$0.put("palette", $$2.values().stream().collect(Collectors.toCollection(net.minecraft.nbt.ListTag::new)));
      }

      Optional<net.minecraft.nbt.ListTag> $$4 = $$0.getList("data");
      if ($$4.isPresent()) {
         Object2IntMap<String> $$5 = new Object2IntOpenHashMap();
         $$5.defaultReturnValue(-1);

         for (int $$6 = 0; $$6 < $$1.size(); $$6++) {
            $$5.put($$1.getString($$6).orElseThrow(), $$6);
         }

         net.minecraft.nbt.ListTag $$7 = $$4.get();

         for (int $$8 = 0; $$8 < $$7.size(); $$8++) {
            net.minecraft.nbt.CompoundTag $$9 = $$7.getCompound($$8).orElseThrow();
            String $$10 = $$9.getString("state").orElseThrow();
            int $$11 = $$5.getInt($$10);
            if ($$11 == -1) {
               throw new IllegalStateException("Entry " + $$10 + " missing from palette");
            }

            $$9.putInt("state", $$11);
         }

         $$0.put("blocks", $$7);
         $$0.remove("data");
      }

      return $$0;
   }

   @VisibleForTesting
   static String packBlockState(net.minecraft.nbt.CompoundTag $$0) {
      StringBuilder $$1 = new StringBuilder($$0.getString("Name").orElseThrow());
      $$0.getCompound("Properties")
         .ifPresent(
            $$1x -> {
               String $$2 = $$1x.entrySet()
                  .stream()
                  .sorted(Entry.comparingByKey())
                  .map($$0xx -> (String)$$0xx.getKey() + ":" + ((net.minecraft.nbt.Tag)$$0xx.getValue()).asString().orElseThrow())
                  .collect(Collectors.joining(","));
               $$1.append('{').append($$2).append('}');
            }
         );
      return $$1.toString();
   }

   @VisibleForTesting
   static net.minecraft.nbt.CompoundTag unpackBlockState(String $$0) {
      net.minecraft.nbt.CompoundTag $$1 = new net.minecraft.nbt.CompoundTag();
      int $$2 = $$0.indexOf(123);
      String $$3;
      if ($$2 >= 0) {
         $$3 = $$0.substring(0, $$2);
         net.minecraft.nbt.CompoundTag $$4 = new net.minecraft.nbt.CompoundTag();
         if ($$2 + 2 <= $$0.length()) {
            String $$5 = $$0.substring($$2 + 1, $$0.indexOf(125, $$2));
            COMMA_SPLITTER.split($$5).forEach($$2x -> {
               List<String> $$3x = COLON_SPLITTER.splitToList($$2x);
               if ($$3x.size() == 2) {
                  $$4.putString($$3x.get(0), $$3x.get(1));
               } else {
                  LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", $$0);
               }
            });
            $$1.put("Properties", $$4);
         }
      } else {
         $$3 = $$0;
      }

      $$1.putString("Name", $$3);
      return $$1;
   }

   public static net.minecraft.nbt.CompoundTag addCurrentDataVersion(net.minecraft.nbt.CompoundTag $$0) {
      int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
      return addDataVersion($$0, $$1);
   }

   public static net.minecraft.nbt.CompoundTag addDataVersion(net.minecraft.nbt.CompoundTag $$0, int $$1) {
      $$0.putInt("DataVersion", $$1);
      return $$0;
   }

   public static Dynamic<net.minecraft.nbt.Tag> addCurrentDataVersion(Dynamic<net.minecraft.nbt.Tag> $$0) {
      int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
      return addDataVersion($$0, $$1);
   }

   public static Dynamic<net.minecraft.nbt.Tag> addDataVersion(Dynamic<net.minecraft.nbt.Tag> $$0, int $$1) {
      return $$0.set("DataVersion", $$0.createInt($$1));
   }

   public static void addCurrentDataVersion(ValueOutput $$0) {
      int $$1 = SharedConstants.getCurrentVersion().dataVersion().version();
      addDataVersion($$0, $$1);
   }

   public static void addDataVersion(ValueOutput $$0, int $$1) {
      $$0.putInt("DataVersion", $$1);
   }

   public static int getDataVersion(net.minecraft.nbt.CompoundTag $$0) {
      return getDataVersion($$0, -1);
   }

   public static int getDataVersion(net.minecraft.nbt.CompoundTag $$0, int $$1) {
      return $$0.getIntOr("DataVersion", $$1);
   }

   public static int getDataVersion(Dynamic<?> $$0, int $$1) {
      return $$0.get("DataVersion").asInt($$1);
   }
}
