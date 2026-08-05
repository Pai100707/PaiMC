package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

public record SelectorContents(SelectorPattern selector, Optional<Component> separator) implements ComponentContents {
   public static final MapCodec<SelectorContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            SelectorPattern.CODEC.fieldOf("selector").forGetter(SelectorContents::selector),
            ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::separator)
         )
         .apply($$0, SelectorContents::new)
   );

   @Override
   public MapCodec<SelectorContents> codec() {
      return MAP_CODEC;
   }

   @Override
   public MutableComponent resolve(CommandSourceStack $$0, Entity $$1, int $$2) throws CommandSyntaxException {
      if ($$0 == null) {
         return Component.empty();
      } else {
         Optional<? extends Component> $$3 = ComponentUtils.updateForEntity($$0, this.separator, $$1, $$2);
         return ComponentUtils.formatList(this.selector.resolved().findEntities($$0), $$3, Entity::getDisplayName);
      }
   }

   @Override
   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
      return $$0.accept($$1, this.selector.pattern());
   }

   @Override
   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
      return $$0.accept(this.selector.pattern());
   }

   @Override
   public String toString() {
      return "pattern{" + this.selector + "}";
   }
}
