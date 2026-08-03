package net.minecraft.network.protocol.game;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputSet;

public record ClientboundUpdateRecipesPacket(
   Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets, SingleInputSet<StonecutterRecipe> stonecutterRecipes
) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundUpdateRecipesPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(RecipePropertySet.TYPE_KEY), RecipePropertySet.STREAM_CODEC),
      ClientboundUpdateRecipesPacket::itemSets,
      SingleInputSet.noRecipeCodec(),
      ClientboundUpdateRecipesPacket::stonecutterRecipes,
      ClientboundUpdateRecipesPacket::new
   );

   @Override
   public PacketType<ClientboundUpdateRecipesPacket> type() {
      return GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleUpdateRecipes(this);
   }
}
