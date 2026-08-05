/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

public record WrittenBookPredicate(Optional<CollectionPredicate<Filterable<Component>, PagePredicate>> pages, Optional<String> author, Optional<String> title, MinMaxBounds.Ints generation, Optional<Boolean> resolved) implements SingleComponentItemPredicate<WrittenBookContent>
{
    public static final Codec<WrittenBookPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)CollectionPredicate.codec(PagePredicate.CODEC).optionalFieldOf("pages").forGetter(WrittenBookPredicate::pages), (App)Codec.STRING.optionalFieldOf("author").forGetter(WrittenBookPredicate::author), (App)Codec.STRING.optionalFieldOf("title").forGetter(WrittenBookPredicate::title), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("generation", (Object)MinMaxBounds.Ints.ANY).forGetter(WrittenBookPredicate::generation), (App)Codec.BOOL.optionalFieldOf("resolved").forGetter(WrittenBookPredicate::resolved)).apply((Applicative)$$0, WrittenBookPredicate::new));

    @Override
    public DataComponentType<WrittenBookContent> componentType() {
        return DataComponents.WRITTEN_BOOK_CONTENT;
    }

    @Override
    public boolean matches(WrittenBookContent $$0) {
        if (this.author.isPresent() && !this.author.get().equals($$0.author())) {
            return false;
        }
        if (this.title.isPresent() && !this.title.get().equals($$0.title().raw())) {
            return false;
        }
        if (!this.generation.matches($$0.generation())) {
            return false;
        }
        if (this.resolved.isPresent() && this.resolved.get().booleanValue() != $$0.resolved()) {
            return false;
        }
        return !this.pages.isPresent() || this.pages.get().test($$0.pages());
    }

    public record PagePredicate(Component contents) implements Predicate<Filterable<Component>>
    {
        public static final Codec<PagePredicate> CODEC = ComponentSerialization.CODEC.xmap(PagePredicate::new, PagePredicate::contents);

        @Override
        public boolean test(Filterable<Component> $$0) {
            return $$0.raw().equals(this.contents);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Filterable)object);
        }
    }
}

