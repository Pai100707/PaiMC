package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceDisk(
   LevelBasedValue radius,
   LevelBasedValue height,
   Vec3i offset,
   Optional<BlockPredicate> predicate,
   BlockStateProvider blockState,
   Optional<Holder<GameEvent>> triggerGameEvent
) implements EnchantmentEntityEffect {
   public static final MapCodec<ReplaceDisk> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            LevelBasedValue.CODEC.fieldOf("radius").forGetter(ReplaceDisk::radius),
            LevelBasedValue.CODEC.fieldOf("height").forGetter(ReplaceDisk::height),
            Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceDisk::offset),
            BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceDisk::predicate),
            BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceDisk::blockState),
            GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceDisk::triggerGameEvent)
         )
         .apply($$0, ReplaceDisk::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      BlockPos $$5 = BlockPos.containing($$4).offset(this.offset);
      RandomSource $$6 = $$3.getRandom();
      int $$7 = (int)this.radius.calculate($$1);
      int $$8 = (int)this.height.calculate($$1);

      for (BlockPos $$9 : BlockPos.betweenClosed($$5.offset(-$$7, 0, -$$7), $$5.offset($$7, Math.min($$8 - 1, 0), $$7))) {
         if ($$9.distToCenterSqr($$4.x(), $$9.getY() + 0.5, $$4.z()) < Mth.square($$7)
            && this.predicate.map($$2x -> $$2x.test($$0, $$9)).orElse(true)
            && $$0.setBlockAndUpdate($$9, this.blockState.getState($$6, $$9))) {
            this.triggerGameEvent.ifPresent($$3x -> $$0.gameEvent($$3, $$3x, $$9));
         }
      }
   }

   @Override
   public MapCodec<ReplaceDisk> codec() {
      return CODEC;
   }
}
