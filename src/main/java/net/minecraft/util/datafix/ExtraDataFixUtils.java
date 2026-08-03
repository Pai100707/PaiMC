package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class ExtraDataFixUtils {
   public static Dynamic<?> fixBlockPos(Dynamic<?> $$0) {
      Optional<Number> $$1 = $$0.get("X").asNumber().result();
      Optional<Number> $$2 = $$0.get("Y").asNumber().result();
      Optional<Number> $$3 = $$0.get("Z").asNumber().result();
      return !$$1.isEmpty() && !$$2.isEmpty() && !$$3.isEmpty() ? createBlockPos($$0, $$1.get().intValue(), $$2.get().intValue(), $$3.get().intValue()) : $$0;
   }

   public static Dynamic<?> fixInlineBlockPos(Dynamic<?> $$0, String $$1, String $$2, String $$3, String $$4) {
      Optional<Number> $$5 = $$0.get($$1).asNumber().result();
      Optional<Number> $$6 = $$0.get($$2).asNumber().result();
      Optional<Number> $$7 = $$0.get($$3).asNumber().result();
      return !$$5.isEmpty() && !$$6.isEmpty() && !$$7.isEmpty()
         ? $$0.remove($$1).remove($$2).remove($$3).set($$4, createBlockPos($$0, $$5.get().intValue(), $$6.get().intValue(), $$7.get().intValue()))
         : $$0;
   }

   public static Dynamic<?> createBlockPos(Dynamic<?> $$0, int $$1, int $$2, int $$3) {
      return $$0.createIntList(IntStream.of($$1, $$2, $$3));
   }

   public static <T, R> Typed<R> cast(Type<R> $$0, Typed<T> $$1) {
      return new Typed($$0, $$1.getOps(), $$1.getValue());
   }

   public static <T> Typed<T> cast(Type<T> $$0, Object $$1, DynamicOps<?> $$2) {
      return new Typed($$0, $$2, $$1);
   }

   public static Type<?> patchSubType(Type<?> $$0, Type<?> $$1, Type<?> $$2) {
      return $$0.all(typePatcher($$1, $$2), true, false).view().newType();
   }

   private static <A, B> TypeRewriteRule typePatcher(Type<A> $$0, Type<B> $$1) {
      RewriteResult<A, B> $$2 = RewriteResult.create(View.create("Patcher", $$0, $$1, $$0x -> $$0xx -> {
         throw new UnsupportedOperationException();
      }), new BitSet());
      return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame($$0, $$2), PointFreeRule.nop(), true, true);
   }

   @SafeVarargs
   public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... $$0) {
      return $$1 -> {
         for (Function<Typed<?>, Typed<?>> $$2 : $$0) {
            $$1 = $$2.apply($$1);
         }

         return $$1;
      };
   }

   public static Dynamic<?> blockState(String $$0, Map<String, String> $$1) {
      Dynamic<Tag> $$2 = new Dynamic(NbtOps.INSTANCE, new CompoundTag());
      Dynamic<Tag> $$3 = $$2.set("Name", $$2.createString($$0));
      if (!$$1.isEmpty()) {
         $$3 = $$3.set(
            "Properties",
            $$2.createMap(
               $$1.entrySet()
                  .stream()
                  .collect(Collectors.toMap($$1x -> $$2.createString((String)$$1x.getKey()), $$1x -> $$2.createString((String)$$1x.getValue())))
            )
         );
      }

      return $$3;
   }

   public static Dynamic<?> blockState(String $$0) {
      return blockState($$0, Map.of());
   }

   public static Dynamic<?> fixStringField(Dynamic<?> $$0, String $$1, UnaryOperator<String> $$2) {
      return $$0.update($$1, $$2x -> (Dynamic)DataFixUtils.orElse($$2x.asString().map($$2).map($$0::createString).result(), $$2x));
   }

   public static String dyeColorIdToName(int $$0) {
      return switch ($$0) {
         case 1 -> "orange";
         case 2 -> "magenta";
         case 3 -> "light_blue";
         case 4 -> "yellow";
         case 5 -> "lime";
         case 6 -> "pink";
         case 7 -> "gray";
         case 8 -> "light_gray";
         case 9 -> "cyan";
         case 10 -> "purple";
         case 11 -> "blue";
         case 12 -> "brown";
         case 13 -> "green";
         case 14 -> "red";
         case 15 -> "black";
         default -> "white";
      };
   }

   public static <T> Typed<?> readAndSet(Typed<?> $$0, OpticFinder<T> $$1, Dynamic<?> $$2) {
      return $$0.set($$1, net.minecraft.util.Util.readTypedOrThrow($$1.type(), $$2, true));
   }
}
