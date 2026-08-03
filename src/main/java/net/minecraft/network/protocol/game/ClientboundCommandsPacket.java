package net.minecraft.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentTypeInfo.Template;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundCommandsPacket> STREAM_CODEC = Packet.codec(
      ClientboundCommandsPacket::write, ClientboundCommandsPacket::new
   );
   private static final byte MASK_TYPE = 3;
   private static final byte FLAG_EXECUTABLE = 4;
   private static final byte FLAG_REDIRECT = 8;
   private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
   private static final byte FLAG_RESTRICTED = 32;
   private static final byte TYPE_ROOT = 0;
   private static final byte TYPE_LITERAL = 1;
   private static final byte TYPE_ARGUMENT = 2;
   private final int rootIndex;
   private final List<ClientboundCommandsPacket.Entry> entries;

   public <S> ClientboundCommandsPacket(RootCommandNode<S> $$0, ClientboundCommandsPacket.NodeInspector<S> $$1) {
      Object2IntMap<CommandNode<S>> $$2 = enumerateNodes($$0);
      this.entries = createEntries($$2, $$1);
      this.rootIndex = $$2.getInt($$0);
   }

   private ClientboundCommandsPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.entries = $$0.readList(ClientboundCommandsPacket::readNode);
      this.rootIndex = $$0.readVarInt();
      validateEntries(this.entries);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeCollection(this.entries, ($$0x, $$1) -> $$1.write($$0x));
      $$0.writeVarInt(this.rootIndex);
   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> $$0, BiPredicate<ClientboundCommandsPacket.Entry, IntSet> $$1) {
      IntSet $$2 = new IntOpenHashSet(IntSets.fromTo(0, $$0.size()));

      while (!$$2.isEmpty()) {
         boolean $$3 = $$2.removeIf($$3x -> $$1.test($$0.get($$3x), $$2));
         if (!$$3) {
            throw new IllegalStateException("Server sent an impossible command tree");
         }
      }
   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> $$0) {
      validateEntries($$0, ClientboundCommandsPacket.Entry::canBuild);
      validateEntries($$0, ClientboundCommandsPacket.Entry::canResolve);
   }

   private static <S> Object2IntMap<CommandNode<S>> enumerateNodes(RootCommandNode<S> $$0) {
      Object2IntMap<CommandNode<S>> $$1 = new Object2IntOpenHashMap();
      Queue<CommandNode<S>> $$2 = new ArrayDeque<>();
      $$2.add($$0);

      CommandNode<S> $$3;
      while (($$3 = $$2.poll()) != null) {
         if (!$$1.containsKey($$3)) {
            int $$4 = $$1.size();
            $$1.put($$3, $$4);
            $$2.addAll($$3.getChildren());
            if ($$3.getRedirect() != null) {
               $$2.add($$3.getRedirect());
            }
         }
      }

      return $$1;
   }

   private static <S> List<ClientboundCommandsPacket.Entry> createEntries(Object2IntMap<CommandNode<S>> $$0, ClientboundCommandsPacket.NodeInspector<S> $$1) {
      ObjectArrayList<ClientboundCommandsPacket.Entry> $$2 = new ObjectArrayList($$0.size());
      $$2.size($$0.size());
      ObjectIterator var3 = Object2IntMaps.fastIterable($$0).iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<CommandNode<S>> $$3 = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<CommandNode<S>>)var3.next();
         $$2.set($$3.getIntValue(), createEntry((CommandNode<S>)$$3.getKey(), $$1, $$0));
      }

      return $$2;
   }

   private static ClientboundCommandsPacket.Entry readNode(net.minecraft.network.FriendlyByteBuf $$0) {
      byte $$1 = $$0.readByte();
      int[] $$2 = $$0.readVarIntArray();
      int $$3 = ($$1 & 8) != 0 ? $$0.readVarInt() : 0;
      ClientboundCommandsPacket.NodeStub $$4 = read($$0, $$1);
      return new ClientboundCommandsPacket.Entry($$4, $$1, $$3, $$2);
   }

   @Nullable
   private static ClientboundCommandsPacket.NodeStub read(net.minecraft.network.FriendlyByteBuf $$0, byte $$1) {
      int $$2 = $$1 & 3;
      if ($$2 == 2) {
         String $$3 = $$0.readUtf();
         int $$4 = $$0.readVarInt();
         ArgumentTypeInfo<?, ?> $$5 = (ArgumentTypeInfo<?, ?>)BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId($$4);
         if ($$5 == null) {
            return null;
         } else {
            Template<?> $$6 = $$5.deserializeFromNetwork($$0);
            Identifier $$7 = ($$1 & 16) != 0 ? $$0.readIdentifier() : null;
            return new ClientboundCommandsPacket.ArgumentNodeStub($$3, $$6, $$7);
         }
      } else if ($$2 == 1) {
         String $$8 = $$0.readUtf();
         return new ClientboundCommandsPacket.LiteralNodeStub($$8);
      } else {
         return null;
      }
   }

   private static <S> ClientboundCommandsPacket.Entry createEntry(
      CommandNode<S> $$0, ClientboundCommandsPacket.NodeInspector<S> $$1, Object2IntMap<CommandNode<S>> $$2
   ) {
      int $$3 = 0;
      int $$4;
      if ($$0.getRedirect() != null) {
         $$3 |= 8;
         $$4 = $$2.getInt($$0.getRedirect());
      } else {
         $$4 = 0;
      }

      if ($$1.isExecutable($$0)) {
         $$3 |= 4;
      }

      if ($$1.isRestricted($$0)) {
         $$3 |= 32;
      }

      ClientboundCommandsPacket.NodeStub $$7;
      switch ($$0) {
         case RootCommandNode<S> $$6:
            $$3 |= 0;
            $$7 = null;
            break;
         case ArgumentCommandNode<S, ?> $$8:
            Identifier $$9 = $$1.suggestionId($$8);
            $$7 = new ClientboundCommandsPacket.ArgumentNodeStub($$8.getName(), ArgumentTypeInfos.unpack($$8.getType()), $$9);
            $$3 |= 2;
            if ($$9 != null) {
               $$3 |= 16;
            }
            break;
         case LiteralCommandNode<S> $$11:
            $$7 = new ClientboundCommandsPacket.LiteralNodeStub($$11.getLiteral());
            $$3 |= 1;
            break;
         default:
            throw new UnsupportedOperationException("Unknown node type " + $$0);
      }

      int[] $$14 = $$0.getChildren().stream().mapToInt($$2::getInt).toArray();
      return new ClientboundCommandsPacket.Entry($$7, $$3, $$4, $$14);
   }

   @Override
   public PacketType<ClientboundCommandsPacket> type() {
      return GamePacketTypes.CLIENTBOUND_COMMANDS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleCommands(this);
   }

   public <S> RootCommandNode<S> getRoot(CommandBuildContext $$0, ClientboundCommandsPacket.NodeBuilder<S> $$1) {
      return (RootCommandNode<S>)new ClientboundCommandsPacket.NodeResolver<>($$0, $$1, this.entries).resolve(this.rootIndex);
   }

   record ArgumentNodeStub(String id, Template<?> argumentType, @Nullable Identifier suggestionId) implements ClientboundCommandsPacket.NodeStub {
      @Override
      public <S> ArgumentBuilder<S, ?> build(CommandBuildContext $$0, ClientboundCommandsPacket.NodeBuilder<S> $$1) {
         ArgumentType<?> $$2 = this.argumentType.instantiate($$0);
         return $$1.createArgument(this.id, $$2, this.suggestionId);
      }

      @Override
      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeUtf(this.id);
         serializeCap($$0, this.argumentType);
         if (this.suggestionId != null) {
            $$0.writeIdentifier(this.suggestionId);
         }
      }

      private static <A extends ArgumentType<?>> void serializeCap(net.minecraft.network.FriendlyByteBuf $$0, Template<A> $$1) {
         serializeCap($$0, $$1.type(), $$1);
      }

      private static <A extends ArgumentType<?>, T extends Template<A>> void serializeCap(
         net.minecraft.network.FriendlyByteBuf $$0, ArgumentTypeInfo<A, T> $$1, Template<A> $$2
      ) {
         $$0.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId($$1));
         $$1.serializeToNetwork($$2, $$0);
      }
   }

   record Entry(@Nullable ClientboundCommandsPacket.NodeStub stub, int flags, int redirect, int[] children) {

      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
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
         return (this.flags & 8) != 0 ? !$$0.contains(this.redirect) : true;
      }

      public boolean canResolve(IntSet $$0) {
         for (int $$1 : this.children) {
            if ($$0.contains($$1)) {
               return false;
            }
         }

         return true;
      }
   }

   record LiteralNodeStub(String id) implements ClientboundCommandsPacket.NodeStub {
      @Override
      public <S> ArgumentBuilder<S, ?> build(CommandBuildContext $$0, ClientboundCommandsPacket.NodeBuilder<S> $$1) {
         return $$1.createLiteral(this.id);
      }

      @Override
      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeUtf(this.id);
      }
   }

   public interface NodeBuilder<S> {
      ArgumentBuilder<S, ?> createLiteral(String var1);

      ArgumentBuilder<S, ?> createArgument(String var1, ArgumentType<?> var2, @Nullable Identifier var3);

      ArgumentBuilder<S, ?> configure(ArgumentBuilder<S, ?> var1, boolean var2, boolean var3);
   }

   public interface NodeInspector<S> {
      @Nullable
      Identifier suggestionId(ArgumentCommandNode<S, ?> var1);

      boolean isExecutable(CommandNode<S> var1);

      boolean isRestricted(CommandNode<S> var1);
   }

   static class NodeResolver<S> {
      private final CommandBuildContext context;
      private final ClientboundCommandsPacket.NodeBuilder<S> builder;
      private final List<ClientboundCommandsPacket.Entry> entries;
      private final List<CommandNode<S>> nodes;

      NodeResolver(CommandBuildContext $$0, ClientboundCommandsPacket.NodeBuilder<S> $$1, List<ClientboundCommandsPacket.Entry> $$2) {
         this.context = $$0;
         this.builder = $$1;
         this.entries = $$2;
         ObjectArrayList<CommandNode<S>> $$3 = new ObjectArrayList();
         $$3.size($$2.size());
         this.nodes = $$3;
      }

      public CommandNode<S> resolve(int $$0) {
         CommandNode<S> $$1 = this.nodes.get($$0);
         if ($$1 != null) {
            return $$1;
         } else {
            ClientboundCommandsPacket.Entry $$2 = this.entries.get($$0);
            CommandNode<S> $$3;
            if ($$2.stub == null) {
               $$3 = new RootCommandNode();
            } else {
               ArgumentBuilder<S, ?> $$4 = $$2.stub.build(this.context, this.builder);
               if (($$2.flags & 8) != 0) {
                  $$4.redirect(this.resolve($$2.redirect));
               }

               boolean $$5 = ($$2.flags & 4) != 0;
               boolean $$6 = ($$2.flags & 32) != 0;
               $$3 = this.builder.configure($$4, $$5, $$6).build();
            }

            this.nodes.set($$0, $$3);

            for (int $$8 : $$2.children) {
               CommandNode<S> $$9 = this.resolve($$8);
               if (!($$9 instanceof RootCommandNode)) {
                  $$3.addChild($$9);
               }
            }

            return $$3;
         }
      }
   }

   interface NodeStub {
      <S> ArgumentBuilder<S, ?> build(CommandBuildContext var1, ClientboundCommandsPacket.NodeBuilder<S> var2);

      void write(net.minecraft.network.FriendlyByteBuf var1);
   }
}
