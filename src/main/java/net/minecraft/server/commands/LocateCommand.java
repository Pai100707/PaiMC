package net.minecraft.server.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocateCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.locate.structure.not_found", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.locate.structure.invalid", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.locate.biome.not_found", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.locate.poi.not_found", new Object[]{$$0})
   );
   private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
   private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
   private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
   private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
   private static final int POI_SEARCH_RADIUS = 256;

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("locate")
                     .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                  .then(
                     Commands.literal("structure")
                        .then(
                           Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
                              .executes(
                                 $$0x -> locateStructure(
                                    (CommandSourceStack)$$0x.getSource(),
                                    ResourceOrTagKeyArgument.getResourceOrTagKey($$0x, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID)
                                 )
                              )
                        )
                  ))
               .then(
                  Commands.literal("biome")
                     .then(
                        Commands.argument("biome", ResourceOrTagArgument.resourceOrTag($$1, Registries.BIOME))
                           .executes(
                              $$0x -> locateBiome((CommandSourceStack)$$0x.getSource(), ResourceOrTagArgument.getResourceOrTag($$0x, "biome", Registries.BIOME))
                           )
                     )
               ))
            .then(
               Commands.literal("poi")
                  .then(
                     Commands.argument("poi", ResourceOrTagArgument.resourceOrTag($$1, Registries.POINT_OF_INTEREST_TYPE))
                        .executes(
                           $$0x -> locatePoi(
                              (CommandSourceStack)$$0x.getSource(), ResourceOrTagArgument.getResourceOrTag($$0x, "poi", Registries.POINT_OF_INTEREST_TYPE)
                           )
                        )
                  )
            )
      );
   }

   private static Optional<? extends ListBacked<Structure>> getHolders(Result<Structure> $$0, Registry<Structure> $$1) {
      return (Optional<? extends ListBacked<Structure>>)$$0.unwrap().map($$1x -> $$1.get($$1x).map($$0xx -> HolderSet.direct(new Holder[]{$$0xx})), $$1::get);
   }

   private static int locateStructure(CommandSourceStack $$0, Result<Structure> $$1) throws CommandSyntaxException {
      Registry<Structure> $$2 = $$0.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
      HolderSet<Structure> $$3 = (HolderSet<Structure>)getHolders($$1, $$2).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create($$1.asPrintable()));
      BlockPos $$4 = BlockPos.containing($$0.getPosition());
      ServerLevel $$5 = $$0.getLevel();
      Stopwatch $$6 = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Structure>> $$7 = $$5.getChunkSource().getGenerator().findNearestMapStructure($$5, $$3, $$4, 100, false);
      $$6.stop();
      if ($$7 == null) {
         throw ERROR_STRUCTURE_NOT_FOUND.create($$1.asPrintable());
      } else {
         return showLocateResult($$0, $$1, $$4, $$7, "commands.locate.structure.success", false, $$6.elapsed());
      }
   }

   private static int locateBiome(CommandSourceStack $$0, net.minecraft.commands.arguments.ResourceOrTagArgument.Result<Biome> $$1) throws CommandSyntaxException {
      BlockPos $$2 = BlockPos.containing($$0.getPosition());
      Stopwatch $$3 = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Biome>> $$4 = $$0.getLevel().findClosestBiome3d($$1, $$2, 6400, 32, 64);
      $$3.stop();
      if ($$4 == null) {
         throw ERROR_BIOME_NOT_FOUND.create($$1.asPrintable());
      } else {
         return showLocateResult($$0, $$1, $$2, $$4, "commands.locate.biome.success", true, $$3.elapsed());
      }
   }

   private static int locatePoi(CommandSourceStack $$0, net.minecraft.commands.arguments.ResourceOrTagArgument.Result<PoiType> $$1) throws CommandSyntaxException {
      BlockPos $$2 = BlockPos.containing($$0.getPosition());
      ServerLevel $$3 = $$0.getLevel();
      Stopwatch $$4 = Stopwatch.createStarted(Util.TICKER);
      Optional<Pair<Holder<PoiType>, BlockPos>> $$5 = $$3.getPoiManager().findClosestWithType($$1, $$2, 256, Occupancy.ANY);
      $$4.stop();
      if ($$5.isEmpty()) {
         throw ERROR_POI_NOT_FOUND.create($$1.asPrintable());
      } else {
         return showLocateResult($$0, $$1, $$2, $$5.get().swap(), "commands.locate.poi.success", false, $$4.elapsed());
      }
   }

   public static int showLocateResult(
      CommandSourceStack $$0,
      net.minecraft.commands.arguments.ResourceOrTagArgument.Result<?> $$1,
      BlockPos $$2,
      Pair<BlockPos, ? extends Holder<?>> $$3,
      String $$4,
      boolean $$5,
      Duration $$6
   ) {
      String $$7 = (String)$$1.unwrap().map($$1x -> $$1.asPrintable(), $$2x -> $$1.asPrintable() + " (" + ((Holder)$$3.getSecond()).getRegisteredName() + ")");
      return showLocateResult($$0, $$2, $$3, $$4, $$5, $$7, $$6);
   }

   public static int showLocateResult(
      CommandSourceStack $$0, Result<?> $$1, BlockPos $$2, Pair<BlockPos, ? extends Holder<?>> $$3, String $$4, boolean $$5, Duration $$6
   ) {
      String $$7 = (String)$$1.unwrap()
         .map($$0x -> $$0x.identifier().toString(), $$1x -> "#" + $$1x.location() + " (" + ((Holder)$$3.getSecond()).getRegisteredName() + ")");
      return showLocateResult($$0, $$2, $$3, $$4, $$5, $$7, $$6);
   }

   private static int showLocateResult(
      CommandSourceStack $$0, BlockPos $$1, Pair<BlockPos, ? extends Holder<?>> $$2, String $$3, boolean $$4, String $$5, Duration $$6
   ) {
      BlockPos $$7 = (BlockPos)$$2.getFirst();
      int $$8 = $$4 ? Mth.floor(Mth.sqrt((float)$$1.distSqr($$7))) : Mth.floor(dist($$1.getX(), $$1.getZ(), $$7.getX(), $$7.getZ()));
      String $$9 = $$4 ? String.valueOf($$7.getY()) : "~";
      Component $$10 = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", new Object[]{$$7.getX(), $$9, $$7.getZ()}))
         .withStyle(
            $$2x -> $$2x.withColor(ChatFormatting.GREEN)
               .withClickEvent(new SuggestCommand("/tp @s " + $$7.getX() + " " + $$9 + " " + $$7.getZ()))
               .withHoverEvent(new ShowText(Component.translatable("chat.coordinates.tooltip")))
         );
      $$0.sendSuccess(() -> Component.translatable($$3, new Object[]{$$5, $$10, $$8}), false);
      LOGGER.info("Locating element {} took {} ms", $$5, $$6.toMillis());
      return $$8;
   }

   private static float dist(int $$0, int $$1, int $$2, int $$3) {
      int $$4 = $$2 - $$0;
      int $$5 = $$3 - $$1;
      return Mth.sqrt($$4 * $$4 + $$5 * $$5);
   }
}
