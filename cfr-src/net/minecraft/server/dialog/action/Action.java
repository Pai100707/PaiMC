/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.server.dialog.action;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;

public interface Action {
    public static final Codec<Action> CODEC = BuiltInRegistries.DIALOG_ACTION_TYPE.byNameCodec().dispatch(Action::codec, $$0 -> $$0);

    public MapCodec<? extends Action> codec();

    public Optional<ClickEvent> createAction(Map<String, ValueGetter> var1);

    public static interface ValueGetter {
        public String asTemplateSubstitution();

        public Tag asTag();

        public static Map<String, String> getAsTemplateSubstitutions(Map<String, ValueGetter> $$0) {
            return Maps.transformValues($$0, ValueGetter::asTemplateSubstitution);
        }

        public static ValueGetter of(final String $$0) {
            return new ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return $$0;
                }

                @Override
                public Tag asTag() {
                    return StringTag.valueOf($$0);
                }
            };
        }

        public static ValueGetter of(final Supplier<String> $$0) {
            return new ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return (String)$$0.get();
                }

                @Override
                public Tag asTag() {
                    return StringTag.valueOf((String)$$0.get());
                }
            };
        }
    }
}

