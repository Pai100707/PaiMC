package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextKeySet.Builder;

public class LootContextParamSets {
   private static final BiMap<Identifier, ContextKeySet> REGISTRY = HashBiMap.create();
   public static final Codec<ContextKeySet> CODEC = Identifier.CODEC
      .comapFlatMap(
         $$0 -> Optional.ofNullable((ContextKeySet)REGISTRY.get($$0))
            .<DataResult>map(DataResult::success)
            .orElseGet(() -> DataResult.error(() -> "No parameter set exists with id: '" + $$0 + "'")),
         REGISTRY.inverse()::get
      );
   public static final ContextKeySet EMPTY = register("empty", $$0 -> {});
   public static final ContextKeySet CHEST = register("chest", $$0 -> $$0.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet COMMAND = register("command", $$0 -> $$0.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet SELECTOR = register("selector", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet FISHING = register(
      "fishing", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY)
   );
   public static final ContextKeySet ENTITY = register(
      "entity",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.DAMAGE_SOURCE)
         .optional(LootContextParams.ATTACKING_ENTITY)
         .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
         .optional(LootContextParams.LAST_DAMAGE_PLAYER)
   );
   public static final ContextKeySet EQUIPMENT = register("equipment", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet ARCHAEOLOGY = register(
      "archaeology", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL)
   );
   public static final ContextKeySet GIFT = register("gift", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet PIGLIN_BARTER = register("barter", $$0 -> $$0.required(LootContextParams.THIS_ENTITY));
   public static final ContextKeySet VAULT = register(
      "vault", $$0 -> $$0.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.TOOL)
   );
   public static final ContextKeySet ADVANCEMENT_REWARD = register(
      "advancement_reward", $$0 -> $$0.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
   );
   public static final ContextKeySet ADVANCEMENT_ENTITY = register(
      "advancement_entity", $$0 -> $$0.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
   );
   public static final ContextKeySet ADVANCEMENT_LOCATION = register(
      "advancement_location",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.TOOL)
         .required(LootContextParams.BLOCK_STATE)
   );
   public static final ContextKeySet BLOCK_USE = register(
      "block_use", $$0 -> $$0.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE)
   );
   public static final ContextKeySet ALL_PARAMS = register(
      "generic",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.LAST_DAMAGE_PLAYER)
         .required(LootContextParams.DAMAGE_SOURCE)
         .required(LootContextParams.ATTACKING_ENTITY)
         .required(LootContextParams.DIRECT_ATTACKING_ENTITY)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.BLOCK_STATE)
         .required(LootContextParams.BLOCK_ENTITY)
         .required(LootContextParams.TOOL)
         .required(LootContextParams.EXPLOSION_RADIUS)
   );
   public static final ContextKeySet BLOCK = register(
      "block",
      $$0 -> $$0.required(LootContextParams.BLOCK_STATE)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.TOOL)
         .optional(LootContextParams.THIS_ENTITY)
         .optional(LootContextParams.BLOCK_ENTITY)
         .optional(LootContextParams.EXPLOSION_RADIUS)
   );
   public static final ContextKeySet SHEARING = register(
      "shearing", $$0 -> $$0.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL)
   );
   public static final ContextKeySet ENTITY_INTERACT = register(
      "entity_interact", $$0 -> $$0.required(LootContextParams.TARGET_ENTITY).optional(LootContextParams.INTERACTING_ENTITY).required(LootContextParams.TOOL)
   );
   public static final ContextKeySet BLOCK_INTERACT = register(
      "block_interact",
      $$0 -> $$0.required(LootContextParams.BLOCK_STATE)
         .optional(LootContextParams.BLOCK_ENTITY)
         .optional(LootContextParams.INTERACTING_ENTITY)
         .optional(LootContextParams.TOOL)
   );
   public static final ContextKeySet ENCHANTED_DAMAGE = register(
      "enchanted_damage",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.ENCHANTMENT_LEVEL)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.DAMAGE_SOURCE)
         .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
         .optional(LootContextParams.ATTACKING_ENTITY)
   );
   public static final ContextKeySet ENCHANTED_ITEM = register(
      "enchanted_item", $$0 -> $$0.required(LootContextParams.TOOL).required(LootContextParams.ENCHANTMENT_LEVEL)
   );
   public static final ContextKeySet ENCHANTED_LOCATION = register(
      "enchanted_location",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.ENCHANTMENT_LEVEL)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.ENCHANTMENT_ACTIVE)
   );
   public static final ContextKeySet ENCHANTED_ENTITY = register(
      "enchanted_entity", $$0 -> $$0.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN)
   );
   public static final ContextKeySet HIT_BLOCK = register(
      "hit_block",
      $$0 -> $$0.required(LootContextParams.THIS_ENTITY)
         .required(LootContextParams.ENCHANTMENT_LEVEL)
         .required(LootContextParams.ORIGIN)
         .required(LootContextParams.BLOCK_STATE)
   );

   private static ContextKeySet register(String $$0, Consumer<Builder> $$1) {
      Builder $$2 = new Builder();
      $$1.accept($$2);
      ContextKeySet $$3 = $$2.build();
      Identifier $$4 = Identifier.withDefaultNamespace($$0);
      ContextKeySet $$5 = (ContextKeySet)REGISTRY.put($$4, $$3);
      if ($$5 != null) {
         throw new IllegalStateException("Loot table parameter set " + $$4 + " is already registered");
      } else {
         return $$3;
      }
   }
}
