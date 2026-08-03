package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.Nullable;

public record ScoreContents(Either<SelectorPattern, String> name, String objective) implements ComponentContents {
   public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.either(SelectorPattern.CODEC, Codec.STRING).fieldOf("name").forGetter(ScoreContents::name),
            Codec.STRING.fieldOf("objective").forGetter(ScoreContents::objective)
         )
         .apply($$0, ScoreContents::new)
   );
   public static final MapCodec<ScoreContents> MAP_CODEC = INNER_CODEC.fieldOf("score");

   @Override
   public MapCodec<ScoreContents> codec() {
      return MAP_CODEC;
   }

   private ScoreHolder findTargetName(CommandSourceStack $$0) throws CommandSyntaxException {
      Optional<SelectorPattern> $$1 = this.name.left();
      if ($$1.isPresent()) {
         List<? extends Entity> $$2 = $$1.get().resolved().findEntities($$0);
         if (!$$2.isEmpty()) {
            if ($$2.size() != 1) {
               throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            } else {
               return (ScoreHolder)$$2.getFirst();
            }
         } else {
            return ScoreHolder.forNameOnly($$1.get().pattern());
         }
      } else {
         return ScoreHolder.forNameOnly((String)this.name.right().orElseThrow());
      }
   }

   private MutableComponent getScore(ScoreHolder $$0, CommandSourceStack $$1) {
      MinecraftServer $$2 = $$1.getServer();
      if ($$2 != null) {
         Scoreboard $$3 = $$2.getScoreboard();
         Objective $$4 = $$3.getObjective(this.objective);
         if ($$4 != null) {
            ReadOnlyScoreInfo $$5 = $$3.getPlayerScoreInfo($$0, $$4);
            if ($$5 != null) {
               return $$5.formatValue($$4.numberFormatOrDefault(StyledFormat.NO_STYLE));
            }
         }
      }

      return Component.empty();
   }

   @Override
   public MutableComponent resolve(@Nullable CommandSourceStack $$0, @Nullable Entity $$1, int $$2) throws CommandSyntaxException {
      if ($$0 == null) {
         return Component.empty();
      } else {
         ScoreHolder $$3 = this.findTargetName($$0);
         ScoreHolder $$4 = (ScoreHolder)($$1 != null && $$3.equals(ScoreHolder.WILDCARD) ? $$1 : $$3);
         return this.getScore($$4, $$0);
      }
   }

   @Override
   public String toString() {
      return "score{name='" + this.name + "', objective='" + this.objective + "'}";
   }
}
