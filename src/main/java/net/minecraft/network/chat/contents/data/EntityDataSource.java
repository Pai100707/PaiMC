package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public record EntityDataSource(String selectorPattern, EntitySelector compiledSelector) implements DataSource {
   public static final MapCodec<EntityDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.STRING.fieldOf("entity").forGetter(EntityDataSource::selectorPattern)).apply($$0, EntityDataSource::new)
   );

   public EntityDataSource(String $$0) {
      this($$0, compileSelector($$0));
   }

   
   private static EntitySelector compileSelector(String $$0) {
      try {
         EntitySelectorParser $$1 = new EntitySelectorParser(new StringReader($$0), true);
         return $$1.parse();
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   @Override
   public Stream<CompoundTag> getData(CommandSourceStack $$0) throws CommandSyntaxException {
      if (this.compiledSelector != null) {
         List<? extends Entity> $$1 = this.compiledSelector.findEntities($$0);
         return $$1.stream().map(NbtPredicate::getEntityTagToCompare);
      } else {
         return Stream.empty();
      }
   }

   @Override
   public MapCodec<EntityDataSource> codec() {
      return MAP_CODEC;
   }

   @Override
   public String toString() {
      return "entity=" + this.selectorPattern;
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof EntityDataSource $$1 && this.selectorPattern.equals($$1.selectorPattern);
   }

   @Override
   public int hashCode() {
      return this.selectorPattern.hashCode();
   }
}
