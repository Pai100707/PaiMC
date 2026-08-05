package net.minecraft.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
   public static final int DATA_PAGE = 0;
   public static final int NUM_DATA = 1;
   public static final int SLOT_BOOK = 0;
   public static final int NUM_SLOTS = 1;
   private final Container bookAccess = new Container() {
      public int getContainerSize() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternBlockEntity.this.book.isEmpty();
      }

      public ItemStack getItem(int $$0) {
         return $$0 == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
      }

      public ItemStack removeItem(int $$0, int $$1) {
         if ($$0 == 0) {
            ItemStack $$2 = LecternBlockEntity.this.book.split($$1);
            if (LecternBlockEntity.this.book.isEmpty()) {
               LecternBlockEntity.this.onBookItemRemove();
            }

            return $$2;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public ItemStack removeItemNoUpdate(int $$0) {
         if ($$0 == 0) {
            ItemStack $$1 = LecternBlockEntity.this.book;
            LecternBlockEntity.this.book = ItemStack.EMPTY;
            LecternBlockEntity.this.onBookItemRemove();
            return $$1;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public void setItem(int $$0, ItemStack $$1) {
      }

      public int getMaxStackSize() {
         return 1;
      }

      public void setChanged() {
         LecternBlockEntity.this.setChanged();
      }

      public boolean stillValid(Player $$0) {
         return Container.stillValidBlockEntity(LecternBlockEntity.this, $$0) && LecternBlockEntity.this.hasBook();
      }

      public boolean canPlaceItem(int $$0, ItemStack $$1) {
         return false;
      }

      public void clearContent() {
      }
   };
   private final ContainerData dataAccess = new ContainerData() {
      public int get(int $$0) {
         return $$0 == 0 ? LecternBlockEntity.this.page : 0;
      }

      public void set(int $$0, int $$1) {
         if ($$0 == 0) {
            LecternBlockEntity.this.setPage($$1);
         }
      }

      public int getCount() {
         return 1;
      }
   };
   ItemStack book = ItemStack.EMPTY;
   int page;
   private int pageCount;

   public LecternBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.LECTERN, $$0, $$1);
   }

   public ItemStack getBook() {
      return this.book;
   }

   public boolean hasBook() {
      return this.book.has(DataComponents.WRITABLE_BOOK_CONTENT) || this.book.has(DataComponents.WRITTEN_BOOK_CONTENT);
   }

   public void setBook(ItemStack $$0) {
      this.setBook($$0, null);
   }

   void onBookItemRemove() {
      this.page = 0;
      this.pageCount = 0;
      LecternBlock.resetBookState(null, this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
   }

   public void setBook(ItemStack $$0, Player $$1) {
      this.book = this.resolveBook($$0, $$1);
      this.page = 0;
      this.pageCount = getPageCount(this.book);
      this.setChanged();
   }

   void setPage(int $$0) {
      int $$1 = Mth.clamp($$0, 0, this.pageCount - 1);
      if ($$1 != this.page) {
         this.page = $$1;
         this.setChanged();
         LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   public int getPage() {
      return this.page;
   }

   public int getRedstoneSignal() {
      float $$0 = this.pageCount > 1 ? this.getPage() / (this.pageCount - 1.0F) : 1.0F;
      return Mth.floor($$0 * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   private ItemStack resolveBook(ItemStack $$0, Player $$1) {
      if (this.level instanceof ServerLevel $$2) {
         WrittenBookContent.resolveForItem($$0, this.createCommandSourceStack($$1, $$2), $$1);
      }

      return $$0;
   }

   private CommandSourceStack createCommandSourceStack(Player $$0, ServerLevel $$1) {
      String $$2;
      Component $$3;
      if ($$0 == null) {
         $$2 = "Lectern";
         $$3 = Component.literal("Lectern");
      } else {
         $$2 = $$0.getPlainTextName();
         $$3 = $$0.getDisplayName();
      }

      Vec3 $$6 = Vec3.atCenterOf(this.worldPosition);
      return new CommandSourceStack(CommandSource.NULL, $$6, Vec2.ZERO, $$1, LevelBasedPermissionSet.GAMEMASTER, $$2, $$3, $$1.getServer(), $$0);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.book = $$0.<ItemStack>read("Book", ItemStack.CODEC).map($$0x -> this.resolveBook($$0x, null)).orElse(ItemStack.EMPTY);
      this.pageCount = getPageCount(this.book);
      this.page = Mth.clamp($$0.getIntOr("Page", 0), 0, this.pageCount - 1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.getBook().isEmpty()) {
         $$0.store("Book", ItemStack.CODEC, this.getBook());
         $$0.putInt("Page", this.page);
      }
   }

   public void clearContent() {
      this.setBook(ItemStack.EMPTY);
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      if ($$1.getValue(LecternBlock.HAS_BOOK) && this.level != null) {
         Direction $$2 = $$1.getValue(LecternBlock.FACING);
         ItemStack $$3 = this.getBook().copy();
         float $$4 = 0.25F * $$2.getStepX();
         float $$5 = 0.25F * $$2.getStepZ();
         ItemEntity $$6 = new ItemEntity(this.level, $$0.getX() + 0.5 + $$4, $$0.getY() + 1, $$0.getZ() + 0.5 + $$5, $$3);
         $$6.setDefaultPickUpDelay();
         this.level.addFreshEntity($$6);
      }
   }

   public AbstractContainerMenu createMenu(int $$0, Inventory $$1, Player $$2) {
      return new LecternMenu($$0, this.bookAccess, this.dataAccess);
   }

   public Component getDisplayName() {
      return Component.translatable("container.lectern");
   }

   private static int getPageCount(ItemStack $$0) {
      WrittenBookContent $$1 = (WrittenBookContent)$$0.get(DataComponents.WRITTEN_BOOK_CONTENT);
      if ($$1 != null) {
         return $$1.pages().size();
      } else {
         WritableBookContent $$2 = (WritableBookContent)$$0.get(DataComponents.WRITABLE_BOOK_CONTENT);
         return $$2 != null ? $$2.pages().size() : 0;
      }
   }
}
