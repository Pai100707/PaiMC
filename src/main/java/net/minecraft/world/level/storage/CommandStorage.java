package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class CommandStorage {
   private static final String ID_PREFIX = "command_storage_";
   private final Map<String, CommandStorage.Container> namespaces = new HashMap<>();
   private final DimensionDataStorage storage;

   public CommandStorage(DimensionDataStorage $$0) {
      this.storage = $$0;
   }

   public CompoundTag get(Identifier $$0) {
      CommandStorage.Container $$1 = this.getContainer($$0.getNamespace());
      return $$1 != null ? $$1.get($$0.getPath()) : new CompoundTag();
   }

   
   private CommandStorage.Container getContainer(String $$0) {
      CommandStorage.Container $$1 = this.namespaces.get($$0);
      if ($$1 != null) {
         return $$1;
      } else {
         CommandStorage.Container $$2 = this.storage.get(CommandStorage.Container.type($$0));
         if ($$2 != null) {
            this.namespaces.put($$0, $$2);
         }

         return $$2;
      }
   }

   private CommandStorage.Container getOrCreateContainer(String $$0) {
      CommandStorage.Container $$1 = this.namespaces.get($$0);
      if ($$1 != null) {
         return $$1;
      } else {
         CommandStorage.Container $$2 = this.storage.computeIfAbsent(CommandStorage.Container.type($$0));
         this.namespaces.put($$0, $$2);
         return $$2;
      }
   }

   public void set(Identifier $$0, CompoundTag $$1) {
      this.getOrCreateContainer($$0.getNamespace()).put($$0.getPath(), $$1);
   }

   public Stream<Identifier> keys() {
      return this.namespaces.entrySet().stream().flatMap($$0 -> $$0.getValue().getKeys($$0.getKey()));
   }

   static String createId(String $$0) {
      return "command_storage_" + $$0;
   }

   static class Container extends SavedData {
      public static final Codec<CommandStorage.Container> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(Codec.unboundedMap(ExtraCodecs.RESOURCE_PATH_CODEC, CompoundTag.CODEC).fieldOf("contents").forGetter($$0x -> $$0x.storage))
            .apply($$0, CommandStorage.Container::new)
      );
      private final Map<String, CompoundTag> storage;

      private Container(Map<String, CompoundTag> $$0) {
         this.storage = new HashMap<>($$0);
      }

      private Container() {
         this(new HashMap<>());
      }

      public static SavedDataType<CommandStorage.Container> type(String $$0) {
         return new SavedDataType<>(CommandStorage.createId($$0), CommandStorage.Container::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
      }

      public CompoundTag get(String $$0) {
         CompoundTag $$1 = this.storage.get($$0);
         return $$1 != null ? $$1 : new CompoundTag();
      }

      public void put(String $$0, CompoundTag $$1) {
         if ($$1.isEmpty()) {
            this.storage.remove($$0);
         } else {
            this.storage.put($$0, $$1);
         }

         this.setDirty();
      }

      public Stream<Identifier> getKeys(String $$0) {
         return this.storage.keySet().stream().map($$1 -> Identifier.fromNamespaceAndPath($$0, $$1));
      }
   }
}
