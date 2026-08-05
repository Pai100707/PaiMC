package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;

public record SignableCommand<S>(List<SignableCommand.Argument<S>> arguments) {
   public static <S> boolean hasSignableArguments(ParseResults<S> $$0) {
      return !of($$0).arguments().isEmpty();
   }

   public static <S> SignableCommand<S> of(ParseResults<S> $$0) {
      String $$1 = $$0.getReader().getString();
      CommandContextBuilder<S> $$2 = $$0.getContext();
      CommandContextBuilder<S> $$3 = $$2;
      List<SignableCommand.Argument<S>> $$4 = collectArguments($$1, $$2);

      CommandContextBuilder<S> $$5;
      while (($$5 = $$3.getChild()) != null && $$5.getRootNode() != $$2.getRootNode()) {
         $$4.addAll(collectArguments($$1, $$5));
         $$3 = $$5;
      }

      return new SignableCommand<>($$4);
   }

   private static <S> List<SignableCommand.Argument<S>> collectArguments(String $$0, CommandContextBuilder<S> $$1) {
      List<SignableCommand.Argument<S>> $$2 = new ArrayList<>();

      for (ParsedCommandNode<S> $$3 : $$1.getNodes()) {
         if ($$3.getNode() instanceof ArgumentCommandNode<S, ?> $$4 && $$4.getType() instanceof SignedArgument) {
            ParsedArgument<S, ?> $$5 = (ParsedArgument<S, ?>)$$1.getArguments().get($$4.getName());
            if ($$5 != null) {
               String $$6 = $$5.getRange().get($$0);
               $$2.add(new SignableCommand.Argument<>($$4, $$6));
            }
         }
      }

      return $$2;
   }

   
   public SignableCommand.Argument<S> getArgument(String $$0) {
      for (SignableCommand.Argument<S> $$1 : this.arguments) {
         if ($$0.equals($$1.name())) {
            return $$1;
         }
      }

      return null;
   }

   public record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
      public String name() {
         return this.node.getName();
      }
   }
}
