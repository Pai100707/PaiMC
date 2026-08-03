package net.minecraft.world.scores;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.jspecify.annotations.Nullable;

public interface ScoreHolder {
   String WILDCARD_NAME = "*";
   net.minecraft.world.scores.ScoreHolder WILDCARD = new net.minecraft.world.scores.ScoreHolder() {
      @Override
      public String getScoreboardName() {
         return "*";
      }
   };

   String getScoreboardName();

   @Nullable
   default Component getDisplayName() {
      return null;
   }

   default Component getFeedbackDisplayName() {
      Component $$0 = this.getDisplayName();
      return $$0 != null
         ? $$0.copy().withStyle($$0x -> $$0x.withHoverEvent(new ShowText(Component.literal(this.getScoreboardName()))))
         : Component.literal(this.getScoreboardName());
   }

   static net.minecraft.world.scores.ScoreHolder forNameOnly(final String $$0) {
      if ($$0.equals("*")) {
         return WILDCARD;
      } else {
         final Component $$1 = Component.literal($$0);
         return new net.minecraft.world.scores.ScoreHolder() {
            @Override
            public String getScoreboardName() {
               return $$0;
            }

            @Override
            public Component getFeedbackDisplayName() {
               return $$1;
            }
         };
      }
   }

   static net.minecraft.world.scores.ScoreHolder fromGameProfile(GameProfile $$0) {
      final String $$1 = $$0.name();
      return new net.minecraft.world.scores.ScoreHolder() {
         @Override
         public String getScoreboardName() {
            return $$1;
         }
      };
   }
}
