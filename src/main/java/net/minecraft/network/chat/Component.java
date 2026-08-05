package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

public interface Component extends Message, FormattedText {
   Style getStyle();

   ComponentContents getContents();

   @Override
   default String getString() {
      return FormattedText.super.getString();
   }

   default String getString(int $$0) {
      StringBuilder $$1 = new StringBuilder();
      this.visit($$2 -> {
         int $$3 = $$0 - $$1.length();
         if ($$3 <= 0) {
            return STOP_ITERATION;
         } else {
            $$1.append($$2.length() <= $$3 ? $$2 : $$2.substring(0, $$3));
            return Optional.empty();
         }
      });
      return $$1.toString();
   }

   List<Component> getSiblings();

   
   default String tryCollapseToString() {
      return this.getContents() instanceof PlainTextContents $$0 && this.getSiblings().isEmpty() && this.getStyle().isEmpty() ? $$0.text() : null;
   }

   default MutableComponent plainCopy() {
      return MutableComponent.create(this.getContents());
   }

   default MutableComponent copy() {
      return new MutableComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
   }

   FormattedCharSequence getVisualOrderText();

   @Override
   default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
      Style $$2 = this.getStyle().applyTo($$1);
      Optional<T> $$3 = this.getContents().visit($$0, $$2);
      if ($$3.isPresent()) {
         return $$3;
      } else {
         for (Component $$4 : this.getSiblings()) {
            Optional<T> $$5 = $$4.visit($$0, $$2);
            if ($$5.isPresent()) {
               return $$5;
            }
         }

         return Optional.empty();
      }
   }

   @Override
   default <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
      Optional<T> $$1 = this.getContents().visit($$0);
      if ($$1.isPresent()) {
         return $$1;
      } else {
         for (Component $$2 : this.getSiblings()) {
            Optional<T> $$3 = $$2.visit($$0);
            if ($$3.isPresent()) {
               return $$3;
            }
         }

         return Optional.empty();
      }
   }

   default List<Component> toFlatList() {
      return this.toFlatList(Style.EMPTY);
   }

   default List<Component> toFlatList(Style $$0) {
      List<Component> $$1 = Lists.newArrayList();
      this.visit(($$1x, $$2) -> {
         if (!$$2.isEmpty()) {
            $$1.add(literal($$2).withStyle($$1x));
         }

         return Optional.empty();
      }, $$0);
      return $$1;
   }

   default boolean contains(Component $$0) {
      if (this.equals($$0)) {
         return true;
      } else {
         List<Component> $$1 = this.toFlatList();
         List<Component> $$2 = $$0.toFlatList(this.getStyle());
         return Collections.indexOfSubList($$1, $$2) != -1;
      }
   }

   static Component nullToEmpty(String $$0) {
      return (Component)($$0 != null ? literal($$0) : CommonComponents.EMPTY);
   }

   static MutableComponent literal(String $$0) {
      return MutableComponent.create(PlainTextContents.create($$0));
   }

   static MutableComponent translatable(String $$0) {
      return MutableComponent.create(new TranslatableContents($$0, null, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatable(String $$0, Object... $$1) {
      return MutableComponent.create(new TranslatableContents($$0, null, $$1));
   }

   static MutableComponent translatableEscape(String $$0, Object... $$1) {
      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         Object $$3 = $$1[$$2];
         if (!TranslatableContents.isAllowedPrimitiveArgument($$3) && !($$3 instanceof Component)) {
            $$1[$$2] = String.valueOf($$3);
         }
      }

      return translatable($$0, $$1);
   }

   static MutableComponent translatableWithFallback(String $$0, String $$1) {
      return MutableComponent.create(new TranslatableContents($$0, $$1, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatableWithFallback(String $$0, String $$1, Object... $$2) {
      return MutableComponent.create(new TranslatableContents($$0, $$1, $$2));
   }

   static MutableComponent empty() {
      return MutableComponent.create(PlainTextContents.EMPTY);
   }

   static MutableComponent keybind(String $$0) {
      return MutableComponent.create(new KeybindContents($$0));
   }

   static MutableComponent nbt(String $$0, boolean $$1, Optional<Component> $$2, DataSource $$3) {
      return MutableComponent.create(new NbtContents($$0, $$1, $$2, $$3));
   }

   static MutableComponent score(SelectorPattern $$0, String $$1) {
      return MutableComponent.create(new ScoreContents(Either.left($$0), $$1));
   }

   static MutableComponent score(String $$0, String $$1) {
      return MutableComponent.create(new ScoreContents(Either.right($$0), $$1));
   }

   static MutableComponent selector(SelectorPattern $$0, Optional<Component> $$1) {
      return MutableComponent.create(new SelectorContents($$0, $$1));
   }

   static MutableComponent object(ObjectInfo $$0) {
      return MutableComponent.create(new ObjectContents($$0));
   }

   static Component translationArg(Date $$0) {
      return literal($$0.toString());
   }

   static Component translationArg(Message $$0) {
      return (Component)($$0 instanceof Component $$1 ? $$1 : literal($$0.getString()));
   }

   static Component translationArg(UUID $$0) {
      return literal($$0.toString());
   }

   static Component translationArg(Identifier $$0) {
      return literal($$0.toString());
   }

   static Component translationArg(ChunkPos $$0) {
      return literal($$0.toString());
   }

   static Component translationArg(URI $$0) {
      return literal($$0.toString());
   }
}
