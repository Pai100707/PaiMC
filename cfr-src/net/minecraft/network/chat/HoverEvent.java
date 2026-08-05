/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface HoverEvent {
    public static final Codec<HoverEvent> CODEC = Action.CODEC.dispatch("action", HoverEvent::action, $$0 -> $$0.codec);

    public Action action();

    public static enum Action implements StringRepresentable
    {
        SHOW_TEXT("show_text", true, ShowText.CODEC),
        SHOW_ITEM("show_item", true, ShowItem.CODEC),
        SHOW_ENTITY("show_entity", true, ShowEntity.CODEC);

        public static final Codec<Action> UNSAFE_CODEC;
        public static final Codec<Action> CODEC;
        private final String name;
        private final boolean allowFromServer;
        final MapCodec<? extends HoverEvent> codec;

        private Action(String $$0, boolean $$1, MapCodec<? extends HoverEvent> $$2) {
            this.name = $$0;
            this.allowFromServer = $$1;
            this.codec = $$2;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action> filterForSerialization(Action $$0) {
            if (!$$0.isAllowedFromServer()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf($$0));
            }
            return DataResult.success((Object)$$0, (Lifecycle)Lifecycle.stable());
        }

        static {
            UNSAFE_CODEC = StringRepresentable.fromValues(Action::values);
            CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
        }
    }

    public static class EntityTooltipInfo {
        public static final MapCodec<EntityTooltipInfo> CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter($$0 -> $$0.type), (App)UUIDUtil.LENIENT_CODEC.fieldOf("uuid").forGetter($$0 -> $$0.uuid), (App)ComponentSerialization.CODEC.optionalFieldOf("name").forGetter($$0 -> $$0.name)).apply((Applicative)$$02, EntityTooltipInfo::new));
        public final EntityType<?> type;
        public final UUID uuid;
        public final Optional<Component> name;
        private @Nullable List<Component> linesCache;

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
                this.linesCache = new ArrayList<Component>();
                this.name.ifPresent(this.linesCache::add);
                this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(Component.literal(this.uuid.toString()));
            }
            return this.linesCache;
        }

        public boolean equals(Object $$0) {
            if (this == $$0) {
                return true;
            }
            if ($$0 == null || this.getClass() != $$0.getClass()) {
                return false;
            }
            EntityTooltipInfo $$1 = (EntityTooltipInfo)$$0;
            return this.type.equals($$1.type) && this.uuid.equals($$1.uuid) && this.name.equals($$1.name);
        }

        public int hashCode() {
            int $$0 = this.type.hashCode();
            $$0 = 31 * $$0 + this.uuid.hashCode();
            $$0 = 31 * $$0 + this.name.hashCode();
            return $$0;
        }
    }

    public record ShowEntity(EntityTooltipInfo entity) implements HoverEvent
    {
        public static final MapCodec<ShowEntity> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)EntityTooltipInfo.CODEC.forGetter(ShowEntity::entity)).apply((Applicative)$$0, ShowEntity::new));

        @Override
        public Action action() {
            return Action.SHOW_ENTITY;
        }
    }

    public record ShowItem(ItemStack item) implements HoverEvent
    {
        public static final MapCodec<ShowItem> CODEC = ItemStack.MAP_CODEC.xmap(ShowItem::new, ShowItem::item);

        public ShowItem(ItemStack $$0) {
            this.item = $$0 = $$0.copy();
        }

        @Override
        public Action action() {
            return Action.SHOW_ITEM;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object $$0) {
            if (!($$0 instanceof ShowItem)) return false;
            ShowItem $$1 = (ShowItem)$$0;
            if (!ItemStack.matches(this.item, $$1.item)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(this.item);
        }
    }

    public record ShowText(Component value) implements HoverEvent
    {
        public static final MapCodec<ShowText> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ComponentSerialization.CODEC.fieldOf("value").forGetter(ShowText::value)).apply((Applicative)$$0, ShowText::new));

        @Override
        public Action action() {
            return Action.SHOW_TEXT;
        }
    }
}

