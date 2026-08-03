package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record SetBlockProperties(BlockItemStateProperties properties, Vec3i offset, Optional<Holder<GameEvent>> triggerGameEvent)
   implements EnchantmentEntityEffect {
   public static final MapCodec<SetBlockProperties> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetBlockProperties::properties),
            Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(SetBlockProperties::offset),
            GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockProperties::triggerGameEvent)
         )
         .apply($$0, SetBlockProperties::new)
   );

   public SetBlockProperties(BlockItemStateProperties $$0) {
      this($$0, Vec3i.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
   }

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      BlockPos $$5 = BlockPos.containing($$4).offset(this.offset);
      BlockState $$6 = $$3.level().getBlockState($$5);
      BlockState $$7 = this.properties.apply($$6);
      if ($$6 != $$7 && $$3.level().setBlock($$5, $$7, 3)) {
         this.triggerGameEvent.ifPresent($$3x -> $$0.gameEvent($$3, $$3x, $$5));
      }
   }

   @Override
   public MapCodec<SetBlockProperties> codec() {
      return CODEC;
   }
}
