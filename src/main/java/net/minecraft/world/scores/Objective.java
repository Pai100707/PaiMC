package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
   private final net.minecraft.world.scores.Scoreboard scoreboard;
   private final String name;
   private final ObjectiveCriteria criteria;
   private Component displayName;
   private Component formattedDisplayName;
   private ObjectiveCriteria.RenderType renderType;
   private boolean displayAutoUpdate;
   
   private NumberFormat numberFormat;

   public Objective(
      net.minecraft.world.scores.Scoreboard $$0,
      String $$1,
      ObjectiveCriteria $$2,
      Component $$3,
      ObjectiveCriteria.RenderType $$4,
      boolean $$5,
      NumberFormat $$6
   ) {
      this.scoreboard = $$0;
      this.name = $$1;
      this.criteria = $$2;
      this.displayName = $$3;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.renderType = $$4;
      this.displayAutoUpdate = $$5;
      this.numberFormat = $$6;
   }

   public net.minecraft.world.scores.Objective.Packed pack() {
      return new net.minecraft.world.scores.Objective.Packed(
         this.name, this.criteria, this.displayName, this.renderType, this.displayAutoUpdate, Optional.ofNullable(this.numberFormat)
      );
   }

   public net.minecraft.world.scores.Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public String getName() {
      return this.name;
   }

   public ObjectiveCriteria getCriteria() {
      return this.criteria;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public boolean displayAutoUpdate() {
      return this.displayAutoUpdate;
   }

   
   public NumberFormat numberFormat() {
      return this.numberFormat;
   }

   public NumberFormat numberFormatOrDefault(NumberFormat $$0) {
      return Objects.requireNonNullElse(this.numberFormat, $$0);
   }

   private Component createFormattedDisplayName() {
      return ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle($$0 -> $$0.withHoverEvent(new ShowText(Component.literal(this.name)))));
   }

   public Component getFormattedDisplayName() {
      return this.formattedDisplayName;
   }

   public void setDisplayName(Component $$0) {
      this.displayName = $$0;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.scoreboard.onObjectiveChanged(this);
   }

   public ObjectiveCriteria.RenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderType(ObjectiveCriteria.RenderType $$0) {
      this.renderType = $$0;
      this.scoreboard.onObjectiveChanged(this);
   }

   public void setDisplayAutoUpdate(boolean $$0) {
      this.displayAutoUpdate = $$0;
      this.scoreboard.onObjectiveChanged(this);
   }

   public void setNumberFormat(NumberFormat $$0) {
      this.numberFormat = $$0;
      this.scoreboard.onObjectiveChanged(this);
   }

   public record Packed(
      String name,
      ObjectiveCriteria criteria,
      Component displayName,
      ObjectiveCriteria.RenderType renderType,
      boolean displayAutoUpdate,
      Optional<NumberFormat> numberFormat
   ) {
      public static final Codec<net.minecraft.world.scores.Objective.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("Name").forGetter(net.minecraft.world.scores.Objective.Packed::name),
               ObjectiveCriteria.CODEC
                  .optionalFieldOf("CriteriaName", ObjectiveCriteria.DUMMY)
                  .forGetter(net.minecraft.world.scores.Objective.Packed::criteria),
               ComponentSerialization.CODEC.fieldOf("DisplayName").forGetter(net.minecraft.world.scores.Objective.Packed::displayName),
               ObjectiveCriteria.RenderType.CODEC
                  .optionalFieldOf("RenderType", ObjectiveCriteria.RenderType.INTEGER)
                  .forGetter(net.minecraft.world.scores.Objective.Packed::renderType),
               Codec.BOOL.optionalFieldOf("display_auto_update", false).forGetter(net.minecraft.world.scores.Objective.Packed::displayAutoUpdate),
               NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(net.minecraft.world.scores.Objective.Packed::numberFormat)
            )
            .apply($$0, net.minecraft.world.scores.Objective.Packed::new)
      );
   }
}
