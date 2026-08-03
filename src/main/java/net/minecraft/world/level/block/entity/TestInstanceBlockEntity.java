package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.gametest.framework.GameTestRunner.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TestInstanceBlockEntity extends BlockEntity implements BeaconBeamOwner, BoundingBoxRenderable {
   private static final Component INVALID_TEST_NAME = Component.translatable("test_instance_block.invalid_test");
   private static final List<BeaconBeamOwner.Section> BEAM_CLEARED = List.of();
   private static final List<BeaconBeamOwner.Section> BEAM_RUNNING = List.of(new BeaconBeamOwner.Section(ARGB.color(128, 128, 128)));
   private static final List<BeaconBeamOwner.Section> BEAM_SUCCESS = List.of(new BeaconBeamOwner.Section(ARGB.color(0, 255, 0)));
   private static final List<BeaconBeamOwner.Section> BEAM_REQUIRED_FAILED = List.of(new BeaconBeamOwner.Section(ARGB.color(255, 0, 0)));
   private static final List<BeaconBeamOwner.Section> BEAM_OPTIONAL_FAILED = List.of(new BeaconBeamOwner.Section(ARGB.color(255, 128, 0)));
   private static final Vec3i STRUCTURE_OFFSET = new Vec3i(0, 1, 1);
   private TestInstanceBlockEntity.Data data;
   private final List<TestInstanceBlockEntity.ErrorMarker> errorMarkers = new ArrayList<>();

   public TestInstanceBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.TEST_INSTANCE_BLOCK, $$0, $$1);
      this.data = new TestInstanceBlockEntity.Data(Optional.empty(), Vec3i.ZERO, Rotation.NONE, false, TestInstanceBlockEntity.Status.CLEARED, Optional.empty());
   }

   public void set(TestInstanceBlockEntity.Data $$0) {
      this.data = $$0;
      this.setChanged();
   }

   public static Optional<Vec3i> getStructureSize(ServerLevel $$0, ResourceKey<GameTestInstance> $$1) {
      return getStructureTemplate($$0, $$1).map(StructureTemplate::getSize);
   }

   public BoundingBox getStructureBoundingBox() {
      BlockPos $$0 = this.getStructurePos();
      BlockPos $$1 = $$0.offset(this.getTransformedSize()).offset(-1, -1, -1);
      return BoundingBox.fromCorners($$0, $$1);
   }

   public AABB getStructureBounds() {
      return AABB.of(this.getStructureBoundingBox());
   }

   private static Optional<StructureTemplate> getStructureTemplate(ServerLevel $$0, ResourceKey<GameTestInstance> $$1) {
      return $$0.registryAccess().get($$1).map($$0x -> ((GameTestInstance)$$0x.value()).structure()).flatMap($$1x -> $$0.getStructureManager().get($$1x));
   }

   public Optional<ResourceKey<GameTestInstance>> test() {
      return this.data.test();
   }

   public Component getTestName() {
      return this.test().map($$0 -> Component.literal($$0.identifier().toString())).orElse(INVALID_TEST_NAME);
   }

   private Optional<Reference<GameTestInstance>> getTestHolder() {
      return this.test().flatMap(this.level.registryAccess()::get);
   }

   public boolean ignoreEntities() {
      return this.data.ignoreEntities();
   }

   public Vec3i getSize() {
      return this.data.size();
   }

   public Rotation getRotation() {
      return this.getTestHolder().map(Holder::value).<Rotation>map(GameTestInstance::rotation).orElse(Rotation.NONE).getRotated(this.data.rotation());
   }

   public Optional<Component> errorMessage() {
      return this.data.errorMessage();
   }

   public void setErrorMessage(Component $$0) {
      this.set(this.data.withError($$0));
   }

   public void setSuccess() {
      this.set(this.data.withStatus(TestInstanceBlockEntity.Status.FINISHED));
   }

   public void setRunning() {
      this.set(this.data.withStatus(TestInstanceBlockEntity.Status.RUNNING));
   }

   @Override
   public void setChanged() {
      super.setChanged();
      if (this.level instanceof ServerLevel) {
         this.level.sendBlockUpdated(this.getBlockPos(), Blocks.AIR.defaultBlockState(), this.getBlockState(), 3);
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      $$0.<TestInstanceBlockEntity.Data>read("data", TestInstanceBlockEntity.Data.CODEC).ifPresent(this::set);
      this.errorMarkers.clear();
      this.errorMarkers
         .addAll($$0.<List<? extends TestInstanceBlockEntity.ErrorMarker>>read("errors", TestInstanceBlockEntity.ErrorMarker.LIST_CODEC).orElse(List.of()));
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      $$0.store("data", TestInstanceBlockEntity.Data.CODEC, this.data);
      if (!this.errorMarkers.isEmpty()) {
         $$0.store("errors", TestInstanceBlockEntity.ErrorMarker.LIST_CODEC, this.errorMarkers);
      }
   }

   @Override
   public BoundingBoxRenderable.Mode renderMode() {
      return BoundingBoxRenderable.Mode.BOX;
   }

   public BlockPos getStructurePos() {
      return getStructurePos(this.getBlockPos());
   }

   public static BlockPos getStructurePos(BlockPos $$0) {
      return $$0.offset(STRUCTURE_OFFSET);
   }

   @Override
   public BoundingBoxRenderable.RenderableBox getRenderableBox() {
      return new BoundingBoxRenderable.RenderableBox(new BlockPos(STRUCTURE_OFFSET), this.getTransformedSize());
   }

   @Override
   public List<BeaconBeamOwner.Section> getBeamSections() {
      return switch (this.data.status()) {
         case CLEARED -> BEAM_CLEARED;
         case RUNNING -> BEAM_RUNNING;
         case FINISHED -> this.errorMessage().isEmpty()
            ? BEAM_SUCCESS
            : (this.getTestHolder().map(Holder::value).<Boolean>map(GameTestInstance::required).orElse(true) ? BEAM_REQUIRED_FAILED : BEAM_OPTIONAL_FAILED);
      };
   }

   private Vec3i getTransformedSize() {
      Vec3i $$0 = this.getSize();
      Rotation $$1 = this.getRotation();
      boolean $$2 = $$1 == Rotation.CLOCKWISE_90 || $$1 == Rotation.COUNTERCLOCKWISE_90;
      int $$3 = $$2 ? $$0.getZ() : $$0.getX();
      int $$4 = $$2 ? $$0.getX() : $$0.getZ();
      return new Vec3i($$3, $$0.getY(), $$4);
   }

   public void resetTest(Consumer<Component> $$0) {
      this.removeBarriers();
      this.clearErrorMarkers();
      boolean $$1 = this.placeStructure();
      if ($$1) {
         $$0.accept(Component.translatable("test_instance_block.reset_success", new Object[]{this.getTestName()}).withStyle(ChatFormatting.GREEN));
      }

      this.set(this.data.withStatus(TestInstanceBlockEntity.Status.CLEARED));
   }

   public Optional<Identifier> saveTest(Consumer<Component> $$0) {
      Optional<Reference<GameTestInstance>> $$1 = this.getTestHolder();
      Optional<Identifier> $$2;
      if ($$1.isPresent()) {
         $$2 = Optional.of(((GameTestInstance)$$1.get().value()).structure());
      } else {
         $$2 = this.test().map(ResourceKey::identifier);
      }

      if ($$2.isEmpty()) {
         BlockPos $$4 = this.getBlockPos();
         $$0.accept(
            Component.translatable("test_instance_block.error.unable_to_save", new Object[]{$$4.getX(), $$4.getY(), $$4.getZ()}).withStyle(ChatFormatting.RED)
         );
         return $$2;
      } else {
         if (this.level instanceof ServerLevel $$5) {
            StructureBlockEntity.saveStructure($$5, $$2.get(), this.getStructurePos(), this.getSize(), this.ignoreEntities(), "", true, List.of(Blocks.AIR));
         }

         return $$2;
      }
   }

   public boolean exportTest(Consumer<Component> $$0) {
      Optional<Identifier> $$1 = this.saveTest($$0);
      return !$$1.isEmpty() && this.level instanceof ServerLevel $$2 ? export($$2, $$1.get(), $$0) : false;
   }

   public static boolean export(ServerLevel $$0, Identifier $$1, Consumer<Component> $$2) {
      Path $$3 = StructureUtils.testStructuresDir;
      Path $$4 = $$0.getStructureManager().createAndValidatePathToGeneratedStructure($$1, ".nbt");
      Path $$5 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, $$4, $$1.getPath(), $$3.resolve($$1.getNamespace()).resolve("structure"));
      if ($$5 == null) {
         $$2.accept(Component.literal("Failed to export " + $$4).withStyle(ChatFormatting.RED));
         return true;
      } else {
         try {
            FileUtil.createDirectoriesSafe($$5.getParent());
         } catch (IOException var7) {
            $$2.accept(Component.literal("Could not create folder " + $$5.getParent()).withStyle(ChatFormatting.RED));
            return true;
         }

         $$2.accept(Component.literal("Exported " + $$1 + " to " + $$5.toAbsolutePath()));
         return false;
      }
   }

   public void runTest(Consumer<Component> $$0) {
      if (this.level instanceof ServerLevel $$1) {
         Optional var7 = this.getTestHolder();
         BlockPos $$4 = this.getBlockPos();
         if (var7.isEmpty()) {
            $$0.accept(
               Component.translatable("test_instance_block.error.no_test", new Object[]{$$4.getX(), $$4.getY(), $$4.getZ()}).withStyle(ChatFormatting.RED)
            );
         } else if (!this.placeStructure()) {
            $$0.accept(
               Component.translatable("test_instance_block.error.no_test_structure", new Object[]{$$4.getX(), $$4.getY(), $$4.getZ()})
                  .withStyle(ChatFormatting.RED)
            );
         } else {
            this.clearErrorMarkers();
            GameTestTicker.SINGLETON.clear();
            FailedTestTracker.forgetFailedTests();
            $$0.accept(Component.translatable("test_instance_block.starting", new Object[]{((Reference)var7.get()).getRegisteredName()}));
            GameTestInfo $$5 = new GameTestInfo((Reference)var7.get(), this.data.rotation(), $$1, RetryOptions.noRetries());
            $$5.setTestBlockPos($$4);
            GameTestRunner $$6 = Builder.fromInfo(List.of($$5), $$1).build();
            TestCommand.trackAndStartRunner($$1.getServer().createCommandSourceStack(), $$6);
         }
      }
   }

   public boolean placeStructure() {
      if (this.level instanceof ServerLevel $$0) {
         Optional<StructureTemplate> $$1 = this.data.test().flatMap($$1x -> getStructureTemplate($$0, (ResourceKey<GameTestInstance>)$$1x));
         if ($$1.isPresent()) {
            this.placeStructure($$0, $$1.get());
            return true;
         }
      }

      return false;
   }

   private void placeStructure(ServerLevel $$0, StructureTemplate $$1) {
      StructurePlaceSettings $$2 = new StructurePlaceSettings()
         .setRotation(this.getRotation())
         .setIgnoreEntities(this.data.ignoreEntities())
         .setKnownShape(true);
      BlockPos $$3 = this.getStartCorner();
      this.forceLoadChunks();
      StructureUtils.clearSpaceForStructure(this.getStructureBoundingBox(), $$0);
      this.removeEntities();
      $$1.placeInWorld($$0, $$3, $$3, $$2, $$0.getRandom(), 818);
   }

   private void removeEntities() {
      this.level.getEntities(null, this.getStructureBounds()).stream().filter($$0 -> !($$0 instanceof Player)).forEach(Entity::discard);
   }

   private void forceLoadChunks() {
      if (this.level instanceof ServerLevel $$0) {
         this.getStructureBoundingBox().intersectingChunks().forEach($$1 -> $$0.setChunkForced($$1.x, $$1.z, true));
      }
   }

   public BlockPos getStartCorner() {
      Vec3i $$0 = this.getSize();
      Rotation $$1 = this.getRotation();
      BlockPos $$2 = this.getStructurePos();

      return switch ($$1) {
         case NONE -> $$2;
         case CLOCKWISE_90 -> $$2.offset($$0.getZ() - 1, 0, 0);
         case CLOCKWISE_180 -> $$2.offset($$0.getX() - 1, 0, $$0.getZ() - 1);
         case COUNTERCLOCKWISE_90 -> $$2.offset(0, 0, $$0.getX() - 1);
      };
   }

   public void encaseStructure() {
      this.processStructureBoundary($$0 -> {
         if (!this.level.getBlockState($$0).is(Blocks.TEST_INSTANCE_BLOCK)) {
            this.level.setBlockAndUpdate($$0, Blocks.BARRIER.defaultBlockState());
         }
      });
   }

   public void removeBarriers() {
      this.processStructureBoundary($$0 -> {
         if (this.level.getBlockState($$0).is(Blocks.BARRIER)) {
            this.level.setBlockAndUpdate($$0, Blocks.AIR.defaultBlockState());
         }
      });
   }

   public void processStructureBoundary(Consumer<BlockPos> $$0) {
      AABB $$1 = this.getStructureBounds();
      boolean $$2 = !this.getTestHolder().map($$0x -> ((GameTestInstance)$$0x.value()).skyAccess()).orElse(false);
      BlockPos $$3 = BlockPos.containing($$1.minX, $$1.minY, $$1.minZ).offset(-1, -1, -1);
      BlockPos $$4 = BlockPos.containing($$1.maxX, $$1.maxY, $$1.maxZ);
      BlockPos.betweenClosedStream($$3, $$4)
         .forEach(
            $$4x -> {
               boolean $$5 = $$4x.getX() == $$3.getX()
                  || $$4x.getX() == $$4.getX()
                  || $$4x.getZ() == $$3.getZ()
                  || $$4x.getZ() == $$4.getZ()
                  || $$4x.getY() == $$3.getY();
               boolean $$6 = $$4x.getY() == $$4.getY();
               if ($$5 || $$6 && $$2) {
                  $$0.accept($$4x);
               }
            }
         );
   }

   public void markError(BlockPos $$0, Component $$1) {
      this.errorMarkers.add(new TestInstanceBlockEntity.ErrorMarker($$0, $$1));
      this.setChanged();
   }

   public void clearErrorMarkers() {
      if (!this.errorMarkers.isEmpty()) {
         this.errorMarkers.clear();
         this.setChanged();
      }
   }

   public List<TestInstanceBlockEntity.ErrorMarker> getErrorMarkers() {
      return this.errorMarkers;
   }

   public record Data(
      Optional<ResourceKey<GameTestInstance>> test,
      Vec3i size,
      Rotation rotation,
      boolean ignoreEntities,
      TestInstanceBlockEntity.Status status,
      Optional<Component> errorMessage
   ) {
      public static final Codec<TestInstanceBlockEntity.Data> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ResourceKey.codec(Registries.TEST_INSTANCE).optionalFieldOf("test").forGetter(TestInstanceBlockEntity.Data::test),
               Vec3i.CODEC.fieldOf("size").forGetter(TestInstanceBlockEntity.Data::size),
               Rotation.CODEC.fieldOf("rotation").forGetter(TestInstanceBlockEntity.Data::rotation),
               Codec.BOOL.fieldOf("ignore_entities").forGetter(TestInstanceBlockEntity.Data::ignoreEntities),
               TestInstanceBlockEntity.Status.CODEC.fieldOf("status").forGetter(TestInstanceBlockEntity.Data::status),
               ComponentSerialization.CODEC.optionalFieldOf("error_message").forGetter(TestInstanceBlockEntity.Data::errorMessage)
            )
            .apply($$0, TestInstanceBlockEntity.Data::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, TestInstanceBlockEntity.Data> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.TEST_INSTANCE)),
         TestInstanceBlockEntity.Data::test,
         Vec3i.STREAM_CODEC,
         TestInstanceBlockEntity.Data::size,
         Rotation.STREAM_CODEC,
         TestInstanceBlockEntity.Data::rotation,
         ByteBufCodecs.BOOL,
         TestInstanceBlockEntity.Data::ignoreEntities,
         TestInstanceBlockEntity.Status.STREAM_CODEC,
         TestInstanceBlockEntity.Data::status,
         ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
         TestInstanceBlockEntity.Data::errorMessage,
         TestInstanceBlockEntity.Data::new
      );

      public TestInstanceBlockEntity.Data withSize(Vec3i $$0) {
         return new TestInstanceBlockEntity.Data(this.test, $$0, this.rotation, this.ignoreEntities, this.status, this.errorMessage);
      }

      public TestInstanceBlockEntity.Data withStatus(TestInstanceBlockEntity.Status $$0) {
         return new TestInstanceBlockEntity.Data(this.test, this.size, this.rotation, this.ignoreEntities, $$0, Optional.empty());
      }

      public TestInstanceBlockEntity.Data withError(Component $$0) {
         return new TestInstanceBlockEntity.Data(
            this.test, this.size, this.rotation, this.ignoreEntities, TestInstanceBlockEntity.Status.FINISHED, Optional.of($$0)
         );
      }
   }

   public record ErrorMarker(BlockPos pos, Component text) {
      public static final Codec<TestInstanceBlockEntity.ErrorMarker> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BlockPos.CODEC.fieldOf("pos").forGetter(TestInstanceBlockEntity.ErrorMarker::pos),
               ComponentSerialization.CODEC.fieldOf("text").forGetter(TestInstanceBlockEntity.ErrorMarker::text)
            )
            .apply($$0, TestInstanceBlockEntity.ErrorMarker::new)
      );
      public static final Codec<List<TestInstanceBlockEntity.ErrorMarker>> LIST_CODEC = CODEC.listOf();
   }

   public static enum Status implements StringRepresentable {
      CLEARED("cleared", 0),
      RUNNING("running", 1),
      FINISHED("finished", 2);

      private static final IntFunction<TestInstanceBlockEntity.Status> ID_MAP = ByIdMap.continuous($$0 -> $$0.index, values(), OutOfBoundsStrategy.ZERO);
      public static final Codec<TestInstanceBlockEntity.Status> CODEC = StringRepresentable.fromEnum(TestInstanceBlockEntity.Status::values);
      public static final StreamCodec<ByteBuf, TestInstanceBlockEntity.Status> STREAM_CODEC = ByteBufCodecs.idMapper(
         TestInstanceBlockEntity.Status::byIndex, $$0 -> $$0.index
      );
      private final String id;
      private final int index;

      private Status(final String $$0, final int $$1) {
         this.id = $$0;
         this.index = $$1;
      }

      public String getSerializedName() {
         return this.id;
      }

      public static TestInstanceBlockEntity.Status byIndex(int $$0) {
         return ID_MAP.apply($$0);
      }
   }
}
