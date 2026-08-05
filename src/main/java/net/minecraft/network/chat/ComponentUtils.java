package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.annotation.CheckReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
   public static final String DEFAULT_SEPARATOR_TEXT = ", ";
   public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
   public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

   @CheckReturnValue
   public static MutableComponent mergeStyles(MutableComponent $$0, Style $$1) {
      if ($$1.isEmpty()) {
         return $$0;
      } else {
         Style $$2 = $$0.getStyle();
         if ($$2.isEmpty()) {
            return $$0.setStyle($$1);
         } else {
            return $$2.equals($$1) ? $$0 : $$0.setStyle($$2.applyTo($$1));
         }
      }
   }

   @CheckReturnValue
   public static Component mergeStyles(Component $$0, Style $$1) {
      if ($$1.isEmpty()) {
         return $$0;
      } else {
         Style $$2 = $$0.getStyle();
         if ($$2.isEmpty()) {
            return $$0.copy().setStyle($$1);
         } else {
            return (Component)($$2.equals($$1) ? $$0 : $$0.copy().setStyle($$2.applyTo($$1)));
         }
      }
   }

   public static Optional<MutableComponent> updateForEntity(CommandSourceStack $$0, Optional<Component> $$1, Entity $$2, int $$3) throws CommandSyntaxException {
      return $$1.isPresent() ? Optional.of(updateForEntity($$0, $$1.get(), $$2, $$3)) : Optional.empty();
   }

   public static MutableComponent updateForEntity(CommandSourceStack $$0, Component $$1, Entity $$2, int $$3) throws CommandSyntaxException {
      if ($$3 > 100) {
         return $$1.copy();
      } else {
         MutableComponent $$4 = $$1.getContents().resolve($$0, $$2, $$3 + 1);

         for (Component $$5 : $$1.getSiblings()) {
            $$4.append(updateForEntity($$0, $$5, $$2, $$3 + 1));
         }

         return $$4.withStyle(resolveStyle($$0, $$1.getStyle(), $$2, $$3));
      }
   }

   private static Style resolveStyle(CommandSourceStack $$0, Style $$1, Entity $$2, int $$3) throws CommandSyntaxException {
      if ($$1.getHoverEvent() instanceof HoverEvent.ShowText(Component $$6)) {
         HoverEvent $$6x = new HoverEvent.ShowText(updateForEntity($$0, $$6, $$2, $$3 + 1));
         return $$1.withHoverEvent($$6x);
      } else {
         return $$1;
      }
   }

   public static Component formatList(Collection<String> $$0) {
      return formatAndSortList($$0, $$0x -> Component.literal($$0x).withStyle(ChatFormatting.GREEN));
   }

   public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> $$0, Function<T, Component> $$1) {
      if ($$0.isEmpty()) {
         return CommonComponents.EMPTY;
      } else if ($$0.size() == 1) {
         return $$1.apply($$0.iterator().next());
      } else {
         List<T> $$2 = Lists.newArrayList($$0);
         $$2.sort(Comparable::compareTo);
         return formatList($$2, $$1);
      }
   }

   public static <T> Component formatList(Collection<? extends T> $$0, Function<T, Component> $$1) {
      return formatList($$0, DEFAULT_SEPARATOR, $$1);
   }

   public static <T> MutableComponent formatList(Collection<? extends T> $$0, Optional<? extends Component> $$1, Function<T, Component> $$2) {
      return formatList($$0, (Component)DataFixUtils.orElse($$1, DEFAULT_SEPARATOR), $$2);
   }

   public static Component formatList(Collection<? extends Component> $$0, Component $$1) {
      return formatList($$0, $$1, Function.identity());
   }

   public static <T> MutableComponent formatList(Collection<? extends T> $$0, Component $$1, Function<T, Component> $$2) {
      if ($$0.isEmpty()) {
         return Component.empty();
      } else if ($$0.size() == 1) {
         return $$2.apply((T)$$0.iterator().next()).copy();
      } else {
         MutableComponent $$3 = Component.empty();
         boolean $$4 = true;

         for (T $$5 : $$0) {
            if (!$$4) {
               $$3.append($$1);
            }

            $$3.append($$2.apply($$5));
            $$4 = false;
         }

         return $$3;
      }
   }

   public static MutableComponent wrapInSquareBrackets(Component $$0) {
      return Component.translatable("chat.square_brackets", $$0);
   }

   public static Component fromMessage(Message $$0) {
      return (Component)($$0 instanceof Component $$1 ? $$1 : Component.literal($$0.getString()));
   }

   public static boolean isTranslationResolvable(Component $$0) {
      if ($$0 != null && $$0.getContents() instanceof TranslatableContents $$1) {
         String $$2 = $$1.getKey();
         String $$3 = $$1.getFallback();
         return $$3 != null || Language.getInstance().has($$2);
      } else {
         return true;
      }
   }

   public static MutableComponent copyOnClickText(String $$0) {
      return wrapInSquareBrackets(
         Component.literal($$0)
            .withStyle(
               (UnaryOperator<Style>)($$1 -> $$1.withColor(ChatFormatting.GREEN)
                  .withClickEvent(new ClickEvent.CopyToClipboard($$0))
                  .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click")))
                  .withInsertion($$0))
            )
      );
   }
}
