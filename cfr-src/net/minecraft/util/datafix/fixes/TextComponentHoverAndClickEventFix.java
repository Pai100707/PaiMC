/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public class TextComponentHoverAndClickEventFix
extends DataFix {
    public TextComponentHoverAndClickEventFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.createFixer(this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT), this.getOutputSchema().getType(References.TEXT_COMPONENT), $$0);
    }

    private <C1, C2, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C1> $$0, Type<C2> $$1, Type<H> $$22) {
        Type $$3 = DSL.named((String)References.TEXT_COMPONENT.typeName(), (Type)DSL.or((Type)DSL.or((Type)DSL.string(), (Type)DSL.list($$0)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"extra", (Type)DSL.list($$0))), (Type)DSL.optional((Type)DSL.field((String)"separator", $$0)), (Type)DSL.optional((Type)DSL.field((String)"hoverEvent", $$22)), (Type)DSL.remainderType())));
        if (!$$3.equals((Object)this.getInputSchema().getType(References.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf($$3) + " but got " + String.valueOf(this.getInputSchema().getType(References.TEXT_COMPONENT)));
        }
        Type<?> $$4 = ExtraDataFixUtils.patchSubType($$3, $$3, $$1);
        return this.fixTypeEverywhere("TextComponentHoverAndClickEventFix", $$3, $$1, $$2 -> $$3 -> {
            boolean $$4 = (Boolean)((Either)$$3.getSecond()).map($$0 -> false, $$0 -> {
                Object $$2 = (Pair)((Pair)$$0.getSecond()).getSecond();
                Type $$1 = ((Either)$$2.getFirst()).left().isPresent();
                Object $$3 = ((Dynamic)$$2.getSecond()).get("clickEvent").result().isPresent();
                return $$1 || $$3;
            });
            if (!$$4) {
                return $$3;
            }
            return Util.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast($$4, $$3, $$2), $$1, TextComponentHoverAndClickEventFix::fixTextComponent).getValue();
        });
    }

    private static Dynamic<?> fixTextComponent(Dynamic<?> $$0) {
        return $$0.renameAndFixField("hoverEvent", "hover_event", TextComponentHoverAndClickEventFix::fixHoverEvent).renameAndFixField("clickEvent", "click_event", TextComponentHoverAndClickEventFix::fixClickEvent);
    }

    private static Dynamic<?> copyFields(Dynamic<?> $$0, Dynamic<?> $$1, String ... $$2) {
        for (String $$3 : $$2) {
            $$0 = Dynamic.copyField($$1, (String)$$3, $$0, (String)$$3);
        }
        return $$0;
    }

    private static Dynamic<?> fixHoverEvent(Dynamic<?> $$0) {
        String $$1;
        return switch ($$1 = $$0.get("action").asString("")) {
            case "show_text" -> $$0.renameField("contents", "value");
            case "show_item" -> {
                Dynamic $$2 = $$0.get("contents").orElseEmptyMap();
                Optional $$3 = $$2.asString().result();
                if ($$3.isPresent()) {
                    yield $$0.renameField("contents", "id");
                }
                yield TextComponentHoverAndClickEventFix.copyFields($$0.remove("contents"), $$2, "id", "count", "components");
            }
            case "show_entity" -> {
                Dynamic $$4 = $$0.get("contents").orElseEmptyMap();
                yield TextComponentHoverAndClickEventFix.copyFields($$0.remove("contents"), $$4, "id", "type", "name").renameField("id", "uuid").renameField("type", "id");
            }
            default -> $$0;
        };
    }

    private static <T> @Nullable Dynamic<T> fixClickEvent(Dynamic<T> $$0) {
        String $$1 = $$0.get("action").asString("");
        String $$2 = $$0.get("value").asString("");
        return switch ($$1) {
            case "open_url" -> {
                if (!TextComponentHoverAndClickEventFix.validateUri($$2)) {
                    yield null;
                }
                yield $$0.renameField("value", "url");
            }
            case "open_file" -> $$0.renameField("value", "path");
            case "run_command", "suggest_command" -> {
                if (!TextComponentHoverAndClickEventFix.validateChat($$2)) {
                    yield null;
                }
                yield $$0.renameField("value", "command");
            }
            case "change_page" -> {
                Integer $$3 = $$0.get("value").result().map(TextComponentHoverAndClickEventFix::parseOldPage).orElse(null);
                if ($$3 == null) {
                    yield null;
                }
                int $$4 = Math.max($$3, 1);
                yield $$0.remove("value").set("page", $$0.createInt($$4));
            }
            default -> $$0;
        };
    }

    private static @Nullable Integer parseOldPage(Dynamic<?> $$0) {
        Optional $$1 = $$0.asNumber().result();
        if ($$1.isPresent()) {
            return ((Number)$$1.get()).intValue();
        }
        try {
            return Integer.parseInt($$0.asString(""));
        }
        catch (Exception $$2) {
            return null;
        }
    }

    private static boolean validateUri(String $$0) {
        try {
            URI $$1 = new URI($$0);
            String $$2 = $$1.getScheme();
            if ($$2 == null) {
                return false;
            }
            String $$3 = $$2.toLowerCase(Locale.ROOT);
            return "http".equals($$3) || "https".equals($$3);
        }
        catch (URISyntaxException $$4) {
            return false;
        }
    }

    private static boolean validateChat(String $$0) {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            char $$2 = $$0.charAt($$1);
            if ($$2 != '\u00a7' && $$2 >= ' ' && $$2 != '\u007f') continue;
            return false;
        }
        return true;
    }
}

