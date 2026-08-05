/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public enum DyeColor implements StringRepresentable
{
    WHITE(0, "white", 0xF9FFFE, MapColor.SNOW, 0xF0F0F0, 0xFFFFFF),
    ORANGE(1, "orange", 16351261, MapColor.COLOR_ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MapColor.COLOR_MAGENTA, 12801229, 0xFF00FF),
    LIGHT_BLUE(3, "light_blue", 3847130, MapColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MapColor.COLOR_YELLOW, 14602026, 0xFFFF00),
    LIME(5, "lime", 8439583, MapColor.COLOR_LIGHT_GREEN, 4312372, 0xBFFF00),
    PINK(6, "pink", 15961002, MapColor.COLOR_PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MapColor.COLOR_GRAY, 0x434343, 0x808080),
    LIGHT_GRAY(8, "light_gray", 0x9D9D97, MapColor.COLOR_LIGHT_GRAY, 0xABABAB, 0xD3D3D3),
    CYAN(9, "cyan", 1481884, MapColor.COLOR_CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MapColor.COLOR_PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MapColor.COLOR_BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MapColor.COLOR_BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MapColor.COLOR_GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MapColor.COLOR_RED, 11743532, 0xFF0000),
    BLACK(15, "black", 0x1D1D21, MapColor.COLOR_BLACK, 0x1E1B1B, 0);

    private static final IntFunction<DyeColor> BY_ID;
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR;
    public static final StringRepresentable.EnumCodec<DyeColor> CODEC;
    public static final StreamCodec<ByteBuf, DyeColor> STREAM_CODEC;
    @Deprecated
    public static final Codec<DyeColor> LEGACY_ID_CODEC;
    private final int id;
    private final String name;
    private final MapColor mapColor;
    private final int textureDiffuseColor;
    private final int fireworkColor;
    private final int textColor;

    private DyeColor(int $$0, String $$1, int $$2, MapColor $$3, int $$4, int $$5) {
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

    public static DyeColor byId(int $$0) {
        return BY_ID.apply($$0);
    }

    @Contract(value="_,!null->!null;_,null->_")
    public static @Nullable DyeColor byName(String $$0, @Nullable DyeColor $$1) {
        DyeColor $$2 = CODEC.byName($$0);
        return $$2 != null ? $$2 : $$1;
    }

    public static @Nullable DyeColor byFireworkColor(int $$0) {
        return (DyeColor)BY_FIREWORK_COLOR.get($$0);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static DyeColor getMixedColor(ServerLevel $$0, DyeColor $$1, DyeColor $$22) {
        CraftingInput $$3 = DyeColor.makeCraftColorInput($$1, $$22);
        return $$0.recipeAccess().getRecipeFor(RecipeType.CRAFTING, $$3, $$0).map($$2 -> ((CraftingRecipe)$$2.value()).assemble($$3, $$0.registryAccess())).map(ItemStack::getItem).filter(DyeItem.class::isInstance).map(DyeItem.class::cast).map(DyeItem::getDyeColor).orElseGet(() -> $$0.random.nextBoolean() ? $$1 : $$22);
    }

    private static CraftingInput makeCraftColorInput(DyeColor $$0, DyeColor $$1) {
        return CraftingInput.of(2, 1, List.of(new ItemStack(DyeItem.byColor($$0)), new ItemStack(DyeItem.byColor($$1))));
    }

    static {
        BY_ID = ByIdMap.continuous(DyeColor::getId, DyeColor.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap($$0 -> $$0.fireworkColor, $$0 -> $$0)));
        CODEC = StringRepresentable.fromEnum(DyeColor::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DyeColor::getId);
        LEGACY_ID_CODEC = Codec.BYTE.xmap(DyeColor::byId, $$0 -> (byte)$$0.id);
    }
}

