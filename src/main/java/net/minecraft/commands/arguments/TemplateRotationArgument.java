package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.level.block.Rotation;

public class TemplateRotationArgument extends StringRepresentableArgument<Rotation> {
   private TemplateRotationArgument() {
      super(Rotation.CODEC, Rotation::values);
   }

   public static TemplateRotationArgument templateRotation() {
      return new TemplateRotationArgument();
   }

   public static Rotation getRotation(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Rotation)$$0.getArgument($$1, Rotation.class);
   }
}
