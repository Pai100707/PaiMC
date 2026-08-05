package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SkullBlockEntity extends BlockEntity {
   private static final String TAG_PROFILE = "profile";
   private static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
   private static final String TAG_CUSTOM_NAME = "custom_name";
   
   private ResolvableProfile owner;
   
   private Identifier noteBlockSound;
   private int animationTickCount;
   private boolean isAnimating;
   
   private Component customName;

   public SkullBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.SKULL, $$0, $$1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.storeNullable("profile", ResolvableProfile.CODEC, this.owner);
      $$0.storeNullable("note_block_sound", Identifier.CODEC, this.noteBlockSound);
      $$0.storeNullable("custom_name", ComponentSerialization.CODEC, this.customName);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.owner = $$0.<ResolvableProfile>read("profile", ResolvableProfile.CODEC).orElse(null);
      this.noteBlockSound = $$0.<Identifier>read("note_block_sound", Identifier.CODEC).orElse(null);
      this.customName = parseCustomNameSafe($$0, "custom_name");
   }

   public static void animation(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, SkullBlockEntity $$3) {
      if ($$2.hasProperty(SkullBlock.POWERED) && $$2.getValue(SkullBlock.POWERED)) {
         $$3.isAnimating = true;
         $$3.animationTickCount++;
      } else {
         $$3.isAnimating = false;
      }
   }

   public float getAnimation(float $$0) {
      return this.isAnimating ? this.animationTickCount + $$0 : this.animationTickCount;
   }

   
   public ResolvableProfile getOwnerProfile() {
      return this.owner;
   }

   
   public Identifier getNoteBlockSound() {
      return this.noteBlockSound;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.owner = (ResolvableProfile)$$0.get(DataComponents.PROFILE);
      this.noteBlockSound = (Identifier)$$0.get(DataComponents.NOTE_BLOCK_SOUND);
      this.customName = (Component)$$0.get(DataComponents.CUSTOM_NAME);
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.PROFILE, this.owner);
      $$0.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
      $$0.set(DataComponents.CUSTOM_NAME, this.customName);
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      super.removeComponentsFromTag($$0);
      $$0.discard("profile");
      $$0.discard("note_block_sound");
      $$0.discard("custom_name");
   }
}
