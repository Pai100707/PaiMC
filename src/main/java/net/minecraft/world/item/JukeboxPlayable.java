package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public record JukeboxPlayable(net.minecraft.world.item.EitherHolder<net.minecraft.world.item.JukeboxSong> song) implements TooltipProvider {
   public static final Codec<net.minecraft.world.item.JukeboxPlayable> CODEC = net.minecraft.world.item.EitherHolder.codec(
         Registries.JUKEBOX_SONG, net.minecraft.world.item.JukeboxSong.CODEC
      )
      .xmap(net.minecraft.world.item.JukeboxPlayable::new, net.minecraft.world.item.JukeboxPlayable::song);
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.JukeboxPlayable> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.item.EitherHolder.streamCodec(Registries.JUKEBOX_SONG, net.minecraft.world.item.JukeboxSong.STREAM_CODEC),
      net.minecraft.world.item.JukeboxPlayable::song,
      net.minecraft.world.item.JukeboxPlayable::new
   );

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      Provider $$4 = $$0.registries();
      if ($$4 != null) {
         this.song
            .unwrap($$4)
            .ifPresent(
               $$1x -> {
                  Component $$2x = ComponentUtils.mergeStyles(
                     ((net.minecraft.world.item.JukeboxSong)$$1x.value()).description(), Style.EMPTY.withColor(ChatFormatting.GRAY)
                  );
                  $$1.accept($$2x);
               }
            );
      }
   }

   public static InteractionResult tryInsertIntoJukebox(Level $$0, BlockPos $$1, net.minecraft.world.item.ItemStack $$2, Player $$3) {
      net.minecraft.world.item.JukeboxPlayable $$4 = (net.minecraft.world.item.JukeboxPlayable)$$2.get(DataComponents.JUKEBOX_PLAYABLE);
      if ($$4 == null) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      } else {
         BlockState $$5 = $$0.getBlockState($$1);
         if ($$5.is(Blocks.JUKEBOX) && !(Boolean)$$5.getValue(JukeboxBlock.HAS_RECORD)) {
            if (!$$0.isClientSide()) {
               net.minecraft.world.item.ItemStack $$6 = $$2.consumeAndReturn(1, $$3);
               if ($$0.getBlockEntity($$1) instanceof JukeboxBlockEntity $$7) {
                  $$7.setTheItem($$6);
                  $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$1, Context.of($$3, $$5));
               }

               $$3.awardStat(Stats.PLAY_RECORD);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
      }
   }
}
