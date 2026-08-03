package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface EnchantmentEntityEffect extends EnchantmentLocationBasedEffect {
   Codec<EnchantmentEntityEffect> CODEC = BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE
      .byNameCodec()
      .dispatch(EnchantmentEntityEffect::codec, Function.identity());

   static MapCodec<? extends EnchantmentEntityEffect> bootstrap(Registry<MapCodec<? extends EnchantmentEntityEffect>> $$0) {
      Registry.register($$0, "all_of", AllOf.EntityEffects.CODEC);
      Registry.register($$0, "apply_mob_effect", ApplyMobEffect.CODEC);
      Registry.register($$0, "change_item_damage", ChangeItemDamage.CODEC);
      Registry.register($$0, "damage_entity", DamageEntity.CODEC);
      Registry.register($$0, "explode", ExplodeEffect.CODEC);
      Registry.register($$0, "ignite", Ignite.CODEC);
      Registry.register($$0, "apply_impulse", ApplyEntityImpulse.CODEC);
      Registry.register($$0, "apply_exhaustion", ApplyExhaustion.CODEC);
      Registry.register($$0, "play_sound", PlaySoundEffect.CODEC);
      Registry.register($$0, "replace_block", ReplaceBlock.CODEC);
      Registry.register($$0, "replace_disk", ReplaceDisk.CODEC);
      Registry.register($$0, "run_function", RunFunction.CODEC);
      Registry.register($$0, "set_block_properties", SetBlockProperties.CODEC);
      Registry.register($$0, "spawn_particles", SpawnParticlesEffect.CODEC);
      return (MapCodec<? extends EnchantmentEntityEffect>)Registry.register($$0, "summon_entity", SummonEntityEffect.CODEC);
   }

   void apply(ServerLevel var1, int var2, EnchantedItemInUse var3, Entity var4, Vec3 var5);

   @Override
   default void onChangedBlock(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4, boolean $$5) {
      this.apply($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   MapCodec<? extends EnchantmentEntityEffect> codec();
}
