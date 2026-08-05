/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.commands.functions.PlainTextFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {
    private @Nullable List<UnboundEntryAction<T>> plainEntries = new ArrayList<UnboundEntryAction<T>>();
    private @Nullable List<MacroFunction.Entry<T>> macroEntries;
    private final List<String> macroArguments = new ArrayList<String>();

    FunctionBuilder() {
    }

    public void addCommand(UnboundEntryAction<T> $$0) {
        if (this.macroEntries != null) {
            this.macroEntries.add(new MacroFunction.PlainTextEntry<T>($$0));
        } else {
            this.plainEntries.add($$0);
        }
    }

    private int getArgumentIndex(String $$0) {
        int $$1 = this.macroArguments.indexOf($$0);
        if ($$1 == -1) {
            $$1 = this.macroArguments.size();
            this.macroArguments.add($$0);
        }
        return $$1;
    }

    private IntList convertToIndices(List<String> $$0) {
        IntArrayList $$1 = new IntArrayList($$0.size());
        for (String $$2 : $$0) {
            $$1.add(this.getArgumentIndex($$2));
        }
        return $$1;
    }

    /*
     * WARNING - void declaration
     */
    public void addMacro(String $$0, int $$1, T $$2) {
        void $$5;
        try {
            StringTemplate $$3 = StringTemplate.fromString($$0);
        }
        catch (Exception $$4) {
            throw new IllegalArgumentException("Can't parse function line " + $$1 + ": '" + $$0 + "'", $$4);
        }
        if (this.plainEntries != null) {
            this.macroEntries = new ArrayList<MacroFunction.Entry<T>>(this.plainEntries.size() + 1);
            for (UnboundEntryAction<T> $$6 : this.plainEntries) {
                this.macroEntries.add(new MacroFunction.PlainTextEntry<T>($$6));
            }
            this.plainEntries = null;
        }
        this.macroEntries.add(new MacroFunction.MacroEntry<T>((StringTemplate)$$5, this.convertToIndices($$5.variables()), $$2));
    }

    public CommandFunction<T> build(Identifier $$0) {
        if (this.macroEntries != null) {
            return new MacroFunction<T>($$0, this.macroEntries, this.macroArguments);
        }
        return new PlainTextFunction<T>($$0, this.plainEntries);
    }
}

