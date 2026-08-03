package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public class SpawnArmorTrimsCommand {
   private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(
      TrimPatterns.SENTRY,
      TrimPatterns.DUNE,
      TrimPatterns.COAST,
      TrimPatterns.WILD,
      TrimPatterns.WARD,
      TrimPatterns.EYE,
      TrimPatterns.VEX,
      TrimPatterns.TIDE,
      TrimPatterns.SNOUT,
      TrimPatterns.RIB,
      TrimPatterns.SPIRE,
      TrimPatterns.WAYFINDER,
      TrimPatterns.SHAPER,
      TrimPatterns.SILENCE,
      TrimPatterns.RAISER,
      TrimPatterns.HOST,
      TrimPatterns.FLOW,
      TrimPatterns.BOLT
   );
   private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(
      TrimMaterials.QUARTZ,
      TrimMaterials.IRON,
      TrimMaterials.NETHERITE,
      TrimMaterials.REDSTONE,
      TrimMaterials.COPPER,
      TrimMaterials.GOLD,
      TrimMaterials.EMERALD,
      TrimMaterials.DIAMOND,
      TrimMaterials.LAPIS,
      TrimMaterials.AMETHYST,
      TrimMaterials.RESIN
   );
   private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
   private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);
   private static final DynamicCommandExceptionType ERROR_INVALID_PATTERN = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("Invalid pattern", new Object[]{$$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawn_armor_trims")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  Commands.literal("*_lag_my_game")
                     .executes($$0x -> spawnAllArmorTrims((CommandSourceStack)$$0x.getSource(), ((CommandSourceStack)$$0x.getSource()).getPlayerOrException()))
               ))
            .then(
               Commands.argument("pattern", ResourceKeyArgument.key(Registries.TRIM_PATTERN))
                  .executes(
                     $$0x -> spawnArmorTrim(
                        (CommandSourceStack)$$0x.getSource(),
                        ((CommandSourceStack)$$0x.getSource()).getPlayerOrException(),
                        ResourceKeyArgument.getRegistryKey($$0x, "pattern", Registries.TRIM_PATTERN, ERROR_INVALID_PATTERN)
                     )
                  )
            )
      );
   }

   private static int spawnAllArmorTrims(CommandSourceStack $$0, Player $$1) {
      return spawnArmorTrims($$0, $$1, $$0.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).listElements());
   }

   private static int spawnArmorTrim(CommandSourceStack $$0, Player $$1, ResourceKey<TrimPattern> $$2) {
      return spawnArmorTrims(
         $$0, $$1, Stream.of((Reference<TrimPattern>)$$0.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).get($$2).orElseThrow())
      );
   }

   private static int spawnArmorTrims(CommandSourceStack $$0, Player $$1, Stream<Reference<TrimPattern>> $$2) {
      ServerLevel $$3 = $$0.getLevel();
      List<Reference<TrimPattern>> $$4 = $$2.sorted(Comparator.comparing($$0x -> TRIM_PATTERN_ORDER.applyAsInt($$0x.key()))).toList();
      List<Reference<TrimMaterial>> $$5 = $$3.registryAccess()
         .lookupOrThrow(Registries.TRIM_MATERIAL)
         .listElements()
         .sorted(Comparator.comparing($$0x -> TRIM_MATERIAL_ORDER.applyAsInt($$0x.key())))
         .toList();
      List<Reference<Item>> $$6 = findEquippableItemsWithAssets($$3.registryAccess().lookupOrThrow(Registries.ITEM));
      BlockPos $$7 = $$1.blockPosition().relative($$1.getDirection(), 5);
      double $$8 = 3.0;

      for (int $$9 = 0; $$9 < $$5.size(); $$9++) {
         Reference<TrimMaterial> $$10 = $$5.get($$9);

         for (int $$11 = 0; $$11 < $$4.size(); $$11++) {
            Reference<TrimPattern> $$12 = $$4.get($$11);
            ArmorTrim $$13 = new ArmorTrim($$10, $$12);

            for (int $$14 = 0; $$14 < $$6.size(); $$14++) {
               Reference<Item> $$15 = $$6.get($$14);
               double $$16 = $$7.getX() + 0.5 - $$14 * 3.0;
               double $$17 = $$7.getY() + 0.5 + $$9 * 3.0;
               double $$18 = $$7.getZ() + 0.5 + $$11 * 10;
               ArmorStand $$19 = new ArmorStand($$3, $$16, $$17, $$18);
               $$19.setYRot(180.0F);
               $$19.setNoGravity(true);
               ItemStack $$20 = new ItemStack($$15);
               Equippable $$21 = Objects.requireNonNull((Equippable)$$20.get(DataComponents.EQUIPPABLE));
               $$20.set(DataComponents.TRIM, $$13);
               $$19.setItemSlot($$21.slot(), $$20);
               if ($$14 == 0) {
                  $$19.setCustomName(
                     ((TrimPattern)$$13.pattern().value())
                        .copyWithStyle($$13.material())
                        .copy()
                        .append(" & ")
                        .append(((TrimMaterial)$$13.material().value()).description())
                  );
                  $$19.setCustomNameVisible(true);
               } else {
                  $$19.setInvisible(true);
               }

               $$3.addFreshEntity($$19);
            }
         }
      }

      $$0.sendSuccess(() -> Component.literal("Armorstands with trimmed armor spawned around you"), true);
      return 1;
   }

   private static List<Reference<Item>> findEquippableItemsWithAssets(HolderLookup<Item> $$0) {
      List<Reference<Item>> $$1 = new ArrayList<>();
      $$0.listElements().forEach($$1x -> {
         Equippable $$2 = (Equippable)((Item)$$1x.value()).components().get(DataComponents.EQUIPPABLE);
         if ($$2 != null && $$2.slot().getType() == Type.HUMANOID_ARMOR && $$2.assetId().isPresent()) {
            $$1.add($$1x);
         }
      });
      return $$1;
   }
}
