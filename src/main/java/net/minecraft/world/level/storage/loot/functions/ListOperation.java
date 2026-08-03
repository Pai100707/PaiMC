package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public interface ListOperation {
   MapCodec<ListOperation> UNLIMITED_CODEC = codec(Integer.MAX_VALUE);

   static MapCodec<ListOperation> codec(int $$0) {
      return ListOperation.Type.CODEC.dispatchMap("mode", ListOperation::mode, $$0x -> $$0x.mapCodec).validate($$1 -> {
         if ($$1 instanceof ListOperation.ReplaceSection $$2 && $$2.size().isPresent()) {
            int $$3 = $$2.size().get();
            if ($$3 > $$0) {
               return DataResult.error(() -> "Size value too large: " + $$3 + ", max size is " + $$0);
            }
         }

         return DataResult.success($$1);
      });
   }

   ListOperation.Type mode();

   default <T> List<T> apply(List<T> $$0, List<T> $$1) {
      return this.apply($$0, $$1, Integer.MAX_VALUE);
   }

   <T> List<T> apply(List<T> var1, List<T> var2, int var3);

   public static class Append implements ListOperation {
      private static final Logger LOGGER = LogUtils.getLogger();
      public static final ListOperation.Append INSTANCE = new ListOperation.Append();
      public static final MapCodec<ListOperation.Append> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

      private Append() {
      }

      @Override
      public ListOperation.Type mode() {
         return ListOperation.Type.APPEND;
      }

      @Override
      public <T> List<T> apply(List<T> $$0, List<T> $$1, int $$2) {
         if ($$0.size() + $$1.size() > $$2) {
            LOGGER.error("Contents overflow in section append");
            return $$0;
         } else {
            return Stream.concat($$0.stream(), $$1.stream()).toList();
         }
      }
   }

   public record Insert(int offset) implements ListOperation {
      private static final Logger LOGGER = LogUtils.getLogger();
      public static final MapCodec<ListOperation.Insert> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.Insert::offset))
            .apply($$0, ListOperation.Insert::new)
      );

      @Override
      public ListOperation.Type mode() {
         return ListOperation.Type.INSERT;
      }

      @Override
      public <T> List<T> apply(List<T> $$0, List<T> $$1, int $$2) {
         int $$3 = $$0.size();
         if (this.offset > $$3) {
            LOGGER.error("Cannot insert when offset is out of bounds");
            return $$0;
         } else if ($$3 + $$1.size() > $$2) {
            LOGGER.error("Contents overflow in section insertion");
            return $$0;
         } else {
            Builder<T> $$4 = ImmutableList.builder();
            $$4.addAll($$0.subList(0, this.offset));
            $$4.addAll($$1);
            $$4.addAll($$0.subList(this.offset, $$3));
            return $$4.build();
         }
      }
   }

   public static class ReplaceAll implements ListOperation {
      public static final ListOperation.ReplaceAll INSTANCE = new ListOperation.ReplaceAll();
      public static final MapCodec<ListOperation.ReplaceAll> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

      private ReplaceAll() {
      }

      @Override
      public ListOperation.Type mode() {
         return ListOperation.Type.REPLACE_ALL;
      }

      @Override
      public <T> List<T> apply(List<T> $$0, List<T> $$1, int $$2) {
         return $$1;
      }
   }

   public record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation {
      private static final Logger LOGGER = LogUtils.getLogger();
      public static final MapCodec<ListOperation.ReplaceSection> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.ReplaceSection::offset),
               ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ListOperation.ReplaceSection::size)
            )
            .apply($$0, ListOperation.ReplaceSection::new)
      );

      public ReplaceSection(int $$0) {
         this($$0, Optional.empty());
      }

      @Override
      public ListOperation.Type mode() {
         return ListOperation.Type.REPLACE_SECTION;
      }

      @Override
      public <T> List<T> apply(List<T> $$0, List<T> $$1, int $$2) {
         int $$3 = $$0.size();
         if (this.offset > $$3) {
            LOGGER.error("Cannot replace when offset is out of bounds");
            return $$0;
         } else {
            Builder<T> $$4 = ImmutableList.builder();
            $$4.addAll($$0.subList(0, this.offset));
            $$4.addAll($$1);
            int $$5 = this.offset + this.size.orElse($$1.size());
            if ($$5 < $$3) {
               $$4.addAll($$0.subList($$5, $$3));
            }

            List<T> $$6 = $$4.build();
            if ($$6.size() > $$2) {
               LOGGER.error("Contents overflow in section replacement");
               return $$0;
            } else {
               return $$6;
            }
         }
      }
   }

   public record StandAlone<T>(List<T> value, ListOperation operation) {
      public static <T> Codec<ListOperation.StandAlone<T>> codec(Codec<T> $$0, int $$1) {
         return RecordCodecBuilder.create(
            $$2 -> $$2.group(
                  $$0.sizeLimitedListOf($$1).fieldOf("values").forGetter($$0xx -> $$0xx.value), ListOperation.codec($$1).forGetter($$0xx -> $$0xx.operation)
               )
               .apply($$2, ListOperation.StandAlone::new)
         );
      }

      public List<T> apply(List<T> $$0) {
         return this.operation.apply($$0, this.value);
      }
   }

   public static enum Type implements StringRepresentable {
      REPLACE_ALL("replace_all", ListOperation.ReplaceAll.MAP_CODEC),
      REPLACE_SECTION("replace_section", ListOperation.ReplaceSection.MAP_CODEC),
      INSERT("insert", ListOperation.Insert.MAP_CODEC),
      APPEND("append", ListOperation.Append.MAP_CODEC);

      public static final Codec<ListOperation.Type> CODEC = StringRepresentable.fromEnum(ListOperation.Type::values);
      private final String id;
      final MapCodec<? extends ListOperation> mapCodec;

      private Type(final String $$0, final MapCodec<? extends ListOperation> $$1) {
         this.id = $$0;
         this.mapCodec = $$1;
      }

      public MapCodec<? extends ListOperation> mapCodec() {
         return this.mapCodec;
      }

      public String getSerializedName() {
         return this.id;
      }
   }
}
