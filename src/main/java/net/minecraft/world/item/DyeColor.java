package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public enum DyeColor implements StringRepresentable {
   WHITE(0, "white", 16383998, MapColor.SNOW, 15790320, 16777215),
   ORANGE(1, "orange", 16351261, MapColor.COLOR_ORANGE, 15435844, 16738335),
   MAGENTA(2, "magenta", 13061821, MapColor.COLOR_MAGENTA, 12801229, 16711935),
   LIGHT_BLUE(3, "light_blue", 3847130, MapColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
   YELLOW(4, "yellow", 16701501, MapColor.COLOR_YELLOW, 14602026, 16776960),
   LIME(5, "lime", 8439583, MapColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
   PINK(6, "pink", 15961002, MapColor.COLOR_PINK, 14188952, 16738740),
   GRAY(7, "gray", 4673362, MapColor.COLOR_GRAY, 4408131, 8421504),
   LIGHT_GRAY(8, "light_gray", 10329495, MapColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
   CYAN(9, "cyan", 1481884, MapColor.COLOR_CYAN, 2651799, 65535),
   PURPLE(10, "purple", 8991416, MapColor.COLOR_PURPLE, 8073150, 10494192),
   BLUE(11, "blue", 3949738, MapColor.COLOR_BLUE, 2437522, 255),
   BROWN(12, "brown", 8606770, MapColor.COLOR_BROWN, 5320730, 9127187),
   GREEN(13, "green", 6192150, MapColor.COLOR_GREEN, 3887386, 65280),
   RED(14, "red", 11546150, MapColor.COLOR_RED, 11743532, 16711680),
   BLACK(15, "black", 1908001, MapColor.COLOR_BLACK, 1973019, 0);

   private static final IntFunction<net.minecraft.world.item.DyeColor> BY_ID = ByIdMap.continuous(
      net.minecraft.world.item.DyeColor::getId, values(), OutOfBoundsStrategy.ZERO
   );
   private static final Int2ObjectOpenHashMap<net.minecraft.world.item.DyeColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap(
      Arrays.stream(values()).collect(Collectors.toMap($$0 -> $$0.fireworkColor, $$0 -> (net.minecraft.world.item.DyeColor)$$0))
   );
   public static final EnumCodec<net.minecraft.world.item.DyeColor> CODEC = StringRepresentable.fromEnum(net.minecraft.world.item.DyeColor::values);
   public static final StreamCodec<ByteBuf, net.minecraft.world.item.DyeColor> STREAM_CODEC = ByteBufCodecs.idMapper(
      BY_ID, net.minecraft.world.item.DyeColor::getId
   );
   @Deprecated
   public static final Codec<net.minecraft.world.item.DyeColor> LEGACY_ID_CODEC = Codec.BYTE.xmap(net.minecraft.world.item.DyeColor::byId, $$0 -> (byte)$$0.id);
   private final int id;
   private final String name;
   private final MapColor mapColor;
   private final int textureDiffuseColor;
   private final int fireworkColor;
   private final int textColor;

   private DyeColor(final int $$0, final String $$1, final int $$2, final MapColor $$3, final int $$4, final int $$5) {
      this.id = $$0;
      this.name = $$1;
      this.mapColor = $$3;
      this.textColor = ARGB.opaque($$5);
      this.textureDiffuseColor = ARGB.opaque($$2);
      this.fireworkColor = $$4;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public int getTextureDiffuseColor() {
      return this.textureDiffuseColor;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public int getFireworkColor() {
      return this.fireworkColor;
   }

   public int getTextColor() {
      return this.textColor;
   }

   public static net.minecraft.world.item.DyeColor byId(int $$0) {
      return BY_ID.apply($$0);
   }

   @Contract("_,!null->!null;_,null->_")
   @Nullable
   public static net.minecraft.world.item.DyeColor byName(String $$0, @Nullable net.minecraft.world.item.DyeColor $$1) {
      net.minecraft.world.item.DyeColor $$2 = (net.minecraft.world.item.DyeColor)CODEC.byName($$0);
      return $$2 != null ? $$2 : $$1;
   }

   @Nullable
   public static net.minecraft.world.item.DyeColor byFireworkColor(int $$0) {
      return (net.minecraft.world.item.DyeColor)BY_FIREWORK_COLOR.get($$0);
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public static net.minecraft.world.item.DyeColor getMixedColor(ServerLevel $$0, net.minecraft.world.item.DyeColor $$1, net.minecraft.world.item.DyeColor $$2) {
      CraftingInput $$3 = makeCraftColorInput($$1, $$2);
      return $$0.recipeAccess()
         .getRecipeFor(RecipeType.CRAFTING, $$3, $$0)
         .map($$2x -> ((CraftingRecipe)$$2x.value()).assemble($$3, $$0.registryAccess()))
         .map(net.minecraft.world.item.ItemStack::getItem)
         .filter(net.minecraft.world.item.DyeItem.class::isInstance)
         .map(net.minecraft.world.item.DyeItem.class::cast)
         .map(net.minecraft.world.item.DyeItem::getDyeColor)
         .orElseGet(() -> $$0.random.nextBoolean() ? $$1 : $$2);
   }

   private static CraftingInput makeCraftColorInput(net.minecraft.world.item.DyeColor $$0, net.minecraft.world.item.DyeColor $$1) {
      return CraftingInput.of(
         2,
         1,
         List.of(
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.DyeItem.byColor($$0)),
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.DyeItem.byColor($$1))
         )
      );
   }
}
