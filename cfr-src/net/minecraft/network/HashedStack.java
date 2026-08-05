/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 */
package net.minecraft.network;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface HashedStack {
    public static final HashedStack EMPTY = new HashedStack(){

        public String toString() {
            return "<empty>";
        }

        @Override
        public boolean matches(ItemStack $$0, HashedPatchMap.HashGenerator $$1) {
            return $$0.isEmpty();
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, HashedStack> STREAM_CODEC = ByteBufCodecs.optional(ActualItem.STREAM_CODEC).map($$0 -> (HashedStack)DataFixUtils.orElse((Optional)$$0, (Object)EMPTY), $$0 -> {
        Optional<Object> optional;
        if ($$0 instanceof ActualItem) {
            ActualItem $$1 = (ActualItem)$$0;
            optional = Optional.of($$1);
        } else {
            optional = Optional.empty();
        }
        return optional;
    });

    public boolean matches(ItemStack var1, HashedPatchMap.HashGenerator var2);

    public static HashedStack create(ItemStack $$0, HashedPatchMap.HashGenerator $$1) {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        return new ActualItem($$0.getItemHolder(), $$0.getCount(), HashedPatchMap.create($$0.getComponentsPatch(), $$1));
    }

    public record ActualItem(Holder<Item> item, int count, HashedPatchMap components) implements HashedStack
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, ActualItem> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.ITEM), ActualItem::item, ByteBufCodecs.VAR_INT, ActualItem::count, HashedPatchMap.STREAM_CODEC, ActualItem::components, ActualItem::new);

        @Override
        public boolean matches(ItemStack $$0, HashedPatchMap.HashGenerator $$1) {
            if (this.count != $$0.getCount()) {
                return false;
            }
            if (!this.item.equals($$0.getItemHolder())) {
                return false;
            }
            return this.components.matches($$0.getComponentsPatch(), $$1);
        }
    }
}

