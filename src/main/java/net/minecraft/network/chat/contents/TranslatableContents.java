package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {
   public static final Object[] NO_ARGS = new Object[0];
   private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.JAVA.validate(TranslatableContents::filterAllowedArguments);
   private static final Codec<Object> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC)
      .xmap(
         $$0 -> $$0.map($$0x -> $$0x, $$0x -> Objects.requireNonNullElse($$0x.tryCollapseToString(), $$0x)),
         $$0 -> $$0 instanceof Component $$1 ? Either.right($$1) : Either.left($$0)
      );
   public static final MapCodec<TranslatableContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.STRING.fieldOf("translate").forGetter($$0x -> $$0x.key),
            Codec.STRING.lenientOptionalFieldOf("fallback").forGetter($$0x -> Optional.ofNullable($$0x.fallback)),
            ARG_CODEC.listOf().optionalFieldOf("with").forGetter($$0x -> adjustArgs($$0x.args))
         )
         .apply($$0, TranslatableContents::create)
   );
   private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
   private static final FormattedText TEXT_NULL = FormattedText.of("null");
   private final String key;
   
   private final String fallback;
   private final Object[] args;
   
   private Language decomposedWith;
   private List<FormattedText> decomposedParts = ImmutableList.of();
   private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   private static DataResult<Object> filterAllowedArguments(Object $$0) {
      return !isAllowedPrimitiveArgument($$0) ? DataResult.error(() -> "This value needs to be parsed as component") : DataResult.success($$0);
   }

   public static boolean isAllowedPrimitiveArgument(Object $$0) {
      return $$0 instanceof Number || $$0 instanceof Boolean || $$0 instanceof String;
   }

   private static Optional<List<Object>> adjustArgs(Object[] $$0) {
      return $$0.length == 0 ? Optional.empty() : Optional.of(Arrays.asList($$0));
   }

   private static Object[] adjustArgs(Optional<List<Object>> $$0) {
      return $$0.<Object[]>map($$0x -> $$0x.isEmpty() ? NO_ARGS : $$0x.toArray()).orElse(NO_ARGS);
   }

   private static TranslatableContents create(String $$0, Optional<String> $$1, Optional<List<Object>> $$2) {
      return new TranslatableContents($$0, $$1.orElse(null), adjustArgs($$2));
   }

   public TranslatableContents(String $$0, String $$1, Object[] $$2) {
      this.key = $$0;
      this.fallback = $$1;
      this.args = $$2;
   }

   @Override
   public MapCodec<TranslatableContents> codec() {
      return MAP_CODEC;
   }

   private void decompose() {
      Language $$0 = Language.getInstance();
      if ($$0 != this.decomposedWith) {
         this.decomposedWith = $$0;
         String $$1 = this.fallback != null ? $$0.getOrDefault(this.key, this.fallback) : $$0.getOrDefault(this.key);

         try {
            Builder<FormattedText> $$2 = ImmutableList.builder();
            this.decomposeTemplate($$1, $$2::add);
            this.decomposedParts = $$2.build();
         } catch (TranslatableFormatException var4) {
            this.decomposedParts = ImmutableList.of(FormattedText.of($$1));
         }
      }
   }

   private void decomposeTemplate(String $$0, Consumer<FormattedText> $$1) {
      Matcher $$2 = FORMAT_PATTERN.matcher($$0);

      try {
         int $$3 = 0;
         int $$4 = 0;

         while ($$2.find($$4)) {
            int $$5 = $$2.start();
            int $$6 = $$2.end();
            if ($$5 > $$4) {
               String $$7 = $$0.substring($$4, $$5);
               if ($$7.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               $$1.accept(FormattedText.of($$7));
            }

            String $$8 = $$2.group(2);
            String $$9 = $$0.substring($$5, $$6);
            if ("%".equals($$8) && "%%".equals($$9)) {
               $$1.accept(TEXT_PERCENT);
            } else {
               if (!"s".equals($$8)) {
                  throw new TranslatableFormatException(this, "Unsupported format: '" + $$9 + "'");
               }

               String $$10 = $$2.group(1);
               int $$11 = $$10 != null ? Integer.parseInt($$10) - 1 : $$3++;
               $$1.accept(this.getArgument($$11));
            }

            $$4 = $$6;
         }

         if ($$4 < $$0.length()) {
            String $$12 = $$0.substring($$4);
            if ($$12.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            $$1.accept(FormattedText.of($$12));
         }
      } catch (IllegalArgumentException var12) {
         throw new TranslatableFormatException(this, var12);
      }
   }

   private FormattedText getArgument(int $$0) {
      if ($$0 >= 0 && $$0 < this.args.length) {
         Object $$1 = this.args[$$0];
         if ($$1 instanceof Component $$2) {
            return $$2;
         } else {
            return $$1 == null ? TEXT_NULL : FormattedText.of($$1.toString());
         }
      } else {
         throw new TranslatableFormatException(this, $$0);
      }
   }

   @Override
   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
      this.decompose();

      for (FormattedText $$2 : this.decomposedParts) {
         Optional<T> $$3 = $$2.visit($$0, $$1);
         if ($$3.isPresent()) {
            return $$3;
         }
      }

      return Optional.empty();
   }

   @Override
   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
      this.decompose();

      for (FormattedText $$1 : this.decomposedParts) {
         Optional<T> $$2 = $$1.visit($$0);
         if ($$2.isPresent()) {
            return $$2;
         }
      }

      return Optional.empty();
   }

   @Override
   public MutableComponent resolve(CommandSourceStack $$0, Entity $$1, int $$2) throws CommandSyntaxException {
      Object[] $$3 = new Object[this.args.length];

      for (int $$4 = 0; $$4 < $$3.length; $$4++) {
         Object $$5 = this.args[$$4];
         if ($$5 instanceof Component $$6) {
            $$3[$$4] = ComponentUtils.updateForEntity($$0, $$6, $$1, $$2);
         } else {
            $$3[$$4] = $$5;
         }
      }

      return MutableComponent.create(new TranslatableContents(this.key, this.fallback, $$3));
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0
         ? true
         : $$0 instanceof TranslatableContents $$1
            && Objects.equals(this.key, $$1.key)
            && Objects.equals(this.fallback, $$1.fallback)
            && Arrays.equals(this.args, $$1.args);
   }

   @Override
   public int hashCode() {
      int $$0 = Objects.hashCode(this.key);
      $$0 = 31 * $$0 + Objects.hashCode(this.fallback);
      return 31 * $$0 + Arrays.hashCode(this.args);
   }

   @Override
   public String toString() {
      return "translation{key='"
         + this.key
         + "'"
         + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "")
         + ", args="
         + Arrays.toString(this.args)
         + "}";
   }

   public String getKey() {
      return this.key;
   }

   
   public String getFallback() {
      return this.fallback;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
