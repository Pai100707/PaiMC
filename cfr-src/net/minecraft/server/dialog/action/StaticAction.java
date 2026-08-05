/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.util.Util;

public record StaticAction(ClickEvent value) implements Action
{
    public static final Map<ClickEvent.Action, MapCodec<StaticAction>> WRAPPED_CODECS = Util.make(() -> {
        EnumMap<ClickEvent.Action, MapCodec> $$0 = new EnumMap<ClickEvent.Action, MapCodec>(ClickEvent.Action.class);
        for (ClickEvent.Action $$1 : (ClickEvent.Action[])ClickEvent.Action.class.getEnumConstants()) {
            if (!$$1.isAllowedFromServer()) continue;
            MapCodec<? extends ClickEvent> $$2 = $$1.valueCodec();
            $$0.put($$1, $$2.xmap(StaticAction::new, StaticAction::value));
        }
        return Collections.unmodifiableMap($$0);
    });

    public MapCodec<StaticAction> codec() {
        return WRAPPED_CODECS.get(this.value.action());
    }

    @Override
    public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> $$0) {
        return Optional.of(this.value);
    }
}

