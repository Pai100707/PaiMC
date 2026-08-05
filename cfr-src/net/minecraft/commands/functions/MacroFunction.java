/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.functions.PlainTextFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MacroFunction<T extends ExecutionCommandSource<T>>
implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ROOT)), $$0 -> $$0.setMaximumFractionDigits(15));
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25f);
    private final Identifier id;
    private final List<Entry<T>> entries;

    public MacroFunction(Identifier $$0, List<Entry<T>> $$1, List<String> $$2) {
        this.id = $$0;
        this.entries = $$1;
        this.parameters = $$2;
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag $$0, CommandDispatcher<T> $$1) throws FunctionInstantiationException {
        if ($$0 == null) {
            throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
        }
        ArrayList<String> $$2 = new ArrayList<String>(this.parameters.size());
        for (String $$3 : this.parameters) {
            Tag $$4 = $$0.get($$3);
            if ($$4 == null) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), $$3));
            }
            $$2.add(MacroFunction.stringify($$4));
        }
        InstantiatedFunction $$5 = (InstantiatedFunction)this.cache.getAndMoveToLast($$2);
        if ($$5 != null) {
            return $$5;
        }
        if (this.cache.size() >= 8) {
            this.cache.removeFirst();
        }
        InstantiatedFunction<T> $$6 = this.substituteAndParse(this.parameters, $$2, $$1);
        this.cache.put($$2, $$6);
        return $$6;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String stringify(Tag $$0) {
        String string;
        Tag tag = $$0;
        Objects.requireNonNull(tag);
        Tag tag2 = tag;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FloatTag.class, DoubleTag.class, ByteTag.class, ShortTag.class, LongTag.class, StringTag.class}, (Object)tag2, n)) {
            case 0: {
                float $$1;
                FloatTag floatTag = (FloatTag)tag2;
                try {
                    float f;
                    $$1 = f = floatTag.value();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                string = DECIMAL_FORMAT.format($$1);
                return string;
            }
            case 1: {
                double $$2;
                DoubleTag doubleTag = (DoubleTag)tag2;
                {
                    double d;
                    $$2 = d = doubleTag.value();
                }
                string = DECIMAL_FORMAT.format($$2);
                return string;
            }
            case 2: {
                byte $$3;
                ByteTag byteTag = (ByteTag)tag2;
                {
                    byte by;
                    $$3 = by = byteTag.value();
                }
                string = String.valueOf($$3);
                return string;
            }
            case 3: {
                short $$4;
                ShortTag shortTag = (ShortTag)tag2;
                {
                    short s;
                    $$4 = s = shortTag.value();
                }
                string = String.valueOf($$4);
                return string;
            }
            case 4: {
                long $$5;
                LongTag longTag = (LongTag)tag2;
                {
                    long l;
                    $$5 = l = longTag.value();
                }
                string = String.valueOf($$5);
                return string;
            }
            case 5: {
                StringTag stringTag = (StringTag)tag2;
                {
                    String string2;
                    String $$6;
                    string = $$6 = (string2 = stringTag.value());
                    return string;
                }
            }
        }
        string = $$0.toString();
        return string;
    }

    private static void lookupValues(List<String> $$0, IntList $$1, List<String> $$22) {
        $$22.clear();
        $$1.forEach($$2 -> $$22.add((String)$$0.get($$2)));
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> $$0, List<String> $$12, CommandDispatcher<T> $$2) throws FunctionInstantiationException {
        ArrayList $$3 = new ArrayList(this.entries.size());
        ArrayList<String> $$4 = new ArrayList<String>($$12.size());
        for (Entry<T> $$5 : this.entries) {
            MacroFunction.lookupValues($$12, $$5.parameters(), $$4);
            $$3.add($$5.instantiate($$4, $$2, this.id));
        }
        return new PlainTextFunction(this.id().withPath($$1 -> $$1 + "/" + $$0.hashCode()), $$3);
    }

    static interface Entry<T> {
        public IntList parameters();

        public UnboundEntryAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, Identifier var3) throws FunctionInstantiationException;
    }

    static class MacroEntry<T extends ExecutionCommandSource<T>>
    implements Entry<T> {
        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public MacroEntry(StringTemplate $$0, IntList $$1, T $$2) {
            this.template = $$0;
            this.parameters = $$1;
            this.compilationContext = $$2;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> $$0, CommandDispatcher<T> $$1, Identifier $$2) throws FunctionInstantiationException {
            String $$3 = this.template.substitute($$0);
            try {
                return CommandFunction.parseCommand($$1, this.compilationContext, new StringReader($$3));
            }
            catch (CommandSyntaxException $$4) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.parse", Component.translationArg($$2), $$3, $$4.getMessage()));
            }
        }
    }

    static class PlainTextEntry<T>
    implements Entry<T> {
        private final UnboundEntryAction<T> compiledAction;

        public PlainTextEntry(UnboundEntryAction<T> $$0) {
            this.compiledAction = $$0;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> $$0, CommandDispatcher<T> $$1, Identifier $$2) {
            return this.compiledAction;
        }
    }
}

