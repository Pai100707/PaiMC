package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SummonEntityEffect(HolderSet<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect {
   public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(SummonEntityEffect::entityTypes),
            Codec.BOOL.optionalFieldOf("join_team", false).forGetter(SummonEntityEffect::joinTeam)
         )
         .apply($$0, SummonEntityEffect::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      BlockPos $$5 = BlockPos.containing($$4);
      if (Level.isInSpawnableBounds($$5)) {
         Optional<Holder<EntityType<?>>> $$6 = this.entityTypes().getRandomElement($$0.getRandom());
         if (!$$6.isEmpty()) {
            Entity $$7 = ((EntityType)$$6.get().value()).spawn($$0, $$5, EntitySpawnReason.TRIGGERED);
            if ($$7 != null) {
               if ($$7 instanceof LightningBolt $$8 && $$2.owner() instanceof ServerPlayer $$9) {
                  $$8.setCause($$9);
               }

               if (this.joinTeam && $$3.getTeam() != null) {
                  $$0.getScoreboard().addPlayerToTeam($$7.getScoreboardName(), $$3.getTeam());
               }

               $$7.snapTo($$4.x, $$4.y, $$4.z, $$7.getYRot(), $$7.getXRot());
            }
         }
      }
   }

   @Override
   public MapCodec<SummonEntityEffect> codec() {
      return CODEC;
   }
}
