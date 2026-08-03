package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.util.Util;

public record StaticAction(ClickEvent value) implements Action {
   public static final Map<net.minecraft.network.chat.ClickEvent.Action, MapCodec<StaticAction>> WRAPPED_CODECS = (Map<net.minecraft.network.chat.ClickEvent.Action, MapCodec<StaticAction>>)Util.make(
      () -> {
         Map<net.minecraft.network.chat.ClickEvent.Action, MapCodec<StaticAction>> $$0 = new EnumMap<>(net.minecraft.network.chat.ClickEvent.Action.class);

         for (net.minecraft.network.chat.ClickEvent.Action $$1 : net.minecraft.network.chat.ClickEvent.Action.class.getEnumConstants()) {
            if ($$1.isAllowedFromServer()) {
               MapCodec<ClickEvent> $$2 = $$1.valueCodec();
               $$0.put($$1, $$2.xmap(StaticAction::new, StaticAction::value));
            }
         }

         return Collections.unmodifiableMap($$0);
      }
   );

   @Override
   public MapCodec<StaticAction> codec() {
      return WRAPPED_CODECS.get(this.value.action());
   }

   @Override
   public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> $$0) {
      return Optional.of(this.value);
   }
}
