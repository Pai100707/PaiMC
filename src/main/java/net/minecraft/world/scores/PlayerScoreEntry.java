package net.minecraft.world.scores;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public record PlayerScoreEntry(String owner, int value, Component display, NumberFormat numberFormatOverride) {
   public boolean isHidden() {
      return this.owner.startsWith("#");
   }

   public Component ownerName() {
      return (Component)(this.display != null ? this.display : Component.literal(this.owner()));
   }

   public MutableComponent formatValue(NumberFormat $$0) {
      return Objects.requireNonNullElse(this.numberFormatOverride, $$0).format(this.value);
   }
}
