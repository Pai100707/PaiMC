package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;

public record BedRule(
   net.minecraft.world.attribute.BedRule.Rule canSleep,
   net.minecraft.world.attribute.BedRule.Rule canSetSpawn,
   boolean explodes,
   Optional<Component> errorMessage
) {
   public static final net.minecraft.world.attribute.BedRule CAN_SLEEP_WHEN_DARK = new net.minecraft.world.attribute.BedRule(
      net.minecraft.world.attribute.BedRule.Rule.WHEN_DARK,
      net.minecraft.world.attribute.BedRule.Rule.ALWAYS,
      false,
      Optional.of(Component.translatable("block.minecraft.bed.no_sleep"))
   );
   public static final net.minecraft.world.attribute.BedRule EXPLODES = new net.minecraft.world.attribute.BedRule(
      net.minecraft.world.attribute.BedRule.Rule.NEVER, net.minecraft.world.attribute.BedRule.Rule.NEVER, true, Optional.empty()
   );
   public static final Codec<net.minecraft.world.attribute.BedRule> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.world.attribute.BedRule.Rule.CODEC.fieldOf("can_sleep").forGetter(net.minecraft.world.attribute.BedRule::canSleep),
            net.minecraft.world.attribute.BedRule.Rule.CODEC.fieldOf("can_set_spawn").forGetter(net.minecraft.world.attribute.BedRule::canSetSpawn),
            Codec.BOOL.optionalFieldOf("explodes", false).forGetter(net.minecraft.world.attribute.BedRule::explodes),
            ComponentSerialization.CODEC.optionalFieldOf("error_message").forGetter(net.minecraft.world.attribute.BedRule::errorMessage)
         )
         .apply($$0, net.minecraft.world.attribute.BedRule::new)
   );

   public boolean canSleep(Level $$0) {
      return this.canSleep.test($$0);
   }

   public boolean canSetSpawn(Level $$0) {
      return this.canSetSpawn.test($$0);
   }

   public BedSleepingProblem asProblem() {
      return new BedSleepingProblem(this.errorMessage.orElse(null));
   }

   public static enum Rule implements StringRepresentable {
      ALWAYS("always"),
      WHEN_DARK("when_dark"),
      NEVER("never");

      public static final Codec<net.minecraft.world.attribute.BedRule.Rule> CODEC = StringRepresentable.fromEnum(
         net.minecraft.world.attribute.BedRule.Rule::values
      );
      private final String name;

      private Rule(final String $$0) {
         this.name = $$0;
      }

      public boolean test(Level $$0) {
         return switch (this) {
            case ALWAYS -> true;
            case WHEN_DARK -> $$0.isDarkOutside();
            case NEVER -> false;
         };
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
