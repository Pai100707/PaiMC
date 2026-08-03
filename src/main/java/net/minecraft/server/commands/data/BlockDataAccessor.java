package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;

public class BlockDataAccessor implements DataAccessor {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(Component.translatable("commands.data.block.invalid"));
   public static final Function<String, DataCommands.DataProvider> PROVIDER = $$0 -> new DataCommands.DataProvider() {
      @Override
      public DataAccessor access(CommandContext<CommandSourceStack> $$0x) throws CommandSyntaxException {
         BlockPos $$1 = BlockPosArgument.getLoadedBlockPos($$0, $$0 + "Pos");
         BlockEntity $$2 = ((CommandSourceStack)$$0.getSource()).getLevel().getBlockEntity($$1);
         if ($$2 == null) {
            throw BlockDataAccessor.ERROR_NOT_A_BLOCK_ENTITY.create();
         } else {
            return new BlockDataAccessor($$2, $$1);
         }
      }

      @Override
      public ArgumentBuilder<CommandSourceStack, ?> wrap(
         ArgumentBuilder<CommandSourceStack, ?> $$0x, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> $$1
      ) {
         return $$0.then(Commands.literal("block").then($$1.apply(Commands.argument($$0 + "Pos", BlockPosArgument.blockPos()))));
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
      ScopedCollector $$2 = new ScopedCollector(this.entity.problemPath(), LOGGER);

      try {
         this.entity.loadWithComponents(TagValueInput.create($$2, this.entity.getLevel().registryAccess(), $$0));
         this.entity.setChanged();
         this.entity.getLevel().sendBlockUpdated(this.pos, $$1, $$1, 3);
      } catch (Throwable var7) {
         try {
            $$2.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      $$2.close();
   }

   @Override
   public CompoundTag getData() {
      return this.entity.saveWithFullMetadata(this.entity.getLevel().registryAccess());
   }

   @Override
   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.block.modified", new Object[]{this.pos.getX(), this.pos.getY(), this.pos.getZ()});
   }

   @Override
   public Component getPrintSuccess(Tag $$0) {
      return Component.translatable(
         "commands.data.block.query", new Object[]{this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtUtils.toPrettyComponent($$0)}
      );
   }

   @Override
   public Component getPrintSuccess(NbtPath $$0, double $$1, int $$2) {
      return Component.translatable(
         "commands.data.block.get",
         new Object[]{$$0.asString(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", $$1), $$2}
      );
   }
}
