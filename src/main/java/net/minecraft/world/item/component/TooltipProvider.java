package net.minecraft.world.item.component;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;

public interface TooltipProvider {
   void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext var1, Consumer<Component> var2, net.minecraft.world.item.TooltipFlag var3, DataComponentGetter var4
   );
}
