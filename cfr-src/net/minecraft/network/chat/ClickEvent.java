/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
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
    public static final Codec<ClickEvent> CODEC = Action.CODEC.dispatch("action", ClickEvent::action, $$0 -> $$0.codec);

    public Action action();

    public static enum Action implements StringRepresentable
    {
        OPEN_URL("open_url", true, OpenUrl.CODEC),
        OPEN_FILE("open_file", false, OpenFile.CODEC),
        RUN_COMMAND("run_command", true, RunCommand.CODEC),
        SUGGEST_COMMAND("suggest_command", true, SuggestCommand.CODEC),
        SHOW_DIALOG("show_dialog", true, ShowDialog.CODEC),
        CHANGE_PAGE("change_page", true, ChangePage.CODEC),
        COPY_TO_CLIPBOARD("copy_to_clipboard", true, CopyToClipboard.CODEC),
        CUSTOM("custom", true, Custom.CODEC);

        public static final Codec<Action> UNSAFE_CODEC;
        public static final Codec<Action> CODEC;
        private final boolean allowFromServer;
        private final String name;
        final MapCodec<? extends ClickEvent> codec;

        private Action(String $$0, boolean $$1, MapCodec<? extends ClickEvent> $$2) {
            this.name = $$0;
            this.allowFromServer = $$1;
            this.codec = $$2;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public MapCodec<? extends ClickEvent> valueCodec() {
            return this.codec;
        }

        public static DataResult<Action> filterForSerialization(Action $$0) {
            if (!$$0.isAllowedFromServer()) {
                return DataResult.error(() -> "Click event type not allowed: " + String.valueOf($$0));
            }
            return DataResult.success((Object)$$0, (Lifecycle)Lifecycle.stable());
        }

        static {
            UNSAFE_CODEC = StringRepresentable.fromEnum(Action::values);
            CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
        }
    }

    public record Custom(Identifier id, Optional<Tag> payload) implements ClickEvent
    {
        public static final MapCodec<Custom> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Identifier.CODEC.fieldOf("id").forGetter(Custom::id), (App)ExtraCodecs.NBT.optionalFieldOf("payload").forGetter(Custom::payload)).apply((Applicative)$$0, Custom::new));

        @Override
        public Action action() {
            return Action.CUSTOM;
        }
    }

    public record CopyToClipboard(String value) implements ClickEvent
    {
        public static final MapCodec<CopyToClipboard> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("value").forGetter(CopyToClipboard::value)).apply((Applicative)$$0, CopyToClipboard::new));

        @Override
        public Action action() {
            return Action.COPY_TO_CLIPBOARD;
        }
    }

    public record ChangePage(int page) implements ClickEvent
    {
        public static final MapCodec<ChangePage> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ExtraCodecs.POSITIVE_INT.fieldOf("page").forGetter(ChangePage::page)).apply((Applicative)$$0, ChangePage::new));

        @Override
        public Action action() {
            return Action.CHANGE_PAGE;
        }
    }

    public record ShowDialog(Holder<Dialog> dialog) implements ClickEvent
    {
        public static final MapCodec<ShowDialog> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Dialog.CODEC.fieldOf("dialog").forGetter(ShowDialog::dialog)).apply((Applicative)$$0, ShowDialog::new));

        @Override
        public Action action() {
            return Action.SHOW_DIALOG;
        }
    }

    public record SuggestCommand(String command) implements ClickEvent
    {
        public static final MapCodec<SuggestCommand> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(SuggestCommand::command)).apply((Applicative)$$0, SuggestCommand::new));

        @Override
        public Action action() {
            return Action.SUGGEST_COMMAND;
        }
    }

    public record RunCommand(String command) implements ClickEvent
    {
        public static final MapCodec<RunCommand> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(RunCommand::command)).apply((Applicative)$$0, RunCommand::new));

        @Override
        public Action action() {
            return Action.RUN_COMMAND;
        }
    }

    public record OpenFile(String path) implements ClickEvent
    {
        public static final MapCodec<OpenFile> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("path").forGetter(OpenFile::path)).apply((Applicative)$$0, OpenFile::new));

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
        public Action action() {
            return Action.OPEN_FILE;
        }
    }

    public record OpenUrl(URI uri) implements ClickEvent
    {
        public static final MapCodec<OpenUrl> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ExtraCodecs.UNTRUSTED_URI.fieldOf("url").forGetter(OpenUrl::uri)).apply((Applicative)$$0, OpenUrl::new));

        @Override
        public Action action() {
            return Action.OPEN_URL;
        }
    }
}

