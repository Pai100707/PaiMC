package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource {
   public static final MapCodec<BlockDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply($$0, BlockDataSource::new)
   );

   public BlockDataSource(String $$0) {
      this($$0, compilePos($$0));
   }

   @Nullable
   private static Coordinates compilePos(String $$0) {
      try {
         return BlockPosArgument.blockPos().parse(new StringReader($$0));
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   @Override
   public Stream<CompoundTag> getData(CommandSourceStack $$0) {
      if (this.compiledPos != null) {
         ServerLevel $$1 = $$0.getLevel();
         BlockPos $$2 = this.compiledPos.getBlockPos($$0);
         if ($$1.isLoaded($$2)) {
            BlockEntity $$3 = $$1.getBlockEntity($$2);
            if ($$3 != null) {
               return Stream.of($$3.saveWithFullMetadata($$0.registryAccess()));
            }
         }
      }

      return Stream.empty();
   }

   @Override
   public MapCodec<BlockDataSource> codec() {
      return MAP_CODEC;
   }

   @Override
   public String toString() {
      return "block=" + this.posPattern;
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof BlockDataSource $$1 && this.posPattern.equals($$1.posPattern);
   }

   @Override
   public int hashCode() {
      return this.posPattern.hashCode();
   }
}
