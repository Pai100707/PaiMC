package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceBlock(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<Holder<GameEvent>> triggerGameEvent)
   implements EnchantmentEntityEffect {
   public static final MapCodec<ReplaceBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceBlock::offset),
            BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlock::predicate),
            BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceBlock::blockState),
            GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlock::triggerGameEvent)
         )
         .apply($$0, ReplaceBlock::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      BlockPos $$5 = BlockPos.containing($$4).offset(this.offset);
      if (this.predicate.map($$2x -> $$2x.test($$0, $$5)).orElse(true) && $$0.setBlockAndUpdate($$5, this.blockState.getState($$3.getRandom(), $$5))) {
         this.triggerGameEvent.ifPresent($$3x -> $$0.gameEvent($$3, $$3x, $$5));
      }
   }

   @Override
   public MapCodec<ReplaceBlock> codec() {
      return CODEC;
   }
}
