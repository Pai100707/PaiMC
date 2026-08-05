/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dialog.action;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.ParsedTemplate;

public record CommandTemplate(ParsedTemplate template) implements Action
{
    public static final MapCodec<CommandTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ParsedTemplate.CODEC.fieldOf("template").forGetter(CommandTemplate::template)).apply((Applicative)$$0, CommandTemplate::new));

    public MapCodec<CommandTemplate> codec() {
        return MAP_CODEC;
    }

    @Override
    public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> $$0) {
        String $$1 = this.template.instantiate(Action.ValueGetter.getAsTemplateSubstitutions($$0));
        return Optional.of(new ClickEvent.RunCommand($$1));
    }
}

