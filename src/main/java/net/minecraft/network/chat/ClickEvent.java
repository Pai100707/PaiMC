package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public interface ClickEvent {
   Codec<ClickEvent> CODEC = ClickEvent.Action.CODEC.dispatch("action", ClickEvent::action, $$0 -> $$0.codec);

   ClickEvent.Action action();

   public static enum Action implements StringRepresentable {
      OPEN_URL("open_url", true, ClickEvent.OpenUrl.CODEC),
      OPEN_FILE("open_file", false, ClickEvent.OpenFile.CODEC),
      RUN_COMMAND("run_command", true, ClickEvent.RunCommand.CODEC),
      SUGGEST_COMMAND("suggest_command", true, ClickEvent.SuggestCommand.CODEC),
      SHOW_DIALOG("show_dialog", true, ClickEvent.ShowDialog.CODEC),
      CHANGE_PAGE("change_page", true, ClickEvent.ChangePage.CODEC),
      COPY_TO_CLIPBOARD("copy_to_clipboard", true, ClickEvent.CopyToClipboard.CODEC),
      CUSTOM("custom", true, ClickEvent.Custom.CODEC);

      public static final Codec<ClickEvent.Action> UNSAFE_CODEC = StringRepresentable.fromEnum(ClickEvent.Action::values);
      public static final Codec<ClickEvent.Action> CODEC = UNSAFE_CODEC.validate(ClickEvent.Action::filterForSerialization);
      private final boolean allowFromServer;
      private final String name;
      final MapCodec<? extends ClickEvent> codec;

      private Action(final String $$0, final boolean $$1, final MapCodec<? extends ClickEvent> $$2) {
         this.name = $$0;
         this.allowFromServer = $$1;
         this.codec = $$2;
      }

      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      public String getSerializedName() {
         return this.name;
      }

      public MapCodec<? extends ClickEvent> valueCodec() {
         return this.codec;
      }

      public static DataResult<ClickEvent.Action> filterForSerialization(ClickEvent.Action $$0) {
         return !$$0.isAllowedFromServer() ? DataResult.error(() -> "Click event type not allowed: " + $$0) : DataResult.success($$0, Lifecycle.stable());
      }
   }

   public record ChangePage(int page) implements ClickEvent {
      public static final MapCodec<ClickEvent.ChangePage> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.POSITIVE_INT.fieldOf("page").forGetter(ClickEvent.ChangePage::page)).apply($$0, ClickEvent.ChangePage::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.CHANGE_PAGE;
      }
   }

   public record CopyToClipboard(String value) implements ClickEvent {
      public static final MapCodec<ClickEvent.CopyToClipboard> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Codec.STRING.fieldOf("value").forGetter(ClickEvent.CopyToClipboard::value)).apply($$0, ClickEvent.CopyToClipboard::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.COPY_TO_CLIPBOARD;
      }
   }

   public record Custom(Identifier id, Optional<Tag> payload) implements ClickEvent {
      public static final MapCodec<ClickEvent.Custom> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Identifier.CODEC.fieldOf("id").forGetter(ClickEvent.Custom::id),
               ExtraCodecs.NBT.optionalFieldOf("payload").forGetter(ClickEvent.Custom::payload)
            )
            .apply($$0, ClickEvent.Custom::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.CUSTOM;
      }
   }

   public record OpenFile(String path) implements ClickEvent {
      public static final MapCodec<ClickEvent.OpenFile> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Codec.STRING.fieldOf("path").forGetter(ClickEvent.OpenFile::path)).apply($$0, ClickEvent.OpenFile::new)
      );

      public OpenFile(File $$0) {
         this($$0.toString());
      }

      public OpenFile(Path $$0) {
         this($$0.toFile());
      }

      public File file() {
         return new File(this.path);
      }

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.OPEN_FILE;
      }
   }

   public record OpenUrl(URI uri) implements ClickEvent {
      public static final MapCodec<ClickEvent.OpenUrl> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.UNTRUSTED_URI.fieldOf("url").forGetter(ClickEvent.OpenUrl::uri)).apply($$0, ClickEvent.OpenUrl::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.OPEN_URL;
      }
   }

   public record RunCommand(String command) implements ClickEvent {
      public static final MapCodec<ClickEvent.RunCommand> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(ClickEvent.RunCommand::command)).apply($$0, ClickEvent.RunCommand::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.RUN_COMMAND;
      }
   }

   public record ShowDialog(Holder<Dialog> dialog) implements ClickEvent {
      public static final MapCodec<ClickEvent.ShowDialog> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Dialog.CODEC.fieldOf("dialog").forGetter(ClickEvent.ShowDialog::dialog)).apply($$0, ClickEvent.ShowDialog::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.SHOW_DIALOG;
      }
   }

   public record SuggestCommand(String command) implements ClickEvent {
      public static final MapCodec<ClickEvent.SuggestCommand> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(ClickEvent.SuggestCommand::command)).apply($$0, ClickEvent.SuggestCommand::new)
      );

      @Override
      public ClickEvent.Action action() {
         return ClickEvent.Action.SUGGEST_COMMAND;
      }
   }
}
