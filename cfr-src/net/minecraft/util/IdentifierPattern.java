/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class IdentifierPattern {
    public static final Codec<IdentifierPattern> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter($$0 -> $$0.namespacePattern), (App)ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter($$0 -> $$0.pathPattern)).apply((Applicative)$$02, IdentifierPattern::new));
    private final Optional<Pattern> namespacePattern;
    private final Predicate<String> namespacePredicate;
    private final Optional<Pattern> pathPattern;
    private final Predicate<String> pathPredicate;
    private final Predicate<Identifier> locationPredicate;

    private IdentifierPattern(Optional<Pattern> $$02, Optional<Pattern> $$1) {
        this.namespacePattern = $$02;
        this.namespacePredicate = $$02.map(Pattern::asPredicate).orElse($$0 -> true);
        this.pathPattern = $$1;
        this.pathPredicate = $$1.map(Pattern::asPredicate).orElse($$0 -> true);
        this.locationPredicate = $$0 -> this.namespacePredicate.test($$0.getNamespace()) && this.pathPredicate.test($$0.getPath());
    }

    public Predicate<String> namespacePredicate() {
        return this.namespacePredicate;
    }

    public Predicate<String> pathPredicate() {
        return this.pathPredicate;
    }

    public Predicate<Identifier> locationPredicate() {
        return this.locationPredicate;
    }
}

