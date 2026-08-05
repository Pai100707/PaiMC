/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands.data;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;

public class BlockDataAccessor
implements DataAccessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType((Message)Component.translatable("commands.data.block.invalid"));
    public static final Function<String, DataCommands.DataProvider> PROVIDER = $$0 -> new DataCommands.DataProvider((String)$$0){
        final /* synthetic */ String val$argPrefix;
        {
            this.val$argPrefix = string;
        }

        @Override
        public DataAccessor access(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
            BlockPos $$1 = BlockPosArgument.getLoadedBlockPos($$0, this.val$argPrefix + "Pos");
            BlockEntity $$2 = ((CommandSourceStack)$$0.getSource()).getLevel().getBlockEntity($$1);
            if ($$2 == null) {
                throw ERROR_NOT_A_BLOCK_ENTITY.create();
            }
            return new BlockDataAccessor($$2, $$1);
        }

        @Override
        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> $$0, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> $$1) {
            return $$0.then(Commands.literal("block").then($$1.apply((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument(this.val$argPrefix + "Pos", BlockPosArgument.blockPos()))));
        }
    };
    private final BlockEntity entity;
    private final BlockPos pos;

    public BlockDataAccessor(BlockEntity $$0, BlockPos $$1) {
        this.entity = $$0;
        this.pos = $$1;
    }

    @Override
    public void setData(CompoundTag $$0) {
        BlockState $$1 = this.entity.getLevel().getBlockState(this.pos);
        try (ProblemReporter.ScopedCollector $$2 = new ProblemReporter.ScopedCollector(this.entity.problemPath(), LOGGER);){
            this.entity.loadWithComponents(TagValueInput.create((ProblemReporter)$$2, (HolderLookup.Provider)this.entity.getLevel().registryAccess(), $$0));
            this.entity.setChanged();
            this.entity.getLevel().sendBlockUpdated(this.pos, $$1, $$1, 3);
        }
    }

    @Override
    public CompoundTag getData() {
        return this.entity.saveWithFullMetadata(this.entity.getLevel().registryAccess());
    }

    @Override
    public Component getModifiedSuccess() {
        return Component.translatable("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public Component getPrintSuccess(Tag $$0) {
        return Component.translatable("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtUtils.toPrettyComponent($$0));
    }

    @Override
    public Component getPrintSuccess(NbtPathArgument.NbtPath $$0, double $$1, int $$2) {
        return Component.translatable("commands.data.block.get", $$0.asString(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", $$1), $$2);
    }
}

