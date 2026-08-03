package net.minecraft.world.level;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public interface Spawner {
   void setEntityId(EntityType<?> var1, RandomSource var2);

   static void appendHoverText(@Nullable TypedEntityData<BlockEntityType<?>> $$0, Consumer<Component> $$1, String $$2) {
      Component $$3 = getSpawnEntityDisplayName($$0, $$2);
      if ($$3 != null) {
         $$1.accept($$3);
      } else {
         $$1.accept(CommonComponents.EMPTY);
         $$1.accept(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
         $$1.accept(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
      }
   }

   @Nullable
   static Component getSpawnEntityDisplayName(@Nullable TypedEntityData<BlockEntityType<?>> $$0, String $$1) {
      return $$0 == null
         ? null
         : (Component)$$0.getUnsafe()
            .getCompound($$1)
            .flatMap($$0x -> $$0x.getCompound("entity"))
            .flatMap($$0x -> $$0x.read("id", EntityType.CODEC))
            .map($$0x -> Component.translatable($$0x.getDescriptionId()).withStyle(ChatFormatting.GRAY))
            .orElse(null);
   }
}
