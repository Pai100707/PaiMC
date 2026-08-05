/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;

public interface FormattedText {
    public static final Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
    public static final FormattedText EMPTY = new FormattedText(){

        @Override
        public <T> Optional<T> visit(ContentConsumer<T> $$0) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> visit(StyledContentConsumer<T> $$0, Style $$1) {
            return Optional.empty();
        }
    };

    public <T> Optional<T> visit(ContentConsumer<T> var1);

    public <T> Optional<T> visit(StyledContentConsumer<T> var1, Style var2);

    public static FormattedText of(final String $$0) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> $$02) {
                return $$02.accept($$0);
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> $$02, Style $$1) {
                return $$02.accept($$1, $$0);
            }
        };
    }

    public static FormattedText of(final String $$0, final Style $$1) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> $$02) {
                return $$02.accept($$0);
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> $$02, Style $$12) {
                return $$02.accept($$1.applyTo($$12), $$0);
            }
        };
    }

    public static FormattedText composite(FormattedText ... $$0) {
        return FormattedText.composite((List<? extends FormattedText>)ImmutableList.copyOf((Object[])$$0));
    }

    public static FormattedText composite(final List<? extends FormattedText> $$0) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> $$02) {
                for (FormattedText $$1 : $$0) {
                    Optional<T> $$2 = $$1.visit($$02);
                    if (!$$2.isPresent()) continue;
                    return $$2;
                }
                return Optional.empty();
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> $$02, Style $$1) {
                for (FormattedText $$2 : $$0) {
                    Optional<T> $$3 = $$2.visit($$02, $$1);
                    if (!$$3.isPresent()) continue;
                    return $$3;
                }
                return Optional.empty();
            }
        };
    }

    default public String getString() {
        StringBuilder $$0 = new StringBuilder();
        this.visit($$1 -> {
            $$0.append($$1);
            return Optional.empty();
        });
        return $$0.toString();
    }

    public static interface ContentConsumer<T> {
        public Optional<T> accept(String var1);
    }

    public static interface StyledContentConsumer<T> {
        public Optional<T> accept(Style var1, String var2);
    }
}

