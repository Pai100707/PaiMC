package net.minecraft.server.dialog.action;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;

public interface Action {
   Codec<Action> CODEC = BuiltInRegistries.DIALOG_ACTION_TYPE.byNameCodec().dispatch(Action::codec, $$0 -> $$0);

   MapCodec<? extends Action> codec();

   Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> var1);

   public interface ValueGetter {
      String asTemplateSubstitution();

      Tag asTag();

      static Map<String, String> getAsTemplateSubstitutions(Map<String, Action.ValueGetter> $$0) {
         return Maps.transformValues($$0, Action.ValueGetter::asTemplateSubstitution);
      }

      static Action.ValueGetter of(final String $$0) {
         return new Action.ValueGetter() {
            @Override
            public String asTemplateSubstitution() {
               return $$0;
            }

            @Override
            public Tag asTag() {
               return StringTag.valueOf($$0);
            }
         };
      }

      static Action.ValueGetter of(final Supplier<String> $$0) {
         return new Action.ValueGetter() {
            @Override
            public String asTemplateSubstitution() {
               return $$0.get();
            }

            @Override
            public Tag asTag() {
               return StringTag.valueOf($$0.get());
            }
         };
      }
   }
}
