package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.RunCommand;

public record CommandTemplate(ParsedTemplate template) implements Action {
   public static final MapCodec<CommandTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ParsedTemplate.CODEC.fieldOf("template").forGetter(CommandTemplate::template)).apply($$0, CommandTemplate::new)
   );

   @Override
   public MapCodec<CommandTemplate> codec() {
      return MAP_CODEC;
   }

   @Override
   public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> $$0) {
      String $$1 = this.template.instantiate(Action.ValueGetter.getAsTemplateSubstitutions($$0));
      return Optional.of(new RunCommand($$1));
   }
}
