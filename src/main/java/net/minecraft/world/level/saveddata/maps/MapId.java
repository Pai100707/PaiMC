package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.TooltipProvider;

public record MapId(int id) implements TooltipProvider {
   public static final Codec<MapId> CODEC = Codec.INT.xmap(MapId::new, MapId::id);
   public static final StreamCodec<ByteBuf, MapId> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(MapId::new, MapId::id);
   private static final Component LOCKED_TEXT = Component.translatable("filled_map.locked").withStyle(ChatFormatting.GRAY);

   public String key() {
      return "map_" + this.id;
   }

   public void addToTooltip(TooltipContext $$0, Consumer<Component> $$1, TooltipFlag $$2, DataComponentGetter $$3) {
      MapItemSavedData $$4 = $$0.mapData(this);
      if ($$4 == null) {
         $$1.accept(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
      } else {
         MapPostProcessing $$5 = (MapPostProcessing)$$3.get(DataComponents.MAP_POST_PROCESSING);
         if ($$3.get(DataComponents.CUSTOM_NAME) == null && $$5 == null) {
            $$1.accept(Component.translatable("filled_map.id", new Object[]{this.id}).withStyle(ChatFormatting.GRAY));
         }

         if ($$4.locked || $$5 == MapPostProcessing.LOCK) {
            $$1.accept(LOCKED_TEXT);
         }

         if ($$2.isAdvanced()) {
            int $$6 = $$5 == MapPostProcessing.SCALE ? 1 : 0;
            int $$7 = Math.min($$4.scale + $$6, 4);
            $$1.accept(Component.translatable("filled_map.scale", new Object[]{1 << $$7}).withStyle(ChatFormatting.GRAY));
            $$1.accept(Component.translatable("filled_map.level", new Object[]{$$7, 4}).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
