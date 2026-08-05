/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 */
package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.commands.functions.CommandFunction;

public record StringTemplate(List<String> segments, List<String> variables) {
    public static StringTemplate fromString(String $$0) {
        ImmutableList.Builder $$1 = ImmutableList.builder();
        ImmutableList.Builder $$2 = ImmutableList.builder();
        int $$3 = $$0.length();
        int $$4 = 0;
        int $$5 = $$0.indexOf(36);
        while ($$5 != -1) {
            if ($$5 == $$3 - 1 || $$0.charAt($$5 + 1) != '(') {
                $$5 = $$0.indexOf(36, $$5 + 1);
                continue;
            }
            $$1.add((Object)$$0.substring($$4, $$5));
            int $$6 = $$0.indexOf(41, $$5 + 1);
            if ($$6 == -1) {
                throw new IllegalArgumentException("Unterminated macro variable");
            }
            String $$7 = $$0.substring($$5 + 2, $$6);
            if (!StringTemplate.isValidVariableName($$7)) {
                throw new IllegalArgumentException("Invalid macro variable name '" + $$7 + "'");
            }
            $$2.add((Object)$$7);
            $$4 = $$6 + 1;
            $$5 = $$0.indexOf(36, $$4);
        }
        if ($$4 == 0) {
            throw new IllegalArgumentException("No variables in macro");
        }
        if ($$4 != $$3) {
            $$1.add((Object)$$0.substring($$4));
        }
        return new StringTemplate((List<String>)$$1.build(), (List<String>)$$2.build());
    }

    public static boolean isValidVariableName(String $$0) {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            char $$2 = $$0.charAt($$1);
            if (Character.isLetterOrDigit($$2) || $$2 == '_') continue;
            return false;
        }
        return true;
    }

    public String substitute(List<String> $$0) {
        StringBuilder $$1 = new StringBuilder();
        for (int $$2 = 0; $$2 < this.variables.size(); ++$$2) {
            $$1.append(this.segments.get($$2)).append($$0.get($$2));
            CommandFunction.checkCommandLineLength($$1);
        }
        if (this.segments.size() > this.variables.size()) {
            $$1.append(this.segments.getLast());
        }
        CommandFunction.checkCommandLineLength($$1);
        return $$1.toString();
    }
}

