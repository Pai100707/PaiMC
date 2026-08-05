/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCommandsPacket> STREAM_CODEC = Packet.codec(ClientboundCommandsPacket::write, ClientboundCommandsPacket::new);
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte FLAG_RESTRICTED = 32;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<Entry> entries;

    public <S> ClientboundCommandsPacket(RootCommandNode<S> $$0, NodeInspector<S> $$1) {
        Object2IntMap<CommandNode<S>> $$2 = ClientboundCommandsPacket.enumerateNodes($$0);
        this.entries = ClientboundCommandsPacket.createEntries($$2, $$1);
        this.rootIndex = $$2.getInt($$0);
    }

    private ClientboundCommandsPacket(FriendlyByteBuf $$0) {
        this.entries = $$0.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = $$0.readVarInt();
        ClientboundCommandsPacket.validateEntries(this.entries);
    }

    private void write(FriendlyByteBuf $$02) {
        $$02.writeCollection(this.entries, ($$0, $$1) -> $$1.write((FriendlyByteBuf)((Object)$$0)));
        $$02.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<Entry> $$0, BiPredicate<Entry, IntSet> $$1) {
        IntOpenHashSet $$2 = new IntOpenHashSet((IntCollection)IntSets.fromTo((int)0, (int)$$0.size()));
        while (!$$2.isEmpty()) {
            boolean $$3 = $$2.removeIf(arg_0 -> ClientboundCommandsPacket.lambda$validateEntries$1($$1, $$0, (IntSet)$$2, arg_0));
            if ($$3) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validateEntries(List<Entry> $$0) {
        ClientboundCommandsPacket.validateEntries($$0, Entry::canBuild);
        ClientboundCommandsPacket.validateEntries($$0, Entry::canResolve);
    }

    private static <S> Object2IntMap<CommandNode<S>> enumerateNodes(RootCommandNode<S> $$0) {
        CommandNode $$3;
        Object2IntOpenHashMap $$1 = new Object2IntOpenHashMap();
        ArrayDeque<Object> $$2 = new ArrayDeque<Object>();
        $$2.add($$0);
        while (($$3 = (CommandNode)$$2.poll()) != null) {
            if ($$1.containsKey((Object)$$3)) continue;
            int $$4 = $$1.size();
            $$1.put((Object)$$3, $$4);
            $$2.addAll($$3.getChildren());
            if ($$3.getRedirect() == null) continue;
            $$2.add($$3.getRedirect());
        }
        return $$1;
    }

    private static <S> List<Entry> createEntries(Object2IntMap<CommandNode<S>> $$0, NodeInspector<S> $$1) {
        ObjectArrayList $$2 = new ObjectArrayList($$0.size());
        $$2.size($$0.size());
        for (Object2IntMap.Entry $$3 : Object2IntMaps.fastIterable($$0)) {
            $$2.set($$3.getIntValue(), (Object)ClientboundCommandsPacket.createEntry((CommandNode)$$3.getKey(), $$1, $$0));
        }
        return $$2;
    }

    private static Entry readNode(FriendlyByteBuf $$0) {
        byte $$1 = $$0.readByte();
        int[] $$2 = $$0.readVarIntArray();
        int $$3 = ($$1 & 8) != 0 ? $$0.readVarInt() : 0;
        NodeStub $$4 = ClientboundCommandsPacket.read($$0, $$1);
        return new Entry($$4, $$1, $$3, $$2);
    }

    private static @Nullable NodeStub read(FriendlyByteBuf $$0, byte $$1) {
        int $$2 = $$1 & 3;
        if ($$2 == 2) {
            String $$3 = $$0.readUtf();
            int $$4 = $$0.readVarInt();
            ArgumentTypeInfo $$5 = (ArgumentTypeInfo)BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId($$4);
            if ($$5 == null) {
                return null;
            }
            Object $$6 = $$5.deserializeFromNetwork($$0);
            Identifier $$7 = ($$1 & 0x10) != 0 ? $$0.readIdentifier() : null;
            return new ArgumentNodeStub($$3, (ArgumentTypeInfo.Template<?>)$$6, $$7);
        }
        if ($$2 == 1) {
            String $$8 = $$0.readUtf();
            return new LiteralNodeStub($$8);
        }
        return null;
    }

    /*
     * WARNING - void declaration
     */
    private static <S> Entry createEntry(CommandNode<S> $$0, NodeInspector<S> $$1, Object2IntMap<CommandNode<S>> $$2) {
        void $$13;
        int $$5;
        int $$3 = 0;
        if ($$0.getRedirect() != null) {
            $$3 |= 8;
            int $$4 = $$2.getInt((Object)$$0.getRedirect());
        } else {
            $$5 = 0;
        }
        if ($$1.isExecutable($$0)) {
            $$3 |= 4;
        }
        if ($$1.isRestricted($$0)) {
            $$3 |= 0x20;
        }
        CommandNode<S> commandNode = $$0;
        Objects.requireNonNull(commandNode);
        CommandNode<S> commandNode2 = commandNode;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, ArgumentCommandNode.class, LiteralCommandNode.class}, commandNode2, n)) {
            case 0: {
                RootCommandNode $$6 = (RootCommandNode)commandNode2;
                $$3 |= 0;
                Object $$7 = null;
                break;
            }
            case 1: {
                ArgumentCommandNode $$8 = (ArgumentCommandNode)commandNode2;
                Identifier $$9 = $$1.suggestionId($$8);
                ArgumentNodeStub $$10 = new ArgumentNodeStub($$8.getName(), ArgumentTypeInfos.unpack($$8.getType()), $$9);
                $$3 |= 2;
                if ($$9 != null) {
                    $$3 |= 0x10;
                }
                break;
            }
            case 2: {
                LiteralCommandNode $$11 = (LiteralCommandNode)commandNode2;
                LiteralNodeStub $$12 = new LiteralNodeStub($$11.getLiteral());
                $$3 |= 1;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown node type " + String.valueOf($$0));
            }
        }
        int[] $$14 = $$0.getChildren().stream().mapToInt(arg_0 -> $$2.getInt(arg_0)).toArray();
        return new Entry((NodeStub)$$13, $$3, $$5, $$14);
    }

    @Override
    public PacketType<ClientboundCommandsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_COMMANDS;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleCommands(this);
    }

    public <S> RootCommandNode<S> getRoot(CommandBuildContext $$0, NodeBuilder<S> $$1) {
        return (RootCommandNode)new NodeResolver<S>($$0, $$1, this.entries).resolve(this.rootIndex);
    }

    private static /* synthetic */ boolean lambda$validateEntries$1(BiPredicate $$0, List $$1, IntSet $$2, int $$3) {
        return $$0.test((Entry)$$1.get($$3), $$2);
    }

    public static interface NodeInspector<S> {
        public @Nullable Identifier suggestionId(ArgumentCommandNode<S, ?> var1);

        public boolean isExecutable(CommandNode<S> var1);

        public boolean isRestricted(CommandNode<S> var1);
    }

    static final class Entry
    extends Record {
        final @Nullable NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable NodeStub $$0, int $$1, int $$2, int[] $$3) {
            this.stub = $$0;
            this.flags = $$1;
            this.redirect = $$2;
            this.children = $$3;
        }

        public void write(FriendlyByteBuf $$0) {
            $$0.writeByte(this.flags);
            $$0.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                $$0.writeVarInt(this.redirect);
            }
            if (this.stub != null) {
                this.stub.write($$0);
            }
        }

        public boolean canBuild(IntSet $$0) {
            if ((this.flags & 8) != 0) {
                return !$$0.contains(this.redirect);
            }
            return true;
        }

        public boolean canResolve(IntSet $$0) {
            for (int $$1 : this.children) {
                if (!$$0.contains($$1)) continue;
                return false;
            }
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this, $$0);
        }

        public @Nullable NodeStub stub() {
            return this.stub;
        }

        public int flags() {
            return this.flags;
        }

        public int redirect() {
            return this.redirect;
        }

        public int[] children() {
            return this.children;
        }
    }

    static interface NodeStub {
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext var1, NodeBuilder<S> var2);

        public void write(FriendlyByteBuf var1);
    }

    record ArgumentNodeStub(String id, ArgumentTypeInfo.Template<?> argumentType, @Nullable Identifier suggestionId) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext $$0, NodeBuilder<S> $$1) {
            Object $$2 = this.argumentType.instantiate($$0);
            return $$1.createArgument(this.id, (ArgumentType<?>)$$2, this.suggestionId);
        }

        @Override
        public void write(FriendlyByteBuf $$0) {
            $$0.writeUtf(this.id);
            ArgumentNodeStub.serializeCap($$0, this.argumentType);
            if (this.suggestionId != null) {
                $$0.writeIdentifier(this.suggestionId);
            }
        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf $$0, ArgumentTypeInfo.Template<A> $$1) {
            ArgumentNodeStub.serializeCap($$0, $$1.type(), $$1);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf $$0, ArgumentTypeInfo<A, T> $$1, ArgumentTypeInfo.Template<A> $$2) {
            $$0.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId($$1));
            $$1.serializeToNetwork($$2, $$0);
        }
    }

    record LiteralNodeStub(String id) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext $$0, NodeBuilder<S> $$1) {
            return $$1.createLiteral(this.id);
        }

        @Override
        public void write(FriendlyByteBuf $$0) {
            $$0.writeUtf(this.id);
        }
    }

    static class NodeResolver<S> {
        private final CommandBuildContext context;
        private final NodeBuilder<S> builder;
        private final List<Entry> entries;
        private final List<CommandNode<S>> nodes;

        NodeResolver(CommandBuildContext $$0, NodeBuilder<S> $$1, List<Entry> $$2) {
            this.context = $$0;
            this.builder = $$1;
            this.entries = $$2;
            ObjectArrayList $$3 = new ObjectArrayList();
            $$3.size($$2.size());
            this.nodes = $$3;
        }

        public CommandNode<S> resolve(int $$0) {
            CommandNode $$7;
            CommandNode<S> $$1 = this.nodes.get($$0);
            if ($$1 != null) {
                return $$1;
            }
            Entry $$2 = this.entries.get($$0);
            if ($$2.stub == null) {
                RootCommandNode $$3 = new RootCommandNode();
            } else {
                ArgumentBuilder<S, ?> $$4 = $$2.stub.build(this.context, this.builder);
                if (($$2.flags & 8) != 0) {
                    $$4.redirect(this.resolve($$2.redirect));
                }
                boolean $$5 = ($$2.flags & 4) != 0;
                boolean $$6 = ($$2.flags & 0x20) != 0;
                $$7 = this.builder.configure($$4, $$5, $$6).build();
            }
            this.nodes.set($$0, $$7);
            for (int $$8 : $$2.children) {
                CommandNode<S> $$9 = this.resolve($$8);
                if ($$9 instanceof RootCommandNode) continue;
                $$7.addChild($$9);
            }
            return $$7;
        }
    }

    public static interface NodeBuilder<S> {
        public ArgumentBuilder<S, ?> createLiteral(String var1);

        public ArgumentBuilder<S, ?> createArgument(String var1, ArgumentType<?> var2, @Nullable Identifier var3);

        public ArgumentBuilder<S, ?> configure(ArgumentBuilder<S, ?> var1, boolean var2, boolean var3);
    }
}

