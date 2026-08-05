/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput {
    private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("arguments.item.overstacked", $$0, $$1));
    private final Holder<Item> item;
    private final DataComponentPatch components;

    public ItemInput(Holder<Item> $$0, DataComponentPatch $$1) {
        this.item = $$0;
        this.components = $$1;
    }

    public Item getItem() {
        return this.item.value();
    }

    public ItemStack createItemStack(int $$0, boolean $$1) throws CommandSyntaxException {
        ItemStack $$2 = new ItemStack(this.item, $$0);
        $$2.applyComponents(this.components);
        if ($$1 && $$0 > $$2.getMaxStackSize()) {
            throw ERROR_STACK_TOO_BIG.create((Object)this.getItemName(), (Object)$$2.getMaxStackSize());
        }
        return $$2;
    }

    public String serialize(HolderLookup.Provider $$0) {
        StringBuilder $$1 = new StringBuilder(this.getItemName());
        String $$2 = this.serializeComponents($$0);
        if (!$$2.isEmpty()) {
            $$1.append('[');
            $$1.append($$2);
            $$1.append(']');
        }
        return $$1.toString();
    }

    private String serializeComponents(HolderLookup.Provider $$0) {
        RegistryOps<Tag> $$1 = $$0.createSerializationContext(NbtOps.INSTANCE);
        return this.components.entrySet().stream().flatMap($$12 -> {
            DataComponentType $$2 = (DataComponentType)$$12.getKey();
            Identifier $$3 = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey($$2);
            if ($$3 == null) {
                return Stream.empty();
            }
            Optional $$4 = (Optional)$$12.getValue();
            if ($$4.isPresent()) {
                TypedDataComponent $$5 = TypedDataComponent.createUnchecked($$2, $$4.get());
                return $$5.encodeValue($$1).result().stream().map($$1 -> $$3.toString() + "=" + String.valueOf($$1));
            }
            return Stream.of("!" + $$3.toString());
        }).collect(Collectors.joining(String.valueOf(',')));
    }

    private String getItemName() {
        return this.item.unwrapKey().map(ResourceKey::identifier).orElseGet(() -> "unknown[" + String.valueOf(this.item) + "]").toString();
    }
}

