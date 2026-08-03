package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class HeightmapTypeArgument extends StringRepresentableArgument<Types> {
   private static final Codec<Types> LOWER_CASE_CODEC = StringRepresentable.fromEnumWithMapping(
      HeightmapTypeArgument::keptTypes, $$0 -> $$0.toLowerCase(Locale.ROOT)
   );

   private static Types[] keptTypes() {
      return Arrays.stream(Types.values()).filter(Types::keepAfterWorldgen).toArray(Types[]::new);
   }

   private HeightmapTypeArgument() {
      super(LOWER_CASE_CODEC, HeightmapTypeArgument::keptTypes);
   }

   public static HeightmapTypeArgument heightmap() {
      return new HeightmapTypeArgument();
   }

   public static Types getHeightmap(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Types)$$0.getArgument($$1, Types.class);
   }

   @Override
   protected String convertId(String $$0) {
      return $$0.toLowerCase(Locale.ROOT);
   }
}
