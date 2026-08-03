package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface HoverEvent {
   Codec<HoverEvent> CODEC = HoverEvent.Action.CODEC.dispatch("action", HoverEvent::action, $$0 -> $$0.codec);

   HoverEvent.Action action();

   public static enum Action implements StringRepresentable {
      SHOW_TEXT("show_text", true, HoverEvent.ShowText.CODEC),
      SHOW_ITEM("show_item", true, HoverEvent.ShowItem.CODEC),
      SHOW_ENTITY("show_entity", true, HoverEvent.ShowEntity.CODEC);

      public static final Codec<HoverEvent.Action> UNSAFE_CODEC = StringRepresentable.fromValues(HoverEvent.Action::values);
      public static final Codec<HoverEvent.Action> CODEC = UNSAFE_CODEC.validate(HoverEvent.Action::filterForSerialization);
      private final String name;
      private final boolean allowFromServer;
      final MapCodec<? extends HoverEvent> codec;

      private Action(final String $$0, final boolean $$1, final MapCodec<? extends HoverEvent> $$2) {
         this.name = $$0;
         this.allowFromServer = $$1;
         this.codec = $$2;
      }

      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      public String getSerializedName() {
         return this.name;
      }

      @Override
      public String toString() {
         return "<action " + this.name + ">";
      }

      private static DataResult<HoverEvent.Action> filterForSerialization(HoverEvent.Action $$0) {
         return !$$0.isAllowedFromServer() ? DataResult.error(() -> "Action not allowed: " + $$0) : DataResult.success($$0, Lifecycle.stable());
      }
   }

   public static class EntityTooltipInfo {
      public static final MapCodec<HoverEvent.EntityTooltipInfo> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter($$0x -> $$0x.type),
               UUIDUtil.LENIENT_CODEC.fieldOf("uuid").forGetter($$0x -> $$0x.uuid),
               ComponentSerialization.CODEC.optionalFieldOf("name").forGetter($$0x -> $$0x.name)
            )
            .apply($$0, HoverEvent.EntityTooltipInfo::new)
      );
      public final EntityType<?> type;
      public final UUID uuid;
      public final Optional<Component> name;
      @Nullable
      private List<Component> linesCache;

      public EntityTooltipInfo(EntityType<?> $$0, UUID $$1, @Nullable Component $$2) {
         this($$0, $$1, Optional.ofNullable($$2));
      }

      public EntityTooltipInfo(EntityType<?> $$0, UUID $$1, Optional<Component> $$2) {
         this.type = $$0;
         this.uuid = $$1;
         this.name = $$2;
      }

      public List<Component> getTooltipLines() {
         if (this.linesCache == null) {
            this.linesCache = new ArrayList<>();
            this.name.ifPresent(this.linesCache::add);
            this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
            this.linesCache.add(Component.literal(this.uuid.toString()));
         }

         return this.linesCache;
      }

      @Override
      public boolean equals(Object $$0) {
         if (this == $$0) {
            return true;
         } else if ($$0 != null && this.getClass() == $$0.getClass()) {
            HoverEvent.EntityTooltipInfo $$1 = (HoverEvent.EntityTooltipInfo)$$0;
            return this.type.equals($$1.type) && this.uuid.equals($$1.uuid) && this.name.equals($$1.name);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int $$0 = this.type.hashCode();
         $$0 = 31 * $$0 + this.uuid.hashCode();
         return 31 * $$0 + this.name.hashCode();
      }
   }

   public record ShowEntity(HoverEvent.EntityTooltipInfo entity) implements HoverEvent {
      public static final MapCodec<HoverEvent.ShowEntity> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(HoverEvent.EntityTooltipInfo.CODEC.forGetter(HoverEvent.ShowEntity::entity)).apply($$0, HoverEvent.ShowEntity::new)
      );

      @Override
      public HoverEvent.Action action() {
         return HoverEvent.Action.SHOW_ENTITY;
      }
   }

   public record ShowItem(ItemStack item) implements HoverEvent {
      public static final MapCodec<HoverEvent.ShowItem> CODEC = ItemStack.MAP_CODEC.xmap(HoverEvent.ShowItem::new, HoverEvent.ShowItem::item);

      public ShowItem(ItemStack item) {
         item = item.copy();
         this.item = item;
      }

      @Override
      public HoverEvent.Action action() {
         return HoverEvent.Action.SHOW_ITEM;
      }

      @Override
      public boolean equals(Object $$0) {
         return $$0 instanceof HoverEvent.ShowItem $$1 && ItemStack.matches(this.item, $$1.item);
      }

      @Override
      public int hashCode() {
         return ItemStack.hashItemAndComponents(this.item);
      }
   }

   public record ShowText(Component value) implements HoverEvent {
      public static final MapCodec<HoverEvent.ShowText> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ComponentSerialization.CODEC.fieldOf("value").forGetter(HoverEvent.ShowText::value)).apply($$0, HoverEvent.ShowText::new)
      );

      @Override
      public HoverEvent.Action action() {
         return HoverEvent.Action.SHOW_TEXT;
      }
   }
}
