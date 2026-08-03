package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record BlocksAttacks(
   float blockDelaySeconds,
   float disableCooldownScale,
   List<BlocksAttacks.DamageReduction> damageReductions,
   BlocksAttacks.ItemDamageFunction itemDamage,
   Optional<TagKey<DamageType>> bypassedBy,
   Optional<Holder<SoundEvent>> blockSound,
   Optional<Holder<SoundEvent>> disableSound
) {
   public static final Codec<BlocksAttacks> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds", 0.0F).forGetter(BlocksAttacks::blockDelaySeconds),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale", 1.0F).forGetter(BlocksAttacks::disableCooldownScale),
            BlocksAttacks.DamageReduction.CODEC
               .listOf()
               .optionalFieldOf("damage_reductions", List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)))
               .forGetter(BlocksAttacks::damageReductions),
            BlocksAttacks.ItemDamageFunction.CODEC
               .optionalFieldOf("item_damage", BlocksAttacks.ItemDamageFunction.DEFAULT)
               .forGetter(BlocksAttacks::itemDamage),
            TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacks::bypassedBy),
            SoundEvent.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacks::blockSound),
            SoundEvent.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacks::disableSound)
         )
         .apply($$0, BlocksAttacks::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      BlocksAttacks::blockDelaySeconds,
      ByteBufCodecs.FLOAT,
      BlocksAttacks::disableCooldownScale,
      BlocksAttacks.DamageReduction.STREAM_CODEC.apply(ByteBufCodecs.list()),
      BlocksAttacks::damageReductions,
      BlocksAttacks.ItemDamageFunction.STREAM_CODEC,
      BlocksAttacks::itemDamage,
      TagKey.streamCodec(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional),
      BlocksAttacks::bypassedBy,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      BlocksAttacks::blockSound,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      BlocksAttacks::disableSound,
      BlocksAttacks::new
   );

   public void onBlocked(ServerLevel $$0, LivingEntity $$1) {
      this.blockSound
         .ifPresent($$2 -> $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), $$2, $$1.getSoundSource(), 1.0F, 0.8F + $$0.random.nextFloat() * 0.4F));
   }

   public void disable(ServerLevel $$0, LivingEntity $$1, float $$2, net.minecraft.world.item.ItemStack $$3) {
      int $$4 = this.disableBlockingForTicks($$2);
      if ($$4 > 0) {
         if ($$1 instanceof Player $$5) {
            $$5.getCooldowns().addCooldown($$3, $$4);
         }

         $$1.stopUsingItem();
         this.disableSound
            .ifPresent($$2x -> $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), $$2x, $$1.getSoundSource(), 0.8F, 0.8F + $$0.random.nextFloat() * 0.4F));
      }
   }

   public void hurtBlockingItem(Level $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2, InteractionHand $$3, float $$4) {
      if ($$2 instanceof Player $$5) {
         if (!$$0.isClientSide()) {
            $$5.awardStat(Stats.ITEM_USED.get($$1.getItem()));
         }

         int $$7 = this.itemDamage.apply($$4);
         if ($$7 > 0) {
            $$1.hurtAndBreak($$7, $$2, $$3.asEquipmentSlot());
         }
      }
   }

   private int disableBlockingForTicks(float $$0) {
      float $$1 = $$0 * this.disableCooldownScale;
      return $$1 > 0.0F ? Math.round($$1 * 20.0F) : 0;
   }

   public int blockDelayTicks() {
      return Math.round(this.blockDelaySeconds * 20.0F);
   }

   public float resolveBlockedDamage(DamageSource $$0, float $$1, double $$2) {
      float $$3 = 0.0F;

      for (BlocksAttacks.DamageReduction $$4 : this.damageReductions) {
         $$3 += $$4.resolve($$0, $$1, $$2);
      }

      return Mth.clamp($$3, 0.0F, $$1);
   }

   public record DamageReduction(float horizontalBlockingAngle, Optional<HolderSet<DamageType>> type, float base, float factor) {
      public static final Codec<BlocksAttacks.DamageReduction> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", 90.0F).forGetter(BlocksAttacks.DamageReduction::horizontalBlockingAngle),
               RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(BlocksAttacks.DamageReduction::type),
               Codec.FLOAT.fieldOf("base").forGetter(BlocksAttacks.DamageReduction::base),
               Codec.FLOAT.fieldOf("factor").forGetter(BlocksAttacks.DamageReduction::factor)
            )
            .apply($$0, BlocksAttacks.DamageReduction::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks.DamageReduction> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT,
         BlocksAttacks.DamageReduction::horizontalBlockingAngle,
         ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional),
         BlocksAttacks.DamageReduction::type,
         ByteBufCodecs.FLOAT,
         BlocksAttacks.DamageReduction::base,
         ByteBufCodecs.FLOAT,
         BlocksAttacks.DamageReduction::factor,
         BlocksAttacks.DamageReduction::new
      );

      public float resolve(DamageSource $$0, float $$1, double $$2) {
         if ($$2 > (float) (Math.PI / 180.0) * this.horizontalBlockingAngle) {
            return 0.0F;
         } else {
            return this.type.isPresent() && !this.type.get().contains($$0.typeHolder()) ? 0.0F : Mth.clamp(this.base + this.factor * $$1, 0.0F, $$1);
         }
      }
   }

   public record ItemDamageFunction(float threshold, float base, float factor) {
      public static final Codec<BlocksAttacks.ItemDamageFunction> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("threshold").forGetter(BlocksAttacks.ItemDamageFunction::threshold),
               Codec.FLOAT.fieldOf("base").forGetter(BlocksAttacks.ItemDamageFunction::base),
               Codec.FLOAT.fieldOf("factor").forGetter(BlocksAttacks.ItemDamageFunction::factor)
            )
            .apply($$0, BlocksAttacks.ItemDamageFunction::new)
      );
      public static final StreamCodec<ByteBuf, BlocksAttacks.ItemDamageFunction> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT,
         BlocksAttacks.ItemDamageFunction::threshold,
         ByteBufCodecs.FLOAT,
         BlocksAttacks.ItemDamageFunction::base,
         ByteBufCodecs.FLOAT,
         BlocksAttacks.ItemDamageFunction::factor,
         BlocksAttacks.ItemDamageFunction::new
      );
      public static final BlocksAttacks.ItemDamageFunction DEFAULT = new BlocksAttacks.ItemDamageFunction(1.0F, 0.0F, 1.0F);

      public int apply(float $$0) {
         return $$0 < this.threshold ? 0 : Mth.floor(this.base + this.factor * $$0);
      }
   }
}
