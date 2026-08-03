package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.data.DataSources;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<NbtContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.STRING.fieldOf("nbt").forGetter(NbtContents::getNbtPath),
            Codec.BOOL.lenientOptionalFieldOf("interpret", false).forGetter(NbtContents::isInterpreting),
            ComponentSerialization.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtContents::getSeparator),
            DataSources.CODEC.forGetter(NbtContents::getDataSource)
         )
         .apply($$0, NbtContents::new)
   );
   private final boolean interpreting;
   private final Optional<Component> separator;
   private final String nbtPathPattern;
   private final DataSource dataSource;
   @Nullable
   protected final NbtPath compiledNbtPath;

   public NbtContents(String $$0, boolean $$1, Optional<Component> $$2, DataSource $$3) {
      this($$0, compileNbtPath($$0), $$1, $$2, $$3);
   }

   private NbtContents(String $$0, @Nullable NbtPath $$1, boolean $$2, Optional<Component> $$3, DataSource $$4) {
      this.nbtPathPattern = $$0;
      this.compiledNbtPath = $$1;
      this.interpreting = $$2;
      this.separator = $$3;
      this.dataSource = $$4;
   }

   @Nullable
   private static NbtPath compileNbtPath(String $$0) {
      try {
         return new NbtPathArgument().parse(new StringReader($$0));
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public String getNbtPath() {
      return this.nbtPathPattern;
   }

   public boolean isInterpreting() {
      return this.interpreting;
   }

   public Optional<Component> getSeparator() {
      return this.separator;
   }

   public DataSource getDataSource() {
      return this.dataSource;
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0
         ? true
         : $$0 instanceof NbtContents $$1
            && this.dataSource.equals($$1.dataSource)
            && this.separator.equals($$1.separator)
            && this.interpreting == $$1.interpreting
            && this.nbtPathPattern.equals($$1.nbtPathPattern);
   }

   @Override
   public int hashCode() {
      int $$0 = this.interpreting ? 1 : 0;
      $$0 = 31 * $$0 + this.separator.hashCode();
      $$0 = 31 * $$0 + this.nbtPathPattern.hashCode();
      return 31 * $$0 + this.dataSource.hashCode();
   }

   @Override
   public String toString() {
      return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
   }

   @Override
   public MutableComponent resolve(@Nullable CommandSourceStack $$0, @Nullable Entity $$1, int $$2) throws CommandSyntaxException {
      if ($$0 != null && this.compiledNbtPath != null) {
         Stream<Tag> $$3 = this.dataSource.getData($$0).flatMap($$0x -> {
            try {
               return this.compiledNbtPath.get($$0x).stream();
            } catch (CommandSyntaxException var3x) {
               return Stream.empty();
            }
         });
         if (this.interpreting) {
            RegistryOps<Tag> $$4 = $$0.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            Component $$5 = (Component)DataFixUtils.orElse(
               ComponentUtils.updateForEntity($$0, this.separator, $$1, $$2), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
            );
            return $$3.flatMap($$4x -> {
               try {
                  Component $$5x = (Component)ComponentSerialization.CODEC.parse($$4, $$4x).getOrThrow();
                  return Stream.of(ComponentUtils.updateForEntity($$0, $$5x, $$1, $$2));
               } catch (Exception var6x) {
                  LOGGER.warn("Failed to parse component: {}", $$4x, var6x);
                  return Stream.of();
               }
            }).reduce(($$1x, $$2x) -> $$1x.append($$5).append($$2x)).orElseGet(Component::empty);
         } else {
            Stream<String> $$6 = $$3.map(NbtContents::asString);
            return ComponentUtils.updateForEntity($$0, this.separator, $$1, $$2)
               .map($$1x -> $$6.map(Component::literal).reduce(($$1xx, $$2x) -> $$1xx.append($$1x).append($$2x)).orElseGet(Component::empty))
               .orElseGet(() -> Component.literal($$6.collect(Collectors.joining(", "))));
         }
      } else {
         return Component.empty();
      }
   }

   private static String asString(Tag $$0) {
      if ($$0 instanceof StringTag var1) {
         StringTag var10000 = var1;

         try {
            var5 = var10000.value();
         } catch (Throwable var4) {
            throw new MatchException(var4.toString(), var4);
         }

         return var5;
      } else {
         return $$0.toString();
      }
   }

   @Override
   public MapCodec<NbtContents> codec() {
      return MAP_CODEC;
   }
}
