/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.server.dialog.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.functions.StringTemplate;

public class ParsedTemplate {
    public static final Codec<ParsedTemplate> CODEC = Codec.STRING.comapFlatMap(ParsedTemplate::parse, $$0 -> $$0.raw);
    public static final Codec<String> VARIABLE_CODEC = Codec.STRING.validate($$0 -> StringTemplate.isValidVariableName($$0) ? DataResult.success((Object)$$0) : DataResult.error(() -> $$0 + " is not a valid input name"));
    private final String raw;
    private final StringTemplate parsed;

    private ParsedTemplate(String $$0, StringTemplate $$1) {
        this.raw = $$0;
        this.parsed = $$1;
    }

    /*
     * WARNING - void declaration
     */
    private static DataResult<ParsedTemplate> parse(String $$0) {
        void $$3;
        try {
            StringTemplate $$1 = StringTemplate.fromString($$0);
        }
        catch (Exception $$2) {
            return DataResult.error(() -> "Failed to parse template " + $$0 + ": " + $$2.getMessage());
        }
        return DataResult.success((Object)new ParsedTemplate($$0, (StringTemplate)$$3));
    }

    public String instantiate(Map<String, String> $$0) {
        List<String> $$12 = this.parsed.variables().stream().map($$1 -> $$0.getOrDefault($$1, "")).toList();
        return this.parsed.substitute($$12);
    }
}

